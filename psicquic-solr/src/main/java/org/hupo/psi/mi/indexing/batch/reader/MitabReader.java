package org.hupo.psi.mi.indexing.batch.reader;

import org.hupo.psi.calimocho.model.Row;
import org.springframework.batch.item.file.FlatFileItemReader;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/05/12</pre>
 */

public class MitabReader extends FlatFileItemReader<Row> {

    private MitabVersion mitabVersion;

    @Override
    protected void doOpen() throws Exception {
        System.out.print("open file");

        super.doOpen();
    }

    public void setMitabVersion(MitabVersion mitabVersion) {
        this.mitabVersion = mitabVersion;
        MitabLineMapper mitabLineMapper = new MitabLineMapper(mitabVersion);
        setLineMapper(mitabLineMapper);
    }
}
