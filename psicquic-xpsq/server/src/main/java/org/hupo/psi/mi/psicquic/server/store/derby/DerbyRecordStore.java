package org.hupo.psi.mi.psicquic.server.store.derby;

/* =============================================================================
 # $Id::                                                                       $
 # Version: $Rev::                                                             $
 #==============================================================================
 #
 # DerbyRecordStore: apache derby-backed RecordStore implementation
 #
 #
 #=========================================================================== */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Clob;
import java.sql.PreparedStatement;

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

public class DerbyRecordStore extends RdbRecordStore{ 
    
    public static final int TIMEOUT_LONG = 30;
    public static final int TIMEOUT_SHORT = 5;

    public static final int TR_ISOLATION_LEVEL =
        Connection.TRANSACTION_READ_UNCOMMITTED;
    
    Connection dbcon = null;
    
    Map<String,Map<String,PsqTransformer>> inTransformerMap = null;
    
    String rmgrURL = null;
    String derbyHome = null;
    
    public DerbyRecordStore(){
        
        try{
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch( Exception ex ){
            ex.printStackTrace();
        }
    }

    public DerbyRecordStore( PsqContext context ){
        
        setPsqContext( context );
        Log log = LogFactory.getLog( this.getClass() );
        try{
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch( Exception ex ){
            ex.printStackTrace();
        }
    }

    public DerbyRecordStore( PsqContext context, String host ){
        
        setPsqContext( context );
        Log log = LogFactory.getLog( this.getClass() );
        if( host != null ){
            this.host = host;
        }

        log.info( "Server Host=" + host);
        
        try{
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch( Exception ex ){
            ex.printStackTrace();
        }
    }
    
    public void initialize(){

        Log log = LogFactory.getLog( this.getClass() );
        log.info( "initialize()" );
        
        derbyHome = System.getProperty( "xpsq.derby.home");
        log.info( "derby-home(prefix): " + derbyHome );
        
        String activeName = getPsqContext().getActiveStoreName();
        
        if( activeName != null 
            && activeName.equalsIgnoreCase("derby") ){
            connect();
        } else {
            log.info( " derby-store not active: initialization skipped");
        }
    }
    
    private void connect(){
        
        if( dbcon == null ){
            
            Log log = LogFactory.getLog( this.getClass() );
            log.info( "DerbyRecordDao:connect" );

            String activeName = getPsqContext().getActiveStoreName();

            if( activeName == null
                || !activeName.equalsIgnoreCase("derby") ){
                log.warn( " derby-store not active: not connecting" );
                return;
            }
            
            if( getPsqContext() != null 
                && getPsqContext().getJsonConfig() != null ){

                Map derbyCfg = 
                    (Map) ((Map) getPsqContext().getJsonConfig().get("store"))
                    .get("derby");
                try{
                    String derbydb = (String) derbyCfg.get("db-home");
                    log.info( " db-home(config): " + derbydb );

                    if( derbyHome == null ){
                        derbyHome = System.getProperty( "xpsq.derby.home");
                    }

                    if( derbyHome != null && !derbydb.startsWith("/") ){
                        derbydb = derbyHome + File.separator + derbydb;
                    }
                    log.info( " db-home(final): " + derbydb );

                    dbcon = 
                        DriverManager.getConnection( "jdbc:derby:" + 
                                                     derbydb + ";create=true");
                    
                    dbcon.setTransactionIsolation( TR_ISOLATION_LEVEL );
                    
                } catch( Exception ex ){
                    ex.printStackTrace();
                }
                
                try{
                    Statement st = dbcon.createStatement();
                    st.setQueryTimeout( TIMEOUT_SHORT );
                    ResultSet rs = 
                        st.executeQuery( " select count(*) from record" );
                    while( rs.next() ){
                        log.info( "   record count= " + rs.getInt(1) );
                    }
                } catch( Exception ex ){
                    
                    // missing table ?
                    log.info( "   creating record table" ); 
                    create();
                }
            }
        }
    }

    //--------------------------------------------------------------------------

    private void create(){

        Log log = LogFactory.getLog( this.getClass() );

        Statement st = null;
        try{
            st = dbcon.createStatement();
            st.setQueryTimeout( TIMEOUT_LONG ); 
            
            st.executeUpdate( "create table record " +
                              "(pk int generated always as identity," +
                              " rid varchar(256),"+
                              " format varchar(32), record clob )");
            
            st.executeUpdate( "create index r_rid on record (rid)" );
            st.executeUpdate( "create index r_ft on record (format)" );
            st.close();
        } catch( Exception ex ){
            ex.printStackTrace();
        } 
        
    }
    
    public void shutdown(){

        if( dbcon != null ){      
            try{
                DriverManager.getConnection( "jdbc:derby:;shutdown=true" );
            } catch( Exception ex ){
                Log log = LogFactory.getLog( this.getClass() );
                log.info( "DerbyRecordDao(shutdown): " + ex);
            }
        }
    }
    
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------

    public void addRecord( String rid, String record, String format ){

        Log log = LogFactory.getLog( this.getClass() );
        log.debug("rid=" + rid + " format=" + format 
                  + " record(length)=" + record.length() );
        
        connect();
        log.debug( "dbcon=" + dbcon );
        PreparedStatement pst = null;
        try{
            pst = dbcon.prepareStatement( "insert into record" 
                                          + " (rid, record, format)" 
                                          + " values (?,?,?)" );
            
            pst.setString( 1, rid );
            
            if( record != null && format != null ){
                pst.setString( 2, record );
                pst.setString( 3, format );
            
                pst.executeUpdate();
            }
            pst.close();
        }catch( Exception ex ){
            ex.printStackTrace();
        }
    }

    //--------------------------------------------------------------------------
    
    public void deleteRecord( String rid, String format ){
        Log log = LogFactory.getLog( this.getClass() );

        if( rid == null || format == null ) return;
        
        connect();
        log.debug( "dbcon=" + dbcon );

        PreparedStatement pst = null;
        try{
            pst = dbcon.prepareStatement( "delete from record"
                                          + " where rid = ? and format = ?" );
            
            pst.setString( 1, rid );
            pst.setString( 2, format );  
            pst.executeUpdate();
            pst.close();
        }catch( Exception ex ){
            ex.printStackTrace();
        }
    }

    //--------------------------------------------------------------------------

    public void deleteRecords( List<String> idList, String format ){
        Log log = LogFactory.getLog( this.getClass() );
        
        if( idList == null || format == null ) return;
        
        connect();
        log.debug( "dbcon=" + dbcon );

        PreparedStatement pst = null;
        try{
            pst = dbcon.prepareStatement( "delete from record"
                                          + " where rid = ? and format = ?" );
            
            pst.setString( 2, format );
            
            dbcon.setAutoCommit( false );
            for( Iterator<String> i = idList.iterator(); i.hasNext(); ){
                pst.setString( 1, i.next());
                pst.addBatch();
            }
            pst.executeUpdate();
            pst.close();
            dbcon.setAutoCommit( true );
        }catch( Exception ex ){
            ex.printStackTrace();
        }
    }

    //--------------------------------------------------------------------------

    public void deleteRecords( List<String> idList ){
        Log log = LogFactory.getLog( this.getClass() );
        
        if( idList == null ) return;
        
        connect();
        log.debug( "dbcon=" + dbcon );
        PreparedStatement pst = null;
        try{
            pst = dbcon.prepareStatement( "delete from record"
                                          + " where rid = ?" );
            dbcon.setAutoCommit( false );
            for( Iterator<String> i = idList.iterator(); i.hasNext(); ){
                pst.setString( 1, i.next());
                pst.addBatch();
            }
            pst.executeUpdate();            
            pst.close();
            dbcon.setAutoCommit( true );
        }catch( Exception ex ){
            ex.printStackTrace();
        }
    }
    
    //--------------------------------------------------------------------------

    public void updateRecord( String rid, String record, String fmt ){
        Log log = LogFactory.getLog( this.getClass() );
        log.warn( "updateRecord: not implemented" );
    }

    //--------------------------------------------------------------------------


    public String getRecord( String rid, String format ){
        connect();
        String record = "";
        
	Log log = LogFactory.getLog( this.getClass() );

        PreparedStatement pst = null;
        ResultSet rs = null;
        Clob rc = null;
        
        try{
            pst = dbcon.prepareStatement( "select rid, record, format" 
                                          + " from record" 
                                          + " where rid = ? and format= ?" );
            pst.setString( 1, rid );
            pst.setString( 2, format );
            rs =  pst.executeQuery();

            String rt = "";

            while( rs.next() ){
                rc = rs.getClob( 2 );
                record = rc.getSubString( 1L, 
                                          new Long(rc.length()).intValue() );
                rt = rs.getString(3);
            } 
            if(record != null && record.length() > 32){
                log.debug( "DerbyRecordDao(getRecord): recId=" + rid 
                           + " rt=" + rt 
                           + "  record=" + record.substring(0, 32) );
            } else {

                log.debug( "DerbyRecordDao(getRecord): recId=" + rid
                           + " rt=" + rt
                           + "  record=" + record );
            }
            rs.close();
            pst.close();

        }catch( Exception ex ){
            ex.printStackTrace();
        }
        return record;
    }

    //--------------------------------------------------------------------------
    
    public List<String> getRecordList( List<String> rid, String format ){
        
        connect();
        List<String> recordList = new ArrayList<String>();
        
        PreparedStatement pst = null;
        ResultSet rs = null;
        Clob rc = null;
        
        try{
            pst = dbcon.prepareStatement( "select rid, record from record" 
                                          + " where rid = ? and format = ?" );
            
            for( Iterator<String> i = rid.iterator(); i.hasNext(); ){
                pst.setString( 1, i.next() );
                pst.setString( 2, format );
                rs =  pst.executeQuery();
                
                while( rs.next() ){
                    rc = rs.getClob(2);
                    String record = 
                        rc.getSubString( 1L, 
                                         new Long(rc.length()).intValue() );
                    recordList.add( record );
                    
                }
                rs.close();
            }
            rs.close();
            pst.close();
        } catch( Exception ex ){
            ex.printStackTrace();
        }
        
        return recordList;
    }
    
    public void clearLocal(){
        
        Log log = LogFactory.getLog( this.getClass() );
        
        if( dbcon == null ){
            connect();
        }
        
        Statement st = null;
        try{
            st = dbcon.createStatement();
            st.setQueryTimeout(60);
            st.executeUpdate( "truncate table record");
            log.info( "record table truncated" );
            st.close();
        } catch( Exception ex ){
            // missing table ?
            log.info( ex.getMessage(), ex );
            log.info( "creating record table" );
            create();
        }
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------

    public Map getMeta(){
        connect();
        Map meta = new HashMap();
        meta.put("resource-class","store");
        meta.put("resource-type","derby");

        PreparedStatement pst = null;
        ResultSet rs = null;
        try{

            Map viewCount = new HashMap();
            
            pst = dbcon.prepareStatement( "select format, count(*)"
                                          + " from record" 
                                          + " group by format" );
            
            rs =  pst.executeQuery();
            long all = 0;
            while( rs.next() ){
                    String vt = rs.getString(1);
                    int vc = rs.getInt(2);
                    all += vc;
                    viewCount.put( vt, vc );
            }
            rs.close();
            pst.close();
            viewCount.put( "all", all );
            meta.put( "counts", viewCount );
        } catch( Exception ex ){
            ex.printStackTrace();
        }
        return meta;
    }
}
