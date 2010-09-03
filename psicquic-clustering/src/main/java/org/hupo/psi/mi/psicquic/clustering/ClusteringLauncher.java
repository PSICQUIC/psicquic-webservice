package org.hupo.psi.mi.psicquic.clustering;

import org.apache.commons.lang.StringUtils;
import org.hupo.psi.mi.psicquic.clustering.job.JobDefinition;
import org.hupo.psi.mi.psicquic.clustering.job.JobIdGenerator;
import org.hupo.psi.mi.psicquic.clustering.job.JobStatus;
import org.hupo.psi.mi.psicquic.clustering.job.dao.ClusteringServiceDaoFactory;
import org.hupo.psi.mi.psicquic.clustering.job.dao.JobDao;
import org.quartz.SchedulerException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
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

    private ApplicationContext springContext;

    private JobRepository jobRepository;

    private JobLauncher jobLauncher;

    private org.springframework.batch.core.Job job;

    //////////////////
    // Constructors

    public ClusteringLauncher() {
        System.out.println( "ClusteringLauncher.ClusteringLauncher: " + this );

        springContext = ClusteringContext.getInstance().getSpringContext();

        if ( jobRepository == null ) {
            System.out.println( "JobDefinition repository was null, attempting to load it through getBean..." );
            jobRepository = ( JobRepository ) springContext.getBean( "jobRepository" );
        }

        if ( jobLauncher == null ) {
            System.out.println( "JobDefinition launcher was null, attempting to load it through getBean..." );
            jobLauncher = ( JobLauncher ) springContext.getBean( "jobLauncher" );
        }

        if ( job == null ) {
            System.out.println( "JobDefinition was null, attempting to load it through getBean..." );
            job = ( org.springframework.batch.core.Job ) springContext.getBean( "clusteringJob" );
        }
    }

    ///////////////////////////
    // Getters and Setters

    public JobRepository getJobRepository() {
        return jobRepository;
    }

    public void setJobRepository( JobRepository jobRepository ) {
        this.jobRepository = jobRepository;
    }

    public JobLauncher getJobLauncher() {
        return jobLauncher;
    }

    public void setJobLauncher( JobLauncher launcher ) {
        this.jobLauncher = launcher;
    }

    public org.springframework.batch.core.Job getJob() {
        return job;
    }

    public void setJob( org.springframework.batch.core.Job job ) {
        this.job = job;
    }

    @Override
    protected void executeInternal( org.quartz.JobExecutionContext context ) {
        System.out.println( "---------------------- QUARTZ TRIGGER ----------------------------" );
        System.out.println( "ClusteringLauncher.executeInternal" );

        final ClusteringServiceDaoFactory daoFactory = ClusteringContext.getInstance().getDaoFactory();
        final JobDao jobDao = daoFactory.getJobDao();

        // run the next available job
        JobDefinition nextJob = jobDao.getNextJobToRun();

        if ( nextJob != null ) {

            try {

                // instanciate the job parameter and give our job definition 
                StringBuilder serviceBuf = new StringBuilder(128);
                for ( Iterator<Service> iterator = nextJob.getServices().iterator(); iterator.hasNext(); ) {
                    Service service = iterator.next();
                    serviceBuf.append( service.getName() );
                    if( iterator.hasNext() ) {
                        serviceBuf.append('|');
                    }
                }

                System.out.println( "miql:     "+ nextJob.getMiql() );
                System.out.println( "services: "+ serviceBuf.toString() );
                System.out.println( "jobId:    "+ new JobIdGenerator().generateJobId( nextJob ) );

                JobParameters jobParameters =
                        new JobParametersBuilder()
                                .addString( "miql", nextJob.getMiql() )
                                .addString( "services", serviceBuf.toString() )
                                .addString( "jobId", new JobIdGenerator().generateJobId( nextJob ) )
                                .toJobParameters();

                if ( jobLauncher == null ) {
                    System.err.println( "null launcher" );
                    return;
                }
                if ( job == null ) {
                    System.err.println( "null job" );
                    return;
                }

                jobLauncher.run( job, jobParameters );


                // TODO the Batch job should update the status of this clustering job upon completion

            } catch ( JobExecutionAlreadyRunningException e ) {
                e.printStackTrace();
            } catch ( JobRestartException e ) {
                e.printStackTrace();
            } catch ( JobInstanceAlreadyCompleteException e ) {
                e.printStackTrace();
            }
            
        } else {
            System.out.println( "No job to be processed." );
        }
    }

    public static void main( String[] args ) throws SchedulerException {

//        final ClassPathXmlApplicationContext springContext = new ClassPathXmlApplicationContext( "job-clustering.xml" );

        final ClusteringLauncher cl = new ClusteringLauncher();
        cl.executeInternal( null );
    }
}
