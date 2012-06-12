package org.hupo.psi.mi.psicquic.server.builder;

/* =============================================================================
 # $Id:: buildindex.java 266 2012-05-21 01:02:31Z lukasz                       $
 # Version: $Rev:: 266                                                         $
 #==============================================================================
 #
 # BuildIndex: Build index
 #
 #=========================================================================== */

import java.util.*;
import java.net.*;
import java.io.*;

import java.lang.Thread.State;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.cli.*;

//import org.w3c.dom.*;
//import javax.xml.parsers.*;

import org.hupo.psi.mi.psicquic.util.JsonContext;

public class buildindex{
    
    public static final String 
        DEFAULT_CONTEXT = "/etc/psq-context-default.json";
    
    public static final String 
        DEFAULT_RECORD_FORMAT = "mif254";   

    public static String rfrmt = DEFAULT_RECORD_FORMAT;

    public static String idir = null;
    public static String ifile = null;
    public static boolean zip = false;
    public static boolean desc = false;
    
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


        Option fileOption = OptionBuilder.withLongOpt( "file" )
            .withArgName( "file" ).hasArg()
            .withDescription( "input file" )
            .create( "f" );

        options.addOption( fileOption );

        Option dirOption = OptionBuilder.withLongOpt( "dir" )
            .withArgName( "dir" ).hasArg()
            .withDescription( "input file directory" )
            .create( "d" );

        options.addOption( dirOption );

        Option recOption = OptionBuilder.withLongOpt( "r" )
            .withDescription( "recursively process directory" )
            .create( "r" );
        
        options.addOption( recOption );
        
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
 
        Option fmtOption = OptionBuilder.withLongOpt( "format" )
            .withArgName( "format" ).hasArg()
            .withDescription( "input record format" )
            .create( "fmt" );
        
        options.addOption( fmtOption );
        
        String ifrmt = DEFAULT_RECORD_FORMAT;
        
        try{
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse( options, args);

            if( cmd.hasOption("help") ){

                HelpFormatter formatter = new HelpFormatter();
                formatter.setWidth( 127 );
                formatter.printHelp( "BuildSolrDerbyIndex", options );
                System.exit(0);
            }

            if( cmd.hasOption( "ctx" ) ){
                context = cmd.getOptionValue( "ctx" );
            } 
        
            if( cmd.hasOption( "fmt" ) ){
                ifrmt = cmd.getOptionValue( "fmt" );
            }

            if( cmd.hasOption( "d" ) ){
                idir = cmd.getOptionValue( "d" );
            }
            
            if( cmd.hasOption( "f" ) ){
                ifile = cmd.getOptionValue( "f" );
            }
            
            if( cmd.hasOption( "z" ) ){
                zip = true;
            }
            
            if( cmd.hasOption("r") ){
                desc = true;
            }

        } catch( Exception exp ) {
            System.out.println( "BuildIndex: Options parsing failed. " +
                                "Reason: " + exp.getMessage() );
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(127);
            formatter.printHelp( "BuildIndex", options );
            System.exit(1);
        }

        IndexBuilder ibuilder = null;
        
        if( context != null ){
            System.out.println( "Context: " + context );
            ibuilder = new IndexBuilder( context, ifrmt, zip );
        }else{
            
            System.out.println( "Context(default): " + DEFAULT_CONTEXT );
            try{
                InputStream ctxStream = buildindex.class
                    .getResourceAsStream( DEFAULT_CONTEXT );
                
                ibuilder = new IndexBuilder( ctxStream, ifrmt, zip );
            } catch( Exception ex){
                ex.printStackTrace();
            }
            
        }

        if( ibuilder != null && ifile != null ){  
            ibuilder.processFile( ifile );
        } else { // file option has a precedence over directory
            if( ibuilder != null && idir != null ){
                ibuilder.processDirectory( idir, desc );
            }
        }
    }
}
