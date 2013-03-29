package org.hupo.psi.mi.psicquic.server;

/* =============================================================================
 # $Id::                                                                       $
 # Version: $Rev::                                                             $
 #==============================================================================
 #
 # Mitab2MifTransformer: transform the incoming xml file into solr documents
 #
 #=========================================================================== */

import java.util.*;
import java.net.*;
import java.io.*;

import psidev.psi.mi.tab.*;
import psidev.psi.mi.tab.converter.tab2xml.*;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.xml.model.*;

import psidev.psi.mi.xml.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
       
import javax.xml.transform.URIResolver;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.util.JAXBResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Mitab2MifTransformer implements PsqTransformer{

    String fileName = null;
    String curRowStr = null;
    int curLineNumber = 1;
    
    private String out ="VIEW";

    String commentPrefix = "#";
 
    PsimiTabReader tabReader = null;
    Iterator<BinaryInteraction> biIterator = null;

    Tab2Xml tabConverter = null; 

    private Transformer viewtr = null;

    private PsimiXmlWriter pxw = null;

    public Mitab2MifTransformer(Map config ){

        tabReader = new PsimiTabReader();
        tabConverter = new Tab2Xml();        
        
        pxw = new PsimiXmlWriter( PsimiXmlVersion.VERSION_254, 
                                  PsimiXmlForm.FORM_EXPANDED );       
        
        if( config == null ){

        } else {
            
            // input format
            //-------------
            
            if( config.get( "in" ) != null ){ 
                    
                if( ((String) config.get( "in" ))
                    .equalsIgnoreCase( "MITAB25" ) ){                   
                }               
            }

            // output format
            //--------------

            if( config.get( "out" ) != null ){
                if( ((String) config.get( "out" ))
                    .equalsIgnoreCase( "VIEW" ) ){    
                }
            }
        }		    
    }

    public void start( String fileName ){

        Log log = LogFactory.getLog( this.getClass() );
        log.info( " starting Mitab2MifTransformer(fileName): filename(o)=" 
                  + this.fileName );
        
        this.fileName = fileName;
        this.curRowStr = null;
        this.curLineNumber = 1;
        
        log.info( " starting Mitab2MifTransformer(fileName): filename(n)=" 
                  + this.fileName );

        try {

            File file = new File( fileName );            
            this.biIterator = tabReader.iterate( file );
            
        }catch( IOException ex ){
            //TODO - do whatever you want to do with this exception
        }
    }
    
    public void start( String fileName, InputStream is ){

        Log log = LogFactory.getLog( this.getClass() );
        log.info( " starting Mitab2MifTransformer(stream): filename(o)=" 
                  + this.fileName );
        
        this.fileName = fileName;
        this.curRowStr = null;
        this.curLineNumber = 1;

        log.info( " starting Mitab2MifTransformer(stream): filename(n)=" 
                  + this.fileName );

        try {
            this.biIterator = tabReader.iterate( is ); 
        }catch( IOException ex ){
            //TODO - do whatever you want to do with this exception
        }
    }

    public String getFileName(){
        return this.fileName;
    }
    
    public boolean hasNext(){
        return biIterator.hasNext();
    }

    public Map next(){

        Log log = LogFactory.getLog( this.getClass() );
        
        if( ! biIterator.hasNext() ) return null;

        BinaryInteraction bi = biIterator.next();
        List<BinaryInteraction> bis = new  ArrayList<BinaryInteraction>();
        bis.add(bi);
        EntrySet eset = null;
        try{
            eset = tabConverter.convert( bis );
        } catch( Exception ex){
            ex.printStackTrace();
        }

        Map resMap = new HashMap();
        
        if( out.equals( "VIEW" ) && eset != null ){
            
            if( viewtr == null ){
                try{
                    TransformerFactory tf
                        = TransformerFactory.newInstance();
                    viewtr = tf.newTransformer();
                    viewtr.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes");
                    
                } catch ( TransformerException tex ){
                    tex.printStackTrace();
                }
            }

            log.info( " next: viewtr=" + viewtr );
            
            if( viewtr != null ){
                try{
                    StringWriter buffer = new StringWriter();                   
                    pxw.write( eset, buffer);
                    String str = buffer.toString();
                    
                    int fIndex = str.indexOf( "<entry>" );
                    int lIndex = str.lastIndexOf( "</entry>" );

                    str = str.substring( fIndex, lIndex + 8 );
                    log.debug( " next: str=" + str.substring(0,64) );
                    resMap.put( "view", str );
                    
                } catch ( Exception ex ){
                    ex.printStackTrace();
                }
            }
            
            curLineNumber++;            
            String recId = fileName+ ":" + curLineNumber;
            resMap.put( "recId", recId  );
        }
        return resMap;
    }    
}
