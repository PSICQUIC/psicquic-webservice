package org.hupo.psi.mi.psicquic.clustering;

import org.hupo.psi.mi.psicquic.clustering.job.dao.ClusteringServiceDaoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Application context.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 */
@Component
public class ClusteringContext {

    /**
     * Singleton instance.
     */
//    private static ClusteringContext ourInstance = new ClusteringContext();

    @Autowired
    @Qualifier( value = "inMemory")
    private ClusteringServiceDaoFactory daoFactory;

    @Autowired
    private ApplicationContext springContext;

    @Autowired
    private ClusteringConfig config;

//    public static ClusteringContext getInstance() {
//        return null;
//    }

    public ClusteringContext() {
        System.out.println("+++ Creating clustering context");
//        config = new ClusteringConfig();
//        daoFactory = new InMemoryClusteringServiceDaoFactory();
    }

    ///////////////////////////
    // Getters and Setters

    public ClusteringServiceDaoFactory getDaoFactory() {
        return daoFactory;
    }

//    @Required
    public void setDaoFactory( ClusteringServiceDaoFactory daoFactory ) {
        this.daoFactory = daoFactory;
    }

    public ClusteringConfig getConfig() {
        return config;
    }

//    @Required
    public void setConfig( ClusteringConfig config ) {
        this.config = config;
    }

    public ApplicationContext getSpringContext() {
        return springContext;
    }

//    @Required
    public void setSpringContext( ApplicationContext springContext ) {
        this.springContext = springContext;
    }
}
