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
        System.out.println( "UserJobs.UserJobs" );
        currentJobs = new ArrayList<ClusteringJob>( );
        jobDao = ClusteringContext.getInstance().getDaoFactory().getJobDao();
    }

    public String clearJobs() {
        currentJobs.clear();
        // TODO the user has abandoned them ... remove them from the storage (synch issue!!!)
        return "interactions";
    }

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
        // TODO might be worth reversing the logic and pulling out all currentJobs rather than all jobs
        for ( ClusteringJob job : jobDao.getAll() ) {
            if( currentJobs.contains( job ) ) {
                if( job.getStatus().equals( status ) ) {
                      count++;
                }
            }
        }
        return count;
    }
    public String getClusteringOverview() {
        // build a message that reflects the current clustering jobs.
        int queuedJobs = getQueuedJobCount();
        int runningJobs = getRunningJobCount();
        int completedJobs = getCompletedJobCount();
        int failedJobs = getFailedJobCount();

        StringBuilder sb = new StringBuilder(256);
        
        if(queuedJobs > 0) sb.append( "queued("+ queuedJobs +") " );
        if(runningJobs > 0) sb.append( "running("+runningJobs+") " );
        if(completedJobs > 0) sb.append( "completed("+ completedJobs +") " );
        if(failedJobs > 0) sb.append( "failed("+failedJobs+") " );
        
        return sb.toString().trim() + ".";
    }
}
