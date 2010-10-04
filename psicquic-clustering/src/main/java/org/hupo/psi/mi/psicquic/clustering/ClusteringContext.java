package org.hupo.psi.mi.psicquic.clustering;

import org.hupo.psi.mi.psicquic.clustering.job.dao.ClusteringServiceDaoFactory;
import org.hupo.psi.mi.psicquic.clustering.job.dao.impl.memory.InMemoryClusteringServiceDaoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Application context.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 */
public class ClusteringContext {

    /**
     * Singleton instance.
     */
    private static ClusteringContext ourInstance = new ClusteringContext();

    private final ClusteringServiceDaoFactory daoFactory;

    @Autowired
    private ApplicationContext springContext;

    private ClusteringConfig config;

    public static ClusteringContext getInstance() {
        return ourInstance;
    }

    private ClusteringContext() {
        config = new ClusteringConfig();
        daoFactory = new InMemoryClusteringServiceDaoFactory();
    }

    public ClusteringServiceDaoFactory getDaoFactory() {
        return daoFactory;
    }

    public ApplicationContext getSpringContext() {
        return springContext;
    }

    public ClusteringConfig getConfig() {
        return config;
    }
}
