package org.hupo.psi.mi.psicquic.clustering;

import org.hupo.psi.mi.psicquic.clustering.job.JobDefinition;
import org.hupo.psi.mi.psicquic.clustering.job.JobIdGenerator;
import org.hupo.psi.mi.psicquic.clustering.job.dao.ClusteringServiceDaoFactory;
import org.hupo.psi.mi.psicquic.clustering.job.dao.JobDao;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

/**
 * A playground to warm up the framework.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 */
@ContextConfiguration( locations = "classpath:job-clustering.xml" )
@RunWith( SpringJUnit4ClassRunner.class )
public class JobRunnerTest {

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
        JobDefinition job1 = new JobDefinition( "brca2", Arrays.asList( new Service( "IntAct" ), new Service( "MINT" ) ) );
        jobDao.addJob( new JobIdGenerator().generateJobId( job1 ), job1 );

//        JobDefinition job2 = new JobDefinition( "mouse", Arrays.asList( new Service( "MINT" ), new Service( "IntAct" ) ) );
//        jobDao.addJob( new JobIdGenerator().generateJobId( job2 ), job2 );

        // run the next available job
        JobDefinition nextJob = jobDao.getNextJobToRun();
        Assert.assertNotNull( nextJob );

        Assert.assertSame( job1, nextJob );

        // get the test to hang in there to let Quartz fire some jobs ...
        for ( ;; ) { }
    }
}
