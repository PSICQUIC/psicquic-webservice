package org.hupo.psi.mi.psicquic.ols.client;

import org.hupo.psi.mi.psicquic.ols.soap.Query;
import org.hupo.psi.mi.psicquic.ols.soap.QueryServiceLocator;

import javax.xml.rpc.ServiceException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class OLSClient {

    /**
     * calls OLS webserver and gets child terms for a termId
     * @return Map of child terms - key is termId, value is termName.
     * Map should not be null.
     */
    public Map<String, String> getTermChildren(String termId, String ontology) {

        Map retval = new HashMap<String, String>();
        QueryServiceLocator locator = new QueryServiceLocator();
        try {
            Query service = locator.getOntologyQuery();
            HashMap terms = service.getTermChildren(termId, ontology, 1, null);
            if (terms != null){
                retval.putAll(terms);
            }
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return retval;

    }

    /**
     * calls OLS webserver and gets suggestions of terms for a given query
     * @return Map of suggested terms - key is termId, value is termName.
     * Map should not be null.
     */
    public Map<String, String> getTermsByName(String text, String ontology) {

        Map<String, String> retval = new HashMap<String, String>();
        QueryServiceLocator locator = new QueryServiceLocator();
        try {
            Query service = locator.getOntologyQuery();

            // We change the getTermByName to getTermsByExactName to have more accuracy when
            // we retrieve the term ids by name. We need to reverse the map because getTermsByExactName
            // doesn't do it for us (something that didn't need it before because true boolean in getTermsByName did it)
            // HashMap terms = service.getTermsByName(text, ontology, true);
            HashMap<String,String> terms = service.getTermsByExactName(text, ontology);

            if (terms != null){
                // retval.putAll(terms);
                for (Map.Entry<String, String> entry : terms.entrySet()){
                    retval.put(entry.getValue(), entry.getKey()) ;
                }
            }
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return retval;

    }

    public String getTermNameByTermId(String termId, String ontology){
    	  String retval = "";
          QueryServiceLocator locator = new QueryServiceLocator();
          try {
              Query service = locator.getOntologyQuery();
              retval = service.getTermById(termId, ontology);
              if (retval == null){
                 retval = "";
              }

          } catch (ServiceException e) {
              e.printStackTrace();
          } catch (RemoteException e) {
              e.printStackTrace();
          }

          return retval;

    }
}

