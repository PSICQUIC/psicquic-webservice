package org.hupo.psi.mi.psicquic.server;

/* =============================================================================
 # $Id::                                                                       $
 # Version: $Rev::                                                             $
 #==============================================================================
 #                                                                             
 # ResultSet: index query results
 #                                                                             
 #=========================================================================== */

import java.util.*;

public class ResultSet{

    List resultList;

    long firstResult;
    long maxResult;

    Map<String,Object> meta;
    
    public ResultSet(){
        firstResult = 0;
        maxResult = 0;
    }
    
    public ResultSet( long first, long max, List results ){
        resultList = results;
        firstResult = first;
        maxResult = max;
    }

    public List getResultList(){
        if(resultList == null ){
            resultList = new ArrayList();
        }
        return resultList;
    }

    public void setResultList( List results ){
        resultList = results;
    }
    
    public long getFirstResult(){
        return firstResult;
    }

    public void setFirstResult( long first ){
        firstResult = first;
    }

    public long getMaxResult(){
        return maxResult;
    }

    public void setMaxResult( long max ){
        maxResult = max;
    }
    
    public Map<String,Object> getMeta(){

        if( meta == null ){
            meta = new HashMap<String,Object>();
        }
        return meta;
    }

    public void setMeta( Map<String,Object> map ){
        meta = map;
    }

    public void setMeta( String key, Object value ){

        if( meta == null ){
            meta = new HashMap<String,Object>();
        }

        meta.put( key, value );
    }

}