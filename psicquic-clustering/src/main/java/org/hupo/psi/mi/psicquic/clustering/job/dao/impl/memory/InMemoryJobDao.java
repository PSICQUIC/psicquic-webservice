package org.hupo.psi.mi.psicquic.clustering.job.dao.impl.memory;

import org.hupo.psi.mi.psicquic.clustering.job.ClusteringJob;
import org.hupo.psi.mi.psicquic.clustering.job.JobStatus;
import org.hupo.psi.mi.psicquic.clustering.job.dao.JobDao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * In memory job dao.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 */
public class InMemoryJobDao implements JobDao {

    private Map<String, ClusteringJob> jobid2job = new HashMap<String, ClusteringJob>();

    public void addJob( String jobId, ClusteringJob job ) {
        if ( jobId == null ) {
            throw new IllegalArgumentException( "You must give a non null jobId" );
        }
        if ( job == null ) {
            throw new IllegalArgumentException( "You must give a non null job" );
        }
        if ( jobid2job.containsKey( jobId ) ) {
            throw new IllegalStateException( "You cannot add more than once a jobId: " + jobId );
        }

        jobid2job.put( jobId, job );
    }

    public ClusteringJob getJob( String jobId ) {
        return jobid2job.get( jobId );
    }

    public ClusteringJob removeJob( String jobId ) {
        ClusteringJob job = jobid2job.remove( jobId );
        if ( job == null ) {
            throw new IllegalArgumentException( "This job could not be found: " + jobId );
        }
        return job;
    }

    /**
     * Fetches the job with the most recent completion data being in state FAILED or COMPLETED.
     *
     * @return
     */
    public ClusteringJob getLastRanJob() {
        ClusteringJob lastRanJob = null;
        for ( Map.Entry<String, ClusteringJob> entry : jobid2job.entrySet() ) {
            ClusteringJob job = entry.getValue();
            final JobStatus jobStatus = job.getStatus();

            if ( jobStatus != null && ( jobStatus.equals( JobStatus.FAILED )
                                        || jobStatus.equals( JobStatus.COMPLETED ) ) ) {

                if ( lastRanJob == null ) {
                    lastRanJob = job;
                } else {
                    // take the one with the most recent completion date.
                    if ( job.getCompleted().after( lastRanJob.getCompleted() ) ) {
                        lastRanJob = job;
                    }
                }
            }
        }

        return lastRanJob;
    }

    /**
     * Fetches the next job with status QUEUED and the oldest creation date.
     *
     * @return
     */
    public ClusteringJob getNextJobToRun() {
        ClusteringJob nextJob = null;
        for ( Map.Entry<String, ClusteringJob> entry : jobid2job.entrySet() ) {
            ClusteringJob job = entry.getValue();
            final JobStatus jobStatus = job.getStatus();
            if ( JobStatus.QUEUED.equals( jobStatus ) ) {
                if ( nextJob == null ) {
                    nextJob = job;
                } else {
                    // take the one with the most recent completion date.
                    if ( job.getCreated().before( nextJob.getCreated() ) ) {
                        nextJob = job;
                    }
                }
            }
        }

        return nextJob;
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
        for ( ClusteringJob job : getAll() ) {
            if ( job.getStatus().equals( status ) ) {
                count++;
            }
        }
        return count;
    }

    //////////////////
    // BaseDao

    public int countAll() {
        return jobid2job.size();
    }

    public Collection<ClusteringJob> getAll() {
        return new ArrayList( jobid2job.values() );
    }

    public void save( ClusteringJob job ) {
        // nothing to do, all in memory
    }

    public void update( ClusteringJob job ) {
        // nothing to do, all in memory
    }

    public void delete( ClusteringJob job ) {
        for ( Map.Entry<String, ClusteringJob> entry : jobid2job.entrySet() ) {
            if ( entry.getValue() == job ) {
                jobid2job.remove( entry.getKey() );
                return;
            }
        }
    }
}
