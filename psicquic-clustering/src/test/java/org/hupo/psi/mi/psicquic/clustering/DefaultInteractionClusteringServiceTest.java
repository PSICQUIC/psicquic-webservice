package org.hupo.psi.mi.psicquic.clustering;

import junit.framework.Assert;
import org.hupo.psi.mi.psicquic.QueryResponse;
import org.hupo.psi.mi.psicquic.clustering.job.JobStatus;
import org.hupo.psi.mi.psicquic.clustering.job.PollResult;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * DefaultInteractionClusteringService Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 */

public class DefaultInteractionClusteringServiceTest extends ClusteringTestCase {

    @Test
    public void submitJob() throws Exception {
        final InteractionClusteringService ics = new DefaultInteractionClusteringService();
        final String jobId = ics.submitJob( "brca2", Arrays.asList( new Service( "IntAct" ) ) );
        PollResult pollResult = ics.poll( jobId );
        Assert.assertNotNull( pollResult.getStatus() );
        Assert.assertTrue( JobStatus.QUEUED.equals( pollResult.getStatus() ) );

        while ( !( JobStatus.COMPLETED.equals( pollResult.getStatus() ) || JobStatus.FAILED.equals( pollResult.getStatus() ) ) ) {
            Thread.sleep( 1000 );
            pollResult = ics.poll( jobId );
        }

        if ( JobStatus.COMPLETED.equals( pollResult.getStatus() ) ) {
            final String format = InteractionClusteringService.RETURN_TYPE_MITAB25;
            final QueryResponse response = ics.query( jobId, "*:*", 0, 200, format );

            Assert.assertNotNull( response );

            Assert.assertNotNull( response.getResultInfo() );
            Assert.assertEquals( 200, response.getResultInfo().getBlockSize() );
            Assert.assertEquals( 0, response.getResultInfo().getFirstResult() );
            Assert.assertNotNull( response.getResultInfo().getResultType() );
            Assert.assertEquals( InteractionClusteringService.RETURN_TYPE_MITAB25, response.getResultInfo().getResultType() );

            Assert.assertEquals( 27, response.getResultInfo().getTotalResults() );

            Assert.assertNotNull( response.getResultSet() );
            Assert.assertNull( response.getResultSet().getEntrySet() );
            final String mitab = response.getResultSet().getMitab();
            Assert.assertNotNull( mitab );

            Assert.assertEquals( 27, mitab.split( "\n" ).length );
        }
    }

    @Test
    public void poll() throws Exception {
    }

    @Test
    public void query() throws Exception {
    }
}
