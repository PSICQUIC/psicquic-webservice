package org.hupo.psi.mi.indexing.batch.tasklet;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * clean solr
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>30/05/12</pre>
 */

public class SolrCleanerTasklet implements Tasklet {

    private String solrUrl;

    public SolrCleanerTasklet() {
    }

    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        if (solrUrl != null){

            SolrServer solrServer = new HttpSolrServer(solrUrl);
            solrServer.deleteByQuery("*:*");

            solrServer.optimize();
            solrServer.commit();

            contribution.getExitStatus().addExitDescription("Cleared: " + solrUrl);
        }
        else {
            contribution.getExitStatus().addExitDescription("no SOLR server url found.");
        }

        return RepeatStatus.FINISHED;
    }

    public void setSolrUrl(String solrUrl) {
        this.solrUrl = solrUrl;
    }
}
