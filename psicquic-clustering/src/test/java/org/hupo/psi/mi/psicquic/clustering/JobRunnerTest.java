package org.hupo.psi.mi.psicquic.clustering;

import org.hupo.psi.mi.psicquic.clustering.job.ClusteringJob;
import org.hupo.psi.mi.psicquic.clustering.job.JobIdGenerator;
import org.hupo.psi.mi.psicquic.clustering.job.dao.ClusteringServiceDaoFactory;
import org.hupo.psi.mi.psicquic.clustering.job.dao.JobDao;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

/**
 * A playground to warm up the framework.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 */
public class JobRunnerTest extends ClusteringTestCase {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobLauncher launcher;

    @Autowired
    private org.springframework.batch.core.Job job;

    @Test
    public void runJob() throws Exception {
        final ClusteringServiceDaoFactory daoFactory = ClusteringContext.getInstance().getDaoFactory();
        final JobDao jobDao = daoFactory.getJobDao();

        // create a job
        ClusteringJob job1 = new ClusteringJob( "brca2", Arrays.asList( new Service( "IntAct" ) ) ); // , new Service( "MINT" )
        jobDao.addJob( new JobIdGenerator().generateJobId( job1 ), job1 );

        ClusteringJob job2 = new ClusteringJob( "brca2", Arrays.asList( new Service( "MINT" ), new Service( "IntAct" ) ) );
        jobDao.addJob( new JobIdGenerator().generateJobId( job2 ), job2 );

        // run the next available job
        ClusteringJob nextJob = jobDao.getNextJobToRun();
        Assert.assertNotNull( nextJob );

        Assert.assertSame( job1, nextJob );

        // get the test to hang in there to let Quartz fire some jobs ...
        for ( ;; ) { }
    }
}
