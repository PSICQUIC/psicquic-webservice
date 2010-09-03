package org.hupo.psi.mi.psicquic.clustering;

import org.hupo.psi.mi.psicquic.clustering.job.dao.ClusteringServiceDaoFactory;
import org.hupo.psi.mi.psicquic.clustering.job.dao.impl.memory.InMemoryClusteringServiceDaoFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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

    private final ClusteringServiceDaoFactory daoFactory = new InMemoryClusteringServiceDaoFactory();

    private final ApplicationContext springContext;

    public static ClusteringContext getInstance() {
        return ourInstance;
    }

    private ClusteringContext() {
        springContext = new ClassPathXmlApplicationContext( "job-clustering.xml" );
    }

    public ClusteringServiceDaoFactory getDaoFactory() {
        return daoFactory;
    }

    public ApplicationContext getSpringContext() {
        return springContext;
    }
}
