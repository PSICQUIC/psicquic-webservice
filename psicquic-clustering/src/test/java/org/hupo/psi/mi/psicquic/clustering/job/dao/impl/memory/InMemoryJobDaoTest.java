package org.hupo.psi.mi.psicquic.clustering.job.dao.impl.memory;

import org.hupo.psi.mi.psicquic.clustering.ClusteringContext;
import org.hupo.psi.mi.psicquic.clustering.Service;
import org.hupo.psi.mi.psicquic.clustering.job.JobDefinition;
import org.hupo.psi.mi.psicquic.clustering.job.JobIdGenerator;
import org.hupo.psi.mi.psicquic.clustering.job.dao.ClusteringServiceDaoFactory;
import org.hupo.psi.mi.psicquic.clustering.job.dao.JobDao;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * InMemoryJobDao Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 */
public class InMemoryJobDaoTest {
    @Test
    public void getNextJobToRun() throws Exception {
        final ClusteringServiceDaoFactory daoFactory = ClusteringContext.getInstance().getDaoFactory();
        final JobDao jobDao = daoFactory.getJobDao();

        // create a job
        JobDefinition job1 = new JobDefinition( "9606", Arrays.asList( new Service( "IntAct" ) ) );
        jobDao.addJob( new JobIdGenerator().generateJobId( job1 ), job1 );

        JobDefinition job2 = new JobDefinition( "mouse", Arrays.asList( new Service( "MINT" ), new Service( "IntAct" ) ) );
        jobDao.addJob( new JobIdGenerator().generateJobId( job2 ), job2 );

        // run the next available job
        JobDefinition nextJob = jobDao.getNextJobToRun();
        Assert.assertNotNull( nextJob );

        Assert.assertSame( job1, nextJob );
    }
}
