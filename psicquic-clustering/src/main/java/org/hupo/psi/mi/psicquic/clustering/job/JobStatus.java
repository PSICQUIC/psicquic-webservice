package org.hupo.psi.mi.psicquic.clustering.job;

/**
 * TODO document this !
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 0.1
 */
public enum JobStatus {

    /*
    State diagram:  queued -> running -> failed|completed
     */

    QUEUED,
    RUNNING,
    FAILED,
    COMPLETED;

}
