package uk.ac.ebi.intact.view.webapp.controller.clustering;

import org.hupo.psi.mi.psicquic.clustering.ClusteringContext;
import org.hupo.psi.mi.psicquic.clustering.job.ClusteringJob;
import org.hupo.psi.mi.psicquic.clustering.job.JobIdGenerator;
import org.hupo.psi.mi.psicquic.clustering.job.JobStatus;
import org.hupo.psi.mi.psicquic.clustering.job.dao.JobDao;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

/**
 * Jobs that have been submitted by the user.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.1
 */
@Controller
@Scope( "session" )
public class UserJobs {

    private List<ClusteringJob> currentJobs;

    private JobDao jobDao;

    public UserJobs() {
        currentJobs = new ArrayList<ClusteringJob>( );
        jobDao = ClusteringContext.getInstance().getDaoFactory().getJobDao();
    }

//    public String clearJobs() {
//        currentJobs.clear();
//        return "interactions";
//    }

    public List<ClusteringJob> getRefreshedJobs() {
        List<ClusteringJob> refreshedJobs = new ArrayList<ClusteringJob>( currentJobs.size() );
        for ( ClusteringJob currentJob : currentJobs ) {
            final ClusteringJob job = jobDao.getJob( currentJob.getJobId() );
            refreshedJobs.add( job );
        }
        currentJobs = refreshedJobs;
        return refreshedJobs;
    }

    public List<ClusteringJob> getCurrentJobs() {
        return currentJobs;
    }

    public void setCurrentJobs( List<ClusteringJob> currentJobs ) {
        this.currentJobs = currentJobs;
    }

    public int getJobCount() {
        return currentJobs.size();
    }

    public int getCompletedJobCount() {
        return countJobsByStatus( JobStatus.COMPLETED );
    }

    public int getQueuedJobCount() {
        return countJobsByStatus( JobStatus.QUEUED );
    }

    public int getRunningJobCount() {
        return countJobsByStatus( JobStatus.RUNNING );
    }

    public int getFailedJobCount() {
        return countJobsByStatus( JobStatus.FAILED );
    }

    private int countJobsByStatus( JobStatus status ) {
        int count = 0;
        for ( ClusteringJob job : getRefreshedJobs() ) {
            if( job.getStatus().equals( status ) ) {
                count++;
            }
        }
        return count;
    }
}
