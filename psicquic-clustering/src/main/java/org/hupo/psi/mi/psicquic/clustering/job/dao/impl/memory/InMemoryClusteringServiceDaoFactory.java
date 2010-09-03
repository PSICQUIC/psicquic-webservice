package org.hupo.psi.mi.psicquic.clustering.job.dao.impl.memory;

import org.hupo.psi.mi.psicquic.clustering.job.dao.ClusteringServiceDaoFactory;
import org.hupo.psi.mi.psicquic.clustering.job.dao.JobDao;

/**
 * TODO document this !
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 */
public class InMemoryClusteringServiceDaoFactory implements ClusteringServiceDaoFactory {

    private final JobDao jobDao = new InMemoryJobDao();

    public InMemoryClusteringServiceDaoFactory() {
    }

    public JobDao getJobDao() {
          return jobDao;
    }
}
