package uk.ac.ebi.intact.view.webapp.controller.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.myfaces.orchestra.viewController.annotations.PreRenderView;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.hupo.psi.mi.psicquic.registry.ServiceType;
import org.hupo.psi.mi.psicquic.registry.client.PsicquicRegistryClientException;
import org.hupo.psi.mi.psicquic.registry.client.registry.DefaultPsicquicRegistryClient;
import org.hupo.psi.mi.psicquic.registry.client.registry.PsicquicRegistryClient;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicClientException;
import org.hupo.psi.mi.psicquic.wsclient.UniversalPsicquicClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.view.webapp.controller.config.PsicquicViewConfig;
import uk.ac.ebi.intact.view.webapp.model.PsicquicResultDataModel;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

/**
 * Search controller.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("searchBean")
@Scope("conversation.access")
@ConversationName("general")
@ViewController(viewIds = {"/main.xhtml"})
public class SearchController extends BaseController {

    private static final Log log = LogFactory.getLog(SearchController.class);

    @Autowired
    private UserQuery userQuery;

    @Autowired
    private PsicquicViewConfig config;

    private List<ServiceType> services;
    private List<ServiceType> allServices;
    private Map<String,ServiceType> servicesMap;

    private int totalResults = -1;

    private Map<String,String> activeServices;
    private Map<String,String> inactiveServices;
    private Map<String,PsicquicResultDataModel> resultDataModelMap;
    private Map<String,Integer> resultCountMap;

    private String[] includedServices;
    private String[] excludedServices;

    private boolean showAlternativeIds;

    public SearchController() {
        refresh(null);
    }

    @PreRenderView
    public void preRender() {
        FacesContext context = FacesContext.getCurrentInstance();

        String queryParam = context.getExternalContext().getRequestParameterMap().get("query");

        if (queryParam != null && queryParam.length()>0) {
            userQuery.reset();
            userQuery.setSearchQuery( queryParam );
        }

        services = new ArrayList<ServiceType>(allServices);

        // included/excluded services
        String includedServicesParam = context.getExternalContext().getRequestParameterMap().get("included");

        if (includedServicesParam != null && includedServicesParam.length()>0) {
            includedServices = includedServicesParam.split(",");

            List<ServiceType> includedServicesList = new ArrayList<ServiceType>(includedServices.length);

            for (ServiceType service : services) {
                for (String serviceName : includedServices) {
                    if (serviceName.equalsIgnoreCase(service.getName())) {
                        includedServicesList.add(service);
                    }
                }
            }

            services = includedServicesList;

            populateActiveServicesMap();
            populateInactiveServicesMap();
        }

        String excludedServicesParam = context.getExternalContext().getRequestParameterMap().get("excluded");

        if (excludedServicesParam != null && excludedServicesParam.length()>0) {
            excludedServices = excludedServicesParam.split(",");

            List<ServiceType> includedServicesList = new ArrayList<ServiceType>(excludedServices.length);

            for (ServiceType service : services) {
                boolean excluded = false;
                for (String serviceName : excludedServices) {
                    if (serviceName.equalsIgnoreCase(service.getName())) {
                        excluded = true;
                        break;
                    }
                }

                if (!excluded) includedServicesList.add(service);
            }

            services = includedServicesList;

            populateActiveServicesMap();
            populateInactiveServicesMap();
        }

        // search
        if (queryParam != null || includedServicesParam != null || excludedServices != null) {
            doBinarySearchAction();
        }
    }

    public void refresh(ActionEvent evt) {
        this.resultDataModelMap = new HashMap<String,PsicquicResultDataModel>();
        this.resultCountMap = new HashMap<String,Integer>();
        this.activeServices = new HashMap<String,String>();
        this.inactiveServices = new HashMap<String,String>();

        try {
            PsicquicRegistryClient registryClient = new DefaultPsicquicRegistryClient();
            allServices = registryClient.listServices();
            services = new ArrayList<ServiceType>(allServices);

            populateActiveServicesMap();
            populateInactiveServicesMap();

            servicesMap = new HashMap<String, ServiceType>(services.size());

            for (ServiceType service : services) {
                servicesMap.put(service.getName(), service);
            }

        } catch (PsicquicRegistryClientException e) {
            throw new RuntimeException("Problem loading services from registry", e);
        }
    }

    public String doBinarySearchAction() {
        String searchQuery = userQuery.getSearchQuery();

        doBinarySearch( searchQuery );

        return "interactions";
    }

    public String doNewBinarySearch() {
        services = new ArrayList<ServiceType>(allServices);

        populateActiveServicesMap();
        populateInactiveServicesMap();

        return doBinarySearchAction();
    }

    public void doBinarySearch(ActionEvent evt) {
        refreshComponent("mainPanels");
        doBinarySearchAction();
    }

    public void doClearFilterAndSearch(ActionEvent evt) {
        userQuery.clearFilters();
        doBinarySearch(evt);
    }

    public void doBinarySearch(String searchQuery) {
        try {
            if ( log.isDebugEnabled() ) {log.debug( "\tquery:  "+ searchQuery );}

            searchAndCreateResultModels();

            totalResults = 0;

            for (int count : resultCountMap.values()) {
                totalResults += count;
            }

            if ( totalResults == 0 ) {
                addErrorMessage( "Your query didn't return any results", "Use a different query" );
            }

        } catch ( Throwable throwable ) {

            if ( searchQuery != null && ( searchQuery.startsWith( "*" ) || searchQuery.startsWith( "?" ) ) ) {
                userQuery.setSearchQuery( "*:*" );
                addErrorMessage( "Your query '"+ searchQuery +"' is not correctly formatted",
                                 "Currently we do not support queries prefixed with wildcard characters such as '*' or '?'. " +
                                 "However, wildcard characters can be used anywhere else in one's query (eg. g?vin or gav* for gavin). " +
                                 "Please do reformat your query." );
            } else {
                addErrorMessage("Psicquic problem", throwable.getMessage());
            }
        }
    }

    private void searchAndCreateResultModels() {
        resultCountMap.clear();
        resultDataModelMap.clear();
        
        for (Map.Entry<String,String> serviceUrl : activeServices.entrySet()) {
            try {
                PsicquicResultDataModel results = psicquicResults(serviceUrl.getValue());

                resultDataModelMap.put(serviceUrl.getKey(), results);
                resultCountMap.put(serviceUrl.getKey(), results.getRowCount());

            } catch (PsicquicClientException e) {
                e.printStackTrace();
            }
        }
    }

    private PsicquicResultDataModel psicquicResults(String endpoint) throws PsicquicClientException {
        return new PsicquicResultDataModel(new UniversalPsicquicClient(endpoint), userQuery.getFilteredSearchQuery());
    }
    private void populateActiveServicesMap() {
        activeServices.clear();
        for (ServiceType service : services) {
            if (service.isActive()) {
                activeServices.put(service.getName(), service.getSoapUrl());
            }
        }
    }

    private void populateInactiveServicesMap() {
        inactiveServices.clear();
        for (ServiceType service : services) {
            if (!service.isActive()) {
                inactiveServices.put(service.getName(), service.getSoapUrl());
            }
        }
    }

    private List<String[]> getServices(String urlStr) {
        List<String[]> serviceUrls = new ArrayList<String[]>();
        try {
            URL url = new URL(urlStr);

            final InputStream inputStream = url.openStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] nameAndUrl = line.split("=");
                serviceUrls.add(nameAndUrl);
            }

            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serviceUrls;
    }

    // Getters & Setters
    /////////////////////

    public int getTotalResults() {
        return totalResults;
    }

    public Map<String, Integer> getResultCountMap() {
        return resultCountMap;
    }

    public Map<String, PsicquicResultDataModel> getResultDataModelMap() {
        return resultDataModelMap;
    }

    public Map<String, String> getActiveServices() {
        return activeServices;
    }

    public Map<String, String> getInactiveServices() {
        return inactiveServices;
    }

    public String[] getActiveServiceNames() {
        final String[] services = activeServices.keySet().toArray(new String[activeServices.size()]);
        Arrays.sort(services);
        return services;
    }

    public String[] getInactiveServiceNames() {
        final String[] services = inactiveServices.keySet().toArray(new String[inactiveServices.size()]);
        Arrays.sort(services);
        return services;
    }

    public List<ServiceType> getServices() {
        return services;
    }

    public Map<String, ServiceType> getServicesMap() {
        return servicesMap;
    }
}
