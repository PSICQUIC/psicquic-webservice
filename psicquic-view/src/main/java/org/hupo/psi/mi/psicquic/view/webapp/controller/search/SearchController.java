package org.hupo.psi.mi.psicquic.view.webapp.controller.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.myfaces.orchestra.viewController.annotations.PreRenderView;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.apache.myfaces.trinidad.context.RequestContext;
import org.hupo.psi.mi.psicquic.clustering.job.ClusteringJob;
import org.hupo.psi.mi.psicquic.clustering.job.dao.DaoException;
import org.hupo.psi.mi.psicquic.clustering.job.dao.JobDao;
import org.hupo.psi.mi.psicquic.registry.ServiceType;
import org.hupo.psi.mi.psicquic.registry.client.PsicquicRegistryClientException;
import org.hupo.psi.mi.psicquic.registry.client.registry.DefaultPsicquicRegistryClient;
import org.hupo.psi.mi.psicquic.registry.client.registry.PsicquicRegistryClient;
import org.hupo.psi.mi.psicquic.view.webapp.controller.BaseController;
import org.hupo.psi.mi.psicquic.view.webapp.controller.QueryHits;
import org.hupo.psi.mi.psicquic.view.webapp.controller.clustering.ClusteringController;
import org.hupo.psi.mi.psicquic.view.webapp.controller.config.PsicquicViewConfig;
import org.hupo.psi.mi.psicquic.view.webapp.controller.services.ServicesController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private static final int MAX_NETWORK_SIZE = 50000;
    private static final String CLUSTER_JOB_ID = "clusterJobId";
    private static final String QUERY = "query";
    private static final String INCLUDED = "included";
    private static final String EXCLUDED = "excluded";

    @Autowired
    private transient ApplicationContext applicationContext;

    @Autowired
    private UserQuery userQuery;

    @Autowired
    private PsicquicViewConfig config;

    @Autowired
    private ServicesController servicesController;

    @Autowired
    private ClusteringController clusteringController;

    private List<ServiceType> services;
    private List<ServiceType> allServices;

    private String[] includedServices;
    private String[] excludedServices;


    public SearchController() {
    }

    @PostConstruct
    public void refresh() {
        refreshApp(null);
        servicesController.initialize(services);
        servicesController.doPsicquicBinarySearch("*");
    }

    private boolean isPartialRequest() {
        FacesContext context = FacesContext.getCurrentInstance();
        return RequestContext.getCurrentInstance().isPartialRequest(context);
    }

    @PreRenderView
    public void preRender() {

        if (isPartialRequest()) {
            return;
        }

        // TODO add new param 'clusterJobId' that can be combined with parameter 'query'

        FacesContext context = FacesContext.getCurrentInstance();

        String statusParam = context.getExternalContext().getRequestParameterMap().get("status");

        if (statusParam != null && "exp".equals(statusParam)) {
            addInfoMessage("Session Expired", "The session have been restored");
        }

        String jobId = context.getExternalContext().getRequestParameterMap().get(CLUSTER_JOB_ID);
        if (jobId != null) {
            final JobDao jobDao = clusteringController.getJobDao();
            final ClusteringJob job;
            try {
                job = jobDao.getJob(jobId);
                if (job == null) {
                    log.error("Could not find job by id: " + jobId);
                } else {
                    clusteringController.getUserClusteringJobs().getCurrentJobs().add(job);
                    clusteringController.setJob(job);
                    clusteringController.setClusterSelected(true);
                }
            } catch (DaoException e) {
                final String msg = "Failed to retrieve requested clustered query";
                addErrorMessage("Error", msg);
                log.error(msg + ": " + jobId, e);
            }
        }

        final String queryParam = context.getExternalContext().getRequestParameterMap().get(QUERY);
        if (queryParam != null && queryParam.length() > 0) {
            userQuery.reset();
            userQuery.setSearchQuery(queryParam);
        }

        services = Collections.synchronizedList(new ArrayList<ServiceType>(allServices));

        // included/excluded services
        final String includedServicesParam = context.getExternalContext().getRequestParameterMap().get(INCLUDED);

        if (includedServicesParam != null && includedServicesParam.length() > 0) {
            processIncludedServices(includedServicesParam);
        }

        final String excludedServicesParam = context.getExternalContext().getRequestParameterMap().get(EXCLUDED);

        if (excludedServicesParam != null && excludedServicesParam.length() > 0) {
            processExcludedServices(excludedServicesParam);
        }

        // search
        if (queryParam != null || includedServicesParam != null || excludedServices != null) {
            doNewBinarySearch();
        }
    }

    public void refreshApp(ActionEvent evt) {
        PsicquicRegistryClient registryClient = new DefaultPsicquicRegistryClient(config.getRegistryURL());
        try {
            if (allServices == null || allServices.isEmpty()) {

                if (config.getRegistryTagsAsString() != null) {
                    allServices = registryClient.listServices("STATUS", true, config.getRegistryTagsAsString());
                } else {
                    allServices = registryClient.listServices();
                }
            }

            services = new ArrayList<ServiceType>(allServices);

            String included = config.getIncludedServices();
            String excluded = config.getExcludedServices();

            if (included != null && included.length() > 0) {
                processIncludedServices(included);
            } else if (excluded != null && excluded.length() > 0) {
                processExcludedServices(excluded);
            }
        } catch (PsicquicRegistryClientException e) {
            log.error("Problem retrieving the services from the Registry", e);
            addErrorMessage("Problem retrieving the services from the Registry", "Please try again later");

            allServices = new ArrayList<ServiceType>();
            services = new ArrayList<ServiceType>(allServices);
            includedServices = new String[]{};
            excludedServices = new String[]{};

        }
    }

    public String doNewBinarySearch() {

        String searchQuery = userQuery.getSearchQuery();

        if (!clusteringController.isClusterSelected()) {
            servicesController.doPsicquicBinarySearch(searchQuery);
        } else {
            clusteringController.doClusteredJobBinarySearch(searchQuery);
        }

        return "interactions";
    }

    private void processExcludedServices(String excludedServicesParam) {
        excludedServices = excludedServicesParam.split(",");

        List<ServiceType> includedServicesList = new ArrayList<ServiceType>(excludedServices.length);

        for (ServiceType service : allServices) {
            boolean excluded = false;
            for (String serviceName : excludedServices) {
                if (serviceName.trim().equalsIgnoreCase(service.getName())) {
                    excluded = true;
                    break;
                }
            }

            if (!excluded) includedServicesList.add(service);
        }

        services = includedServicesList;
    }

    private void processIncludedServices(String includedServicesParam) {
        includedServices = includedServicesParam.split(",");

        List<ServiceType> includedServicesList = new ArrayList<ServiceType>(includedServices.length);

        for (ServiceType service : allServices) {
            for (String serviceName : includedServices) {
                if (serviceName.trim().equalsIgnoreCase(service.getName())) {
                    includedServicesList.add(service);
                }
            }
        }

        services = includedServicesList;
    }

    // Getters & Setters
    /////////////////////
    public String getApplicationUrl() {

        if (isPartialRequest()) {
            return null;
        }

        final FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();

        final String scheme = request.getScheme();
        final String serverName = request.getServerName();
        final int serverport = request.getServerPort();
        final String contextPath = request.getContextPath();

        return scheme + "://" + serverName + ":" + serverport + contextPath;
    }

    public boolean isGraphEnabled() {

        QueryHits selectedService;

        if (!clusteringController.isClusterSelected()) {
            selectedService = servicesController.getSelectedService();
        } else {
            selectedService = clusteringController.getSelectedService();
        }

        Boolean enabled = false;
        if (selectedService != null) {
            final Integer count = selectedService.getHits();
            enabled = (count <= MAX_NETWORK_SIZE);
        }
        return enabled;
    }

    public String getMitabUrl(){
        if (!clusteringController.isClusterSelected()) {
            return servicesController.getMitabUrl();
        } else {
            return clusteringController.getMitabUrl();
        }
    }

    public QueryHits getSelectedService(){
        if (!clusteringController.isClusterSelected()) {
            return servicesController.getSelectedService();
        } else {
            return clusteringController.getSelectedService();
        }
    }

    public int getTotalResults(){
        if (!clusteringController.isClusterSelected()) {
            return servicesController.getTotalResults();
        } else {
            return clusteringController.getTotalResults();
        }
    }

    public UserQuery getUserQuery() {
        return userQuery;
    }

    public void setUserQuery(UserQuery userQuery) {
        this.userQuery = userQuery;
    }

    public int getMaxNetworkSize() {
        return MAX_NETWORK_SIZE;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
