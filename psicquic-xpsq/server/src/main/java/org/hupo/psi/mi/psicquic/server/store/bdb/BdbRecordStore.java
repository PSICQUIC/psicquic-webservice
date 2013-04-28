package org.hupo.psi.mi.psicquic.server.store.bdb;

/* =============================================================================
 # $Id::                                                                       $
 # Version: $Rev::                                                             $
 #==============================================================================
 #
 # BdbRecordStore: Berkeley DB(JE)-backed RecordStore implementation
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

import java.util.concurrent.TimeUnit;

import java.util.regex.*;

import org.hupo.psi.mi.psicquic.util.JsonContext;
import org.hupo.psi.mi.psicquic.server.*;
import org.hupo.psi.mi.psicquic.server.store.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//------------------------------------------------------------------------------

public class BdbRecordStore extends RdbRecordStore{ 
    
    public static final int TIMEOUT_LONG = 30;
    public static final int TIMEOUT_SHORT = 5;
    
    Map<String,Map<String,PsqTransformer>> inTransformerMap = null;
    
    String rmgrURL = null;

    private String bdbHome;
    private Environment bdbEnv;
    private Map<String,Database> bdbMap = new HashMap<String,Database>();
    
    public BdbRecordStore(){
        try{
            
        } catch( Exception ex ){
            ex.printStackTrace();
        }
    }

    public BdbRecordStore( PsqContext context ){

        setPsqContext( context );
        
        Log log = LogFactory.getLog( this.getClass() );
        try{
            
        } catch( Exception ex ){
            ex.printStackTrace();
        }
    }

    public BdbRecordStore( PsqContext context, String host ){
        
        setPsqContext( context );
        Log log = LogFactory.getLog( this.getClass() );
        
        log.info( "Server Host=" + host);
        
        if( host != null ){
            this.host = host;
        }
        
    }
    
    public void initialize(){
        
        Log log = LogFactory.getLog( this.getClass() );
        log.info( "BdbRecordStore: initialize" );
        if( getPsqContext() != null 
            && getPsqContext().getJsonConfig() != null ){
            
            String activeCfg = 
                (String) ((Map) getPsqContext().getJsonConfig().get("store"))
                .get("active");

            if( activeCfg == null ||  !activeCfg.equalsIgnoreCase("bdb") ){
                log.info( " bdb-store not active: initialization skipped");
                return;
            }

            Map bdbCfg = 
                (Map) ((Map) getPsqContext().getJsonConfig().get("store"))
                .get("bdb");
            
            String berkeleydb = (String) bdbCfg.get("db-home");
            log.info( " bdb-home(config): " + berkeleydb );
            
            if( bdbHome == null ){
                bdbHome = System.getProperty( "xpsq.bdb.home");
            }
            
            if( bdbHome != null && !berkeleydb.startsWith("/") ){
                berkeleydb = bdbHome + File.separator + berkeleydb;
            }
            log.info( " bdb-home(final): " + berkeleydb );
            
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
        log.info( "BdbRecordStore: initialize (DONE)" );
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
            //bdbMap.put( format, db );
            log.debug("Bdb(key)=" + format + " new db=" + db);
        }
        
        if( db == null ){
            log.info( "database access error format=" + format );
            return;
        }

        Transaction txn = bdbEnv.beginTransaction( null, null );
        
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

    public void deleteRecord( String rid, String format ){
        Log log = LogFactory.getLog( this.getClass() );
        
        if( rid == null || format == null ) return;
 
        Database myDb = bdbMap.get( format );
        if( myDb == null ) return;
        
        Transaction txn = bdbEnv.beginTransaction( null, null );
        try {
            
            EntryBinding stringBinding =
                TupleBinding.getPrimitiveBinding( String.class );
            
            DatabaseEntry key = new DatabaseEntry();
            stringBinding.objectToEntry( rid, key );
            
            myDb.delete( txn, key);
            txn.commit();
            
        } catch (Exception e) {
            if (txn != null) {
                txn.abort();
                txn = null;
            }
        }
    }

    //--------------------------------------------------------------------------
    
    public void deleteRecords( List<String> idList, String format ){
        Log log = LogFactory.getLog( this.getClass() );
    
        if( idList == null || format == null ) return;

        Database myDb = bdbMap.get( format );
        if( myDb == null ) return;

        EntryBinding stringBinding =
            TupleBinding.getPrimitiveBinding( String.class );
        
        Transaction txn = bdbEnv.beginTransaction(null, null);
        try {
            for( Iterator<String> i = idList.iterator(); i.hasNext(); ){
                
                DatabaseEntry key = new DatabaseEntry();
                stringBinding.objectToEntry( i.next(), key );

                myDb.delete( txn, key);
            }
            txn.commit();
            
        } catch (Exception e) {
            if (txn != null) {
                txn.abort();
                txn = null;
            }
        }
    }

    //--------------------------------------------------------------------------

    public void deleteRecords( List<String> idList ){
        Log log = LogFactory.getLog( this.getClass() );

        if( idList == null || bdbEnv == null ) return;

        EntryBinding stringBinding =
            TupleBinding.getPrimitiveBinding( String.class );

        Transaction txn = bdbEnv.beginTransaction(null, null);
        
        try{
            for( Iterator<String> i = idList.iterator(); i.hasNext(); ){
             
                String id = i.next();
                log.debug("id=>" + id + "<=");
                for( Iterator<Map.Entry<String,Database>>
                         it = bdbMap.entrySet().iterator(); it.hasNext(); ){
                
                    DatabaseEntry key = new DatabaseEntry();
                    stringBinding.objectToEntry( id, key );
                    
                    Map.Entry<String,Database> entry = (Map.Entry) it.next();
                    
                    Database myDb = entry.getValue();
                    myDb.delete( txn, key);
                }            
            }
            txn.commit();
            
        } catch (Exception e) {
            e.printStackTrace();
            if (txn != null) {
                txn.abort();
                txn = null;
            }
        }
    }
    
    //--------------------------------------------------------------------------
    
    public void updateRecord( String rid, String record, String fmt ){
        Log log = LogFactory.getLog( this.getClass() );
        log.info( "updateRecord: not implemented" );        
    }


    //--------------------------------------------------------------------------

    public String getRecord( String rid, String format ){
        
        Log log = LogFactory.getLog( this.getClass() );
        String record = "";
        
        Database myDb = bdbMap.get( format );
        
        if( myDb == null ){
            log.info( "database access error format=" + format );
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
            txn.commit();
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
            log.info( "database access error format=" + format );
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
                txn.commit();
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
        log.info( "BdbRecordStore(clearLocal): START");
        log.info( " BdbEnv=" + bdbEnv );

        if( bdbEnv != null ){
            try{
                for( Iterator<Map.Entry<String,Database>>
                         it = bdbMap.entrySet().iterator(); it.hasNext(); ){

                    Map.Entry<String,Database> entry = (Map.Entry) it.next();
                    
                    Database myDb = entry.getValue();
                  
                    log.info( " Bdb(view)=" + entry.getKey() + " db=" + myDb );
                    
                    Transaction txn = bdbEnv.beginTransaction(null, null);
                    txn.setLockTimeout( 10, TimeUnit.SECONDS ); 
                    try{
                        
                        String name = myDb.getDatabaseName();                                            
                        DatabaseConfig config = myDb.getConfig();
                        
                        myDb.close();
                        
                        long recCount = 
                            bdbEnv.truncateDatabase( txn, name, true );
                        log.info( "BdbRecordStore(clearLocal):"
                                  + " database: " + name 
                                  + " records removed: " + recCount );
                        
                        // reopen
                        //-------
                        
                        myDb  = bdbEnv.openDatabase( txn , name, config );
                        bdbMap.put( entry.getKey(), myDb );
                        
                        txn.commit();
                        
                    }catch( Exception e ){
                        if( txn != null ){
                            txn.abort();
                            txn = null;
                        }
                        e.printStackTrace();
                    }
                }
            } catch( Exception ex ){
                log.info( "BdbRecordStore(clearLocal): DONE");
            }
        }
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------

    public Map getMeta(){
       
        Map meta = new HashMap();
        meta.put("resource-class","store");
        meta.put("resource-type","bdb");
        
        try{
            Map viewCount = new HashMap();
            
            long all = 0;

            for( Iterator<Map.Entry<String,Database>>
                     it = bdbMap.entrySet().iterator(); it.hasNext(); ){

                Map.Entry<String,Database> entry = (Map.Entry) it.next();

                Database myDb = entry.getValue();
                long recCount = myDb.count();
                viewCount.put( entry.getKey(), recCount );
                
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

        Log log = LogFactory.getLog( this.getClass() );
        log.info( "BdbRecordStore(createDatabase): START");        
        Map berkeleydbCfg = 
            (Map) ((Map) getPsqContext().getJsonConfig().get("store"))
            .get("bdb");
        
        Map dbConfig = (Map) ((Map) ((Map) berkeleydbCfg.get("view"))
                              .get( format )).get("config");
        
        String dbName = (String) dbConfig.get( "db-name" );
        log.info( "db-name=" + dbName );

        try{
            DatabaseConfig myConfig = new DatabaseConfig();
            myConfig.setTransactional( true );
            myConfig.setAllowCreate( true );

            Database myDb  = bdbEnv.openDatabase( null, dbName, myConfig );
            bdbMap.put( format, myDb );
        } catch (DatabaseException de) {
            de.printStackTrace();
            // Exception handling goes here
        }
        return bdbMap.get( format );
    }
}
