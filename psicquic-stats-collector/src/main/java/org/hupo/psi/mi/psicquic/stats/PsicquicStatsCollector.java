package org.hupo.psi.mi.psicquic.stats;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.ServiceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import uk.ac.ebi.intact.google.spreadsheet.SpreadsheetFacade;
import uk.ac.ebi.intact.google.spreadsheet.WorksheetFacade;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hupo.psi.mi.psicquic.registry.ServiceType;
import org.hupo.psi.mi.psicquic.registry.client.PsicquicRegistryClientException;
import org.hupo.psi.mi.psicquic.registry.client.registry.DefaultPsicquicRegistryClient;
import org.hupo.psi.mi.psicquic.registry.client.registry.PsicquicRegistryClient;
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

    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "dd/MM/yyyy" );

    private static final String PSICQUIC_REGISTRY_URL_KEY = "psicquic.registry.url";

    private static final String SMTP_CONFIG_FILE_KEY = "smtp.config.file";

    private static final int PSICQUIC_DEFAULT_TIMEOUT = 10000;
    private static final int PSICQUIC_BATCH_SIZE = 200;
    
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
        // if System.properties contain 'smtp.config.file', if so use that ! if not rely on the default
        File smtpConfig = null;
//        final String configFile = System.getProperty( SMTP_CONFIG_FILE_KEY );
        if( config.hasSmtpConfigFile() ) {
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

        List<String> updatedServices = Lists.newArrayList();

        final WorksheetFacade myWorksheet = WorksheetFacade.getWorksheetByName( service, spreadsheetEntry, worksheetName );
        Map<String, Integer> columnName2Index = myWorksheet.getColumnNameIndex();
        final int row = myWorksheet.getNextEmptyRow();
        List<CellEntry> updatedCells = Lists.newArrayList();

        for ( Map.Entry<String, Long> e : db2count.entrySet() ) {
            String db = e.getKey();
            String count = e.getValue().toString();

            final Integer colIndex = columnName2Index.get( db );
            if ( colIndex != null ) {

                // found it, prepare the data
                final CellEntry cell = myWorksheet.getCell( row, colIndex );

                if ( row > 2 ) {
                    final CellEntry previousCell = myWorksheet.getCell( row - 1, colIndex );
                    final CellEntry previousDateCell = myWorksheet.getCell( row - 1, 1 );

                    final String previousValue = previousCell.getCell().getValue();
                    if ( previousValue != null && ! previousValue.equals( count ) ) {
                        if ( log.isDebugEnabled() ) log.info( worksheetName + ": statistics for " +
                                                              db + " has changed since " +
                                                              previousDateCell.getCell().getValue() );
                        if ( !"0".equals( count ) ) {
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
                    updatedServices.add( db );
                }

                updatedCells.add( cell );

            } else {
                sendEmail( "Missing DB name in header of worksheet '" + worksheetName + "'",
                           "Please add " + db + " in the header of the spreadsheet - please visit "
                           + spreadsheetEntry.getSpreadsheetLink().getHref() );
            }
        } // cell values

        if ( !updatedServices.isEmpty() ) {
            // add date to the row
            final CellEntry cell = myWorksheet.getCell( row, 1 );
            cell.changeInputValueLocal( today() );
            updatedCells.add( cell );

            // upload update
            myWorksheet.batchUpdate( updatedCells );
        }

        return updatedServices;
    }

    private String today() {return DATE_FORMAT.format( new Date() );}

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
            System.out.println( s );
        }

        if ( log.isInfoEnabled() ) log.info( "Found " + services.size() + " PSICQUIC services in the Registry." );

        return services;
    }

    /**
     * Update the given list of PSICQUIC services. that is their interaction count.
     * @param psicquicServices the list of services to update.
     * @throws PsicquicClientException
     */
    private Map<String, Long> updatePsicquicInteractionsStats( List<PsicquicService> psicquicServices ) throws PsicquicClientException {
        Map<String, Long> db2interactionCount = Maps.newHashMap();
        for ( PsicquicService service : psicquicServices ) {
            log.info( service.getName() + " -> " + service.getSoapUrl() );
            UniversalPsicquicClient client = new UniversalPsicquicClient( service.getSoapUrl(), PSICQUIC_DEFAULT_TIMEOUT );
            final SearchResult<BinaryInteraction> result = client.getByQuery( "*:*", 0, 0 );
            service.setInteractionCount( result.getTotalCount() );
            db2interactionCount.put( service.getName(), result.getTotalCount().longValue() );
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
                    final SearchResult<BinaryInteraction> query = client.getByQuery( "*:*", current, PSICQUIC_BATCH_SIZE );
                    for ( BinaryInteraction interaction : query.getData() ) {
                        for ( Object o : interaction.getPublications() ) {
                            CrossReference cr = ( CrossReference ) o;
                            if ( "pubmed".equalsIgnoreCase( cr.getDatabase() ) ) {
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
            } catch ( PsicquicClientException pce ) {
                log.error( "An error occured while collecting PMIDs from " + service.getName() );
                error = true;

                // email error
                sendEmail( "[PsicquicStatsCollector] An error occured while reading data from " + service.getName(),
                           ExceptionUtils.getFullStackTrace( pce ) );
            }

            if ( log.isInfoEnabled() )
                log.info( "\n" + service.getName() + " -> " + pmids.size() + " publication(s)." +
                          ( error ? " However the PMID collection may have been interupted." : "" ) );

            if ( !error ) {
                db2publicationsCount.put( service.getName(), ( long ) pmids.size() );
            } else {
                db2publicationsCount.put( service.getName(), -1L );
            }

            // Save pmid list for later processing.
            File parentDir = new File( today() );
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
            out.write( line );
        }
        out.flush();
        out.close();
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
            sendEmail( "Spreadsheet was updated", "Some services had new data online: " + psicquicServices );
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
