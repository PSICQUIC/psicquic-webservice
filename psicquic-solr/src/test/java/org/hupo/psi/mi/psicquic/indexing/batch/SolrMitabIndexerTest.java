package org.hupo.psi.mi.psicquic.indexing.batch;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.hupo.psi.mi.psicquic.indexing.batch.server.SolrJettyRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
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
public class SolrMitabIndexerTest extends AbstractSolrServerTest{

    @Autowired
    private SolrMitabIndexer solrMitabIndexer;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void test_indexing_negative() throws Exception, JobParametersInvalidException, JobRestartException, JobExecutionAlreadyRunningException {

        SolrJettyRunner solrJettyRunner = startJetty();

        solrMitabIndexer.startJob("mitabIndexNegativeJob");

        SolrServer server = solrJettyRunner.getSolrServer();

        Assert.assertEquals(4L, server.query(new SolrQuery("*:*")).getResults().getNumFound());
        // two negative, two positive
        Assert.assertEquals(2L, server.query(new SolrQuery("negative:true")).getResults().getNumFound());
        Assert.assertEquals(2L, server.query(new SolrQuery("negative:false")).getResults().getNumFound());

        // test idA and idB -> identifier and id
        Assert.assertEquals(2L, server.query(new SolrQuery("id:P07228")).getResults().getNumFound());
        Assert.assertEquals(2L, server.query(new SolrQuery("identifier:P07228")).getResults().getNumFound());
        Assert.assertEquals(1L, server.query(new SolrQuery("idA:P07228")).getResults().getNumFound());
        Assert.assertEquals(1L, server.query(new SolrQuery("idB:P07228")).getResults().getNumFound());

        // test altidA and altidB -> identifier and id
        Assert.assertEquals(2L, server.query(new SolrQuery("id:EBI-5606437")).getResults().getNumFound());
        Assert.assertEquals(2L, server.query(new SolrQuery("identifier:EBI-5606437")).getResults().getNumFound());
        Assert.assertEquals(0L, server.query(new SolrQuery("idA:EBI-5606437")).getResults().getNumFound());
        Assert.assertEquals(0L, server.query(new SolrQuery("idB:EBI-5606437")).getResults().getNumFound());

        // test altiasA and aliasB -> identifier and alias
        Assert.assertEquals(2L, server.query(new SolrQuery("alias:RGD-receptor")).getResults().getNumFound());
        Assert.assertEquals(2L, server.query(new SolrQuery("identifier:RGD-receptor")).getResults().getNumFound());
        Assert.assertEquals(0L, server.query(new SolrQuery("idA:RGD-receptor")).getResults().getNumFound());
        Assert.assertEquals(0L, server.query(new SolrQuery("idB:RGD-receptor")).getResults().getNumFound());
        Assert.assertEquals(0L, server.query(new SolrQuery("id:RGD-receptor")).getResults().getNumFound());

        // test detmethod
        Assert.assertEquals(1L, server.query(new SolrQuery("detmethod:\"two hybrid\"")).getResults().getNumFound());

        // test author
        Assert.assertEquals(1L, server.query(new SolrQuery("pubauth:\"Loo DT et al.(1998)\"")).getResults().getNumFound());

        // test pub id
        Assert.assertEquals(4L, server.query(new SolrQuery("pubid:9722563")).getResults().getNumFound());
        Assert.assertEquals(4L, server.query(new SolrQuery("pubid:imex")).getResults().getNumFound());

        // test protein species
        Assert.assertEquals(2L, server.query(new SolrQuery("taxidA:\"human-jurkat\"")).getResults().getNumFound());
        Assert.assertEquals(0L, server.query(new SolrQuery("taxidB:\"human-jurkat\"")).getResults().getNumFound());
        Assert.assertEquals(2L, server.query(new SolrQuery("species:\"human-jurkat\"")).getResults().getNumFound());

        // test interaction type
        Assert.assertEquals(1L, server.query(new SolrQuery("type:\"MI:0407\"")).getResults().getNumFound());

        // test interaction id
        Assert.assertEquals(1L, server.query(new SolrQuery("interaction_id:EBI-5630468")).getResults().getNumFound());

        // test biorole
        Assert.assertEquals(4L, server.query(new SolrQuery("pbioroleA:\"unspecified role\"")).getResults().getNumFound());
        Assert.assertEquals(4L, server.query(new SolrQuery("pbioroleB:\"unspecified role\"")).getResults().getNumFound());
        Assert.assertEquals(4L, server.query(new SolrQuery("pbiorole:\"unspecified role\"")).getResults().getNumFound());

        // test interactor type
        Assert.assertEquals(4L, server.query(new SolrQuery("ptypeA:protein")).getResults().getNumFound());
        Assert.assertEquals(4L, server.query(new SolrQuery("ptypeB:protein")).getResults().getNumFound());
        Assert.assertEquals(4L, server.query(new SolrQuery("ptype:protein")).getResults().getNumFound());

        // test interactor type
        Assert.assertEquals(4L, server.query(new SolrQuery("ptypeA:protein")).getResults().getNumFound());
        Assert.assertEquals(4L, server.query(new SolrQuery("ptypeB:protein")).getResults().getNumFound());
        Assert.assertEquals(4L, server.query(new SolrQuery("ptype:protein")).getResults().getNumFound());

        // test interactor xref
        Assert.assertEquals(1L, server.query(new SolrQuery("pxrefA:\"GO:0008305\"")).getResults().getNumFound());
        Assert.assertEquals(1L, server.query(new SolrQuery("pxrefB:\"GO:0008305\"")).getResults().getNumFound());
        Assert.assertEquals(2L, server.query(new SolrQuery("pxref:\"GO:0008305\"")).getResults().getNumFound());

        // test interaction xref
        Assert.assertEquals(1L, server.query(new SolrQuery("xref:\"IM-17229-4\"")).getResults().getNumFound());

        // test interaction annotations
        Assert.assertEquals(1L, server.query(new SolrQuery("annot:\"Fig. 1.\"")).getResults().getNumFound());

        // test udate
        Assert.assertEquals(1L, server.query(new SolrQuery("udate:[20120301 TO 20120302]")).getResults().getNumFound());

        // test feature
        Assert.assertEquals(1L, server.query(new SolrQuery("ftypeA:\"necessary binding region\"")).getResults().getNumFound());
        Assert.assertEquals(1L, server.query(new SolrQuery("ftypeB:\"necessary binding region\"")).getResults().getNumFound());
        Assert.assertEquals(1L, server.query(new SolrQuery("ftype:\"necessary binding region\"")).getResults().getNumFound());
        Assert.assertEquals(0L, server.query(new SolrQuery("ftypeA:2171-2647")).getResults().getNumFound());

        // test stc
        Assert.assertEquals(0L, server.query(new SolrQuery("stc:true")).getResults().getNumFound());
        Assert.assertEquals(4L, server.query(new SolrQuery("stc:false")).getResults().getNumFound());

        // test participant detection method
        Assert.assertEquals(1L, server.query(new SolrQuery("pmethodA:\"MI:0981\"")).getResults().getNumFound());
        Assert.assertEquals(1L, server.query(new SolrQuery("pmethodB:\"MI:0981\"")).getResults().getNumFound());
        Assert.assertEquals(1L, server.query(new SolrQuery("pmethod:\"MI:0981\"")).getResults().getNumFound());

        stopJetty(solrJettyRunner);
    }

    @Test
    public void test_indexing_parameter() throws Exception, JobParametersInvalidException, JobRestartException, JobExecutionAlreadyRunningException {

        SolrJettyRunner solrJettyRunner = startJetty();

        solrMitabIndexer.startJob("mitabIndexParameterJob");

        SolrServer server = solrJettyRunner.getSolrServer();

        Assert.assertEquals(14L, server.query(new SolrQuery("*:*")).getResults().getNumFound());
        // two parameters
        Assert.assertEquals(2L, server.query(new SolrQuery("param:true")).getResults().getNumFound());
        Assert.assertEquals(12L, server.query(new SolrQuery("param:false")).getResults().getNumFound());

        // test stc
        Assert.assertEquals(2L, server.query(new SolrQuery("stc:true")).getResults().getNumFound());
        Assert.assertEquals(12L, server.query(new SolrQuery("stc:false")).getResults().getNumFound());

        stopJetty(solrJettyRunner);
    }
}


