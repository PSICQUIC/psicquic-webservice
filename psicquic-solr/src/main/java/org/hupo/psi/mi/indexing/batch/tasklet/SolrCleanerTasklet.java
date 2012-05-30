package org.hupo.psi.mi.indexing.batch.tasklet;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.File;

/**
 * clean solr
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>30/05/12</pre>
 */

public class SolrCleanerTasklet implements Tasklet {

    private String solrPath;

    public SolrCleanerTasklet() {
    }

    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        if (solrPath != null){

            File home = new File(solrPath);
            File f = new File( home, "solr.xml" );
            CoreContainer container = new CoreContainer();
            container.load( solrPath, f );

            SolrServer solrServer = new EmbeddedSolrServer( container, "" );
            solrServer.deleteByQuery("*:*");

            solrServer.optimize();
            solrServer.commit();

            contribution.getExitStatus().addExitDescription("Cleared: " + solrPath);
        }
        else {
            contribution.getExitStatus().addExitDescription("no SOLR server url found.");
        }

        return RepeatStatus.FINISHED;
    }

    public void setSolrPath(String solrPath) {
        this.solrPath = solrPath;
    }
}
