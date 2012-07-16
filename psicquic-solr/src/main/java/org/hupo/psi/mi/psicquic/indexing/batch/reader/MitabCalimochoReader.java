package org.hupo.psi.mi.psicquic.indexing.batch.reader;

import org.hupo.psi.calimocho.model.Row;
import org.springframework.batch.item.file.FlatFileItemReader;

/**
 * Reader of MITAB file
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/05/12</pre>
 */

public class MitabCalimochoReader extends FlatFileItemReader<Row> {

    private MitabVersion mitabVersion;

    @Override
    protected void doOpen() throws Exception {

        super.doOpen();
    }

    /**
     * setting the MITAB version will set the mitab line mapper
     * @param mitabVersion
     */
    public void setMitabVersion(MitabVersion mitabVersion) {
        this.mitabVersion = mitabVersion;
        MitabCalimochoLineMapper mitabLineMapper = new MitabCalimochoLineMapper(mitabVersion);
        setLineMapper(mitabLineMapper);
    }
}
