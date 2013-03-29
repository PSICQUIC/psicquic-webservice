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
import java.util.Map;
import java.io.File;

import org.hupo.psi.mi.psicquic.server.*;

public interface RecordStore{
    public void initialize();
    public void clear();
    public void clearLocal();
    public void shutdown();

    public void delete( List<String> idLst);


    public void addFile( File f, String name, String format, 
                         String compress );
    public void addFile( File f, String name, String format, 
                         String compress, Map<String,String> trpar );

    public String getRecord( String id, String format );

    public void addRecord( String id, String record, String format );

    public void updateRecord( String id, String record, String format );

    public void deleteRecord( String id, String format );    
    public void deleteRecords( List<String> idList, String format );
    public void deleteRecords( List<String> idList );

    public List<String> getRecordList( List<String> id, String format );
    public String toString( ResultSet rset );
    public Map getMeta();
}
