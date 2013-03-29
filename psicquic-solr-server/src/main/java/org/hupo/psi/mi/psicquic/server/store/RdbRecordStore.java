package org.hupo.psi.mi.psicquic.server.store;

/* =============================================================================
 # $Id::                                                                       $
 # Version: $Rev::                                                             $
 #==============================================================================
 #
 # RdbRecordStore: realtione database-backed RecordStore implementation
 #
 #
 #=========================================================================== */

import org.w3c.dom.*;
import javax.xml.parsers.*;

import org.hupo.psi.mi.psicquic.server.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

import java.util.regex.*;

import org.hupo.psi.mi.psicquic.util.JsonContext;
import org.hupo.psi.mi.psicquic.server.*;
import org.hupo.psi.mi.psicquic.server.store.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//------------------------------------------------------------------------------

public abstract class RdbRecordStore implements RecordStore{

    public static final int TIMEOUT_LONG = 30;
    public static final int TIMEOUT_SHORT = 5;
    
    Map<String,Map<String,PsqTransformer>> inTransformerMap = null;

    String rmgrURL = null;

    private  PsqContext psqContext = null;

    protected String host = null;
    
    
    public abstract void initialize();
    
    public abstract void shutdown();
    
    public abstract void addRecord( String rid, String record, String fmt );
    public abstract void updateRecord( String rid, String record, String fmt );

    public abstract void deleteRecord( String rid, String fmt );    
    public abstract void deleteRecords( List<String> idList, String format );
    public abstract void deleteRecords( List<String> idList );


    public abstract String getRecord( String rid, String format );

    
    public abstract List<String> getRecordList( List<String> rid, 
                                                String format );

    
    public abstract void clearLocal();
    
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------

    public RdbRecordStore(){} 
    

    public void setPsqContext( PsqContext context ){
        this.psqContext = context;
    }
    
    public PsqContext getPsqContext(){
        return this.psqContext;
    }
    
    //--------------------------------------------------------------------------

    public String toString( org.hupo.psi.mi.psicquic.server.ResultSet rset ){
        
        String rstr = "";
        return rstr;
    }
    
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    
    public void addFile( File file, String fileName,
                         String format, String compress ){

        addFile( file, fileName, format, compress, null );
    }

    
    public void addFile( File file, String fileName, 
                         String format, String compress,
                         Map<String,String> trpar ){
      
        Log log = LogFactory.getLog( this.getClass() );
        
        log.debug( "RdbRecordStore: psqContext=" +getPsqContext() ); 
          
        Map trCfg = (Map) ((Map) getPsqContext().getJsonConfig().get("store"))
            .get("transform");
        
        List rtrList = (List) trCfg.get( format );

        log.debug( "RdbRecordStore:" 
                   + " addFile: file=" + file + " name=" + fileName );
        log.debug( "RdbRecordStore:"
                   + " addFile: format=" + format + " trl=" + rtrList );
        
        if( inTransformerMap == null ){
            inTransformerMap 
                = new HashMap<String,Map<String,PsqTransformer>>();            
        }
        
        Map<String,PsqTransformer> itm = inTransformerMap.get( format );
        if( itm == null ){
            itm = new HashMap<String,PsqTransformer>();
            inTransformerMap.put( format, itm );
            itm = inTransformerMap.get( format );
        }
        
        if( rtrList != null ){
            for( Iterator it = rtrList.iterator(); it.hasNext(); ){
                
                Map itr = (Map) it.next();
                log.debug( "DerbyRecordStore:" +
                           " addFile: view=" + itr.get( "view" ) );
               
                // XSLT transformer
                //------------------

                if( (Boolean) itr.get("active") ){

                    if( ((String) itr.get("type")).equalsIgnoreCase("XSLT")
                        &&itm.get( itr.get("view") ) == null ){            
                        
                        // initialize XSLT transformer
                        //----------------------------
                        
                        log.info( " Initializing transformer: format=" + format
                                  + " type=XSLT config=" + itr.get("config") );
                        
                        PsqTransformer rt =
                            new XsltTransformer( (Map) itr.get("config"), trpar );
                        
                        itm.put( (String) itr.get("view"), rt );
                    }

                    if( ((String) itr.get("type")).equalsIgnoreCase("CALIMOCHO")
                        &&itm.get( itr.get("view") ) == null ){            
                        
                        // initialize CALIMOCHO transformer
                        //---------------------------------
                        
                        log.info( " Initializing transformer: format=" + format
                                  + " type=CALIMOCHO config=" + itr.get("config") );

                        PsqTransformer rt =
                            new CalimochoTransformer( (Map) itr.get( "config" ) );
                        itm.put( (String) itr.get("view"), rt );

                        log.info( " Initializing transformer(" + (String) itr.get("view")
                                  + "): new=" + itm.get( itr.get("view") ) );
                    }
                    
                    if( ((String) itr.get("type")).equalsIgnoreCase("MITAB2MIF")
                        &&itm.get( itr.get("view") ) == null ){
                        
                        // initialize MITAB2MIF transformer
                        //---------------------------------
                        
                        log.info( " Initializing transformer: format=" + format
                                  + " type=MITAB2MIF config=" + itr.get("config") );
                        
                        PsqTransformer rt =
                            new Mitab2MifTransformer( (Map) itr.get("config") );
                        
                        itm.put( (String) itr.get("view"), rt );
                    }
                    
                    PsqTransformer rt = itm.get( itr.get("view") );                     
                    
                    log.info( "rt=" + rt 
                              + "view=" + itr.get( "view" )
                              + "fname=" + fileName );

                    try{
                        if( compress!= null 
                            && compress.equalsIgnoreCase("zip") ){
                            
                            processZipFile( rt, (String) itr.get( "view" ), 
                                            fileName, new ZipFile( file ));                            
                        } else {
                            processFile( rt, (String) itr.get( "view" ),
                                         fileName, new FileInputStream( file ));
                        }
                        
                    } catch( Exception ex ){
                        log.info( ex.getMessage(), ex );
                        return;
                    }
                }
            }            
        }
    }

    //--------------------------------------------------------------------------

    private void processZipFile( PsqTransformer rt, String viewName,
                                 String fileName,  ZipFile zf )
        throws java.io.IOException{
        
        for( Enumeration zfe = zf.entries(); zfe.hasMoreElements(); ){
            
            ZipEntry ze = (ZipEntry) zfe.nextElement();
            if( !ze.isDirectory() ){   
                InputStream is = zf.getInputStream( ze );
                processFile( rt, viewName,
                             fileName + "::" + ze.getName() , is );
            }
        }
    }

    //--------------------------------------------------------------------------

    private void processFile( PsqTransformer rt, String viewName, 
                              String fileName, InputStream is ){

        Log log = LogFactory.getLog( this.getClass() );
        rt.start( fileName, is );

        while( rt.hasNext() ){
					
            Map cdoc= rt.next();
            String recId = (String) cdoc.get( "recId" );
            NodeList fl = (NodeList) cdoc.get( "dom" );
                            
            String vStr = (String) cdoc.get( "view" );
            
            log.info("processFile->recId:" + recId ); 
            log.debug("processFile->dom:" + fl );
            if( vStr != null ){
                log.debug("processFile->view:" + vStr.substring( 0, 64 ) ); 
            }

            try{
                this.add( recId, viewName, vStr );
            }catch( Exception ex ){
                ex.printStackTrace();
            }
        }
    }

    //--------------------------------------------------------------------------
    
    public void delete( List<String> idLst ){
        
        Log log = LogFactory.getLog( this.getClass() );
        
        if( idLst == null || idLst.size() == 0 ) return;

        // delete record
        //--------------
        
        try{
            String postData = "op=delete";
            
            long l = 0;
            for( Iterator<String> i = idLst.iterator(); i.hasNext(); ){
                String id = i.next();
                postData += "&id=" + URLEncoder.encode( id, "UTF-8" );
                l++;
            }

            log.info("POST=" + postData);

            if( rmgrURL == null ){
                rmgrURL = (String) 
                    ((Map) getPsqContext().getJsonConfig().get("store"))
                    .get("record-mgr");            
                
                if( host != null ){
                    rmgrURL = hostReset( rmgrURL, host );
                }
            }
            log.info( "mgr URL=" + rmgrURL);

            URL url = new URL( rmgrURL );
            URLConnection conn = url.openConnection();
            conn.setDoOutput( true );
            OutputStreamWriter wr =
                new OutputStreamWriter( conn.getOutputStream() );
            wr.write( postData );
            wr.flush();
            
            // Get the response

            InputStreamReader isr = 
                new InputStreamReader( conn.getInputStream() );
            BufferedReader rd = new BufferedReader( isr );
            String line = null;

            log.info( "  Response:" );
            while ((line = rd.readLine()) != null) {           
                log.info( line );
            }
            wr.close();
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }
     
    //--------------------------------------------------------------------------

    private void add( String pid, String vType, String view ){
        
        Log log = LogFactory.getLog( this.getClass() );
        
        log.info( "PID=" + pid ); 
        log.info( "  VTP=" + vType ); 
        log.info( "  VIEW=" + view.substring(0,24) + "..." ); 

        // add record
        //-----------
        
        try{
            String postData = URLEncoder.encode("op", "UTF-8") + "="
                + URLEncoder.encode( "add", "UTF-8");
            postData += "&" + URLEncoder.encode("pid", "UTF-8") + "="
                + URLEncoder.encode( pid, "UTF-8");
            postData += "&" + URLEncoder.encode("vt", "UTF-8") + "="
                + URLEncoder.encode( vType, "UTF-8");
            
            postData += "&" + URLEncoder.encode("vv", "UTF-8") + "="
                + URLEncoder.encode( view, "UTF-8");

            if( rmgrURL == null ){
                rmgrURL = (String) 
                    ((Map) getPsqContext().getJsonConfig().get("store"))
                    .get("record-mgr");            

                if( host != null ){
                    rmgrURL = hostReset( rmgrURL, host );
                }
            }
            log.info( "mgr URL=" + rmgrURL);

            URL url = new URL( rmgrURL );
            URLConnection conn = url.openConnection();
            conn.setDoOutput( true );
            OutputStreamWriter wr =
                new OutputStreamWriter( conn.getOutputStream() );
            wr.write( postData );
            wr.flush();
            
            // Get the response

            InputStreamReader isr = 
                new InputStreamReader( conn.getInputStream() );
            BufferedReader rd = new BufferedReader( isr );
            String line = null;

            log.info( "  Response:" );
            while ((line = rd.readLine()) != null) {           
                log.info( line );
            }
            wr.close();
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //--------------------------------------------------------------------------

    public void clear(){

        Log log = LogFactory.getLog( this.getClass() );
        
        try{
            String postData = URLEncoder.encode("op", "UTF-8") + "="
                + URLEncoder.encode( "clear", "UTF-8");
            
            if( rmgrURL == null ){
                rmgrURL = (String) 
                    ((Map) getPsqContext().getJsonConfig().get("store"))
                    .get("record-mgr");            

                if( host != null ){
                    rmgrURL = hostReset( rmgrURL, host );
                }
            }
            
            URL url = new URL( rmgrURL );
            URLConnection conn = url.openConnection();
            conn.setDoOutput( true );
            OutputStreamWriter wr =
                new OutputStreamWriter( conn.getOutputStream() );
            wr.write(postData);
            wr.flush();
            
            // Get the response
            BufferedReader rd =
                new BufferedReader( new InputStreamReader( conn
                                                           .getInputStream()));
            String line;
            
            log.info( "  Response:" );
            while ((line = rd.readLine()) != null) {           
                log.info( line );
            }
            wr.close();
            rd.close();
        } catch (Exception e) {
        }
    }

    //--------------------------------------------------------------------------

    private String hostReset( String url, String newHost ){

        if( host == null ) return url;

        // http://aaa:8888/d/d/d

        try{
            Pattern p = Pattern.compile( "([^/]*//)([^/:]+)(:[0-9]+)?(.*)" );
            Matcher m = p.matcher( url );

            if( m.matches() ){

                String prefix = m.group( 1 );
                String host = m.group( 2 );
                String port = m.group( 3 );
                String postfix = m.group( 4 );

                String newUrl = prefix + newHost + port + postfix;
                return newUrl;

            } else {
                return url;
            }
        } catch(Exception ex ){
            return url;
        }
    }
}
