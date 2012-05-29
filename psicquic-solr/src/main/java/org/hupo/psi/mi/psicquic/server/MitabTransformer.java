package org.hupo.psi.mi.psq.server;

/* =============================================================================
 # $Id::                                                                       $
 # Version: $Rev::                                                             $
 #==============================================================================
 #
 # MitabTransformer: transform the incoming xml file into solr documents
 #
 #=========================================================================== */

import java.util.*;
import java.net.*;
import java.io.*;


public class MitabTransformer implements PsqTransformer{
    
    public MitabTransformer( .... ){
		    
    }

    public void start( String fileName ){};

    boolean hasNext(){
	if( ... ) return false;
	
	return true;
    }

    SolrInputDocument next(){
	return ...;
    }
   
    public void start( String fileName, InputStream is ){
	

}