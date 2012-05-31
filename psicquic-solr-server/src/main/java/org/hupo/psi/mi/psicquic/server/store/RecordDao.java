package org.hupo.psi.mi.psicquic.server.store;

/* =============================================================================
 # $Id:: ResultSet.java 934 2012-05-29 15:08:30Z lukasz99                      $
 # Version: $Rev:: 934                                                         $
 #==============================================================================
 #
 # Record access: 
 #
 #=========================================================================== */

import java.util.List;

public interface RecordDao{
    public void addRecord( String id, String record, String format );
    public String getRecord( String id, String format );
    public List<String> getRecordList( List<String> id, String format );
}
