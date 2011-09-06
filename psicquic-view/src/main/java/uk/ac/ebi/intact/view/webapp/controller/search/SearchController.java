package uk.ac.ebi.intact.view.webapp.controller.search;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.myfaces.orchestra.viewController.annotations.PreRenderView;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.apache.myfaces.trinidad.context.RequestContext;
import org.apache.myfaces.trinidad.model.SortableModel;
import org.hupo.psi.mi.psicquic.clustering.ClusteringContext;
import org.hupo.psi.mi.psicquic.clustering.InteractionClusteringService;
import org.hupo.psi.mi.psicquic.clustering.Service;
import org.hupo.psi.mi.psicquic.clustering.job.ClusteringJob;
import org.hupo.psi.mi.psicquic.clustering.job.dao.ClusteringServiceDaoFactory;
import org.hupo.psi.mi.psicquic.clustering.job.dao.DaoException;
import org.hupo.psi.mi.psicquic.clustering.job.dao.JobDao;
import org.hupo.psi.mi.psicquic.registry.ServiceType;
import org.hupo.psi.mi.psicquic.registry.client.PsicquicRegistryClientException;
import org.hupo.psi.mi.psicquic.registry.client.registry.DefaultPsicquicRegistryClient;
import org.hupo.psi.mi.psicquic.registry.client.registry.PsicquicRegistryClient;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicClientException;
import org.hupo.psi.mi.psicquic.wsclient.UniversalPsicquicClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import psidev.psi.mi.xml.converter.ConverterException;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.view.webapp.controller.clustering.UserJobs;
import uk.ac.ebi.intact.view.webapp.controller.config.PsicquicViewConfig;
import uk.ac.ebi.intact.view.webapp.model.ClusteringResultDataModel;
import uk.ac.ebi.intact.view.webapp.model.PsicquicResultDataModel;
import uk.ac.ebi.intact.view.webapp.util.UrlUtils;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    private static final int MAX_NETWORK_SIZE = 1000;
    private static final String CLUSTER_JOB_ID = "clusterJobId";
    private static final String QUERY = "query";
    private static final String INCLUDED = "included";
    private static final String EXCLUDED = "excluded";

    @Autowired
    private UserQuery userQuery;

    @Autowired
    private PsicquicViewConfig config;

    @Autowired
    private ClusteringContext clusteringContext;

    @Autowired
    private InteractionClusteringService clusteringService;

    @Autowired
    private UserJobs userJobs;

    @Autowired
    private SearchCache searchCache;

    private List<ServiceType> services;
    private List<ServiceType> allServices;
    private Map<String,ServiceType> servicesMap;
    private Map<String,Boolean> serviceSelectionMap;

    private int totalResults = -1;

    private Map<String,String> activeServices;
    private Map<String,String> inactiveServices;
    private Map<String,SortableModel> resultDataModelMap;
    private Map<String,Integer> resultCountMap;

    private String[] includedServices;
    private String[] excludedServices;

    private String selectedServiceName;

    private boolean clusterSelected;
    private ClusteringJob job;

    private String mitabUrl;

    private Date registryTimestamp;

    public SearchController() {
        this.clusterSelected = false;

        this.serviceSelectionMap = Collections.synchronizedMap(new HashMap<String,Boolean>());
    }

    @PostConstruct
    public void refresh() {
        System.out.println( "SearchController.refresh" );
        doPsicquicBinarySearch("*");
    }

    private boolean isPartialRequest() {
        FacesContext context = FacesContext.getCurrentInstance();
        return RequestContext.getCurrentInstance().isPartialRequest( context );
    }

    @PreRenderView
    public void preRender() {
        System.out.println( "SearchController.preRender" );

        if( isPartialRequest() ) {
            return;
        }

        // TODO add new param 'clusterJobId' that can be combined with parameter 'query'

        FacesContext context = FacesContext.getCurrentInstance();

        String jobId = context.getExternalContext().getRequestParameterMap().get( CLUSTER_JOB_ID );
        if( jobId != null ) {
            final JobDao jobDao = clusteringContext.getDaoFactory().getJobDao();
            final ClusteringJob job;
            try {
                job = jobDao.getJob( jobId );
                if( job == null ) {
                log.error( "Could not find job by id: " + jobId );
            } else {
                this.userJobs.getCurrentJobs().add( job );
                this.job = job;
                this.clusterSelected = true;
            }
            } catch ( DaoException e ) {
                final String msg = "Failed to retrieve requested clustered query";
                addErrorMessage( "Error", msg );
                log.error( msg + ": " + jobId, e );
            }
        }

        final String queryParam = context.getExternalContext().getRequestParameterMap().get( QUERY );
        if (queryParam != null && queryParam.length()>0) {
            userQuery.reset();
            userQuery.setSearchQuery( queryParam );
        }

        services = new ArrayList<ServiceType>(allServices);

        // included/excluded services
        final String includedServicesParam = context.getExternalContext().getRequestParameterMap().get( INCLUDED );

        if (includedServicesParam != null && includedServicesParam.length()>0) {
            processIncludedServices(includedServicesParam);
        }

        final String excludedServicesParam = context.getExternalContext().getRequestParameterMap().get( EXCLUDED );

        if (excludedServicesParam != null && excludedServicesParam.length()>0) {
            processExcludedServices(excludedServicesParam);
        }

        // search
        if (queryParam != null || includedServicesParam != null || excludedServices != null) {
            doBinarySearchAction();
        }
    }

    public void refresh(ActionEvent evt) {
        System.out.println( "SearchController.refresh(evt)" );
        PsicquicRegistryClient registryClient = new DefaultPsicquicRegistryClient();
        try {
            Date newRegistryTimeStamp = registryClient.registryTimestamp();

            if (registryTimestamp != null && (newRegistryTimeStamp.equals(registryTimestamp) || newRegistryTimeStamp.before(registryTimestamp))) {
                return;
            } else {
                registryTimestamp = newRegistryTimeStamp;
            }
        } catch (PsicquicRegistryClientException e) {
            throw new RuntimeException("Problem checking timestamp from the Registry", e);
        }

        searchCache.clearCache();

        this.resultDataModelMap = Collections.synchronizedMap(new HashMap<String,SortableModel>());
        this.resultCountMap = Collections.synchronizedMap(new HashMap<String,Integer>());
        this.activeServices = Collections.synchronizedMap(new HashMap<String,String>());
        this.inactiveServices = Collections.synchronizedMap(new HashMap<String,String>());

        try {
            refreshServices();
        } catch (PsicquicRegistryClientException e) {
            throw new RuntimeException("Problem loading services from the Registry", e);
        }
    }

    public String doBinarySearchAction() {
        System.out.println( "SearchController.doBinarySearchAction" );
        String searchQuery = userQuery.getSearchQuery();

        if( ! clusterSelected ) {
            doPsicquicBinarySearch( searchQuery );
        } else {
            doClusteredJobBinarySearch( searchQuery );
        }

        return "interactions";
    }

    public String doNewBinarySearch() {
        System.out.println( "SearchController.doNewBinarySearch" );
        try {
            // TODO don't need that when querying the clustered job
            // TODO this method is calling doBinarySearchAction ... when it's going to be called here too ?!!!
            refreshServices();
        } catch (PsicquicRegistryClientException e) {
            addErrorMessage("Problem loading services", e.getMessage());
            log.error("Problem loading services", e);
        }
        return doBinarySearchAction();
    }

    public void doBinarySearch(ActionEvent evt) {
        System.out.println( "SearchController.doBinarySearch" );
        refreshComponent("mainPanels");
        doBinarySearchAction();
    }

    public void doClearFilterAndSearch(ActionEvent evt) {
        userQuery.clearFilters();
        doBinarySearch(evt);
    }

    public String getActiveServicesName() {
        StringBuilder sb = new StringBuilder( 256 );
        final Iterator<String> it = activeServices.keySet().iterator();
        while ( it.hasNext() ) {
            String serviceName = it.next();
            sb.append( serviceName );
            if( it.hasNext() ) {
                sb.append( ", " );
            }
        }
        return sb.toString();
    }

    public String getInactiveServicesName() {
        StringBuilder sb = new StringBuilder( 256 );
        final Iterator<String> it = inactiveServices.keySet().iterator();
        while ( it.hasNext() ) {
            String serviceName = it.next();
            sb.append( serviceName );
            if( it.hasNext() ) {
                sb.append( ", " );
            }
        }
        return sb.toString();
    }

    private void doClusteredJobBinarySearch( String searchQuery ) {
        if ( log.isDebugEnabled() ) {log.debug( "\tcluster query:  "+ searchQuery );}

        final String jobId = job.getJobId();
        final String serviceName =  "Clustered query: '"+ job.getMiql() +"' from " + printServiceNames( job.getServices() );
        selectedServiceName = serviceName;

        final ServiceType service = new ServiceType();
        service.setName( serviceName );

        // Enable download of clustered data via table via a servlet
        service.setRestUrl( "/download?jobId="+jobId+"&query=*" );

        servicesMap.put( serviceName, service );
        int totalCount = job.getClusteredInteractionCount();
        resultCountMap.put( serviceName, totalCount );
        totalResults = totalCount;

        loadResults( service );
    }

    private String printServiceNames( List<Service> services ) {
        StringBuilder sb = new StringBuilder( 256 );
        final Iterator<Service> it = services.iterator();
        while ( it.hasNext() ) {
            Service service = it.next();
            sb.append( service.getName() );
            if( it.hasNext() ) {
                sb.append( ", " );
            }
        }
        return sb.toString();
    }

    public void doPsicquicBinarySearch(String searchQuery) {
        System.out.println( "SearchController.doPsicquicBinarySearch" );
        try {
            if ( log.isDebugEnabled() ) {log.debug( "\tquery:  "+ searchQuery );}

            searchAndCreateResultModels();

            totalResults = 0;

            for (int count : resultCountMap.values()) {
                totalResults += count;
            }

            if ( totalResults == 0 ) {
                addWarningMessage( "Your query didn't return any results", "Use a different query" );
            }

            setSelectedServiceName(null);
        } catch ( Throwable t ) {
            if ( searchQuery != null && ( searchQuery.startsWith( "*" ) || searchQuery.startsWith( "?" ) ) ) {
                userQuery.setSearchQuery( "*:*" );
                addErrorMessage( "Your query '"+ searchQuery +"' is not correctly formatted",
                                 "Currently we do not support queries prefixed with wildcard characters such as '*' or '?'. " +
                                 "However, wildcard characters can be used anywhere else in a query (eg. g?vin or gav* for gavin). " +
                                 "Please do reformat your query." );
            } else {
                addErrorMessage("Psicquic problem", t.getMessage());
                log.error( "Error while building results based on your query: '" + searchQuery + "'", t );
            }
        }
    }

    public void loadResults(ServiceType service) {
        System.out.println( "SearchController.loadResults" );
        generateMitabUrl();

        if (resultDataModelMap.containsKey(service.getName())) {
            return;
        }

        SortableModel results = null;
        try {
            if( ! clusterSelected ) {
                results = new PsicquicResultDataModel(new UniversalPsicquicClient(service.getSoapUrl()), userQuery.getFilteredSearchQuery());
            } else {
                // TODO for the time being we load ALL clustered data, later we could allow further filtering
                results = new ClusteringResultDataModel( clusteringService, job, "*" );
            }
            resultDataModelMap.put(service.getName(), results);
        } catch (PsicquicClientException e) {
            log.error( "Error while building results", e );
        }
    }

    private void generateMitabUrl() {
        final String serviceName = getSelectedServiceName();
        final ServiceType serviceType = getServicesMap().get(serviceName);
        final String query = UrlUtils.encodeUrl( getUserQuery().getFilteredSearchQuery() );
        final String restUrl = serviceType.getRestUrl();
        final String queryUrl;

        if( ! clusterSelected ) {
            boolean endWithSlash = restUrl.endsWith( "/" );
            queryUrl = restUrl + (endWithSlash ? "" : "/") + "query/" + query;
        } else {
            queryUrl = getApplicationUrl() + restUrl;
        }

        if(log.isDebugEnabled()) log.debug("Reading data from: " + queryUrl);

        mitabUrl = queryUrl;
    }

    private void refreshServices() throws PsicquicRegistryClientException {
        System.out.println( "SearchController.refreshServices" );
        PsicquicRegistryClient registryClient = new DefaultPsicquicRegistryClient();

        if (allServices == null) {
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

        populateActiveServicesMap();
        populateInactiveServicesMap();

        servicesMap = new HashMap<String, ServiceType>(services.size());
        
        boolean populateSelectionMap = serviceSelectionMap.isEmpty();

        for (ServiceType service : services) {
            servicesMap.put(service.getName(), service);
            
            if (populateSelectionMap) {
                serviceSelectionMap.put(service.getName(), service.isActive());
            }
        }
    }

    private void searchAndCreateResultModels() {
        System.out.println( "SearchController.searchAndCreateResultModels" );
        refresh(null);

        resultDataModelMap = new HashMap<String, SortableModel>(32);
        resultCountMap = new HashMap<String, Integer>(32);

        final String filteredSearchQuery = userQuery.getFilteredSearchQuery();

        String selectedServicesStr = serviceSelectionMap.keySet().toString();

        // check the search cache
        if (searchCache.contains(filteredSearchQuery+selectedServicesStr)) {
            Map<String, Integer> resultsInCache = searchCache.get(filteredSearchQuery);

            if (resultsInCache != null) {
                resultCountMap = resultsInCache;
                return;
            }
        }

        // count the results
        final ExecutorService executorService = Executors.newCachedThreadPool();

        for (final ServiceType service : services) {
            if (service.isActive() && serviceSelectionMap.get(service.getName())) {
                Runnable runnable = new Runnable() {
                    public void run() {
                            int count = countInPsicquicService(service, filteredSearchQuery);
                            resultCountMap.put(service.getName(), count);
                    }
                };
                executorService.submit(runnable);
            }
        }

        executorService.shutdown();

        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error( "Error while terminating ExecutorService", e );
        }

        searchCache.put(filteredSearchQuery, resultCountMap);
    }

    private int countInPsicquicService(ServiceType service, String query) {
        if (query == null || query.trim().startsWith("*")) {
            return Long.valueOf(service.getCount()).intValue();
        }

        int psicquicCount = 0;

            HttpURLConnection connection = null;

        try {
            String encoded = URLEncoder.encode(query, "UTF-8");
            encoded = encoded.replaceAll("\\+", "%20");

            String separator = (service.getRestUrl().endsWith( "/" ) ? "" : "/" );
            String countUrl = service.getRestUrl() + separator + "query/" + encoded + "?format=count";

            URL url = new URL(countUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            connection.connect();

            String strCount = IOUtils.toString(connection.getInputStream());
            psicquicCount = Integer.parseInt(strCount);

        } catch (SocketTimeoutException se) {
            // happens if timeout
            psicquicCount = -1;
        } catch (IOException e) {
            e.printStackTrace();
            service.setActive(false);
            serviceSelectionMap.put(service.getName(), false);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return psicquicCount;
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

    public int getTotalResults() {
        return totalResults;
    }

    public Map<String, Integer> getResultCountMap() {
        return resultCountMap;
    }

    public Map<String, SortableModel> getResultDataModelMap() {
        return resultDataModelMap;
    }

    public Map<String, String> getActiveServices() {
        return activeServices;
    }

    public Map<String, String> getInactiveServices() {
        return inactiveServices;
    }

    public String[] getAllServiceNames() {
        final String[] services = servicesMap.keySet().toArray(new String[servicesMap.size()]);
        Arrays.sort(services, new ServiceNameComparator());
        return services;
    }

    public String[] getActiveServiceNames() {
        final String[] services = activeServices.keySet().toArray(new String[activeServices.size()]);
        Arrays.sort(services, new ServiceNameComparator());
        return services;
    }

    public String[] getInactiveServiceNames() {
        final String[] services = inactiveServices.keySet().toArray(new String[inactiveServices.size()]);
        Arrays.sort(services, new ServiceNameComparator());
        return services;
    }

    private class ServiceNameComparator implements Comparator<String> {
        public int compare(String o1, String o2) {
            return o1.toLowerCase().compareTo(o2.toLowerCase());
        }
    }

    public List<ServiceType> getServices() {
        return services;
    }

    public Map<String, ServiceType> getServicesMap() {
        return servicesMap;
    }

    public String getSelectedServiceName() {
        return selectedServiceName;
    }

    public void setSelectedServiceName(String selectedServiceName) {
        this.selectedServiceName = selectedServiceName;

        if (selectedServiceName != null) {
            loadResults(servicesMap.get(selectedServiceName));
        }
    }

    public boolean isClusterSelected() {
        return clusterSelected;
    }

    public void setClusterSelected( boolean clusterSelected ) {
        this.clusterSelected = clusterSelected;
    }

    public ClusteringJob getJob() {
        return job;
    }

    public void setJob( ClusteringJob job ) {
        this.job = job;
    }

    public UserJobs getUserJobs() {
        return userJobs;
    }

    public void setUserJobs( UserJobs userJobs ) {
        this.userJobs = userJobs;
    }

    public String unselectClusterJob() {
        clusterSelected = false;
        job = null;

        try {
            refreshServices(); // clean up the list of services
        } catch ( PsicquicRegistryClientException e ) {
            // TODO implement error handling
            e.printStackTrace();
        }
        doBinarySearchAction();

        return "interactions";
    }

    public UserQuery getUserQuery() {
        return userQuery;
    }

    public void setUserQuery(UserQuery userQuery) {
        this.userQuery = userQuery;
    }

    private String encodeUrl(String s) {
        return s.replaceAll( " ", "%20" );
    }

    public String getMitabUrl() throws IOException, ConverterException {
        return mitabUrl;
    }

    public String getApplicationUrl() {

        if( isPartialRequest() ) {
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
        Boolean enabled = false;
        final String serviceName = getSelectedServiceName();
        if( serviceName != null ) {
            final Integer count = getResultCountMap().get(serviceName);
            if( count != null ) {
                enabled = ( count <= MAX_NETWORK_SIZE );
            }
        }
        return enabled;
    }

    public int getMaxNetworkSize() {
        return MAX_NETWORK_SIZE;
    }

    public int getSelectedServicesCount() {
        int count = 0;

        for (boolean selected : serviceSelectionMap.values()) {
            if (selected) count++;
        }

        return count;
    }

    public Map<String, Boolean> getServiceSelectionMap() {
        return serviceSelectionMap;
    }

    public void setServiceSelectionMap(Map<String, Boolean> serviceSelectionMap) {
        this.serviceSelectionMap = serviceSelectionMap;
    }
}
