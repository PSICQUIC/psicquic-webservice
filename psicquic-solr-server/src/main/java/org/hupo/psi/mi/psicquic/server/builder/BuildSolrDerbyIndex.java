package org.hupo.psi.mi.psicquic.server.builder;

/* =============================================================================
 # $Id:: BuildSolrDerbyIndex.java 266 2012-05-21 01:02:31Z lukasz              $
 # Version: $Rev:: 266                                                         $
 #==============================================================================
 #
 # BuildSolrDerbyIndex: populate Solr/Derby-based PSQ index 
 #
 #=========================================================================== */

import java.util.*;
import java.util.zip.*;
import java.net.*;
import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hupo.psi.mi.psicquic.server.index.*;

import org.hupo.psi.mi.psicquic.server.index.solr.SolrIndex;
import org.hupo.psi.mi.psicquic.server.store.derby.DerbyStore;

import org.hupo.psi.mi.psicquic.util.JsonContext;

public class BuildSolrDerbyIndex{

    private List<String> solrURL = 
        Arrays.asList( "http://127.0.0.1:8080/psicquic-solr/ws/psq" );    

    private String rmgrURL = 
        "http://127.0.0.1:8080/psicquic-solr/recordmgr";
    
    public static final String 
        CONTEXT = "/etc/psq-context-default.json";
    
    public static final String 
        RFRMT = "mif254";
        
    String rfrmt;
    
    String rootDir= null;
    boolean zip = false;

    boolean defCtx = true;

    SolrIndex sIndex = null;
    DerbyStore dStore = null;
    
    //--------------------------------------------------------------------------
    
    JsonContext psqContext;
    
    public void setPsqContext( JsonContext context ){
        psqContext = context;
    } 

    public JsonContext getPsqContext(){
        return psqContext;
    }

    //--------------------------------------------------------------------------
    
    public BuildSolrDerbyIndex( String ctx, String rfrmt,
                                String dir, boolean zip ){
        
        InputStream ctxStream = null;
        try{
            ctxStream = new FileInputStream( ctx );
        } catch( Exception ex ){
            ex.printStackTrace();
        }
        
        defCtx = false;
        this.initialize( ctxStream, rfrmt,  dir, zip );
    }

    public BuildSolrDerbyIndex( InputStream ctxStream, String rfrmt,
                                String dir, boolean zip ){
        this.initialize( ctxStream, rfrmt,  dir, zip );
    }
    
    private void initialize( InputStream ctxStream, String rfrmt,
                             String dir, boolean zip ){
        
        this.zip = zip;
        this.rfrmt = rfrmt;
        this.rootDir = dir;
        
        // get context
        //------------

        psqContext = new JsonContext();

        try{                     
            //psqContext.readJsonConfigDef( new FileInputStream( ctx ) );
            
            psqContext.readJsonConfigDef( ctxStream );
            Map jctx = (Map) getPsqContext().getJsonConfig();
            
            // SOLR Index
            //-----------

            if( ((Map)jctx.get( "index" )).get( "solr" ) != null ){
                sIndex = new SolrIndex( psqContext );

                sIndex.initialize();
                sIndex.connect();
            }            
            
            // Derby data store
            //-----------------

            if( ((Map)jctx.get( "store" )).get( "derby" ) != null ){
                
                dStore = new DerbyStore( psqContext );
                dStore.initialize();
            }
            
        } catch( Exception ex ){
            ex.printStackTrace();
        }        
    }

    //--------------------------------------------------------------------------

    public void start(){
    
        File dirFl = new File( rootDir );
        try{
            processFiles( dirFl.getCanonicalPath(), dirFl );
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    private void processFiles( String root, File file ){
        
        File[] filesAndDirs = file.listFiles();
        List<File> filesDirs = Arrays.asList( filesAndDirs );
        for(File sfile : filesDirs) {
            if ( ! sfile.isFile() ) {
                //recursive call: must be a directory
                processFiles( root, sfile );
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
            
            sIndex.addFile( rfrmt, 
                            file.getCanonicalPath().replace( root, "" ), is );
            
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
            
            dStore.addFile( rfrmt,
                            file.getCanonicalPath().replace( root, "" ), is );
            
        }catch( Exception ex ){
            ex.printStackTrace();
        }       
    }
}
