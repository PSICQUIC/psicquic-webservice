package org.hupo.psi.mi.psicquic.indexing.batch.reader;

import org.hupo.psi.calimocho.model.Row;
import org.hupo.psi.calimocho.tab.io.DefaultRowReader;
import org.hupo.psi.calimocho.tab.io.RowReader;
import org.hupo.psi.calimocho.tab.model.ColumnBasedDocumentDefinition;
import org.hupo.psi.calimocho.tab.util.MitabDocumentDefinitionFactory;
import org.springframework.batch.item.file.LineMapper;

/**
 * Mapper that will map a MITAB line to a Calimocho Row
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/05/12</pre>
 */

public class MitabLineMapper implements LineMapper<Row>{

    private ColumnBasedDocumentDefinition documentDefinition;
    private RowReader rowReader;

    public MitabLineMapper(MitabVersion mitabVersion) {

        // if no version, then we suppose it is MITAB 25
        if (mitabVersion == null){
            mitabVersion = MitabVersion.MITAB25;
        }

        switch (mitabVersion){
            case MITAB25:
                documentDefinition = MitabDocumentDefinitionFactory.mitab25();
                break;
            case MITAB26:
                documentDefinition = MitabDocumentDefinitionFactory.mitab26();
                break;
            case MITAB27:
                documentDefinition = MitabDocumentDefinitionFactory.mitab27();
                break;
            default:
                documentDefinition = MitabDocumentDefinitionFactory.mitab25();
                break;
        }

        this.rowReader = new DefaultRowReader( documentDefinition );
    }

    /**
     *
     * @param line
     * @param lineNumber
     * @return the matching Calimocho Row
     * @throws Exception
     */
    public Row mapLine(String line, int lineNumber) throws Exception {
        try {

            if (line == null){
                return null;
            }

            Row row = rowReader.readLine(line);

            return row;
        } catch (Exception e) {
            throw new Exception("Problem converting to binary interaction line "+lineNumber+": "+line, e);
        }
    }
}
