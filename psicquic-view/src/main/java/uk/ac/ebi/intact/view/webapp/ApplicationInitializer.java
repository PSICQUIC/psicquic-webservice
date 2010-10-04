/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.view.webapp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.h2.tools.Server;
import org.hupo.psi.mi.psicquic.clustering.ClusteringContext;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;

/**
 * Initializes the application.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component
public class ApplicationInitializer implements InitializingBean {

    public static final Log log = LogFactory.getLog( ApplicationInitializer.class);

    @Autowired
    private ApplicationContext springContext;

    public ApplicationInitializer() {
    }

    public void afterPropertiesSet() throws Exception {

        if( springContext != null ) {
            Server server = ( Server ) (( BeanFactory )springContext).getBean( "h2TcpServer" );
            log.info( "************************************************************************************************" );
            log.info( "Clustering Batch H2 Server Status: " + server.getStatus() );
            log.info( "************************************************************************************************" );

            if( ClusteringContext.getInstance().getSpringContext() == null ) {
                log.error( "Failed to initialize ClusteringContext: springContext is null." );
            }
        } else {
            log.error( "||||||||||||||||||||||||||| SPRING WAS NOT INITIALIZED ||||||||||||||||||||||" );
        }
    }
}