package uk.ac.ebi.intact.view.webapp.controller.clustering;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.hupo.psi.mi.psicquic.clustering.ClusteringContext;
import org.hupo.psi.mi.psicquic.clustering.DefaultInteractionClusteringService;
import org.hupo.psi.mi.psicquic.clustering.InteractionClusteringService;
import org.hupo.psi.mi.psicquic.clustering.Service;
import org.hupo.psi.mi.psicquic.clustering.job.ClusteringJob;
import org.hupo.psi.mi.psicquic.clustering.job.dao.JobDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.view.webapp.controller.BaseController;
import uk.ac.ebi.intact.view.webapp.controller.config.PsicquicViewConfig;
import uk.ac.ebi.intact.view.webapp.controller.search.SearchController;
import uk.ac.ebi.intact.view.webapp.controller.search.UserQuery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    private PsicquicViewConfig appConfig;

    @Autowired
    private UserQuery userQuery;

    @Autowired
    private PsicquicViewConfig config;

    @Autowired
    private SearchController searchController;

    @Autowired
    private UserJobs userJobs;

    public ClusteringController() {
    }

    public String start() {
        final int totalCount = searchController.getTotalResults();
        if ( totalCount > 0 ) {

            if ( totalCount < appConfig.getClusteringSizeLimit() ) {
                clusterQuery( userQuery.getSearchQuery() );
            } else {
                log.info( "Too many interactions (" + totalCount +
                          ") to be clustered, please narrow down your search to a set not exceeding " +
                          appConfig.getClusteringSizeLimit() );
            }
        } else {
            log.info( "No interaction to be clustered !!" );
        }

        return "interactions";
    }

    private void clusterQuery( String searchQuery ) {
        final InteractionClusteringService ics = new DefaultInteractionClusteringService();

        List<Service> clusteredServices = collectServicesToBeClustered();
        final String jobId = ics.submitJob( userQuery.getSearchQuery(), clusteredServices );
        final JobDao jobDao = ClusteringContext.getInstance().getDaoFactory().getJobDao();
        final ClusteringJob job = jobDao.getJob( jobId );

        // keep a bookmark on that job
        if( ! userJobs.getCurrentJobs().contains( job ) ) {
            // only add jobs that are not yet submitted.
            userJobs.getCurrentJobs().add( job );
        }
    }

    private List<Service> collectServicesToBeClustered() {
        final Map<String,Integer> service2count = searchController.getResultCountMap();
        final List<Service> services = new ArrayList<Service>( service2count.size() );
        for ( Map.Entry<String, Integer> entry : service2count.entrySet() ) {
            final String serviceName = entry.getKey();
            final Integer interactionCount = entry.getValue();
            if( interactionCount > 0 && searchController.getServiceSelectionMap().get(serviceName)) {
                services.add( new Service( serviceName ) );
            }
        }
        return services;
    }

    public String viewJob() {
        String jobId = getParameterValue("jobId");
        log.debug( "Selected viewJob: " + jobId );

        final JobDao jobDao = ClusteringContext.getInstance().getDaoFactory().getJobDao();
        final ClusteringJob job = jobDao.getJob( jobId );
        if( job == null ) {
            log.error( "Could not view job by id: " + jobId );
        } else {
            // Configure the searchController to read Job index rather than the registry's services
            searchController.setJob( job );
            searchController.setClusterSelected( true );

            searchController.doBinarySearchAction();
        }

        return "interactions";
    }

    public String removeJob() {
        String jobId = getParameterValue("jobId");
        log.debug( "Selected removeJob: " + jobId );

        // remove the job from the user's current job but let it run in the background if already running.
        final Iterator<ClusteringJob> iterator = userJobs.getRefreshedJobs().iterator();
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
        if( searchController.getTotalResults() > appConfig.getClusteringSizeLimit() ) {
            return "You can only cluster queries no bigger than " + appConfig.getClusteringSizeLimit() +
                   " interactions. Please refine your query.";
        } else {
            return "Press the button to start building a non redundant set of interactions based on your current query.";
        }
    }
}
