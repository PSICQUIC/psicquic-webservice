package org.hupo.psi.mi.psicquic.server.builder;

/* =============================================================================
 # $Id:: BuildSolrDerbyIndex.java 266 2012-05-21 01:02:31Z lukasz              $
 # Version: $Rev:: 266                                                         $
 #==============================================================================
 #
 # IndexBuilder: populate  PSICQUIC index 
 #
 #=========================================================================== */

import java.util.*;
import java.util.zip.*;
import java.net.*;
import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hupo.psi.mi.psicquic.server.index.*;
import org.hupo.psi.mi.psicquic.server.index.solr.SolrRecordIndex;

import org.hupo.psi.mi.psicquic.server.store.*;
import org.hupo.psi.mi.psicquic.server.store.derby.DerbyRecordStore;
import org.hupo.psi.mi.psicquic.server.store.solr.SolrRecordStore;

import org.hupo.psi.mi.psicquic.util.JsonContext;

public class IndexBuilder{

    private List<String> solrURL = 
        Arrays.asList( "http://127.0.0.1:8080/psicquic-solr/ws/psq" );    

    private String rmgrURL = 
        "http://127.0.0.1:8080/psicquic-solr/recordmgr";
    
    public static final String 
        CONTEXT = "/etc/psq-context-default.json";
    
    public static final String 
        RFRMT = "mif254";
        
    String format;
    
    String source = null;
    boolean zip = false;

    boolean defCtx = true;

    RecordIndex recordIndex = null;
    RecordStore recordStore = null;
    
    //--------------------------------------------------------------------------
    
    JsonContext psqContext;
    
    public void setPsqContext( JsonContext context ){
        psqContext = context;
    } 

    public JsonContext getPsqContext(){
        return psqContext;
    }

    //--------------------------------------------------------------------------
    
    public IndexBuilder( String ctx, String format, boolean zip ){
        
        InputStream isCtx = null;
        try{
            isCtx = new FileInputStream( ctx );
        } catch( Exception ex ){
            ex.printStackTrace();
        }
        
        defCtx = false;
        this.initialize( isCtx, format, zip );
    }

    public IndexBuilder( InputStream isCtx, String format, boolean zip ){
        this.initialize( isCtx, format, zip );
    }
    
    private void initialize( InputStream isCtx, String format, boolean zip ){
        
        this.zip = zip;
        this.format=format;
        this.source = source;



        // setup threads here
        //-------------------







        
        // get context
        //------------

        psqContext = new JsonContext();

        try{                     
            
            psqContext.readJsonConfigDef( isCtx );
            Map jctx = (Map) getPsqContext().getJsonConfig();
            
            // SOLR Index
            //-----------

            String activeIndex = 
                (String) ((Map)jctx.get( "index" )).get( "active" );

            if( activeIndex.equals( "solr" ) ){
                recordIndex = new SolrRecordIndex( psqContext );
                
                recordIndex.initialize();
                recordIndex.connect();
            }            
            
            // Derby data store
            //-----------------

            String activeStore = 
                (String) ((Map)jctx.get( "store" )).get( "active" );
            
            if( activeStore.equals( "solr" ) ){
                
                recordStore = new SolrRecordStore( psqContext );
                recordStore.initialize();
            }

            if( activeStore.equals( "derby" ) ){
                
                recordStore = new DerbyRecordStore( psqContext );
                recordStore.initialize();
            }
            
        } catch( Exception ex ){
            ex.printStackTrace();
        }        
    }

    //--------------------------------------------------------------------------

    public void processFile( String file ){
        File srcFl = new File( file );
        index("", srcFl );        
    }
    
    public void processDirectory( String dir, boolean desc ){
        File dirFl = new File( dir );
        try{
            processFiles( dirFl.getCanonicalPath(), dirFl, desc );
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    private void processFiles( String root, File file, boolean desc ){
        
        File[] filesAndDirs = file.listFiles();
        List<File> filesDirs = Arrays.asList( filesAndDirs );
        for(File sfile : filesDirs) {
            if ( ! sfile.isFile() && desc) {
                
                //recursive call: must be a directory to descend
                
                processFiles( root, sfile, desc );
            } else {
                index( root, sfile );                
            }
        }
    }
    
    public void index( String root, File file ){
        
        System.out.println( file );
        
        // transform/add -> index
        //-----------------------

        try{

            InputStream is = null;
            
            if( zip ){
                ZipFile zf = new ZipFile( file );
                is = zf.getInputStream( zf.entries().nextElement() );
            } else {
                is = new FileInputStream( file );  
            }

            recordIndex.addFile( format, 
                                 file.getCanonicalPath().replace( root, "" ), 
                                 is );
            
        }catch( Exception ex ){
            ex.printStackTrace();
        }
        
        // transform/add -> datastore
        //---------------------------

        try{
            
            InputStream is = null;
            
            if( zip ){
                ZipFile zf = new ZipFile( file );
                is = zf.getInputStream( zf.entries().nextElement() );
            } else {
                is = new FileInputStream( file );  
            }
            
            recordStore.addFile( format,
                                 file.getCanonicalPath().replace( root, "" ), 
                                 is );
            
        }catch( Exception ex ){
            ex.printStackTrace();
        }       
    }
}
