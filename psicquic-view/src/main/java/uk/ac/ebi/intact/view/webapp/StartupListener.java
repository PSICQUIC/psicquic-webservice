/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.view.webapp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hupo.psi.mi.psicquic.clustering.ClusteringContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Initializes the applications
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class StartupListener implements ServletContextListener {

    public static final Log log = LogFactory.getLog(StartupListener.class);

    /**
     * Notification that the web application initialization
     * process is starting.
     * All ServletContextListeners are notified of context
     * initialisation before any filter or servlet in the web
     * application is initialized.
     */
    public void contextInitialized(ServletContextEvent sce) {

        if( ClusteringContext.getInstance().getSpringContext() == null ) {
            throw new RuntimeException( "Failed to initialize clustering context" );
        }
    }

    /**
     * Notification that the servlet context is about to be shut down. All servlets
     * have been destroy()ed before any ServletContextListeners are notified of context
     * destruction.
     */
    public void contextDestroyed(ServletContextEvent sce) {
        log.debug("LogFactory.release and destroying application");
        LogFactory.release(Thread.currentThread().getContextClassLoader());
    }
}