package com.github.ssw.springbatchdemo.quartz;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * 스케줄 job 실행
 */
public class QuartzJobLauncher extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(QuartzJobLauncher.class);

    private String jobName;
    private JobLauncher jobLauncher;
    private JobLocator jobLocator;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public JobLauncher getJobLauncher() {
        return jobLauncher;
    }

    public void setJobLauncher(JobLauncher jobLauncher) {
        this.jobLauncher = jobLauncher;
    }

    public JobLocator getJobLocator() {
        return jobLocator;
    }

    public void setJobLocator(JobLocator jobLocator) {
        this.jobLocator = jobLocator;
    }

    /**
     * job 실행
     */
    @Override
    protected void executeInternal(JobExecutionContext arg) {
        logger.info("########### QuartzJobLauncher Start ###########");

        JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();

        try {
            Job job = jobLocator.getJob(jobName);
            JobExecution jobExecution = jobLauncher.run(job, jobParameters);

            logger.info("Status: {}", jobExecution.getStatus());

        } catch(JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
                | JobParametersInvalidException | NoSuchJobException e) {
            e.printStackTrace();
        }

        logger.info("########### QuartzJobLauncher End ###########");
    }

}
