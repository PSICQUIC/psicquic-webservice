package org.hupo.psi.mi.psicquic.indexing.batch.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

/**
 * Listener of a PSICQUIC Job
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>30/05/12</pre>
 */

public class PsicquicJobExecutionListener implements JobExecutionListener {

    public void beforeJob(JobExecution jobExecution) {
        System.out.println("\nJOB STARTED: "+jobExecution+"\n");
    }

    public void afterJob(JobExecution jobExecution) {
        System.out.println("\nJOB FINISHED: "+jobExecution+"\n");
    }
}
