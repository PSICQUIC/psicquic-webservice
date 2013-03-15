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
    
    Connection dbcon = null;

    Map<String,Map<String,PsqTransformer>> inTransformerMap = null;
    
    String rmgrURL = null;
    //String host = null;
    
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
        
    }
    
    private void connect(){
        
        if( dbcon == null ){
            
            Log log = LogFactory.getLog( this.getClass() );
            log.info( "DerbyRecordDao:connect" );
            
            if( getPsqContext() != null 
                && getPsqContext().getJsonConfig() != null ){

                Map derbyCfg = 
                    (Map) ((Map) getPsqContext().getJsonConfig().get("store"))
                    .get("derby");
                try{
                    String derbydb = (String) derbyCfg.get("derby-db");
                    log.info( "               location: " + derbydb );

                    dbcon = 
                        DriverManager.getConnection( "jdbc:derby:" + 
                                                     derbydb + ";create=true");
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
        
        try{
            Statement st = dbcon.createStatement();
            st.setQueryTimeout( TIMEOUT_LONG ); 
            
            st.executeUpdate( "create table record " +
                              "(pk int generated always as identity," +
                              " rid varchar(256),"+
                              " format varchar(32), record clob )");
            
            st.executeUpdate( "create index r_rid on record (rid)" );
            
            st.executeUpdate( "create index r_ft on record (format)" );
            
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
        try{
            PreparedStatement pst = dbcon
                .prepareStatement( "insert into record (rid, record, format)" +
                                   " values (?,?,?)" );
            
            pst.setString( 1, rid );
            
            if( record != null && format != null ){
                pst.setString( 2, record );
                pst.setString( 3, format );
            
                pst.executeUpdate();
            } 
        }catch( Exception ex ){
            ex.printStackTrace();
        }
    }

    //--------------------------------------------------------------------------

    public String getRecord( String rid, String format ){
        connect();
        String record = "";
        
	Log log = LogFactory.getLog( this.getClass() );

        try{
            PreparedStatement pst = dbcon
                .prepareStatement( "select rid, record, format from record" +
                                   " where rid = ? and format= ?" );
            
            pst.setString( 1, rid );
            pst.setString( 2, format );
            ResultSet rs =  pst.executeQuery();

            String rt = "";

            while( rs.next() ){
                Clob rc = rs.getClob( 2 );
                record = rc.getSubString( 1L, 
                                          new Long(rc.length()).intValue() );
                rt = rs.getString(3);
            } 
            if(record != null ){
                log.debug( "DerbyRecordDao(getRecord): recId=" + rid 
                           + " rt=" + rt 
                           + "  record=" + record.substring(0, 32) );
            }
        }catch( Exception ex ){
            ex.printStackTrace();
        }
        return record;
    }

    //--------------------------------------------------------------------------
    
    public List<String> getRecordList( List<String> rid, String format ){
        
        connect();
        List<String> recordList = new ArrayList<String>();
        
        try{
            PreparedStatement pst = dbcon
                .prepareStatement( "select rid, record from record" +
                                   " where rid = ? and format = ?" );
            
            for( Iterator<String> i = rid.iterator(); i.hasNext(); ){
                pst.setString( 1, i.next() );
                pst.setString( 2, format );
                ResultSet rs =  pst.executeQuery();
                
                while( rs.next() ){
                    Clob rc = rs.getClob(2);
                    String record = 
                        rc.getSubString( 1L, 
                                         new Long(rc.length()).intValue() );
                    recordList.add( record );
                }
            }
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

        try{
            Statement st = dbcon.createStatement();
            st.setQueryTimeout(60);
            st.executeUpdate( "truncate table record");
            log.info( "record table truncated" );
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

        try{

            Map viewCount = new HashMap();
            
            

            PreparedStatement pst = dbcon
                .prepareStatement( "select format, count(*) from record" +
                                   " group by format" );
            
            ResultSet rs =  pst.executeQuery();
            long all = 0;
            while( rs.next() ){
                    String vt = rs.getString(1);
                    int vc = rs.getInt(2);
                    all += vc;
                    viewCount.put( vt, vc );
            }
            viewCount.put( "all", all );
            meta.put( "counts", viewCount );
        } catch( Exception ex ){
            ex.printStackTrace();
        }
        return meta;
    }
}
