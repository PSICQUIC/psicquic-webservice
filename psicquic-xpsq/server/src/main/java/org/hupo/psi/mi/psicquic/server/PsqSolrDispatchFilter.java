package org.hupo.psi.mi.psicquic.server;

/* =============================================================================
 # $Id::                                                                       $
 # Version: $Rev::                                                             $
 #==============================================================================
 #
 # PsqSolrDispatchFilter: Customized Solr initialization
 #
 #=========================================================================== */

import java.net.URL;
import java.io.File;

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.solr.servlet.*;

public class PsqSolrDispatchFilter  extends SolrDispatchFilter{
    
    public void init( FilterConfig config ) throws ServletException{

        Log log = LogFactory.getLog( this.getClass() );
        log.info( "PsqSolrDispatchFilter: init(config) called");
        
        String path = "";        
        
        try{ 
            URL pathUrl = config.getServletContext().getResource("/");
            //path = pathUrl.getPath();

            path = new File( config.getServletContext().getRealPath("/")).getAbsolutePath();

            log.info( " protocol=" + pathUrl.getProtocol() );
            log.info( " path=" + path );
            
        } catch(Exception ex){ ex.printStackTrace();}
        
        if( config.getInitParameter( "solr-home" ) != null ){
            
            String solrHome = config.getInitParameter( "solr-home" ) ;
            if( !solrHome.startsWith("/") ){
                solrHome = path + File.separator + solrHome;
            }    
            log.info( " solr-home(final)=" + path );
            System.setProperty( "solr.solr.home",  solrHome );
        }
        
        super.init( config );
    }
}
