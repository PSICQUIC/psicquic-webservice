package org.hupo.psi.mi.psicquic.view.webapp.controller.clustering;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.apache.myfaces.trinidad.model.SortableModel;
import org.hupo.psi.mi.psicquic.clustering.ClusteringContext;
import org.hupo.psi.mi.psicquic.clustering.InteractionClusteringService;
import org.hupo.psi.mi.psicquic.clustering.Service;
import org.hupo.psi.mi.psicquic.clustering.job.ClusteringJob;
import org.hupo.psi.mi.psicquic.clustering.job.dao.DaoException;
import org.hupo.psi.mi.psicquic.clustering.job.dao.JobDao;
import org.hupo.psi.mi.psicquic.registry.ServiceType;
import org.hupo.psi.mi.psicquic.view.webapp.controller.BaseController;
import org.hupo.psi.mi.psicquic.view.webapp.controller.QueryHits;
import org.hupo.psi.mi.psicquic.view.webapp.controller.services.ServicesController;
import org.hupo.psi.mi.psicquic.view.webapp.controller.config.PsicquicViewConfig;
import org.hupo.psi.mi.psicquic.view.webapp.controller.search.SearchController;
import org.hupo.psi.mi.psicquic.view.webapp.controller.search.UserQuery;
import org.hupo.psi.mi.psicquic.view.webapp.model.ClusteringResultDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
* Clustering controller.
*
* @author Samuel Kerrien (skerrien@ebi.ac.uk)
* @version $Id$
*/

@Controller( "clusteringBean" )
@Scope( "conversation.access" )
@ConversationName( "general" )
public class ClusteringController extends BaseController {

    private static final Log log = LogFactory.getLog( ClusteringController.class );

    @Autowired
    private PsicquicViewConfig config;

    @Autowired
    private UserQuery userQuery;

    @Autowired
    private SearchController searchController;

    @Autowired
    private ServicesController servicesController;

    @Autowired
    private UserClusteringJobs userClusteringJobs;

    @Autowired
    private ClusteringContext clusteringContext;

    @Autowired
    private InteractionClusteringService clusteringService;

    private boolean clusterSelected = false;
    private ClusteringJob job;
    private String mitabUrl;
    private int totalResults;
    private QueryHits selectedService;


    public ClusteringController() {
    }

    public void doClusteredJobBinarySearch(String searchQuery) {
        if (log.isDebugEnabled()) {
            log.debug("\tcluster query:  " + searchQuery);
        }

        final ServiceType service = new ServiceType();
        final QueryHits extendedService;

        final String jobId = job.getJobId();
        final String serviceName = "Clustered query: '" + job.getMiql() + "' from " + printServiceNames(job.getServices());

        service.setName(serviceName);

        // Enable download of clustered data via table via a servlet
        service.setRestUrl("/clusterDownload?jobId=" + jobId + "&query=*");

        //Number of indexed interactions
        int totalCount = job.getClusteredInteractionCount();
        SortableModel results = new ClusteringResultDataModel(clusteringService, job, "*");

        extendedService = new QueryHits(service, totalCount, false);
        extendedService.setResults(results);
        generateMitabUrl(extendedService);

        //TODO: store in the own controller
        setSelectedService(extendedService);
        setTotalResults(totalCount);
    }

    public String start() {
        final int totalCount = servicesController.getSelectedResults();
        if ( totalCount > 0 ) {

            if ( totalCount < config.getClusteringSizeLimit() ) {
                clusterCurrentQuery();
            } else {
                log.info("Too many interactions (" + totalCount +
                        ") to be clustered, please narrow down your search to a set not exceeding " +
                        config.getClusteringSizeLimit());
            }
        } else {
            log.info( "No interaction to be clustered !!" );
        }

        return "interactions";
    }

    private void generateMitabUrl(QueryHits service) {
        final String restUrl = service.getRestUrl();
        mitabUrl = searchController.getApplicationUrl() + restUrl;

        if (log.isDebugEnabled()) log.debug("Reading data from: " + mitabUrl);


    }

    public String viewJob() {
        String jobId = getParameterValue("jobId");
        log.debug( "Selected viewJob: " + jobId );

        final JobDao jobDao = clusteringContext.getDaoFactory().getJobDao();
        try {
            final ClusteringJob job = jobDao.getJob( jobId );
            if( job == null ) {
                log.error( "Could not view job by id: " + jobId );
            } else {
                // Configure the searchController to read Job index rather than the registry's services
                setJob(job);
                setClusterSelected(true);

                String searchQuery = userQuery.getSearchQuery();
                doClusteredJobBinarySearch(searchQuery);
            }
        } catch ( DaoException e ) {
            final String msg = "Failed to list currently clustered queries";
            addErrorMessage( "Error", msg );
            log.error( msg, e );
        }

        return "interactions";
    }

    public String removeJob() {
        String jobId = getParameterValue("jobId");
        log.debug( "Selected removeJob: " + jobId );

        // remove the job from the user's current job but let it run in the background if already running.
        final Iterator<ClusteringJob> iterator = userClusteringJobs.getRefreshedJobs().iterator();
        boolean found = true;
        while ( iterator.hasNext() ) {
            ClusteringJob job = iterator.next();
            if( job.getJobId().equals( jobId ) ) {
                iterator.remove();
                log.debug( "Removed job " + job + " on user request." );
                break;
            }
        }

        if( ! found ) {
            log.error( "Could not find job by job id in the current jobs: " + jobId );
        }

        return "interactions";
    }

    public String getClusterButtonHelpMessage() {
        if( servicesController.getSelectedResults() > config.getClusteringSizeLimit() ) {
            return "You can only cluster queries no bigger than " + config.getClusteringSizeLimit() +
                   " interactions. Please refine your query.";
        } else {
            return "Press the button to start building a non redundant set of interactions based on your current query.";
        }
    }

    private void clusterCurrentQuery() {
        List<Service> clusteredServices = collectServicesToBeClustered();
        final String jobId = clusteringService.submitJob( userQuery.getSearchQuery(), clusteredServices );
        final JobDao jobDao = clusteringContext.getDaoFactory().getJobDao();
        final ClusteringJob job;
        try {
            job = jobDao.getJob( jobId );
            // keep a bookmark on that job
            if( ! userClusteringJobs.getCurrentJobs().contains( job ) ) {
                // only add jobs that are not yet submitted.
                userClusteringJobs.getCurrentJobs().add( job );
            }
        } catch ( DaoException e ) {
            final String msg = "Failed to build clustered query: '" + userQuery.getSearchQuery() + "'";
            addErrorMessage( "Error", msg );
            log.error( msg, e );
        }
    }

    private List<Service> collectServicesToBeClustered() {
        List<QueryHits> services= servicesController.getServices();
        final List<Service> services2Cluster = new ArrayList<Service>();

        for (final QueryHits service : services) {
            if (service.isActive() && service.isChecked() && service.getHits() > 0) {
                services2Cluster.add( new Service( service.getName() ) );
            }
        }
        return services2Cluster;
    }

    private String printServiceNames(List<Service> services) {
        StringBuilder sb = new StringBuilder(256);
        final Iterator<Service> it = services.iterator();
        while (it.hasNext()) {
            Service service = it.next();
            sb.append(service.getName());
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public JobDao getJobDao() {
        return clusteringContext.getDaoFactory().getJobDao();
    }

    public UserClusteringJobs getUserClusteringJobs() {
        return userClusteringJobs;
    }

    public void setUserClusteringJobs(UserClusteringJobs userClusteringJobs) {
        this.userClusteringJobs = userClusteringJobs;
    }

    public ClusteringJob getJob() {
        return job;
    }

    public void setJob(ClusteringJob job) {
        this.job = job;
    }

    public boolean isClusterSelected() {
        return clusterSelected;
    }

    public void setClusterSelected(boolean clusterSelected) {
        this.clusterSelected = clusterSelected;
    }

    public String getMitabUrl() {
        return mitabUrl;
    }

    public void setMitabUrl(String mitabUrl) {
        this.mitabUrl = mitabUrl;
    }

    public String unselectClusterJob() {
        clusterSelected = false;
        job = null;
        return searchController.doNewBinarySearch();
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setSelectedService(QueryHits selectedService) {
        this.selectedService = selectedService;
    }

    public QueryHits getSelectedService() {
        return selectedService;
    }
}
