package org.hupo.psi.mi.psicquic.clustering;

import org.hupo.psi.mi.psicquic.clustering.job.JobNotCompletedException;
import org.hupo.psi.mi.psicquic.clustering.job.PollResult;

import java.util.List;

/**
 * TODO document this !
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 */
public interface InteractionClusteringService {

    /**
     * Initiate the clustering of a MIQL query based on the given list of services.
     * <br/>
     * This job is asynchronous and the user should call <code>poll( jobId )</code> to find out the status of the job.
     *
     * @param miql
     * @param services
     * @return the jobId assigned.
     */
    String submitJob( String miql, List<Service> services );

    /**
     * Provides a status to a given job, identified by the given jobId.
     *
     * @param jobId
     * @return a poll result that gives indication of the status of the job and optionaly a message.
     */
    PollResult poll( String jobId );

    /**
     * Once a job has a status <code>JobStatus.COMPLETED</code>, a user can query the clustered data. Otherwise
     * @param jobId
     * @param query run a query on the clustered data, * will return all.
     * @param from considering the data being indexed from 0..(n-1), we return data from the interaction position at index 'from'
     * @param maxResult the maximum number of interactions to be returned.
     * @return  a <code>List</code> of MITAB lines.
     */
    List<String> query( String jobId, String query, long from, long maxResult ) throws JobNotCompletedException;

}
