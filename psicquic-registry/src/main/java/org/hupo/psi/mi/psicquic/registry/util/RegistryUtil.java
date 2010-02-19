/**
 * 
 */
package org.hupo.psi.mi.psicquic.registry.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.hupo.psi.mi.psicquic.registry.Registry;





/**
 * @author Erik Pfeiffenberger
 *
 */
public class RegistryUtil {
	   public static Registry readRegistry(String registryXml) {
	        Registry objRegistry;
	        try {
//	        	//TODO: change to URl after debugging
//	            FileInputStream fileStream = new FileInputStream(registryXml);
//	            objRegistry = (Registry) readRegistryXml(fileStream);
	        	System.out.println("REGISTRY URL: "+registryXml);
	        	URL datasetsUrl = new URL(registryXml);
	        	
	            objRegistry = (Registry) readRegistryXml(datasetsUrl.openStream());
	           

	        } catch (Throwable e) {
	            
	            objRegistry = new Registry();
	            
	            e.printStackTrace();
	        }

	        return objRegistry;
	    }
	    
	    private static Object readRegistryXml(InputStream is) throws JAXBException {
	        JAXBContext jc = JAXBContext.newInstance(Registry.class.getPackage().getName());
	        Unmarshaller unmarshaller = jc.createUnmarshaller();
	        return unmarshaller.unmarshal(is);
	    }
}
