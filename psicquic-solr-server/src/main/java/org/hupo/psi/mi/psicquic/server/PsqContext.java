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

    private JsonContext jsonContext;

    public void setPsqConfig( JsonContext context ){
        jsonContext = context;
    }

    public Map<String,Object> getJsonConfig(){
        return jsonContext.getJsonConfig();
    }

    private Map<String,RecordIndex> indexMap;
    
    public void setIndexMap( Map map ){
        indexMap = map;
    }

    public RecordIndex getIndex( String name ){
        return indexMap.get( name );
    }

    public RecordIndex getActiveIndex(){
        return indexMap.get( (String) ((Map) getJsonConfig().get( "index" ))
                             .get( "active" ) );
    }
    
    private Map<String,RecordStore> storeMap;
    
    public void setStoreMap( Map map ){
        storeMap = map;
    }

    public RecordStore getStore( String name ){
        return storeMap.get( name );
    }

    public RecordStore getActiveStore(){
        return storeMap.get( (String) ((Map) getJsonConfig().get( "store" ))
                             .get( "active" ) );
    }
}


