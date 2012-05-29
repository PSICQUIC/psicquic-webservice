package org.hupo.psi.mi.indexing.batch.reader;

import org.hupo.psi.calimocho.model.Row;
import org.hupo.psi.calimocho.tab.io.DefaultRowReader;
import org.hupo.psi.calimocho.tab.io.RowReader;
import org.hupo.psi.calimocho.tab.model.ColumnBasedDocumentDefinition;
import org.hupo.psi.calimocho.tab.util.MitabDocumentDefinitionFactory;
import org.springframework.batch.item.file.LineMapper;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/05/12</pre>
 */

public class MitabLineMapper implements LineMapper<Row>{

    private ColumnBasedDocumentDefinition documentDefinition;
    private RowReader rowReader;

    public MitabLineMapper(MitabVersion mitabVersion) {

        if (mitabVersion == null){
           mitabVersion = MitabVersion.MITAB25;
        }

        switch (mitabVersion){
            case MITAB25:
                documentDefinition = MitabDocumentDefinitionFactory.mitab25();
            case MITAB26:
                documentDefinition = MitabDocumentDefinitionFactory.mitab26();
            case MITAB27:
                documentDefinition = MitabDocumentDefinitionFactory.mitab27();
        }

        this.rowReader = new DefaultRowReader( documentDefinition );
    }

    public Row mapLine(String line, int lineNumber) throws Exception {

        try {

            if (line == null){
               return null;
            }

            return rowReader.readLine(line);
        } catch (Exception e) {
            throw new Exception("Problem converting to binary interaction line "+lineNumber+": "+line);
        }
    }
}
