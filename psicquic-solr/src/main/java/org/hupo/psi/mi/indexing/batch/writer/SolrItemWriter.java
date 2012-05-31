package org.hupo.psi.mi.indexing.batch.writer;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.hupo.psi.calimocho.model.Row;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;
import org.xml.sax.SAXException;
import psidev.psi.mi.calimocho.solr.converter.Converter;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Solr item writer
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/05/12</pre>
 */

public class SolrItemWriter implements ItemWriter<Row>, ItemStream {

    private String solrPath;
    private EmbeddedSolrServer solrServer;

    public void write(List<? extends Row> items) throws Exception {

        if (solrPath == null) {
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
            if (solrPath != null) {
                try {
                    createSolrServer();
                } catch (SAXException e) {
                    new ItemStreamException("Impossible to create a new embedded solr server",e);
                } catch (ParserConfigurationException e) {
                    new ItemStreamException("Impossible to create a new embedded solr server",e);
                }
            }
        } catch (IOException e) {
            throw new ItemStreamException("Problem with ontology solr server: "+ solrPath, e);
        }
    }

    public void update(ExecutionContext executionContext) throws ItemStreamException {
    }

    public void close() throws ItemStreamException {
        try {
            solrServer.optimize();
            solrServer.commit();

            solrServer.shutdown();

        } catch (Exception e) {
            throw new ItemStreamException("Problem closing solr server", e);
        }
    }

    public SolrServer createSolrServer() throws IOException, SAXException, ParserConfigurationException {
        if (solrServer == null) {
            if (solrPath == null) {
                throw new NullPointerException("No 'solr url' configured for SolrItemWriter");
            }

            File home = new File(solrPath);
            File f = new File( home, "solr.xml" );
            CoreContainer container = new CoreContainer();
            container.load( solrPath, f );

            solrServer = new EmbeddedSolrServer( container, "" );
        }

        return solrServer;
    }

    public void setSolrPath(String solrPath) {
        this.solrPath = solrPath;
    }
}
