package org.hupo.psi.mi.indexing.batch.writer;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.hupo.psi.calimocho.model.Row;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;
import psidev.psi.mi.calimocho.solr.converter.Converter;

import java.io.IOException;
import java.util.List;

/**
 * Solr item writer
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/05/12</pre>
 */

public class SolrItemWriter implements ItemWriter<Row>{

    private String solrUrl;
    private SolrServer solrServer;

    public void write(List<? extends Row> items) throws Exception {
        if (solrUrl == null) {
            throw new NullPointerException("No 'solrURL' configured for SolrItemWriter");
        }

        if (items.isEmpty()) {
            return;
        }

        Converter solrDocumentConverter = new Converter();

        for (Row binaryInteraction : items) {
            SolrInputDocument solrInputDoc = solrDocumentConverter.toSolrDocument(binaryInteraction);
            solrServer.add(solrInputDoc);
        }
    }

    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {
            if (solrUrl != null) {
                createSolrServer();
            }
        } catch (IOException e) {
            throw new ItemStreamException("Problem with ontology solr server: "+solrUrl, e);
        }
    }

    public void update(ExecutionContext executionContext) throws ItemStreamException {
    }

    public void close() throws ItemStreamException {
        try {
            SolrServer solrServer = createSolrServer();
            solrServer.commit();
        } catch (Exception e) {
            throw new ItemStreamException("Problem closing solr server", e);
        }
    }

    public SolrServer createSolrServer() throws IOException {
        if (solrServer == null) {
            if (solrUrl == null) {
                throw new NullPointerException("No 'solr url' configured for SolrItemWriter");
            }

            solrServer = new HttpSolrServer(solrUrl);
        }

        return solrServer;
    }

    public void setSolrUrl(String solrUrl) {
        this.solrUrl = solrUrl;
    }
}
