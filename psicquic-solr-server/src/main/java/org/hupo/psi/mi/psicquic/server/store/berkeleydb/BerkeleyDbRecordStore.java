package org.hupo.psi.mi.psicquic.server.store.berkeleydb;

/* =============================================================================
 # $Id::                                                                       $
 # Version: $Rev::                                                             $
 #==============================================================================
 #
 # BerkeleyDbRecordStore: Berkeley DB(JE)-backed RecordStore implementation
 #
 #
 #=========================================================================== */

import com.sleepycat.je.*;
import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.TupleBinding;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

import java.util.regex.*;

import org.hupo.psi.mi.psicquic.util.JsonContext;
import org.hupo.psi.mi.psicquic.server.*;
import org.hupo.psi.mi.psicquic.server.store.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//------------------------------------------------------------------------------

public class BerkeleyDbRecordStore extends RdbRecordStore{ 
    
    public static final int TIMEOUT_LONG = 30;
    public static final int TIMEOUT_SHORT = 5;
    
    Map<String,Map<String,PsqTransformer>> inTransformerMap = null;
    
    String rmgrURL = null;

    private String bdbHome;
    private Environment bdbEnv;
    private Map<String,Database> bdbMap = new HashMap<String,Database>();
    
    public BerkeleyDbRecordStore(){
        try{
            
        } catch( Exception ex ){
            ex.printStackTrace();
        }
    }

    public BerkeleyDbRecordStore( PsqContext context ){

        setPsqContext( context );
        
        Log log = LogFactory.getLog( this.getClass() );
        try{
            
        } catch( Exception ex ){
            ex.printStackTrace();
        }
    }

    public BerkeleyDbRecordStore( PsqContext context, String host ){
        
        setPsqContext( context );
        Log log = LogFactory.getLog( this.getClass() );
        
        log.info( "Server Host=" + host);
        
        try{
        } catch( Exception ex ){
            ex.printStackTrace();
        }
    }
    
    public void initialize(){
        
        Log log = LogFactory.getLog( this.getClass() );
        log.info( "BerkeleyDbRecordStore: initialize" );
        if( getPsqContext() != null 
            && getPsqContext().getJsonConfig() != null ){
            
            Map bdbCfg = 
                (Map) ((Map) getPsqContext().getJsonConfig().get("store"))
                .get("berkeleydb");
            
            String berkeleydb = (String) bdbCfg.get("db-home");
            log.info( " berkeleydb-home(config): " + berkeleydb );
            
            if( bdbHome == null ){
                bdbHome = System.getProperty( "xpsq.berkeleydb.home");
            }
            
            if( bdbHome != null && !berkeleydb.startsWith("/") ){
                berkeleydb = bdbHome + File.separator + berkeleydb;
            }
            log.info( " berkelydb-home(final): " + berkeleydb );
            
            EnvironmentConfig myEnvConfig = new EnvironmentConfig();
            myEnvConfig.setAllowCreate( true );
            myEnvConfig.setTransactional( true );
            
            try{
                
                log.info("Opening environment in: " + berkeleydb );
                bdbEnv = new Environment( new File( berkeleydb ), 
                                          myEnvConfig  );
                
            }  catch( DatabaseException dbex ){
                dbex.printStackTrace();
            }            
        } else {
            
        }
        log.info( "BerkeleyDbRecordStore: initialize (DONE)" );

    }

    //--------------------------------------------------------------------------
    
    public void shutdown(){
        
        if( bdbEnv != null ){      
            try{
                for( Iterator<Map.Entry<String,Database>> 
                         it = bdbMap.entrySet().iterator(); it.hasNext(); ){
                    Map.Entry<String,Database> entry = (Map.Entry) it.next();
                    
                    Database idb = entry.getValue();
                    idb.close();
                }
                bdbEnv.close();
            } catch( Exception ex ){
                Log log = LogFactory.getLog( this.getClass() );
                log.info( "BdbRecordStore(shutdown): " + ex);
            }
        }
    }
    
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------

    public void addRecord( String rid, String record, String format ){
        
        Log log = LogFactory.getLog( this.getClass() );
        log.debug("rid=" + rid + " format=" + format 
                  + " record(length)=" + record.length() );
        

        Database db = bdbMap.get( format );
        if (db == null ){
            db = createDatabase( format );
        }
        
        if( db == null ){
            log.warn( "database access error format=" + format);
            return;
        }

        Transaction txn = bdbEnv.beginTransaction(null, null);
        try {

            EntryBinding stringBinding = 
                TupleBinding.getPrimitiveBinding( String.class );
            
            DatabaseEntry key = new DatabaseEntry();
            stringBinding.objectToEntry( rid, key );
            
            DatabaseEntry val = new DatabaseEntry();
            stringBinding.objectToEntry( record, val );
            
            db.put(txn, key, val );
            txn.commit();
            
        } catch (Exception e) {
            if (txn != null) {
                txn.abort();
                txn = null;
            }
        }
    }

    //--------------------------------------------------------------------------

    public String getRecord( String rid, String format ){
 
        Log log = LogFactory.getLog( this.getClass() );
        String record = "";
        
        Database myDb = bdbMap.get( format );
        
        if( myDb == null ){
            log.warn( "database access error format=" + format );
            return record;
        }
        
        Transaction txn = bdbEnv.beginTransaction(null, null);        
        try {   
             EntryBinding stringBinding =
                 TupleBinding.getPrimitiveBinding( String.class );

            DatabaseEntry key = new DatabaseEntry();
            stringBinding.objectToEntry( rid, key );
            
            DatabaseEntry data = new DatabaseEntry();
            OperationStatus retVal 
                = myDb.get( txn, key, data, LockMode.READ_UNCOMMITTED);
            if( retVal == OperationStatus.SUCCESS ){
                record = (String) stringBinding.entryToObject( data );
            }

        } catch (Exception e) {
            if (txn != null) {
                txn.abort();
                txn = null;
            }
        }
        
        return record;
    }

    //--------------------------------------------------------------------------
    
    public List<String> getRecordList( List<String> rid, String format ){

        Log log = LogFactory.getLog( this.getClass() );
        
        List<String> recordList = new ArrayList<String>();
        
        Database myDb = bdbMap.get( format );

        if( myDb == null ){
            log.warn( "database access error format=" + format );
            return recordList;
        }

        EntryBinding stringBinding =
            TupleBinding.getPrimitiveBinding( String.class );
        
        for( Iterator<String> i = rid.iterator(); i.hasNext(); ){

            Transaction txn = bdbEnv.beginTransaction(null, null);
            
            try {
                
                DatabaseEntry key = new DatabaseEntry();
                stringBinding.objectToEntry( rid, key );

                DatabaseEntry data = new DatabaseEntry();
                
                OperationStatus retVal
                    = myDb.get( txn, key, data, LockMode.READ_UNCOMMITTED);
                if( retVal == OperationStatus.SUCCESS ){
                    String record = 
                        (String) stringBinding.entryToObject( data );
                    if( record != null ){
                        recordList.add( record );
                    }
                }

            } catch (Exception e) {
                    if (txn != null) {
                        txn.abort();
                        txn = null;
                    }
            }
        }
        
        return recordList;
    }

    //--------------------------------------------------------------------------    
    
    public void clearLocal(){
        
        Log log = LogFactory.getLog( this.getClass() );
        log.info( "BerkeleyDbRecordStore(clearLocal): START");

        if( bdbEnv != null ){
            try{
                for( Iterator<Map.Entry<String,Database>>
                         it = bdbMap.entrySet().iterator(); it.hasNext(); ){

                    Map.Entry<String,Database> entry = (Map.Entry) it.next();
                    
                    Database myDb = entry.getValue();
                    Transaction txn = bdbEnv.beginTransaction(null, null);
                    try{
                        long recCount = 
                            bdbEnv.truncateDatabase( txn, 
                                                     myDb.getDatabaseName(), 
                                                     true );
                        log.info( "BerkeleyDbRecordStore(clearLocal):"
                                  + " database: " + myDb.getDatabaseName() 
                                  + " records removed: " + recCount );
                    }catch( Exception e ){
                        if( txn != null ){
                            txn.abort();
                            txn = null;
                        }
                    }
                }
            } catch( Exception ex ){
                log.info( "BerkeleyDbRecordStore(clearLocal): DONE");
            }
        }
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------

    public Map getMeta(){
       
        Map meta = new HashMap();
        meta.put("resource-class","store");
        meta.put("resource-type","berkeleydb");
        
        try{
            Map viewCount = new HashMap();
            
            long all = 0;

            for( Iterator<Map.Entry<String,Database>>
                     it = bdbMap.entrySet().iterator(); it.hasNext(); ){

                Map.Entry<String,Database> entry = (Map.Entry) it.next();

                Database myDb = entry.getValue();
                long recCount = myDb.count();
                viewCount.put( entry.getValue(), recCount );
                
                all += recCount;
            }
            viewCount.put( "all", all );
            meta.put( "counts", viewCount );
        } catch( Exception ex ){
            ex.printStackTrace();
        }
   
        return meta;
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------

    private Database createDatabase( String format ){
        
        Map berkeleydbCfg = 
            (Map) ((Map) getPsqContext().getJsonConfig().get("store"))
            .get("berkeleydb");

        Map dbConfig = (Map) ((Map) ((Map) berkeleydbCfg.get("view"))
                              .get( format )).get("config");
        
        String dbName = (String) dbConfig.get("db-name");
        
        try{
            DatabaseConfig myConfig = new DatabaseConfig();
            myConfig.setTransactional( true );
            Database myDb  = bdbEnv.openDatabase( null, dbName, myConfig );
            bdbMap.put( format, myDb );
        } catch (DatabaseException de) {
            // Exception handling goes here
        }
        return bdbMap.get( format);
    }
}
