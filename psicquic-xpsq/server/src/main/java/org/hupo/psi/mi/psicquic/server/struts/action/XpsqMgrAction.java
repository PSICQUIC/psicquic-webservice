package org.hupo.psi.mi.psicquic.server.struts.action;

/* =============================================================================
 * $HeadURL::                                                                  $
 * $Id::                                                                       $
 * Version: $Rev::                                                             $
 *==============================================================================
 *
 * XpsqMgrAction - server status/manager action
 *                
 ============================================================================ */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory; 

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.util.ServletContextAware;

import java.io.*;
import java.util.*;

import edu.ucla.mbi.util.data.*;
import edu.ucla.mbi.util.data.dao.*;

import edu.ucla.mbi.util.struts.action.*;
import edu.ucla.mbi.util.struts.interceptor.*;

import org.hupo.psi.mi.psicquic.server.PsqContext;
import org.hupo.psi.mi.psicquic.util.JsonContext;

public class XpsqMgrAction extends ManagerSupport {
    
    private static final String JSON = "json";    
    private static final String REDIRECT = "redirect";    
    private static final String ACL_PAGE = "acl_page";
    private static final String ACL_OPER = "acl_oper";
    

    ////------------------------------------------------------------------------
    /// Context
    //---------

    private PsqContext psqContext;

    public void setPsqContext( PsqContext context ) {
        this.psqContext = context;
    }

    public PsqContext  getPsqContext() {
        return this.psqContext;
    }


    ////-----------------------------------------------------------------------
    /// mode
    //-------
    
    private String mode = "";

    public void setMode( String mode ) {
        this.mode = mode;
    }

    public String getMode(){
        return this.mode;
    }
    
    //--------------------------------------------------------------------------
    // results
    //--------
    
    Map meta = null;
    
    public Map  getMeta(){
        return this.meta;
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------

    public String execute() throws Exception {
        
        Log log = LogFactory.getLog( this.getClass() );
        log.debug(  "id=" + getId() + " op=" + getOp() + " mode=" + getMode());
        
        if( getOp() == null ) return SUCCESS;
        
        for ( Iterator<String> i = getOp().keySet().iterator();
              i.hasNext(); ) {
            
            String key = i.next();
            String val = getOp().get(key);

            if ( val != null && val.length() > 0 ) {
                
                if ( key.equalsIgnoreCase( "meta" ) ) {

                    if( getMode() != null 
                        && getMode().equalsIgnoreCase( "index" ) ){                   
                        return indexMeta();
                    }
                    
                    if( getMode() != null 
                         && getMode().equalsIgnoreCase( "store" ) ){                   
                        return storeMeta();
                    }
                }                                            
            }
        }
        return SUCCESS;
    }

    //--------------------------------------------------------------------------
    
    private String indexMeta(){

        Log log = LogFactory.getLog( this.getClass() );
        log.debug(  "indexMeta: index=" + psqContext.getActiveIndex() );

        meta = psqContext.getActiveIndex().getMeta();
        return JSON;
    }

    //--------------------------------------------------------------------------

    private String storeMeta(){

        Log log = LogFactory.getLog( this.getClass() );
        log.debug(  "storeMeta: store=" + psqContext.getActiveStore() );

        meta = psqContext.getActiveStore().getMeta();        
        return JSON;
    }
    
}