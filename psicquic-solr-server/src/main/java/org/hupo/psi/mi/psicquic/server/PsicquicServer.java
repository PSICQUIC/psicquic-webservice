package org.hupo.psi.mi.psicquic.server;

/* =============================================================================
 # $Id::                                                                       $
 # Version: $Rev::                                                             $
 #==============================================================================
 #
 # PsicquicServer: 
 #
 #=========================================================================== */

import java.util.*;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hupo.psi.mi.psicquic.*;
import org.hupo.psi.mi.psicquic.server.index.*;
import org.hupo.psi.mi.psicquic.server.store.*;

import org.hupo.psi.mi.psicquic.util.JsonContext;

public class PsicquicServer {
    
    PsqContext psqContext;
    
    public void setPsqContext( PsqContext context ){
        psqContext = context;
    }

    public PsqContext getPsqContext(){
        return psqContext;
    }
    
    //--------------------------------------------------------------------------

    private void initialize() {
        initialize( false );
    }
    
    //--------------------------------------------------------------------------
    
    private void initialize( boolean force) {

	Log log = LogFactory.getLog( this.getClass() );
	log.info( " psqContext=" + psqContext );
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------

    public ResultSet getByQuery( String query, String resultType,
                                 long firstResult, long blockSize ){
        
        Log log = LogFactory.getLog( this.getClass() );
        log.info( "getByQuery: context =" + psqContext);
        log.info( "getByQuery: q=" + query );
        
        log.info( " FR(in)=" + firstResult + " BS(in)=" + blockSize );
        
        // PAGING
        //-------
        
        if( firstResult < 0 ){
            firstResult = psqContext.getDefaultFirstResult();
        } 
        
        if( blockSize < 0 ){
            blockSize = psqContext.getDefaultBlockSize();
        } 

        // MIQLX
        //------
        
        Map<String,List<String>> miqlx = null;
        
        if( query != null && query.indexOf( " Miqlx" ) > -1 ){
            MiqlxFilter mf = new MiqlxFilter( psqContext );
            query = mf.process( query );
            miqlx = mf.getMiqlx();
        }
        
        String viewType = psqContext.getDefaultView();
        
        if( miqlx != null ){
            for( Iterator mi = miqlx.entrySet().iterator(); mi.hasNext(); ){
                Map.Entry me = (Map.Entry) mi.next();

                log.info( "MIQLX: field=\'" + me.getKey() 
                          + "\'  value=\'" + me.getValue() +"\'");
            }

            if( miqlx.get("MiqlxView:") != null ){
                viewType = ((List<String>) miqlx.get("MiqlxView:")).get(0);
            }
        }
        log.info( " FR(q)=" + firstResult + " BS(q)=" + blockSize );
        ResultSet qrs = psqContext.getActiveIndex()
            .query( query, miqlx, firstResult, blockSize );
        
        ResultSet prs = 
            new ResultSet( qrs.getFirstResult(), qrs.getMaxResult(),
                           new ArrayList() );
        
	log.info( "getByQuery: rs="+ qrs); 
        
        for( Iterator i = qrs.getResultList().iterator(); i.hasNext(); ){
            Map in = (Map) i.next();
            log.debug( "getByQuery: in="+ in);

            String recId = (String) in.get( psqContext.getRecId() );

            String drecord =  psqContext.getActiveStore()
                .getRecord( recId , viewType );
        
            log.debug( " SolrDoc: recId=" + recId + " :: "  + drecord );
            prs.getResultList().add( drecord );
        }
        
        return prs;
    }
    
    //--------------------------------------------------------------------------
    
    public ResultSet getByInteractor(  String db, String ac, String resultType,
                                       int firstResult, int blockSize ){
        return getByQuery( db+"\\:"+ac, resultType, firstResult, blockSize );
    }
    
    //--------------------------------------------------------------------------

    public ResultSet getByInteractorList( List<Map<String,String>> intId, 
                                          String resultType,
                                          int firstResult, int blockSize,
                                          String operand ){                               
        return null;
     }
    
    //--------------------------------------------------------------------------
    
    public ResultSet getByInteraction( String db, String ac, String resultType,
                                       int firstResult, int blockSize ){

        return getByQuery( db+"\\:"+ac, resultType, firstResult, blockSize );
    }

    //--------------------------------------------------------------------------
    
    public ResultSet getByInteractionList( List<Map<String,String>> intId, 
                                           String resultType,
                                           int firstResult, int blockSize ){    
        return null;
    }

    //--------------------------------------------------------------------------

    public List<String> getSupportedReturnTypes( String service) {
        
        return (List<String>) ((Map) ((Map) psqContext.getJsonConfig()
                                      .get( "service" )).get( service ))
            .get( "supported-return-type" );
    }
    
    //--------------------------------------------------------------------------
    
    public String getVersion( String service ){
        
        return (String) ((Map) ((Map) psqContext.getJsonConfig()
                                .get( "service" )).get( service ))
            .get( "version" );
    }

    //--------------------------------------------------------------------------
    
    public List<String> getSupportedDbAcs( String service ){
        
        return (List<String>) ((Map) ((Map) psqContext.getJsonConfig()
                                      .get( "service" )).get( service ))
            .get( "supported-db-ac" );
    }
    
    //--------------------------------------------------------------------------

    public String getProperty( String service, String property ){     
        
        return (String) ((Map) ((Map) ((Map) psqContext.getJsonConfig()
                                       .get( "service" )) .get( service )) 
                         .get( "properties" ))
            .get( property );
    }
    
    //--------------------------------------------------------------------------
    
    public Set<Map.Entry> getProperties( String service ){
        
        Map propmap = (Map) ((Map) ((Map) psqContext.getJsonConfig()
                                    .get( "service" )).get( service ))
            .get( "properties" );
        
        return (Set<Map.Entry>) propmap.entrySet(); 
    }
}
