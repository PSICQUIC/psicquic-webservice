package org.hupo.psi.mi.psicquic.server;

/* =============================================================================
 # $Id::                                                                       $
 # Version: $Rev::                                                             $
 #==============================================================================
 #
 # PsqStoreDispatchFilter: Customized Record Store initialization
 #
 #=========================================================================== */

import java.net.URL;
import java.io.File;
import java.io.IOException;

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PsqStoreDispatchFilter  implements Filter{
    
    public void init( FilterConfig config ) throws ServletException{

        Log log = LogFactory.getLog( this.getClass() );
        log.info( "PsqStoreDispatchFilter: init(config) called");
        
        String path = "";        
        
        try{ 
            URL pathUrl = config.getServletContext().getResource("/");
            
            path = new File( config.getServletContext().getRealPath("/")).getAbsolutePath();

            log.info( " protocol=" + pathUrl.getProtocol() );
            log.info( " path=" + path );
            
        } catch(Exception ex){ ex.printStackTrace();}
        
        if( config.getInitParameter( "derby-home" ) != null ){
            
            String derbyHome = config.getInitParameter( "derby-home" ) ;
            if( !derbyHome.startsWith("/") ){
                derbyHome = path + File.separator + derbyHome;
            }    
            System.setProperty( "derby.derby.home",  derbyHome );
        }
    }
    
    public void doFilter( ServletRequest request, 
                          ServletResponse response, 
                          FilterChain chain )
        throws ServletException, IOException {
        
        chain.doFilter( request, response );        
    }
    public void destroy(){

    }
}
