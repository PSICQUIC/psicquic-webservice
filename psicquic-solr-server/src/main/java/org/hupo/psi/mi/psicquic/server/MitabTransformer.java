package org.hupo.psi.mi.psicquic.server;

/* =============================================================================
 # $Id:: MitabTransformer.java 937 2012-05-29 15:39:09Z lukasz99               $
 # Version: $Rev:: 937                                                         $
 #==============================================================================
 #
 # MitabTransformer: transform the incoming xml file into solr documents
 #
 #=========================================================================== */

import java.util.*;
import java.net.*;
import java.io.*;

public class MitabTransformer implements PsqTransformer{
    
    public MitabTransformer( String xxx ){
		    
    }

    public void start( String fileName ){}
    public void start( String fileName, InputStream is ){}
	

    public boolean hasNext(){
	if( false ) return false;
	return true;
    }

    public Map next(){
	return null;
    }
   
 
}