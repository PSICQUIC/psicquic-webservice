package org.hupo.psi.mi.psicquic.client;

/* =============================================================================
 # $Id::                                                                       $
 # Version: $Rev::                                                             $
 #==============================================================================
 #
 # xpsq client
 #
 #=========================================================================== */

import java.util.*;
import java.net.*;
import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.*;

import javax.xml.bind.*;
import javax.xml.ws.BindingProvider;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import javax.xml.namespace.QName;
import java.net.URL;

import org.hupo.psi.mi.psq.*;
import org.hupo.psi.mi.psq.*;
import org.hupo.psi.mi.mif.*;

public class XpsqClient{

    String endpoint = "http://127.0.0.1:8080/xpsq/service/soap/current";

    PsqService service = null;
    PsqPort port = null;

    private XpsqClient(){}
    
    public XpsqClient( String host ){
        this.endpoint = "http://" + host + "/xpsq/service/soap/current";
        
        try {
            URL url = new URL( endpoint + "?wsdl" );
            QName qn = new QName( "http://psi.hupo.org/mi/psicquic",
                                  "PsicquicService");
            
            service = new PsqService( url, qn );   
            port = service.getIndexBasedPsicquicServicePort();

            ( (BindingProvider) port ).getRequestContext()
                .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                      endpoint );
            
        } catch ( Exception ex ) {
            ex.printStackTrace();
            System.out.println( "Xpsq server: cannot connect" );
        }
    }

    public String getByQuery( String query, String retType, 
                              int firstRecord, int blockSize ){
        
        if( port != null ){

            RequestInfo infoRequest = new RequestInfo();
            infoRequest.setResultType( retType );    
            infoRequest.setFirstResult( firstRecord );
            infoRequest.setBlockSize( blockSize );
            
            try{
                QueryResponse qr = port.getByQuery( query, infoRequest );
                if( qr!= null ){
                    if( retType != null && 
                        retType.startsWith("psi-mi/tab") ){
                        String mitab = qr.getResultSet().getMitab();
                        return mitab;
                    }
                    if( retType != null &&
                        retType.startsWith("psi-mi/xml") ){
                        EntrySet es = qr.getResultSet().getEntrySet();

                        try{
                            JAXBContext jc = 
                                JAXBContext.newInstance("org.hupo.psi.mi.mif");
                            Marshaller m = jc.createMarshaller();
                            
                            m.setProperty( Marshaller.JAXB_ENCODING, "UTF-8" );
                            
                            m.marshal( new JAXBElement(new QName("uri","entrySet"), 
                                                       EntrySet.class, es ),
                                       System.out );
                            
                        } catch( JAXBException jbx ){
                            jbx.printStackTrace();
                        } catch(Exception ex){
                            ex.printStackTrace();
                        }
                    }

                }
            } catch(NotSupportedMethodException nsmx){
                nsmx.printStackTrace();
            } catch( NotSupportedTypeException nstx ){
                nstx.printStackTrace();
            } catch( PsicquicServiceException psx ){
                psx.printStackTrace();
            }
        }
        return "";
    }


}
