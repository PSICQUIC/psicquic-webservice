package org.hupo.psi.mi.psicquic.clustering.job.dao;

import org.hupo.psi.mi.psicquic.clustering.job.ClusteringJob;

/**
 * ClusteringJob DAO.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 */
public interface JobDao extends BaseDao<ClusteringJob> {

    void addJob( String jobId, ClusteringJob job );

    ClusteringJob getJob( String jobId );

    ClusteringJob removeJob( String jobId );

    ClusteringJob getLastRanJob();

    ClusteringJob getNextJobToRun();
}
