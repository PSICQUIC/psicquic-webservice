package org.hupo.psi.mi.psicquic.clustering.job.dao;

import org.hupo.psi.mi.psicquic.clustering.job.JobDefinition;

/**
 * JobDefinition DAO.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 */
public interface JobDao {

    void addJob( String jobId, JobDefinition job );

    JobDefinition getJob( String jobId );

    JobDefinition removeJob( String jobId );

    JobDefinition getLastRanJob();

    JobDefinition getNextJobToRun();
}
