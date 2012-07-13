package org.hupo.psi.mi.psicquic.server;

/* =============================================================================
 # $Id::                                                                       $
 # Version: $Rev::                                                             $
 #==============================================================================
 #
 # RESTful Web service interface
 #
 #=========================================================================== */

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import javax.ws.rs.*;

import org.hupo.psi.mi.*;
import org.hupo.psi.mi.psq.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PsicquicRestImpl implements PsicquicRest{

    PsqContext psqContext;

    public void setPsqContext( PsqContext context ){
        psqContext = context;
    }

    PsicquicServer psqServer;

    public void setPsqServer( PsicquicServer server ){
        psqServer = server;
    }
    
    //--------------------------------------------------------------------------

    private void initialize() {
        initialize( false );
    }

    //--------------------------------------------------------------------------

    private void initialize( boolean force) {

        Log log = LogFactory.getLog( this.getClass() );
        log.info( " psqContext=" + psqContext );
    }

    //==========================================================================
    // REST SERVICE OPERATIONS
    //========================

    public Object getByInteractor( String intAc,String db, String format,
                                   String firstResult, String maxResults ) 
        throws PsicquicServiceException,
               NotSupportedMethodException,
               NotSupportedTypeException {
        
        throw new NotSupportedMethodException( "", null );
    }
    
    public Object getByInteraction( String intAc, String db, String format,
                                    String firstResult, String maxResults ) 
        throws PsicquicServiceException,
               NotSupportedMethodException,
               NotSupportedTypeException{
        
        throw new NotSupportedMethodException( "", null );
    }
    
    public Object getByQuery( String query, String format,
                              String firstResult, String maxResults ) 
        throws PsicquicServiceException,
               NotSupportedMethodException,
               NotSupportedTypeException{
    
        Log log = LogFactory.getLog( this.getClass() );
        log.info( "PsqRestImpl: getByQuery: context =" + psqContext);
        log.info( "PsqRestImpl: getByQuery: q=" + query );

        long fRes = -1;
        long mRes = -1;

        try{
            if( firstResult != null ){
                fRes = Long.parseLong( firstResult );
            }
        } catch( NumberFormatException nfx ){
            // ignore
        }

        try{
            if( maxResults != null ){
                mRes = Long.parseLong( maxResults );
            }
        } catch( NumberFormatException nfx ){
            // ignore
        }

        ResultSet qrs = psqServer.getByQuery( query, format, fRes, mRes );
        String mitab="";
        log.info( "getByQuery: rs="+ qrs);

        for( Iterator i = qrs.getResultList().iterator(); i.hasNext(); ){
            String record = (String) i.next();
            mitab += record + "\n";
        }
        
        return mitab;        
    }
    
    //==========================================================================
    // META INFO
    //==========

    public Object getSupportedFormats() 
        throws PsicquicServiceException,
               NotSupportedMethodException {
        
        return psqServer.getSupportedReturnTypes( "rest" ).toString();
    }
    
    public Object getProperty( String propertyName ) 
        throws PsicquicServiceException,
               NotSupportedMethodException{
        return psqServer.getProperty( "rest", propertyName );
    }
    
    public Object getProperties() 
        throws PsicquicServiceException,
               NotSupportedMethodException{
        return psqServer.getProperties( "rest" ).toString();
    }
    
    @GET @Path("/version")
    public String getVersion() 
        throws PsicquicServiceException,
               NotSupportedMethodException{

        Log log = LogFactory.getLog( this.getClass() );
        log.info( "PsqRestImpl: getVersion");

        return psqServer.getVersion( "rest" );
    }
}
