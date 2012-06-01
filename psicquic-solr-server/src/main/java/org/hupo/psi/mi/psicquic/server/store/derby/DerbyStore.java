package org.hupo.psi.mi.psicquic.server.store.derby;

/* =============================================================================
 # $Id:: PsqPortImpl.java 259 2012-05-06 16:29:56Z lukasz                      $
 # Version: $Rev:: 259                                                         $
 #==============================================================================
 #
 # DerbyStore: apache derby-based data store builder
 #
 #=========================================================================== */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.*;
import java.util.*;

import org.hupo.psi.mi.psicquic.util.JsonContext;

import org.hupo.psi.mi.psicquic.server.*;
import org.hupo.psi.mi.psicquic.server.store.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;

//------------------------------------------------------------------------------

public class DerbyStore{

    JsonContext context = null;
    String derbyUrl =  null;
    
    private Map<String,List> rscon = null;
    
    Log log;
    
    public DerbyStore(){
        log = LogFactory.getLog( this.getClass() );
    };
    
    public DerbyStore( JsonContext context  ){

        this.context = context;
        log = LogFactory.getLog( this.getClass() );
        initialize( true );
    }
    
    public void setContext( JsonContext context ){
        this.context = context;
    }
    
    //--------------------------------------------------------------------------

    public void initialize(){
        initialize( false );
    }

    public void initialize( boolean force ){

        if( force || derbyUrl == null ){
            
            log.info( " initilizing DerbyStore" );
            
            if( context != null && context.getJsonConfig() != null ){
                
                // SOLR URLs
                //----------
                
                Map jDerbyCon = (Map)
                    ((Map) context.getJsonConfig().get("store")).get("derby");
                
                log.info( "    derbyUrl=" + jDerbyCon.get("record-mgr") );
                
                derbyUrl = (String) jDerbyCon.get("record-mgr");
                
                Map jFmtCon = (Map) ((Map) context.getJsonConfig().get("store"))
                    .get("transform");
                
                rscon = new HashMap<String,List>();
                
                for( Iterator ik = jFmtCon.keySet().iterator(); ik.hasNext(); ){
                    
                    String rtp = (String) ik.next();
                    List rtrLst = (List) jFmtCon.get( rtp );
                    
                    rscon.put( rtp, rtrLst );
                }
            }
        }
    }
    
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    
    public void addFile( String format, String fileName, 
                         InputStream is ){
        
        List rtrList = rscon.get( format );
        
        if( rtrList != null ){
            for( Iterator it = rtrList.iterator(); it.hasNext(); ){
                
                Map itr = (Map) it.next();

                if( ((String) itr.get("type")).equalsIgnoreCase("XSLT") &&
                    (Boolean) itr.get("active") ){
                    
                    if( itr.get( "transformer" ) == null ){

                        log.info( " Initializing transformer: format=" + format
                                  + " type=XSLT config=" + itr.get("config") );
                        
                        PsqTransformer rt =
                            new XsltTransformer( (String) itr.get("config") );
                        itr.put( "transformer", rt );
                    }

                    PsqTransformer rt 
                        = (PsqTransformer) itr.get( "transformer" );
                    rt.start( fileName, is );
		    
		    while( rt.hasNext() ){
					
			Map cdoc= rt.next();
			String recId = (String) cdoc.get( "recId" );
			NodeList fl = (NodeList) cdoc.get( "dom" );
		       
			String vStr = null;
			String vType = (String) itr.get("view");

			for( int j = 0; j< fl.getLength() ;j++ ){
			    if( fl.item(j).getNodeName().equals( "field") ){
				Element fe = (Element) fl.item(j);
				String name = fe.getAttribute("name");
				String value = fe.getFirstChild().getNodeValue();
				
				if( name.equals( "recId" ) ){
				    recId = value;
				}
				if( name.equals( "view" ) ){
				    vStr= value;
				}
			    }
			}
			
			try{
			    this.add( recId, vType, vStr );
			}catch( Exception ex ){
			    ex.printStackTrace();
			}
		    }
		}
            }            
        }
    }

    private void add( String pid, String vType, String view ){
        
        log.info( "PID=" + pid ); 
        log.info( "  VTP=" + vType ); 
        log.info( "  VIEW=" + view.substring( 0,16 ) ); 

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
            
            URL url = new URL( derbyUrl );
            URLConnection conn = url.openConnection();
            conn.setDoOutput( true );
            OutputStreamWriter wr =
                new OutputStreamWriter( conn.getOutputStream() );
            wr.write(postData);
            wr.flush();
            
            // Get the response
            BufferedReader rd =
                new BufferedReader( new InputStreamReader(conn.getInputStream()));
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
}