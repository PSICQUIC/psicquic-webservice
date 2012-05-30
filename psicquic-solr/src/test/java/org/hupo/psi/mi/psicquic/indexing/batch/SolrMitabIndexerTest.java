package org.hupo.psi.mi.psicquic.indexing.batch;

import org.hupo.psi.mi.indexing.batch.SolrMitabIndexer;
import org.hupo.psi.mi.indexing.batch.reader.MitabReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
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
        "classpath*:/META-INF/jobs/psicquic-indexing-spring-test.xml"})
public class SolrMitabIndexerTest {

    @Autowired
    private SolrMitabIndexer solrMitabIndexer;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void test_indexing_mitab27(){

        MitabReader mitabReaderTest = (MitabReader) applicationContext.getBean("mitabReaderTest");

        if (mitabReaderTest != null){
            mitabReaderTest.setResource(new FileSystemResource(SolrMitabIndexerTest.class.getResource("/samples/sampleFileConfidence.txt").getPath()));

            //solrMitabIndexer.startIndexing();
        }
    }
}


