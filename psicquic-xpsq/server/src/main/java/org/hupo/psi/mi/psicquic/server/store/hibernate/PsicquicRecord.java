package org.hupo.psi.mi.psicquic.server.store.hibernate;

/* =============================================================================
 ! $Id::                                                                       $
 ! Version: $Rev::                                                             $
 ==============================================================================
 !
 ! PsicqucRecord: Hibernate implementation of PSICQUIC record
 !
 !=========================================================================== */

public class PsicquicRecord{
    
    private long id;
    private String rid;
    private String format;
    private String value;

    public PsicquicRecord(){
    }

    public PsicquicRecord(String rid, String format, String value){
        this.rid = rid;
        this.format = format;
        this.value = value;

    }

    public void setId(long id){
        this.id = id;
    }

    public void setRid(String rid){
        this.rid = rid;
    }

    public void setFormat(String format){
        this.format = format;
    }

    public void setValue(String value){
        this.value = value;
    }


    public long getId(){
        return id;
    }

    public String getRid(){
        return rid;
    }

    public String getFormat(){
        return format;
    }

    public String getValue(){
        return value;
    }

}
