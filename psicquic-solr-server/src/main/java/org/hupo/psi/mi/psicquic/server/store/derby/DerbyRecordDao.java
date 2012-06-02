package org.hupo.psi.mi.psicquic.server.store.derby;

/* =============================================================================
 # $Id:: PsqPortImpl.java 259 2012-05-06 16:29:56Z lukasz                      $
 # Version: $Rev:: 259                                                         $
 #==============================================================================
 #
 # DerbyRecordDao: apache derby-based RecordDao implementation
 #
 #=========================================================================== */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Clob;
import java.sql.PreparedStatement;

import java.util.*;

import org.hupo.psi.mi.psicquic.util.JsonContext;
import org.hupo.psi.mi.psicquic.server.store.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//------------------------------------------------------------------------------

public class DerbyRecordDao implements RecordDao{

    Connection dbcon = null;

    public DerbyRecordDao(){
        try{
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch( Exception ex ){
            ex.printStackTrace();
        }
    }

    JsonContext context = null;
    
    public void setContext( JsonContext context ){
        this.context = context;
    }
    
    private void connect(){
        
        if( dbcon == null ){
            
            Log log = LogFactory.getLog( this.getClass() );
            log.info( "DerbyRecordDao:connect" );
            
            if( context != null && context.getJsonConfig() != null ){
                
                Map derbyCfg = (Map)
                    ((Map) context.getJsonConfig().get("store")).get("derby");
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
                    st.setQueryTimeout(5);
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
        try{
            Statement statement = dbcon.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            
            statement.executeUpdate( "create table record " +
                                     "(pk int generated always as identity," +
                                     " rid varchar(256),"+
                                     " format varchar(32), record clob )");

            statement.executeUpdate( "create index r_rid on record (rid)" );
            statement.executeUpdate( "create index r_ft on record (format)" );
            
        } catch( Exception ex ){
            ex.printStackTrace();
        }
    }

    public void shutdown(){
        if( dbcon != null ){      
            try{
                DriverManager.getConnection("jdbc:derby:;shutdown=true");
            } catch( Exception ex ){
                Log log = LogFactory.getLog( this.getClass() );
                log.info( "DerbyRecordDao(shutdown): " + ex);
            }
        }
    }
    
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------

    public void addRecord( String rid, String record, String format ){
        connect();
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
                .prepareStatement( "select rid, record from record" +
                                   " where rid = ? and format= ?" );
            
            pst.setString( 1, rid );
            pst.setString( 2, format );
            ResultSet rs =  pst.executeQuery();
            while( rs.next() ){
                Clob rc = rs.getClob( 2 );
                record = rc.getSubString( 1L, 
                                          new Long(rc.length()).intValue() );
            } 

	log.info( "DerbyRecordDao: recId=" + rid + "  record=" + record );

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
}
