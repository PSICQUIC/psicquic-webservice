package org.hupo.psi.mi.psicquic.server.store;

/* =============================================================================
 # $Id::                                                                       $
 # Version: $Rev::                                                             $
 #==============================================================================
 #
 # RecordStore: record access 
 #
 #=========================================================================== */

import java.util.List;
import java.io.InputStream;

public interface RecordStore{
    public void initialize();
    public void clear();
    public void shutdown();
    public void addRecord( String id, String record, String format );
    public String getRecord( String id, String format );
    public void addFile( String fileName, String format, InputStream is );
    public List<String> getRecordList( List<String> id, String format );
}
