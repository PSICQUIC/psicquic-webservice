package org.hupo.psi.mi.psicquic.stats;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.ServiceException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import uk.ac.ebi.intact.google.spreadsheet.SpreadsheetFacade;
import uk.ac.ebi.intact.google.spreadsheet.WorksheetFacade;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicClientException;
import org.hupo.psi.mi.psicquic.wsclient.UniversalPsicquicClient;
import psidev.psi.mi.search.SearchResult;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// How to install the Gdata libs locally ?
//
// 1. Download the gdata libs from Google Code
//
// 2. unpack and install the following jar files present in the gdata/java/libs directory:
//    gdata-spreadsheet-3.0.jar
//    gdata-core-1.0.jar
//
//    Command lines:
//
//        mvn install:install-file -Dfile=gdata-spreadsheet-3.0.jar -DgroupId=com.google.gdata.client.spreadsheet \
//                                 -DartifactId=gdata-spreadsheet -Dversion=3.0 -Dpackaging=jar
//
//        mvn install:install-file -Dfile=gdata-core-1.0.jar -DgroupId=com.google.gdata.data \
//                                 -DartifactId=gdata-core -Dversion=1.0 -Dpackaging=jar
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Utility that collect PSICQUIC statistics from existing services and publishes it into a Google Spreadsheet.
 * <p/>
 * The tool can be used to monitor the PSICQUIC services hosted on on the main Registry but an other Registry can also
 * be used by passing the System property 'psicquic.registry.url'. Whether it's a totally different registry or the
 * public one with some exclusion rules or tag filtering.
 * <p/>
 * One can use the default SMTP configuration file provided (resources/META-INF/smtp.properties) or use an external one
 * by setting it's location as a System's property: 'smtp.config.file'.
 * <p/>
 * Note: We used a lot of code sample from:
 * http://code.google.com/apis/spreadsheets/data/3.0/developers_guide_java.html
 * http://ga-api-java-samples.googlecode.com/svn/trunk/src/v1/SpreadsheetExporter.java
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 */
public class PsicquicStatsCollector {

    private static final Log log = LogFactory.getLog( PsicquicStatsCollector.class );

    public static final String FILE_SEPARATOR = System.getProperty( "file.separator" );

    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "dd/MM/yyyy" );
    private static SimpleDateFormat DATE_AS_DIR = new SimpleDateFormat( "yyyy/MM/dd" );

    private static final String PSICQUIC_REGISTRY_URL_KEY = "psicquic.registry.url";

    private static final String SMTP_CONFIG_FILE_KEY = "smtp.config.file";

    private static final int PSICQUIC_DEFAULT_TIMEOUT = 60000;
    private static final int PSICQUIC_BATCH_SIZE = 200;

    private static final String TOTAL_COLUMN = "Total";
    private static final String DATE_COLUMN = "Date";

    private MailSender mailSender;
    private String senderEmail;
    private String mailSubjectPrefix;

    private List<String> recipients;
    private Config config;

    public PsicquicStatsCollector() throws IOException {
        // Initialize Spring for emails
        String[] configFiles = new String[]{"/META-INF/beans.spring.xml"};
        BeanFactory factory = new ClassPathXmlApplicationContext( configFiles );

        // inject my spring beans
        this.mailSender = ( MailSender ) factory.getBean( "mailSender" );
        this.config = ( Config ) factory.getBean( "statsConfig" );

        // load email properties
        File smtpConfig = null;
        if( ! config.hasSmtpConfigFile() ) {
            log.info( "Using default SMTP config from classpath: '"+ config.DEFAULT_SMTP_CONFIG +"'" );
            smtpConfig = new File( PsicquicStatsCollector.class.getResource( config.DEFAULT_SMTP_CONFIG ).getFile() );
        } else {
            smtpConfig = new File( config.getSmtpConfigFile() );
        }
        log.info( "Using SMTP config from: '"+ smtpConfig.getAbsolutePath() +"'" );

        Properties properties = new Properties();
        FileInputStream in = new FileInputStream( smtpConfig );
        properties.load( in );
        in.close();

        mailSubjectPrefix = properties.getProperty( "email.subject.prefix" );
        log.info( "mailSubjectPrefix = " + mailSubjectPrefix );
        String recipientList = properties.getProperty( "email.recipients" );
        
        recipients = new ArrayList( Arrays.asList( recipientList.split( "," ) ) );
        log.info( "recipients = " + recipients );

        senderEmail = properties.getProperty( "email.sender" );
        log.info( "senderEmail = " + senderEmail );
        if( StringUtils.isEmpty(senderEmail ) ) {
            // disable email sending facilities
            log.warn( "No email sender specified, running with email facilities dsabled." );
            senderEmail = null;
            mailSender = null;
        }

        log.info( config );
    }

    ///////////////////////////////////////
    // Mail handling

    public void sendEmail( String title, String body ) {
        if ( mailSender == null ) {
            log.debug( "------------------------------------------------------------" );
            log.debug( "From:  " + senderEmail );
            log.debug( "To:    " + recipients );
            log.debug( "Title: " + mailSubjectPrefix + title );
            log.debug( body );
            log.debug( "------------------------------------------------------------" );
        } else {
            final SimpleMailMessage message = new SimpleMailMessage();
            message.setTo( recipients.toArray( new String[]{} ) );
            message.setFrom( senderEmail );
            message.setSubject( mailSubjectPrefix + " " + title );
            message.setText( body );
            mailSender.send( message );
        }
    }

    ///////////////////////////////////////
    // Google Spreadsheet related methods

    public List<String> updateSheet( final SpreadsheetService service,
                                     SpreadsheetEntry spreadsheetEntry,
                                     String worksheetName,
                                     Map<String, Long> db2count ) throws IOException, ServiceException {

        final List<String> updatedServices = Lists.newArrayList();

        final WorksheetFacade myWorksheet = WorksheetFacade.getWorksheetByName( service, spreadsheetEntry, worksheetName );
        if( myWorksheet == null ) {
            final String msg = "Could not find the worksheet '" + worksheetName + "' in spreadsheet '" + spreadsheetEntry.getTitle().getPlainText() + "'";
            sendEmail(msg, "");
            throw new IllegalArgumentException( msg );
        }

        Map<String, Integer> columnName2Index = myWorksheet.getColumnNameIndex();
        final int nextEmptyRow = myWorksheet.getNextEmptyRow();
        final List<CellEntry> updatedCells = Lists.newArrayList();

        // If a total column is present, then sum up all cells into it
        boolean hasTotalColumn = false;
        int totalColumnIndex = -1;
        int currentTotalSum = 0;

        for ( Map.Entry<String, Integer> e : columnName2Index.entrySet() ) {
            final String db = e.getKey();
            final Integer colIndex = e.getValue();
            final Long dbCount = db2count.get( db );

            log.info( "Updating worksheet for '"+ db +"' having count of "+ dbCount );

            String count;
            if( dbCount == null ) {
                if( TOTAL_COLUMN.equalsIgnoreCase( db ) || DATE_COLUMN.equalsIgnoreCase( db ) ) {
                    if ( colIndex != null ) {
                        totalColumnIndex = colIndex;
                    }
                    hasTotalColumn = true;

                    log.info( "Skipping column: " + db );
                    continue; // skip further processing as we don't have data for that database
                }
            }

            CellEntry previousCell = null;
            CellEntry previousDateCell = null;

            // TODO create WorksheetFacade.hasData() that returns true if there is at least one row of data in it.
            if ( nextEmptyRow > 2 ) {
                previousCell = myWorksheet.getCell( nextEmptyRow - 1, colIndex );
                log.info( "Collecting data for previous cell (value="+ previousCell.getCell().getValue() +")" );
                previousDateCell = myWorksheet.getCell( nextEmptyRow - 1, 1 );
            }

            CellEntry cell = null;

            if ( dbCount != null ) {

                count = dbCount.toString();

                // we have data for that database
                cell = myWorksheet.getCell( nextEmptyRow, colIndex );

                if ( previousCell != null ) {

                    final String previousValue = previousCell.getCell().getValue();
                    if ( previousValue != null && ! previousValue.equals( count ) ) {
                        if ( log.isDebugEnabled() ) log.info( worksheetName + ": statistics for " +
                                                              db + " has changed since " +
                                                              previousDateCell.getCell().getValue() );
                        if ( ! "0".equals( count ) ) {
                            // the service has a positive count so we record it in the stats
                            cell.changeInputValueLocal( count );
                            updatedServices.add( db );
                        }
                    } else {
                        // For cells that haven't been filled, copy the value of the previous row.
                        if ( log.isDebugEnabled() ) log.info( worksheetName + ": statistics for " +
                                                              db + " has NOT changed since " +
                                                              previousDateCell.getCell().getValue() );
                        cell.changeInputValueLocal( previousCell.getCell().getValue() );
                    }
                } else {
                    // we don't have data yet so all service have to be updated
                    log.info( "No previous cell available." );
                    cell.changeInputValueLocal( count );
                    updatedServices.add( db );
                }


                log.info( "Adding new cell with value: " + cell.getCell().getInputValue() );
                updatedCells.add( cell );

            } else {

                System.out.println( "++++ No data for " + db );
                System.out.println( "++++ previousCell: " + previousCell.getCell().getValue() );

                // We haven't got data for that database, copy the previous cell's data

                if ( previousCell != null ) {

                    if ( log.isDebugEnabled() ) log.info( worksheetName + ": statistics for " + db +
                                                          " has NOT changed since " +
                                                          previousDateCell.getCell().getValue() +
                                                          " copying previous one instead." );

                    cell = myWorksheet.getCell( nextEmptyRow, colIndex.intValue() );
                    cell.changeInputValueLocal( previousCell.getCell().getValue() );
                    updatedCells.add( cell );
                } else {
                    System.out.println( "Previous cell is null :(" );
                }
            }

            // ! updatedServices.isEmpty() &&
            if( cell != null && cell.getCell().getInputValue() != null ) {
                log.info( "Adding " + cell.getCell().getInputValue() + " to total sum" );
                // keep track of current total
                currentTotalSum += Integer.parseInt( cell.getCell().getInputValue() );
            }

        } // All databases registered in the worksheet header

        if( hasTotalColumn ) {
            // process all existing rows and update the total column

            log.info( "Updating Total..." );
            log.info( "getNextEmptyRow(): " + myWorksheet.getNextEmptyRow() );
            for ( int row = 2; row < myWorksheet.getNextEmptyRow(); row++ ) {

                int totalSum = 0;
                boolean abortRow = false;

                log.info( "getNextEmptyColumn(): " + myWorksheet.getNextEmptyColumn() );
                for ( int col = 2; col < myWorksheet.getNextEmptyColumn(); col++ ) {
                    if( col != totalColumnIndex ) {
                        final CellEntry cell = myWorksheet.getCell( row, col );
                        final String value = cell.getCell().getValue();
                        if( value != null ) {
                            try {
                                final int valueInt = Integer.parseInt( value );
                                totalSum += valueInt;

                                log.info( myWorksheet.getCell( row, 1 ).getCell().getValue() + " -> " + valueInt);
                            } catch ( NumberFormatException e ) {
                                // report cell format issue by email, and skip
                                sendEmail( "Cell format error in worksheet: " + worksheetName,
                                           "Cell["+row+","+col+"]='"+value + "' when an integer was expected" );
                                abortRow = true;
                                continue; // exit column loop
                            }
                        }
                    } else {
                        log.info( "Total contains: " + myWorksheet.getCell( row, col ).getCell().getValue() );
                    }
                }

                log.info( myWorksheet.getCell( row, 1 ).getCell().getValue() + " -> " + totalSum + " (SUM)");

                if( ! abortRow ) {
                    final CellEntry cell = myWorksheet.getCell( row, totalColumnIndex );
                    cell.changeInputValueLocal( String.valueOf( totalSum ) );
                    updatedCells.add( cell );
                }
            }

            log.info( "currentTotalSum = " + currentTotalSum );
            if( currentTotalSum != 0 ) {
                // given that the last row of this sheet is not available in the WorksheetFacade until we save the updatedCells,
                // we save the last total additionally
                log.info( "Current total: " + currentTotalSum );
                final CellEntry cell = myWorksheet.getCell( myWorksheet.getNextEmptyRow(), totalColumnIndex );
                cell.changeInputValueLocal( String.valueOf( currentTotalSum ) );
                updatedCells.add( cell );
            }
        } // hasTotalColumn


        // Check if a database is not yet registered in the spreadsheet.
        for ( Map.Entry<String, Long> e : db2count.entrySet() ) {
            String db = e.getKey();

            final Integer colIndex = columnName2Index.get( db );
            if( colIndex == null ) {
                sendEmail( "Missing DB name in header of worksheet '" + worksheetName + "'",
                           "Please add " + db + " in the header of the spreadsheet - please visit "
                           + spreadsheetEntry.getSpreadsheetLink().getHref() );
            }
        }

        if ( ! updatedCells.isEmpty() ) {
            // add date to the row
            final CellEntry cell = myWorksheet.getCell( nextEmptyRow, 1 );
            cell.changeInputValueLocal( today() );
            updatedCells.add( cell );

            // upload update
            myWorksheet.batchUpdate( updatedCells );
        }

        return updatedServices;
    }

    private String today() {return DATE_FORMAT.format( new Date() );}

    private String todayDirectory() {return DATE_AS_DIR.format( new Date() );}

    public List<String> updateInteractionWorksheet( final SpreadsheetService service,
                                                    SpreadsheetEntry spreadsheet,
                                                    Map<String, Long> db2interactionCount ) throws Exception {
        return updateSheet( service, spreadsheet, "Interactions", db2interactionCount );
    }

    private List<String> updatePublicationWorksheet( SpreadsheetService service,
                                                     SpreadsheetEntry spreadsheet,
                                                     Map<String, Long> db2publicationsCount ) throws IOException,
                                                                                                     ServiceException {
        return updateSheet( service, spreadsheet, "Publications", db2publicationsCount );
    }

    //////////////////////////////////////
    // PSICQUIC/Registry related methods

    private List<PsicquicService> collectPsicquicServiceNames() throws IOException {
        final String registryUrl = config.getPsicquicRegistryUrl();

        if ( log.isInfoEnabled() ) log.info( "Reading PSICQUIC services list from: " + registryUrl );

        // collect services via REST to to be able to pass filters on the URL (status, tags...)
        URL url = new URL( registryUrl );
        Properties props = new Properties( );
        final InputStream is = url.openStream();
        props.load( is );
        is.close();

        List<PsicquicService> services  = Lists.newArrayList();

        for ( Map.Entry<Object, Object> service : props.entrySet() ) {
            final PsicquicService s = new PsicquicService( ( String ) service.getKey(),
                                                           ( String ) service.getValue() );
            services.add( s );
            log.info( s );
        }

        if ( log.isInfoEnabled() ) log.info( "Found " + services.size() + " PSICQUIC services in the Registry." );

        return services;
    }

    /**
     * Update the given list of PSICQUIC services. that is their interaction count.
     * @param psicquicServices the list of services to update.
     * @throws PsicquicClientException
     */
    private Map<String, Long> updatePsicquicInteractionsStats( List<PsicquicService> psicquicServices ) {
        Map<String, Long> db2interactionCount = Maps.newHashMap();
        for ( PsicquicService service : psicquicServices ) {
            log.info( service.getName() + " -> " + service.getSoapUrl() );
            UniversalPsicquicClient client = new UniversalPsicquicClient( service.getSoapUrl(), PSICQUIC_DEFAULT_TIMEOUT );
            final SearchResult<BinaryInteraction> result;
            try {
                result = client.getByQuery( config.getMiqlQuery(), 0, 0 );
                service.setInteractionCount( result.getTotalCount() );
                db2interactionCount.put( service.getName(), result.getTotalCount().longValue() );
            } catch ( Throwable t ) {
                log.error( "An error occured while querying PSICQUIC service: " + service.getName(), t );
                sendEmail( "Failed to query " + service.getName(), ExceptionUtils.getFullStackTrace( t ) );
            }
        }

        if ( log.isDebugEnabled() ) {
            log.debug( "Interaction count collected:" );
            for ( Map.Entry<String, Long> entry : db2interactionCount.entrySet() ) {
                log.debug( entry.getKey() + " -> " + entry.getValue() );
            }
        }

        return db2interactionCount;
    }

    private Map<String, Long> collectPsicquicPublicationsStats( List<PsicquicService> psicquicServices,
                                                                List<String> psicquicWhiteList ) throws IOException {
        Map<String, Long> db2publicationsCount = Maps.newHashMap();

        for ( PsicquicService service : psicquicServices ) {
            if ( !psicquicWhiteList.contains( service.getName() ) ) {
                // skip this resource.
                if ( log.isInfoEnabled() ) log.info( "Skipping the count of pudmed for PSICQUIC sercice: " + service.getName() );
                continue;
            }

            final long totalInteractionCount = service.getInteractionCount();
            int current = 0;
            UniversalPsicquicClient client = new UniversalPsicquicClient( service.getSoapUrl(), PSICQUIC_DEFAULT_TIMEOUT );

            if ( log.isInfoEnabled() ) log.info( "Querying PSICQUIC service: " + service.getSoapUrl() );
            Set<String> pmids = Sets.newHashSet();
            boolean error = false;

            try {
                do {
                    final SearchResult<BinaryInteraction> query = client.getByQuery( config.getMiqlQuery(), current, PSICQUIC_BATCH_SIZE );
                    for ( BinaryInteraction interaction : query.getData() ) {
                        for ( Object o : interaction.getPublications() ) {
                            CrossReference cr = ( CrossReference ) o;
                            if ( "pubmed".equalsIgnoreCase( cr.getDatabase() )
                                 ||
                                 "pmid".equalsIgnoreCase( cr.getDatabase() )) {
                                pmids.add( cr.getIdentifier() );
                            }
                        }
                    } // interactions

                    current += query.getData().size();

                    // show progress
                    if ( ( current % 1000 ) == 0 ) {
                        log.info( current );
                    }
                } while ( current < totalInteractionCount );
            } catch ( Throwable t ) {
                log.error( "An error occured while collecting PMIDs from " + service.getName(), t );
                error = true;

                // email error
                sendEmail( "An error occured while collecting PMIDs from: " + service.getName(), 
                           ExceptionUtils.getFullStackTrace( t ) );
            }

            if ( log.isInfoEnabled() )
                log.info( "\n" + service.getName() + " -> " + pmids.size() + " publication(s)." +
                          ( error ? " However the PMID collection may have been interrupted." : "" ) );

            if ( !error ) {
                db2publicationsCount.put( service.getName(), ( long ) pmids.size() );
            } else {
                db2publicationsCount.put( service.getName(), -1L );
            }

            // Save pmid list for later processing.
            String user = null;
            if( senderEmail != null ) {
                System.out.println( "senderEmail='"+senderEmail+"'" );
                if( senderEmail.indexOf( "@" ) != -1 ) {
                    user = senderEmail.substring( 0, senderEmail.indexOf( "@" ));
                }
            }
            File parentDir = new File( (user == null ? "" : user + FILE_SEPARATOR ) + todayDirectory() );
            if ( !parentDir.exists() ) {
                parentDir.mkdirs();
            }
            writeListToDisk( pmids, new File( parentDir, service.getName() + ".pmids.txt" ) );

        } // psicquic services

        return db2publicationsCount;
    }

    private void writeListToDisk( Set<String> list, File file ) throws IOException {
        BufferedWriter out = new BufferedWriter( new FileWriter( file ) );
        for ( String line : list ) {
            out.write( line + "\n" );
        }
        out.flush();
        out.close();
    }

    private String showServices( List<PsicquicService> psicquicServices ) {
        StringBuilder sb = new StringBuilder( 256 );
        sb.append( '[' );
        for ( Iterator<PsicquicService> iterator = psicquicServices.iterator(); iterator.hasNext(); ) {
            PsicquicService service = iterator.next();
            sb.append( service.getName() );
            if( iterator.hasNext() ) {
                sb.append( ", " );
            }
        }
        sb.append( ']' );
        return sb.toString();
    }

    private void updateSpreadsheet( String email, String password, String spreadsheetKey ) throws Exception {
        SpreadsheetService service = new SpreadsheetService( "PSICQUIC-stats-collector-1" );
        // 2010-02: necessary or batch update fail with error like: "If-Match or If-None-Match header required"
        service.setProtocolVersion( SpreadsheetService.Versions.V1 );
        service.setUserCredentials( email, password );

        final SpreadsheetEntry spreadsheetEntry = SpreadsheetFacade.getSpreadsheetWithKey( service, spreadsheetKey );
        List<PsicquicService> psicquicServices = collectPsicquicServiceNames();

        // Update interaction counts
        Map<String, Long> db2interactionCount = updatePsicquicInteractionsStats( psicquicServices );
        final List<String> updatedServices = updateInteractionWorksheet( service, spreadsheetEntry, db2interactionCount );
        if ( log.isInfoEnabled() ) log.info( updatedServices.size() + " services updated: " + updatedServices );

        // Update publication for those services that have a different count of interactions
        Map<String, Long> db2publicationsCount = collectPsicquicPublicationsStats( psicquicServices, updatedServices );
        updatePublicationWorksheet( service, spreadsheetEntry, db2publicationsCount );

        if( ! updatedServices.isEmpty() ) {
            sendEmail( "Spreadsheet was updated", "Some services had new data online: " + showServices(psicquicServices) );
        } else {
            sendEmail( "Spreadsheet was NOT updated", "No PSICQUIC services had new data." );
        }
    }

    /////////////////
    // M A I N

    public static void main( String[] args ) throws Exception {

        // TODO Auto resize of the spreadsheet when we reach the maximum size

        if ( args.length < 3 ) {
            System.err.println( "usage: PsicquicStatsCollector <gmail.account> <password> <spreadsheet.key> " +
                                "[-D"+PSICQUIC_REGISTRY_URL_KEY+"=pathToFile] " +
                                "[-D"+SMTP_CONFIG_FILE_KEY+"=pathToFile]" );
            System.exit( 1 );
        }

        // Handle input parameters
        final String email = args[0];
        final String password = args[1];
        final String spreadsheetKey = args[2];

        PsicquicStatsCollector collector = new PsicquicStatsCollector();
        collector.updateSpreadsheet( email, password, spreadsheetKey );
    }
}
