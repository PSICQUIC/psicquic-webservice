package org.hupo.psi.mi.indexing.batch.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>30/05/12</pre>
 */

public class PsicquicJobExecutionListener implements JobExecutionListener {

    public void beforeJob(JobExecution jobExecution) {


    }

    public void afterJob(JobExecution jobExecution) {
        System.out.println("\nJOB FINISHED: "+jobExecution+"\n");
    }
}
