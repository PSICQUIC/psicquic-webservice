package org.hupo.psi.mi.psicquic.indexing.batch.writer;

import org.apache.solr.client.solrj.SolrServerException;
import org.hupo.psi.calimocho.model.Row;

import java.util.List;

/**
 * extension of solr item writer but throws an exception
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/04/13</pre>
 */

public class SolrItemWriterThrowingSolrServerException extends SolrItemWriter{

    private boolean secondTime = false;
    private int count = 0;

    @Override
    public void write(List<? extends Row> items) throws Exception {
        super.write(items);
        count+=items.size();
        if (count >= 3 && !secondTime) {
            secondTime = true;
            throw new SolrServerException("Breaking thing");
        }
    }
}
