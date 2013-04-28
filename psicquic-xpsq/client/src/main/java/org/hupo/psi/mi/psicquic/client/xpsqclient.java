package org.hupo.psi.mi.psicquic.client;

/* =============================================================================
 # $Id::                                                                       $
 # Version: $Rev::                                                             $
 #==============================================================================
 #
 # xpsq clommand line-based java client
 #
 #=========================================================================== */

import java.util.*;
import java.net.*;
import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.cli.*;

public class xpsqclient{

    private static final  String DEFAULT_ADDRESS= "127.0.0.1:8080";
    private static final  String DEFAULT_OPERATION="getByQuery";
    private static final  String DEFAULT_QUERY="*:*";
    private static final  String DEFAULT_RTYPE="psi-mi/tab27";

    private static final  int DEFAULT_FIRST = 0;
    private static final  int DEFAULT_BLOCK = 10;
    
    public static void main( String [ ] args ){
        
        Options options = new Options();        
        
        Option hlpOption = OptionBuilder.withLongOpt( "help" )
            .withDescription( "help" )
            .create( "help" );
        
        options.addOption( hlpOption );

        Option hostOption = OptionBuilder.withLongOpt( "host" )
            .withArgName( "host" ).hasArg()
            .withDescription( "host[:80]" )
            .create( "h" );

        options.addOption( hostOption );
        
        Option queryOption = OptionBuilder.withLongOpt( "query" )
            .withDescription( "MIQL query" )
            .withArgName( "miql" ).hasArg()
            .create( "q" );

        options.addOption( queryOption );
        
        Option opOption = OptionBuilder.withLongOpt( "operation" )
            .withArgName( "op" ).hasArg()
            .withDescription( "operation" )
            .create( "op" );

        options.addOption( opOption );

        Option rtypeOption = OptionBuilder.withLongOpt( "return-type" )
            .withArgName( "rtype" ).hasArg()
            .withDescription( "return type" )
            .create( "rt" );

        options.addOption( rtypeOption );
        
        Option frecOption = OptionBuilder.withLongOpt( "first-record" )
            .withArgName( "n" ).hasArg()
            .withDescription( "first record" )
            .create( "fr" );

        options.addOption( frecOption );
        
        Option blckOption = OptionBuilder.withLongOpt( "block-size" )
            .withArgName( "n" ).hasArg()
            .withDescription( "block size" )
            .create( "b" );

        options.addOption( blckOption );
        
        String address = DEFAULT_ADDRESS;     
        String query = DEFAULT_QUERY;     
        String op = DEFAULT_OPERATION;
        String rt = DEFAULT_RTYPE;

        int frec = DEFAULT_FIRST;
        int blck = DEFAULT_BLOCK;
        
        try{
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse( options, args);

            if( cmd.hasOption("help") ){
                HelpFormatter formatter = new HelpFormatter();
                formatter.setWidth( 127 );
                formatter.printHelp( "XpsqClient", options );
                System.exit(0);
            }
            
            if( cmd.hasOption( "h" ) ){
                address = cmd.getOptionValue( "h" );
                System.out.println( "Host: " + address );
            } else {
                System.out.println( "Host(default): " + address );
            }
            
            if( cmd.hasOption( "q" ) ){
                query = cmd.getOptionValue( "q" );
                System.out.println( "Query: " + query );
            } else {
                System.out.println( "Query(default): " + query );
            }
            
            if( cmd.hasOption( "op" ) ){
                op = cmd.getOptionValue( "op" );
                System.out.println( "Operation: " + op );
            } else {
                System.out.println( "Operation(default): " + op );
            }

            if( cmd.hasOption( "rt" ) ){
                rt = cmd.getOptionValue( "rt" );
                System.out.println( "Return type: " + rt );
            } else {
                System.out.println( "Return type(default): " + rt );
            }
            
            if( cmd.hasOption( "fr" ) ){
                String frs = cmd.getOptionValue( "fr" );
                

                try{
                    frec = Integer.parseInt( frs );
                }catch( NumberFormatException nfx ){
                    nfx.printStackTrace();
                } 
                System.out.println( "First Record: " + frec );
            } else {
                System.out.println( "First Record (default): " + frec );
            }

            if( cmd.hasOption( "b" ) ){
                String bls = cmd.getOptionValue( "b" );
                
                try{
                    blck = Integer.parseInt( bls );
                }catch( NumberFormatException nfx ){
                    nfx.printStackTrace();
                } 
                System.out.println( "Block Size: " + blck );
            } else {
                System.out.println( "Block Size (default): " + blck );
            }
            
        } catch( Exception exp ) {
            System.out.println( "BuildIndex: Options parsing failed. " +
                                "Reason: " + exp.getMessage() );
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(127);
            formatter.printHelp( "BuildIndex", options );
            System.exit(1);
        }
        
        XpsqClient xpsqc = null;
        
        xpsqc = new XpsqClient( address  );
        
        if( xpsqc != null && op != null ){  
            
            if( op.equalsIgnoreCase("getByQuery") ){
                String result = xpsqc.getByQuery( query, rt, frec, blck );
                System.out.println( result );
            }
        } else {
            
        }       
    }
}
