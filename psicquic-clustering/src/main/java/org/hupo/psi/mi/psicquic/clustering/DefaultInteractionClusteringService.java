package org.hupo.psi.mi.psicquic.clustering;

import org.hupo.psi.mi.psicquic.clustering.job.JobDefinition;
import org.hupo.psi.mi.psicquic.clustering.job.JobIdGenerator;
import org.hupo.psi.mi.psicquic.clustering.job.JobNotCompletedException;
import org.hupo.psi.mi.psicquic.clustering.job.PollResult;
import org.hupo.psi.mi.psicquic.clustering.job.dao.ClusteringServiceDaoFactory;
import org.hupo.psi.mi.psicquic.clustering.job.dao.JobDao;

import java.util.ArrayList;
import java.util.List;

/**
 * Default interaction clustering service.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 */
public class DefaultInteractionClusteringService implements InteractionClusteringService {

    public String submitJob( String miql, List<Service> services ) {

        // Build a job
        final JobDefinition job = new JobDefinition( miql, services );

        // Generate an id based on the definition of the job
        final String jobId = new JobIdGenerator().generateJobId( job );

        // detect if this job is already running
        ClusteringServiceDaoFactory csd = ClusteringContext.getInstance().getDaoFactory();
        final JobDao jobDao = csd.getJobDao();
        if( jobDao.getJob( miql ) != null ) {
            // job was already submitted

        } else {
            // new job
            jobDao.addJob( jobId, job );
        }

        return jobId;
    }

    public PollResult poll( String jobId ) {

        ClusteringServiceDaoFactory csd = ClusteringContext.getInstance().getDaoFactory();
        final JobDao jobDao = csd.getJobDao();
        final JobDefinition job = jobDao.getJob( jobId );
        final PollResult pr = new PollResult( job.getStatus() );

        // TODO if status is RUNNING, generate an ETA and store into 'PollResult.message'
        // TODO if status is FAILED, collect reason and store into 'PollResult.message'

        return pr;
    }

    public List<String> query( String jobId, String query, long from, long maxResult ) throws JobNotCompletedException {
        ClusteringServiceDaoFactory csd = ClusteringContext.getInstance().getDaoFactory();
        final JobDao jobDao = csd.getJobDao();
        final JobDefinition job = jobDao.getJob( jobId );

        // get Lucene index location from job

        // query chunk from Lucene

        // return the data

        return new ArrayList<String>();
    }
}
