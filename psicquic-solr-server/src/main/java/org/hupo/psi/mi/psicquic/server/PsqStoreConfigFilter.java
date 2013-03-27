package org.hupo.psi.mi.psicquic.server;

/* =============================================================================
 # $Id::                                                                       $
 # Version: $Rev::                                                             $
 #==============================================================================
 #
 # PsqStoreConfigFilter: Customized Record Store initialization
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

import org.hupo.psi.mi.psicquic.server.PsqContext;

public class PsqStoreConfigFilter  implements Filter{
    
    public void init( FilterConfig config ) throws ServletException{

        Log log = LogFactory.getLog( this.getClass() );
        log.info( "PsqStoreConfigFilter: init(config) called");
        
        String path = "";        
        
        try{ 
            URL pathUrl = config.getServletContext().getResource("/");
            
            path = new File( config.getServletContext().getRealPath("/")).getAbsolutePath();

            log.info( " protocol=" + pathUrl.getProtocol() );
            log.info( " path=" + path );
            
        } catch(Exception ex){ ex.printStackTrace();}
        
        if( config.getInitParameter( "derby-home" ) != null ){
            
            String myHome = config.getInitParameter( "derby-home" ) ;
            if( !myHome.startsWith("/") ){
                myHome = path + File.separator + myHome;
            }

            log.info( "derby-home(final)=" + myHome );
            System.setProperty( "xpsq.derby.home",  myHome );

            if( PsqContext.getStore("derby") != null ){
                PsqContext.getStore("derby").initialize();
            }
        } 
        
        if( config.getInitParameter( "bdb-home" ) != null ){
            
            String myHome = config.getInitParameter( "bdb-home" ) ;
            if( !myHome.startsWith("/") ){
                myHome = path + File.separator + myHome;
            }    

            log.info( "bdb-home(final)=" + myHome );
            System.setProperty( "xpsq.bdb.home", myHome );

            if( PsqContext.getStore("bdb") != null ){
                PsqContext.getStore("bdb").initialize();
            }
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
