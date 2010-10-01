package org.hupo.psi.mi.psicquic.clustering;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hupo.psi.mi.psicquic.clustering.job.ClusteringJob;
import org.hupo.psi.mi.psicquic.clustering.job.JobIdGenerator;
import org.hupo.psi.mi.psicquic.clustering.job.JobStatus;
import org.hupo.psi.mi.psicquic.clustering.job.dao.ClusteringServiceDaoFactory;
import org.hupo.psi.mi.psicquic.clustering.job.dao.JobDao;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Iterator;

/**
 * The clustering launcher is periodicaly looking for clustering jobs to launch in spring batch.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 */
public class ClusteringLauncher extends QuartzJobBean {

    private static final Log log = LogFactory.getLog( ClusteringLauncher.class );

    private JobRepository jobRepository;

    private JobLauncher jobLauncher;

    private org.springframework.batch.core.Job job;

    //////////////////
    // Constructors

    public ClusteringLauncher() {
        final ApplicationContext springContext = ClusteringContext.getInstance().getSpringContext();

        if ( jobRepository == null ) {
            jobRepository = ( JobRepository ) springContext.getBean( "jobRepository" );
        }

        if ( jobLauncher == null ) {
            jobLauncher = ( JobLauncher ) springContext.getBean( "jobLauncher" );
        }

        if ( job == null ) {
            job = ( org.springframework.batch.core.Job ) springContext.getBean( "clusteringJob" );
        }
    }

    @Override
    protected void executeInternal( org.quartz.JobExecutionContext context ) {
        log.debug( "---------------------- QUARTZ TRIGGER: ClusteringLauncher ----------------------------" );

        final ClusteringServiceDaoFactory daoFactory = ClusteringContext.getInstance().getDaoFactory();
        final JobDao jobDao = daoFactory.getJobDao();

        // run the next available job
        ClusteringJob nextJob = jobDao.getNextJobToRun();

        if ( nextJob != null ) {

            try {

                // instanciate the job parameter and give our job definition 
                StringBuilder serviceBuf = new StringBuilder( 128 );
                for ( Iterator<Service> iterator = nextJob.getServices().iterator(); iterator.hasNext(); ) {
                    Service service = iterator.next();
                    serviceBuf.append( service.getName() );
                    if ( iterator.hasNext() ) {
                        serviceBuf.append( '|' );
                    }
                }

                log.debug( "miql:     " + nextJob.getMiql() );
                log.debug( "services: " + serviceBuf.toString() );
                log.debug( "jobId:    " + new JobIdGenerator().generateJobId( nextJob ) );

                JobParameters jobParameters =
                        new JobParametersBuilder()
                                .addLong( "now", System.currentTimeMillis() )
                                .addString( "miql", nextJob.getMiql() )
                                .addString( "services", serviceBuf.toString() )
                                .addString( "jobId", new JobIdGenerator().generateJobId( nextJob ) )
                                .toJobParameters();

                if ( jobLauncher == null ) {
                    log.debug( "null launcher" );
                    return;
                }
                if ( job == null ) {
                    log.debug( "null job" );
                    return;
                }

                jobLauncher.run( job, jobParameters );

            } catch ( Exception e ) {
                nextJob.setStatus( JobStatus.FAILED );
                nextJob.setStatusException( e );
                log.error( "Error while starting job:" + nextJob, e );
            }

        } else {
            log.debug( "No job to be processed." );
        }
    }
}
