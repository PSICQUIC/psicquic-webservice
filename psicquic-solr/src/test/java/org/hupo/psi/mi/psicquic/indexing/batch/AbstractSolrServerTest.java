package org.hupo.psi.mi.psicquic.indexing.batch;

import org.apache.commons.io.FileUtils;
import org.hupo.psi.mi.psicquic.indexing.batch.server.SolrJettyRunner;

/**
 * Abstract class for testing solr server
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>16/07/12</pre>
 */

public class AbstractSolrServerTest {

    protected SolrJettyRunner startJetty() throws Exception {
        // Start a jetty server to host the solr index
        SolrJettyRunner solrJettyRunner = new SolrJettyRunner();
        solrJettyRunner.start();

        return solrJettyRunner;
    }

    protected void stopJetty(SolrJettyRunner solrJettyRunner) throws Exception {
        // shutdown solrJetty
        solrJettyRunner.stop();

        FileUtils.deleteQuietly(solrJettyRunner.getSolrHome());
    }
}
