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

import java.io.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.hupo.psi.mi.*;
import org.hupo.psi.mi.psq.*;
      
import org.json.*;

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

    public Object getByPostQuery(String  request)
        throws PsicquicServiceException,
               NotSupportedMethodException,
               NotSupportedTypeException{
        
        Log log = LogFactory.getLog( this.getClass() );
        log.info( "PsqRestImpl: getByPostQuery: context =" + psqContext);
        log.info( "PsqRestImpl: getByPostQuery: q=" + request );

        JSONObject jRequest = null;

        String query = "";
        String format = "tab25";
        String firstResult = "0";
        String maxResults = "500";
        
        try{
            jRequest = new JSONObject( request );
        } catch( JSONException jx ){
            throw new PsicquicServiceException( "Wrong parameter format", 
                                                null );
        }
        
        if( jRequest == null ){
            throw new PsicquicServiceException( "Missing parameters", 
                                                null );            
        }
        
        try{
            query = jRequest.getString( "query" );
        } catch( JSONException jx ){}

        try{
            format = jRequest.getString( "format" );
        } catch( JSONException jx ){}

        try{
            firstResult = jRequest.getString( "firstResult" );
        } catch( JSONException jx ){}
        
        try{
            maxResults = jRequest.getString( "maxResults" );
        } catch( JSONException jx ){} 
       
        return this.getByQuery( query, format, firstResult, maxResults );
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
        
        log.debug( "getByQuery: rs="+ qrs );
        log.debug( "meta=" + qrs.getMeta() );
        
        String mitab = psqServer.getHeader( format );
        if( format != null && format.equalsIgnoreCase("count")){
            mitab += psqServer.getMaxCount( qrs );           
        } else {
            mitab += psqServer.toString( qrs, true, true );
        }    
        mitab += psqServer.getFooter( format );
        
        if( mitab!= null && mitab.length() > 32 ){
            log.debug( "getByQuery: miab(0,32)=" + mitab.substring(0,32) );
        } else {
            log.debug( "getByQuery: miab=" + mitab );
        }

        // Meta info 
        //----------
        
        if( qrs.getMeta() != null ){

            if( qrs.getMeta().get( "groups") != null ){

                if( psqServer.getMediaType( format ).equals( "text/xml" ) ){
                    mitab += "<!--\n";
                }
                
                Map<String,List<ValueCount>> groups 
                    = (Map<String,List<ValueCount>>) qrs.getMeta().get( "groups"); 
                
                for( Iterator<Map.Entry<String,List<ValueCount>>> ig =
                         groups.entrySet().iterator(); ig.hasNext(); ){
                
                    Map.Entry<String,List<ValueCount>> me = ig.next();
                
                    String field = me.getKey();
                
                    for( Iterator<ValueCount> ivc = me.getValue().iterator();
                         ivc.hasNext(); ){
                        
                        ValueCount vc = ivc.next();
                        String value = vc.getValue();
                        long count = vc.getCount();
                        mitab += "#" + "miqlx:groupby" + "\t" 
                            + field + "\t" 
                            + value + "\t" 
                            + count + "\n";
                    }
                }

                if( psqServer.getMediaType( format ).equals( "text/xml" ) ){
                    mitab += "-->\n";                    
                }
            }
        
        }
        
        try{

            return prepareResponse( psqServer, mitab, format,
                                    qrs.getResultCount() ).build(); 
            
        } catch (Throwable e) {

            PsicquicFault psqFault = new PsicquicFault();
            throw new PsicquicServiceException( "Problem creating output", 
                                                psqFault, e);
        }
        
    }
    
    //--------------------------------------------------------------------------
    
    protected Response.ResponseBuilder 
        prepareResponse( PsicquicServer psqServer, Object result, 
                         String format, long totalCount ) throws IOException {
        
        Response.ResponseBuilder rb = Response.status( 200 );
        
        rb.type( psqServer.getMediaType( format ));
        rb.entity( new GenericEntity<String>( (String) result ){} );
        
        prepareHeaders( rb, psqServer )
            .header( "X-PSICQUIC-Count", String.valueOf(totalCount));
        
        return rb;
    }
    
    public Response.ResponseBuilder 
        prepareHeaders( Response.ResponseBuilder rspBuilder,
                        PsicquicServer psqServer ) {

        rspBuilder.header( "X-PSICQUIC-Impl", 
                           psqServer.getProperty("rest","implementation"));
        rspBuilder.header( "X-PSICQUIC-Impl-Version", 
                           psqServer.getProperty("rest","implementation-ver"));
        rspBuilder.header( "X-PSICQUIC-Spec-Version", 
                           psqServer.getVersion("rest") );
        rspBuilder.header( "X-PSICQUIC-Supports-Compression", 
                           Boolean.TRUE );

        return rspBuilder;
    }


    //--------------------------------------------------------------------------
    
    public Object getByInteractor( String intAc, String db, String format,
                                   String firstResult, String maxResults ) 
        throws PsicquicServiceException,
               NotSupportedMethodException,
               NotSupportedTypeException {
       
        String query = psqServer.buildQuery( "identifier", db, intAc );
        return getByQuery( query, format, firstResult, maxResults );
    }
    
    //--------------------------------------------------------------------------

    public Object getByInteraction( String intAc, String db, String format,
                                    String firstResult, String maxResults ) 
        throws PsicquicServiceException,
               NotSupportedMethodException,
               NotSupportedTypeException{

        String query = psqServer.buildQuery( "interaction_id", db, intAc );
        return getByQuery( query, format, firstResult, maxResults );
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
