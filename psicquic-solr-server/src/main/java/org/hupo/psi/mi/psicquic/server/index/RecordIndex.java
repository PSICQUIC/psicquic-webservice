package org.hupo.psi.mi.psicquic.server.index;

import java.net.MalformedURLException;
import java.io.File;

import java.util.Map;
import java.util.List;

import org.hupo.psi.mi.psicquic.server.ResultSet;

public interface RecordIndex{

    public ResultSet query( String query );
    public ResultSet query( String query, long firstResult, long blockSize );
    public ResultSet query( String query, Map<String,List<String>> xquery,
                            long firstResult, long blockSize );
    public void initialize();
    public void clear();
    public void delete( List<String> idLst );

    public void connect() throws MalformedURLException;

    public List<String> addFile( File f, String name, 
                                 String format, String compress, boolean logId );

    public List<String> addFile( File f, String name, 
                                 String format, String compress, boolean logId, 
                                 Map<String,String> trpar );

    public void deleteRecords( List<String> idList );
    
    public Map getMeta();
}
