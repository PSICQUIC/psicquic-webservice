package org.hupo.psi.mi.psicquic.server.index;

import java.net.MalformedURLException;
import java.io.InputStream;
import java.util.Map;
import java.util.List;

public interface RecordIndex{

    public ResultSet query( String query );
    public ResultSet query( String query, Map<String,List<String>> xquery );
    public void initialize();
    public void connect() throws MalformedURLException;

    public void addFile( String format, String fileName, InputStream is );
}
