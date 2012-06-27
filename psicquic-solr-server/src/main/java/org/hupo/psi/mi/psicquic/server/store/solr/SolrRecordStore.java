package org.hupo.psi.mi.psicquic.server.store.solr;

/* =============================================================================
 # $Id:: DerbyRecordStore.java -1                                              $
 # Version: $Rev:: -1                                                          $
 #==============================================================================
 #
 # SolrRecordStore: apache solr-backed RecordStore implementation
 #
 #=========================================================================== */


import java.util.*;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.hupo.psi.mi.psicquic.util.JsonContext;
import org.hupo.psi.mi.psicquic.server.store.*;

import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.SolrDocumentList;

import org.apache.solr.core.*;
import org.apache.solr.common.*;
import org.apache.solr.client.solrj.*;
import org.apache.solr.client.solrj.impl.*;
import org.apache.solr.client.solrj.response.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//------------------------------------------------------------------------------

public class SolrRecordStore implements RecordStore{

    private String baseUrl;
    private List<String> shardUrl;
    private String shardStr= "";

    Map<String,Object> viewMap;

    JsonContext context = null;

    public SolrRecordStore(){}

    public SolrRecordStore( JsonContext context ){
        this.context = context;        
    }
    
    public void setContext( JsonContext context ){
        this.context = context;
    }

    public void initialize(){
        initialize( false );
    }

    public void initialize( boolean force ){

        
        Log log = LogFactory.getLog( this.getClass() );
        log.info( " initilizing SolrRecordIndex: context=" + context );
        if( context != null ){
            log.info( "                        "
                      + "JsonConfig=" + context.getJsonConfig() );
        }
        if( context != null && context.getJsonConfig() != null ){

            // SOLR URLs
            //----------

            Map jSolrCon = (Map)
                ((Map) context.getJsonConfig().get("store")).get("solr");


            log.info( "    url=" + jSolrCon.get("url") );
            log.info( "    shard=" + jSolrCon.get("shard") );

            baseUrl = (String) jSolrCon.get("url");

            if( jSolrCon.get("shard") != null ){
                List shardList = (List) jSolrCon.get("shard");
                
                for( Iterator is = shardList.iterator(); is.hasNext(); ){
                    String csh = (String) is.next();
                    shardStr += csh + ",";
                    if( shardUrl == null ){
                        shardUrl = new ArrayList<String>();
                    }
                    shardUrl.add( "http://" + csh );
                }
                if( !shardStr.equals("") ){
                    shardStr = shardStr.substring( 0, shardStr.length()-1);
                }
            }

            // Views
            //------
            
            viewMap =  (Map) jSolrCon.get("view");

            /* "psi-mi/tab25":{
                  "config":{
                    "col":["idA_o","idB_o","altidA_o","altidB_o",
                           "aliasA_o","aliasB_o","detmethod_o","pubauth_o",
                           "pubid_o","taxidA_o","taxidB_o","type_o","source_o",
                           "interaction_id_o","confidence_o" ]
                  } }                   
            */           
        }
    }
    
    public void shutdown(){}
    
    private void connect(){}
    
    public void addFile( String fileName, String format, InputStream is ){
        // NOTE: dummy operation: records expected to be stored while indexing

        Log log = LogFactory.getLog( this.getClass() );
        log.info( " SolrRecordStore: addFile(dummy) file=" + fileName );
        
    }

    public void addRecord( String rid, String record, String format ){
        // NOTE: dummy operation: records expected to be stored while indexing

    }

    public String getRecord( String rid, String format ){

        String record = "";
        Log log = LogFactory.getLog( this.getClass() );

        try{
            if( baseUrl == null ){ initialize(); }
            log.debug( "   SolrRecordIndex: baseUrl="+ baseUrl );

            if( baseUrl != null ){
                
                SolrServer solr = new CommonsHttpSolrServer( baseUrl );
                ModifiableSolrParams params = new ModifiableSolrParams();
                params.set( "q", "uuId:" + rid.replace( ":","\\:") ); // escape special character
                
                // set shards when needed
                //-----------------------

                if( shardStr != null ){
                    params.set( "shards", shardStr );
                }

                try{
                    QueryResponse response = solr.query( params );
                    log.debug( "response = " + response );

                    SolrDocumentList res = response.getResults();
                    
                    if( res.size() == 1 ){
                        record = convert( res.get(0), format );
                    }

                } catch( SolrServerException sex ){
                    sex.printStackTrace();
                }
            }
        } catch( MalformedURLException mex ){
            mex.printStackTrace();
        }
        
        return record;
    }
    
    public List<String> getRecordList( List<String> rid, String format ){
        
        List<String> recordList = new ArrayList<String>();
        
        connect();

        return recordList;
        
    }

    private String convert( SolrDocument sdoc, String format ){

        Log log = LogFactory.getLog( this.getClass() );
        log.info( " SolrRecordStore: convert to format=" + format );

        Map vconf = ((Map) ((Map) viewMap.get( format )).get("config"));

        
        StringBuffer res = new StringBuffer();
        List<String> cnl = (List<String>) vconf.get( "col" );
        String sep = (String) vconf.get( "col-sep" );
        String nullSep = "-" + (String) vconf.get( "col-sep" );
        String escSep = "\\" + (String) vconf.get( "col-sep" );
        
        for( Iterator<String> icnl = cnl.iterator(); icnl.hasNext(); ){
            
            String cv = (String) sdoc.getFirstValue( icnl.next() );
            if( cv != null ){
                if( !sep.equals("\t") ){
                    try{
                        cv = cv.replaceAll( sep, escSep ); 
                    } catch( Exception ex){
                        // should not happen
                    }
                }
                res.append( cv ).append( sep ); 
            } else {
                res.append( nullSep );
            }
        }
        return res.substring( 0, res.length() - 1 ) + "\n";
    }
}