package org.hupo.psi.mi.psicquic.indexing.batch;

import org.hupo.psi.mi.indexing.batch.SolrMitabIndexer;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>30/05/12</pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( locations = {"classpath*:/META-INF/psicquic-spring.xml",
        "classpath*:/jobs/psicquic-indexing-spring-test.xml"})
public class SolrMitabIndexerTest {

    @Autowired
    private SolrMitabIndexer solrMitabIndexer;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @Ignore
    public void test_indexing_mitab27(){

        try {
            solrMitabIndexer.startIndexing();
        } catch (JobInstanceAlreadyCompleteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (JobParametersInvalidException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (JobRestartException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (JobExecutionAlreadyRunningException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}


