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

import java.lang.Thread.State;

public class IndexBuilder{

    private List<String> solrURL = 
        Arrays.asList( "http://127.0.0.1:8080/psicquic-solr/ws/psq" );    

    private String rmgrURL = 
        "http://127.0.0.1:8080/psicquic-solr/recordmgr";
    
    public static final String 
        CONTEXT = "/etc/psq-context-default.json";
    
    public static final String 
        RFRMT = "mif254";

    Log log = null;  //LogFactory.getLog( this.getClass() );
        
    String format;

    String root = null;
    String source = null;
    boolean zip = false;

    boolean defCtx = true;

    RecordIndex recordIndex = null;
    RecordStore recordStore = null;

    int builderTCount = 2;
    int solrconTCount = 2;
    
    //--------------------------------------------------------------------------
    
    JsonContext psqContext;
    
    public void setPsqContext( JsonContext context ){
        psqContext = context;
    } 

    public JsonContext getPsqContext(){
        return psqContext;
    }

    //--------------------------------------------------------------------------
    
    public IndexBuilder( String ctx, int btc, int stc, 
                         String format, boolean zip ){

        builderTCount = btc;
        solrconTCount = stc;

        InputStream isCtx = null;
        try{
            isCtx = new FileInputStream( ctx );
        } catch( Exception ex ){
            ex.printStackTrace();
        }
        
        defCtx = false;
        this.initialize( isCtx, format, zip );
    }

    public IndexBuilder( InputStream isCtx, int btc, int stc,
                         String format, boolean zip ){

        builderTCount = btc;
        solrconTCount = stc;
        
        this.initialize( isCtx, format, zip );
    }
    
    private void initialize( InputStream isCtx, String format, boolean zip ){
        
        this.zip = zip;
        this.format = format;
        this.source = source;
        
        this.log = LogFactory.getLog( this.getClass() );
        log.info( " initilizing IndexBuilder: threads=" + builderTCount );
        
        // get context
        //------------

        psqContext = new JsonContext();
        
        try{                     
            
            psqContext.readJsonConfigDef( isCtx );
            Map jctx = (Map) getPsqContext().getJsonConfig();
            
        } catch( Exception ex ){
            ex.printStackTrace();
        }        
    }

    //--------------------------------------------------------------------------

    public void processFile( String file ){
        File srcFl = new File( file );
        enqueue("", srcFl );        
    }
    
    public void processDirectory( String dir, boolean desc ){
        File dirFl = new File( dir );
        try{
            processFiles( dirFl.getCanonicalPath(), dirFl, desc );
        } catch( Exception ex ){
            ex.printStackTrace();
        }
    }
    
    private void processFiles( String root, File file, boolean desc ){
                
        this.root = root;
        File[] filesAndDirs = file.listFiles();
        List<File> filesDirs = Arrays.asList( filesAndDirs );
        
        if( ! file.isFile() ){
            log.info("IndexBuilder: processing directory=" + file );
        }
        
        for( File sfile : filesDirs) {
            if ( ! sfile.isFile() && desc) {
                
                //recursive call: must be a directory to descend      
                processFiles( root, sfile, desc );
            } else {
                enqueue( root, sfile );                
            }
        }
    }
    
    List<List<File>> thq;

    int cth = 0;

    public void enqueue( String root, File file ){
        
        if ( file.isFile() ) {

            if(thq == null ){
                thq = new ArrayList<List<File>>();
            }       
            if( thq.size() <= cth ){
                thq.add( new ArrayList<File>());
            }
            thq.get( cth ).add( file );
            cth++;
            if( cth == builderTCount ) cth = 0;
        }
    }
    
    public void start(){
        
        // start threads

        List<IndexThread> itl = new ArrayList<IndexThread>();

        for( Iterator<List<File>> thi = thq.iterator(); thi.hasNext(); ){

            List<File> fq = thi.next();
            log.info( "IndexBuilder: queue size=" + fq.size() );
            for( Iterator<File> fqi = fq.iterator(); fqi.hasNext(); ){
                log.info( "IndexBuilder:  file: " + fqi.next() );
            }
            
            IndexThread it = new IndexThread( psqContext, solrconTCount,
                                              root, fq, format, zip );
            itl.add( it );

            log.info( "IndexBuilder:  starting thread..." );
            it.start();            
        }

        // wait till all threads are done
        //-------------------------------

        boolean running = true;

        try{
            while( running ){        
                for( Iterator<IndexThread> iti = itl.iterator(); iti.hasNext(); ){
                    IndexThread cit = iti.next();
                    if( cit.getState() != Thread.State.TERMINATED ){
                        running = true;
                    }
                    System.out.println( cit.getState() + ":" );
                }
                if( running ){
                    System.out.print(".");
                    Thread.sleep( 1000*5 );
                }
            }

        } catch( InterruptedException ix ){
            ix.printStackTrace();
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

//------------------------------------------------------------------------------
//------------------------------------------------------------------------------

class IndexThread extends Thread{

    RecordIndex recordIndex;
    RecordStore recordStore;

    boolean zip = false;
    String root;
    String format;
    
    List<File> fileq;
    
    public IndexThread( JsonContext psqContext, int solrconTCount,
                        String root, List<File> files, 
                        String format, boolean zip ){
        
        Map jctx = (Map) psqContext.getJsonConfig();
        fileq = files;
        this.zip = zip;
        this.root = root;
        this.format = format;
        


        try{

            // SOLR Index
            //-----------
        
            String activeIndex = 
                (String) ((Map)jctx.get( "index" )).get( "active" );
            
            if( activeIndex.equals( "solr" ) ){
                recordIndex = new SolrRecordIndex( psqContext, solrconTCount );
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
        } catch( MalformedURLException mux ){
            mux.printStackTrace();
        }
    }

    //--------------------------------------------------------------------------    

    public void run(){
 
        Log log = LogFactory.getLog( this.getClass() );
        

        
        log.info( "IndexThread: processing fileq:" + fileq );


        for( Iterator<File> fi = fileq.iterator(); fi.hasNext(); ){
            
            File file = fi.next();
            log.info( "IndexThread: processing file:" + file );

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
                
                recordIndex
                    .addFile( format,
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
                
                recordStore
                    .addFile( format, 
                              file.getCanonicalPath().replace( root, "" ),
                              is );
                
            }catch( Exception ex ){
                ex.printStackTrace();
            }
        }
    }
}