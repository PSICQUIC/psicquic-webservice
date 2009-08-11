package uk.ac.ebi.intact.view.webapp.controller.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.io.IOUtils;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.apache.myfaces.orchestra.viewController.annotations.PreRenderView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.hupo.psi.mi.psicquic.wsclient.UniversalPsicquicClient;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicClientException;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.view.webapp.model.PsicquicResultDataModel;

import javax.faces.event.ActionEvent;
import javax.faces.context.FacesContext;
import java.util.*;
import java.net.URL;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

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

    private int totalResults;

    private Map<String,String> activeServices;
    private Map<String,String> inactiveServices;
    private Map<String,PsicquicResultDataModel> resultDataModelMap;
    private Map<String,Integer> resultCountMap;

    // results
    private PsicquicResultDataModel intactResults;
    private PsicquicResultDataModel biogridResults;
    private PsicquicResultDataModel mintResults;
    private PsicquicResultDataModel irefindexResults;
    private PsicquicResultDataModel mpidbResults;
    private PsicquicResultDataModel matrixDbResults;

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

            doBinarySearchAction();
        }
    }

    public void refresh(ActionEvent evt) {
        this.resultDataModelMap = new HashMap<String,PsicquicResultDataModel>();
        this.resultCountMap = new HashMap<String,Integer>();
        this.activeServices = new HashMap<String,String>();
        this.inactiveServices = new HashMap<String,String>();

        populateActiveServicesMap();
        populateInactiveServicesMap();
    }

    public String doBinarySearchAction() {
        String searchQuery = userQuery.getSearchQuery();

        doBinarySearch( searchQuery );

        return "interactions";
    }

    public String doNewBinarySearch() {
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
        return new PsicquicResultDataModel(new UniversalPsicquicClient(endpoint),
                    userQuery.getSearchQuery());
    }

    private void populateActiveServicesMap() {
        List<String[]> serviceUrls = getServices("http://www.ebi.ac.uk/intact/psicquic-registry/registry?action=ACTIVE&format=txt");

        for (String[] serviceUrl : serviceUrls) {
            activeServices.put(serviceUrl[0], serviceUrl[1]);
        }
    }

    private void populateInactiveServicesMap() {
        List<String[]> serviceUrls = getServices("http://www.ebi.ac.uk/intact/psicquic-registry/registry?action=INACTIVE&format=txt");

        for (String[] serviceUrl : serviceUrls) {
            inactiveServices.put(serviceUrl[0], serviceUrl[1]);
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
}