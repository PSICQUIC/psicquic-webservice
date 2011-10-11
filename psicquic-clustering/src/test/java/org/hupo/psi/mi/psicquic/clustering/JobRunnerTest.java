package org.hupo.psi.mi.psicquic.clustering;

import org.hupo.psi.mi.psicquic.clustering.job.ClusteringJob;
import org.hupo.psi.mi.psicquic.clustering.job.JobIdGenerator;
import org.hupo.psi.mi.psicquic.clustering.job.dao.ClusteringServiceDaoFactory;
import org.hupo.psi.mi.psicquic.clustering.job.dao.JobDao;
import org.hupo.psi.mi.psicquic.clustering.job.dao.impl.memory.InMemoryClusteringServiceDaoFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Arrays;

/**
 * A playground to warm up the framework.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 */
public class JobRunnerTest extends ClusteringTestCase {

    @Test
    public void runJob() throws Exception {

        Assert.assertTrue( getDaoFactory() instanceof InMemoryClusteringServiceDaoFactory );

        final JobDao jobDao = getDaoFactory().getJobDao();

        // create a job
        ClusteringJob job1 = new ClusteringJob( "brca2", Arrays.asList( new Service( "IntAct" ) ) );
        jobDao.addJob( job1.getJobId(), job1 );

        // increase time difference between job creation timestamp
        Thread.sleep( 2000 );

        ClusteringJob job2 = new ClusteringJob( "brca2", Arrays.asList( new Service( "MINT" ), new Service( "IntAct" ) ) );
        jobDao.addJob( job2.getJobId(), job2 );

        // run the next available job
        ClusteringJob nextJob = jobDao.getNextJobToRun();
        Assert.assertNotNull( nextJob );

        Assert.assertSame( job1, nextJob );
    }
}