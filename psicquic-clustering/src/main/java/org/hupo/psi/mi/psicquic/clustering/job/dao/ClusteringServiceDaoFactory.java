package org.hupo.psi.mi.psicquic.clustering.job.dao;

/**
 * DAO Factory.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 */
public interface ClusteringServiceDaoFactory {

    JobDao getJobDao();
}
