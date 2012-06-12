package org.hupo.psi.mi.psicquic.server.index;

import java.net.MalformedURLException;
import java.io.InputStream;

public interface RecordIndex{

    public ResultSet query( String query );
    public void initialize();
    public void connect() throws MalformedURLException;

    public void addFile( String format, String fileName, InputStream is );
}
