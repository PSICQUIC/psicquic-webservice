package org.hupo.psi.mi.psicquic.clustering;

import java.io.File;

/**
 * Clustering config.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.0
 */
public class ClusteringConfig {

    public final static String SYSTEM_KEY_DATA_LOCATION = "psicquic.clustering.data";

    /**
     * Fully qualified filename of the directory that stores the computed data.
     */
    private String dataLocation;

    public ClusteringConfig() {
        final String defaultLocation = System.getProperty( "java.io.tmpdir" );
        dataLocation = System.getProperty( SYSTEM_KEY_DATA_LOCATION, defaultLocation );
    }

    public String getDataLocation() {
        return dataLocation;
    }

    public File getDataLocationFile() {
        return new File(dataLocation);
    }

    public void setDataLocation( String dataLocation ) {
        this.dataLocation = dataLocation;
    }
}
