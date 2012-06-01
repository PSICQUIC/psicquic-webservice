package org.hupo.psi.mi.psicquic.server.builder;

/* =============================================================================
 # $Id:: buildindex.java 266 2012-05-21 01:02:31Z lukasz                       $
 # Version: $Rev:: 266                                                         $
 #==============================================================================
 #
 # BuildSolrIndex: Build Solr-based index
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
import javax.xml.bind.util.JAXBResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.cli.*;

//import org.apache.solr.common.*;
//import org.apache.solr.core.*;

//import org.apache.solr.client.solrj.*;
//import org.apache.solr.client.solrj.impl.*;
//import org.apache.solr.client.solrj.response.*;

import java.util.zip.CRC32;
import java.lang.Thread.State;

import org.hupo.psi.mi.psicquic.util.JsonContext;

public class buildindex{
    
    public static final String 
        CONTEXT = "/etc/psq-context-default.json";
    
    public static final String 
        RFRMT = "mif254";   

    public static String rfrmt;

    public static String dir =  "/home/lukasz/imex_test";
    public static boolean zip = false;
    
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------

    public static void main( String [ ] args ){
        
        Options options = new Options();
 
        Option hlpOption = OptionBuilder.withLongOpt( "help" )
            .withDescription( "help " )
            .create( "help" );

        options.addOption( hlpOption );
        
        Option urlOption = OptionBuilder.withLongOpt( "url" )
            .withArgName( "url" ).hasArg()
            .withDescription( "server url" )
            .create( "url" );


        Option dirOption = OptionBuilder.withLongOpt( "dir" )
            .withArgName( "dir" ).hasArg()
            .withDescription( "file location" )
            .create( "d" );

        options.addOption( dirOption );

        Option zipOption = OptionBuilder.withLongOpt( "zip" )
            .withDescription( "zipped files" )
            .create( "z" );

        options.addOption( zipOption );
        
        Option ctxOption = OptionBuilder.withLongOpt( "context" )
            .withArgName( "file.json" ).hasArg()
            .withDescription( "configuration file" )
            .create( "ctx" );

        options.addOption( ctxOption );
        
        String context = null;
 
        Option iftOption = OptionBuilder.withLongOpt( "iformat" )
            .withArgName( "format" ).hasArg()
            .withDescription( "input record format" )
            .create( "ift" );
        
        options.addOption( iftOption );
        
        String ifrmt = BuildSolrDerbyIndex.RFRMT;
        
        try{
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse( options, args);

            if( cmd.hasOption("help") ){

                HelpFormatter formatter = new HelpFormatter();
                formatter.setWidth( 127 );
                formatter.printHelp( "BuildSolrDerbyIndex", options );
                System.exit(0);
            }

            if( cmd.hasOption("ctx") ){
                context = cmd.getOptionValue("ctx");
            } 
        
            if( cmd.hasOption("ift") ){
                ifrmt = cmd.getOptionValue( "ift" );
            }

            if( cmd.hasOption("d") ){
                dir = cmd.getOptionValue( "d" );
            }
            
            if( cmd.hasOption("z") ){
                zip = true;
            }

        } catch( Exception exp ) {
            System.out.println( "BuildSolrDerbyIndex: Options parsing failed. " +
                                "Reason: " + exp.getMessage() );
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(127);
            formatter.printHelp( "BuildSolrDerbyIndex", options );
            System.exit(1);
        }

       
        BuildSolrDerbyIndex psi = null;
        
        if( context != null ){
            System.out.println( "Context: " + context );
            psi = new BuildSolrDerbyIndex( context, ifrmt,
                                           dir, zip );
        }else{
            
            System.out.println( "Context(default): " 
                                + BuildSolrDerbyIndex.CONTEXT );
            try{
                InputStream ctxStream = buildindex.class
                    .getResourceAsStream( BuildSolrDerbyIndex.CONTEXT );
                
                psi = new BuildSolrDerbyIndex( ctxStream, ifrmt, dir, zip );
            } catch( Exception ex){
                ex.printStackTrace();
            }
            
        }
        psi.start();

    }
}
