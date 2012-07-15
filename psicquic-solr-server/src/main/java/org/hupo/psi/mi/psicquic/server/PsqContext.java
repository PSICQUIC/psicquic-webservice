package org.hupo.psi.mi.psicquic.server;

/* =============================================================================
 # $Id::                                                                       $
 # Version: $Rev::                                                             $
 #==============================================================================
 #
 # PsqContext: server configuration
 #
 #=========================================================================== */

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hupo.psi.mi.psicquic.util.JsonContext;
import org.hupo.psi.mi.psicquic.server.index.RecordIndex;
import org.hupo.psi.mi.psicquic.server.store.RecordStore;

public class PsqContext{

    public PsqContext(){}
    
    public PsqContext( JsonContext context ){
        jsonContext = context;
    }
    
    private JsonContext jsonContext;

    public void setPsqConfig( JsonContext context ){
        jsonContext = context;
    }

    public Map<String,Object> getJsonConfig(){
        if( jsonContext != null ){
            return jsonContext.getJsonConfig();
        } 
        return null;
    }

    private Map<String,RecordIndex> indexMap;
    
    public void setIndexMap( Map map ){
        indexMap = map;
    }

    public RecordIndex getIndex( String name ){
        return indexMap.get( name );
    }
    
    public String getActiveIndexName(){
        return (String) ((Map) getJsonConfig().get( "index" )).get( "active" );
    }
    
    public RecordIndex getActiveIndex(){
        return indexMap.get( getActiveIndexName() );
    }
    
    private Map<String,RecordStore> storeMap;
    
    public void setStoreMap( Map map ){
        storeMap = map;
    }

    public RecordStore getStore( String name ){
        return storeMap.get( name );
    }
    
    public String getActiveStoreName(){
        return (String) ((Map) getJsonConfig().get( "store" )).get( "active" );
    }
    
    public RecordStore getActiveStore(){
        return storeMap.get( getActiveStoreName() );
    }
    
    public Map getMiqlxDef(){
        return (Map) ((Map) getJsonConfig().get( "service" )).get( "miqlx" );
        
    }

    public String getRecId(){
        return (String) 
            ((Map) getJsonConfig().get( "store" )).get( "rec-id" );
    }
    public String getDefaultView(){
        return (String) 
            ((Map) getJsonConfig().get( "store" )).get( "view-default" );
    }

    public long getDefaultFirstResult(){
        return (Long)
            ((Map) getJsonConfig().get( "index" )).get( "first-result" );
    }

    public long getDefaultBlockSize(){
        return (Long)
            ((Map) getJsonConfig().get( "index" )).get( "block-size" );
    }


}


