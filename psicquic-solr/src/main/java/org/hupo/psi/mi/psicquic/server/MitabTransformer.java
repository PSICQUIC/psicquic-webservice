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

import org.apache.solr.common.SolrInputDocument;

public class MitabTransformer implements PsqTransformer{
    
    public MitabTransformer( String xxx ){
		    
    }

    public void start( String fileName ){}
    public void start( String fileName, InputStream is ){}
	

    boolean hasNext(){
	if( false ) return false;
	
	return true;
    }

    SolrInputDocument next(){
	return null;
    }
   
 
}