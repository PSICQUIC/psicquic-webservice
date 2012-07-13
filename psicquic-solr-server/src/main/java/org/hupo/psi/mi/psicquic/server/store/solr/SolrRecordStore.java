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

import java.util.regex.*;

import org.hupo.psi.mi.psicquic.util.JsonContext;

import org.hupo.psi.mi.psicquic.server.*;
import org.hupo.psi.mi.psicquic.server.store.*;
import org.hupo.psi.mi.psicquic.server.index.solr.*;

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
    String host = null;

    public SolrRecordStore(){}

    public SolrRecordStore( JsonContext context ){
        this.context = context;
    }
    
    public SolrRecordStore( JsonContext context, String host ){
        this.context = context;        
    }
    
    public void setContext( JsonContext context ){
        this.context = context;
    }

    PsqContext  psqContext = null;
    public void setPsqContext( PsqContext context ){
        this.psqContext = context;
    }

    SolrRecordIndex sri = null;

    public void setSolrRecordIndex( SolrRecordIndex sri ){
        this.sri = sri;
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

            baseUrl = (String) jSolrCon.get( "url" );

            if( host != null ){
                baseUrl = hostReset( baseUrl , host );
            }

            if( jSolrCon.get("shard") != null ){
                List shardList = (List) jSolrCon.get("shard");
                
                for( Iterator is = shardList.iterator(); is.hasNext(); ){
                    String csh = (String) is.next();
                    shardStr += csh + ",";
                    if( shardUrl == null ){
                        shardUrl = new ArrayList<String>();
                    }
                    
                    if( host != null ){
                        shardUrl.add( hostReset( "http://" + csh, host ) );
                    } else { 
                        shardUrl.add( "http://" + csh );
                    }
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
    
    private String hostReset( String url, String newHost ){

        if( host == null ) return url;
       
        // http://aaa:8888/d/d/d 

        try{
            Pattern p = Pattern.compile( "([^/]*//)([^/:]+)(:[0-9]+)?(.*)" );
            Matcher m = p.matcher( url );
            
            if( m.matches() ){
                
                String prefix = m.group( 1 );
                String host = m.group( 2 );
                String port = m.group( 3 );
                String postfix = m.group( 4 );

                String newUrl = prefix + newHost + port + postfix;
                return newUrl;

            } else {
                return url;
            }
        } catch(Exception ex ){
            return url;
        }
    }


    //--------------------------------------------------------------------------
    // NOTE: dummy operation: records are expected to be handled while indexing
    //--------------------------------------------------------------------------

    private void connect(){}
    public void clear(){}
    public void shutdown(){}
    public void addRecord( String rid, String record, String format ){}
    public void addFile( String fileName, String format, InputStream is ){
        Log log = LogFactory.getLog( this.getClass() );
        log.info( " SolrRecordStore: addFile(dummy) file=" + fileName );        
    }
    
    //-------------------------------------------------------------------------
    //-------------------------------------------------------------------------

    private SolrServer solr = null;

    public String getRecord( String rid, String format ){

        String record = "";
        Log log = LogFactory.getLog( this.getClass() );

        SolrDocument srec = null;
        Map recordCache = null;

        if( sri.getRecordCache() != null ){
            recordCache = sri.getRecordCache();
            synchronized( recordCache ){
                srec = (SolrDocument) recordCache.get( rid );
            }
        }
        
        if( srec == null ){

            if( baseUrl == null ){ initialize(); }
            log.debug( "   SolrRecordIndex: baseUrl="+ baseUrl );

            if( baseUrl != null ){
 
                if( solr == null ){
                    solr = new HttpSolrServer( baseUrl );
                }
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
                    srec = res.get(0);

                    String recfld = psqContext.getRecId();
                    String rec = (String) srec.get( recfld );
                                       
                    if( recordCache != null ){
                        synchronized( recordCache ){
                            recordCache.put( rec, srec );
                        }
                    }
                    
                } catch( SolrServerException sex ){
                    sex.printStackTrace();
                }
            }
        }
        
        if( srec !=null ){
            record = convert( srec , format );
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