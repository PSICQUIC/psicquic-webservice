package org.hupo.psi.mi.psicquic.server;

/* =============================================================================
 # $Id::                                                                       $
 # Version: $Rev::                                                             $
 #==============================================================================
 #
 # XsltTransformer: transform the incoming xml file into solr documents
 #
 #=========================================================================== */

import java.util.*;
import java.net.*;
import java.io.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.URIResolver;

import javax.xml.bind.util.JAXBResult;

import org.apache.solr.common.SolrInputDocument;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XsltTransformer implements PsqTransformer{
    
    private Transformer psqtr = null;
    private Transformer viewtr = null;

    private String fileName = "";
    private String out = "VIEW";

    private NodeList domList = null;
    private int domPos = -1;
    private int nextPos = -1;

    private Log log;

    public XsltTransformer( Map config ){
        this( config, null );
    }

    public XsltTransformer( Map config, Map<String,String> param ){
        
        log = LogFactory.getLog( this.getClass() );

        try {
	    DocumentBuilderFactory
		dbf = DocumentBuilderFactory.newInstance();
	    dbf.setNamespaceAware( true );
	    
	    DocumentBuilder db = dbf.newDocumentBuilder();
            
            if( config != null && config.get("xslt") != null ){
                
                // output format
                //--------------

                if( config.get("out") != null &&
                    ((String) config.get("out")).equalsIgnoreCase( "VIEW" ) ){
                
                    out = "VIEW";
                }

                if( config.get("out") != null &&
                    ((String) config.get("out")).equalsIgnoreCase( "SOLR" ) ){
                
                    out = "SOLR";
                }

                if( config.get("out") != null &&
                    ((String) config.get("out")).equalsIgnoreCase( "DOM" ) ){
                
                    out = "DOM";
                }

                // transformation
                //---------------

                String xslt = (String) config.get("xslt");

                File xslFile = new File( xslt );
                InputStream xslIStr = null;
	    
                if( !xslFile.canRead() ){
                    xslIStr = this.getClass().getClassLoader()
                        .getResourceAsStream( xslt );
                } else {
                    xslIStr = new FileInputStream( xslt );
                }
	    
                Document xslDoc = db.parse( xslIStr );
	    
                DOMSource xslDomSource = new DOMSource( xslDoc );
                TransformerFactory
                    tFactory = TransformerFactory.newInstance();
                
                URIResolver defURIResolver = tFactory.getURIResolver(); 
                URIResolver cpURIResolver 
                    = new ClassPathURIResolver( defURIResolver );

                tFactory.setURIResolver( cpURIResolver );
                
                psqtr = tFactory.newTransformer( xslDomSource );

                // set xslt parameters
                //--------------------

                // Note: Parameters passed as constructor argument
                //       take precedence over defaults

                if( param != null ){

                    for( Iterator<Map.Entry<String,String>>
                             ip = param.entrySet().iterator();
                         ip.hasNext(); ){

                        Map.Entry<String,String> ee = ip.next();

                        log.info( "param(cmdln): name=" + ee.getKey()
                                  + " value=" + ee.getValue() );

                        psqtr.setParameter( ee.getKey(),
                                            ee.getValue() );                    
                    }
                }

                // remaining default parameters 

                if( config.get("param") != null ){
                    Map<String,String> paramap 
                        = (Map<String,String>) config.get("param");

                    for( Iterator<Map.Entry<String,String>> 
                             ipd = paramap.entrySet().iterator(); 
                         ipd.hasNext(); ){    
                        
                        Map.Entry<String,String> eed = ipd.next();

                        if( param == null 
                            ||  param.get( eed.getKey() ) == null ){ 
                                                            
                            log.info( "param(default): name=" + eed.getKey() 
                                      + " value=" + eed.getValue() ); 
                            
                            psqtr.setParameter( eed.getKey(), 
                                                eed.getValue() );
                        }
                    }
                } 
            }
	} catch( Exception ex ) {
	    ex.printStackTrace();
	}
    }

    public void start( String fileName ){
	this.fileName = fileName;	
    }

    public void start( String fileName, InputStream is ){
	
	Log log = LogFactory.getLog( this.getClass() );
	log.info( " XsltTransformer: start(file= " + fileName + ")");
        
	try {
	    this.fileName = fileName;
	    
            StreamSource ssNative = new StreamSource( is );
            DOMResult domResult = new DOMResult();
	    
            //transform into DOM
            //------------------
            
            //psqtr.clearParameters();
    
            psqtr.setParameter( "file", fileName );
            psqtr.transform( ssNative, domResult );
            
	    Node domNode = domResult.getNode();
	    log.debug( " XsltTransformer: node=" + domNode );
            
	    if( domNode != null ){
	        domList = domNode.getFirstChild().getChildNodes();
	    }    
	    log.debug( " XsltTransformer: domList=" + domList );

	} catch( Exception ex ) {
	    ex.printStackTrace();
	}
    }

    public boolean hasNext(){
	if( domList == null || domPos >= domList.getLength() ) return false;
	nextPos = domPos + 1;
	while( nextPos <domList.getLength() ){
	    if( domList.item( nextPos ).getNodeName().equals( "doc" ) ){
		return true;
	    }
	    nextPos++;
	}
	return false;
    }
    
    public Map next(){

	Log log = LogFactory.getLog( this.getClass() );

	if( domPos == nextPos && !hasNext() ) return null;
	if( nextPos < domList.getLength() ){
	    domPos = nextPos;
	
	    Map resMap = new HashMap();
	    
	    log.debug( " XsltTransformer: pos=" + domPos );
	    log.debug( "                  node=" + domList.item( domPos ) );
	    log.debug( "                  out=" + out );

	    if( domPos < domList.getLength() ){
		
                if( domList.item( domPos ).getNodeName().equals( "doc" ) ){
		    
                    String recId = fileName;

                    if( out.equals( "SOLR" ) ){

                        SolrInputDocument doc =  new SolrInputDocument();

                        NodeList field = domList.item( domPos ).getChildNodes();
                        for( int j = 0; j< field.getLength() ;j++ ){
                            if( field.item(j).getNodeName().equals("field") ){
                                Element fe = (Element) field.item(j);
                                String name = fe.getAttribute("name");
                                
                                String value = null;
                                
                                if( fe.getFirstChild() != null ){
                                    value = fe.getFirstChild().getNodeValue();
                                    doc.addField( name, value );
                                }
                                
                                if( name.equals( "recId" ) ){
                                    recId = value;
                                }
                            }
                        }
                        resMap.put( "solr", doc );
                    }

                    if( out.equals( "VIEW" ) ){

                        log.debug( " next: return view" );

                        NodeList field = domList.item( domPos ).getChildNodes();
                        
                        String textView = null;
                        Element elemView = null;
                        
                        for( int j = 0; j< field.getLength() ;j++ ){
                            if( field.item(j).getNodeName().equals("field") ){
                                Element fe = (Element) field.item(j);
                                
                                String name = fe.getAttribute("name");

                                if( name.equals( "recId" ) ){
                                    String value 
                                        = fe.getFirstChild().getNodeValue();
                                    recId = value;
                                }
                                
                                if( name.equals( "view" ) ){

                                    Node vnde =  fe.getFirstChild();
                                    
                            
                                    if( vnde.getNodeType() == Node.TEXT_NODE ){
                                        textView = vnde.getNodeValue();

                                        log.debug( " next: textView" 
                                                   + textView.substring(0,64));
                                    }
                                    if( vnde.getNodeType() == Node.ELEMENT_NODE
                                        && elemView == null ){
                                        elemView = (Element) vnde;

                                        log.debug( " next: elemView" 
                                                   + elemView.getNodeName() );
                                    }
                                }
                            }
                        }

                        if( elemView == null ){
                            resMap.put( "view", textView );
                        } else {

                            if( viewtr == null ){
                                try{
                                    TransformerFactory tf 
                                        = TransformerFactory.newInstance();
                                    viewtr = tf.newTransformer(); 
                                    viewtr.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, 
                                                              "yes");
                                    
                                } catch ( TransformerException tex ){
                                    tex.printStackTrace();
                                }
                            }
                            
                            log.debug( " next: viewtr=" + viewtr );

                            if( viewtr != null ){
                                StringWriter buffer = new StringWriter();
                                try{
                                    viewtr.transform( new DOMSource( elemView ),
                                                      new StreamResult( buffer ));
                                    
                                    String str = buffer.toString();
                                    log.debug( " next: str="+ str.substring(0,64));
                                    resMap.put( "view", str );

                                } catch ( TransformerException tex ){

                                    tex.printStackTrace();

                                }
                            }
                        }
                    }
		    resMap.put( "recId", recId );
		    resMap.put( "dom", 
				domList.item( domPos ).getChildNodes() );
		}
	    }
	    return resMap;
	}
	return null;
    }    
}
