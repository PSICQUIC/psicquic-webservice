package org.hupo.psi.mi.psicquic.indexing.batch.server;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Build solr home
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>21/06/12</pre>
 */

public class SolrHomeBuilder {

    private static Logger log = LoggerFactory.getLogger(SolrHomeBuilder.class);

    private File solrHomeDir;
    private File solrWar;

    public SolrHomeBuilder() {
    }

    public void install(File solrWorkingDir) throws IOException {
        if (log.isInfoEnabled()) log.info("Installing Intact SOLR Home at: "+solrWorkingDir);

        // copy resource directory containing solr-home and war file
        File solrHomeToCreate = new File(solrWorkingDir,"solr-home");
        solrHomeToCreate.mkdirs();

        FileUtils.copyDirectory(new File(SolrHomeBuilder.class.getResource("/solr-home").getFile()), solrHomeToCreate);
        FileUtils.copyFileToDirectory(new File(SolrHomeBuilder.class.getResource("/solr.war").getFile()), solrWorkingDir);

        solrHomeDir = solrHomeToCreate;
        solrWar = new File(solrWorkingDir, "solr.war");

        if (log.isDebugEnabled()) {
            log.debug("\nSolr Home: {}\nSolr WAR: {}", solrHomeDir.toString(), solrWar.toString());
        }

    }

    public File getSolrHomeDir() {
        return solrHomeDir;
    }

    public File getSolrWar() {
        return solrWar;
    }
}
