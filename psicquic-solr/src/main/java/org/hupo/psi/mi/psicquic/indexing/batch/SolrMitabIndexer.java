package org.hupo.psi.mi.psicquic.indexing.batch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.*;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.Resource;
import java.util.List;

/**
 * Main class to index mitab in solr
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>30/05/12</pre>
 */

public class SolrMitabIndexer {

    private static final Log log = LogFactory.getLog(SolrMitabIndexer.class);

    @Resource(name = "batchJobLauncher")
    private JobLauncher jobLauncher;

    @Resource(name = "jobRepository")
    private JobRepository jobRepository;

    @Resource(name = "jobOperator")
    private JobOperator jobOperator;

    @Autowired
    private ApplicationContext applicationContext;

    private String indexingId;

    public SolrMitabIndexer() {
    }

    public static void main(String[] args) throws JobInstanceAlreadyCompleteException, JobParametersInvalidException, JobRestartException, JobExecutionAlreadyRunningException, NoSuchJobExecutionException, NoSuchJobException, NoSuchJobInstanceException {

        // loads the spring context defining beans and jobs
        ApplicationContext context = new ClassPathXmlApplicationContext(
                new String[] {"/META-INF/psicquic-spring.xml", "/META-INF/jobs/psicquic-indexing-spring.xml"});

        SolrMitabIndexer rm = (SolrMitabIndexer)
                context.getBean("solrMitabIndexer");
        
        if ( rm.getIndexingId() != null){
            rm.resumeIndexing();
        }
        else {
            rm.startIndexing();
        }
    }

    public void resumeIndexing() throws JobInstanceAlreadyCompleteException, JobParametersInvalidException, NoSuchJobExecutionException, JobRestartException, NoSuchJobException, NoSuchJobInstanceException {
        Long executionId = findJobId(indexingId, "mitabIndexJob");

        if (executionId == null) {
            throw new IllegalStateException("Indexing Id not found: "+indexingId);
        }

        jobOperator.restart(executionId);
    }

    private Long findJobId(String indexingId, String jobName) throws NoSuchJobException, NoSuchJobExecutionException, NoSuchJobInstanceException {
        final List<Long> jobIds = jobOperator.getJobInstances(jobName, 0, Integer.MAX_VALUE);

        for (Long jobId : jobIds) {
            for (Long executionId : jobOperator.getExecutions(jobId)) {
                final String params = jobOperator.getParameters(executionId);

                if (params.contains(indexingId)) {
                    return executionId;
                }
            }
        }

        return null;
    }

    public String startIndexing() throws JobInstanceAlreadyCompleteException, JobParametersInvalidException, JobRestartException, JobExecutionAlreadyRunningException {
        String indexingId = "psicquic_index_"+System.currentTimeMillis();

        if (log.isInfoEnabled()) log.info("Starting indexing: "+indexingId);

        runJob("mitabIndexJob", indexingId);

        return indexingId;
    }

    protected JobExecution runJob(String jobName, String indexingId) throws JobInstanceAlreadyCompleteException, JobParametersInvalidException, JobRestartException, JobExecutionAlreadyRunningException {
        if (log.isInfoEnabled()) log.info("Starting job: "+jobName);
        Job job = (Job) applicationContext.getBean(jobName);

        JobParametersBuilder jobParamBuilder = new JobParametersBuilder();
        jobParamBuilder.addString("indexingId", indexingId).toJobParameters();

        log.info("starting job " + indexingId);

        return jobLauncher.run(job, jobParamBuilder.toJobParameters());
    }

    public void setIndexingId(String indexingId) {
        this.indexingId = indexingId;
    }

    public String getIndexingId() {
        return indexingId;
    }
}
