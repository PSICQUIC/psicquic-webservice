package org.hupo.psi.mi.psicquic.view.webapp.application;

import org.quartz.Scheduler;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Close threads when shutting down the application
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>04/12/12</pre>
 */

public class ShutDownHook implements ServletContextListener{

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        try
        {
           // Get a reference to the Scheduler and shut it down
            WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
            Scheduler scheduler = (Scheduler) context.getBean("schedulerFactoryBean");
            scheduler.shutdown(true);

           // Sleep for a bit so that we don't get any errors
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
    }
}
