package com.github.ssw.springbatchdemo.quartz;

import com.github.ssw.springbatchdemo.batch.InactiveOrderJobConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class InactiveOrderJobScheduled implements JobScheduled {

    private static final Logger logger = LoggerFactory.getLogger(InactiveOrderJobScheduled.class);

    private final JobLauncher jobLauncher;

    private final Job inactiveOrderJob;

    public InactiveOrderJobScheduled(JobLauncher jobLauncher, @Qualifier("inactiveOrderJob") Job inactiveOrderJob){
        this.jobLauncher = jobLauncher;
        this.inactiveOrderJob = inactiveOrderJob;
    }

    @Scheduled(cron = "*/30 * * * * *")
    @Override
    public void execute() {
        logger.info("################### InactiveOrderJob Start Scheduled");
        JobParameters params = new JobParametersBuilder()
                .addString(InactiveOrderJobConfig.JOB_NAME, String.valueOf(System.currentTimeMillis()))
                .toJobParameters();
        try {
            JobExecution jobExecution = jobLauncher.run(inactiveOrderJob, params);
            logger.info("InactiveOrderJob Status: {}", jobExecution.getStatus());
        }catch (Exception ex){
            logger.error(ex.getMessage());
        }
    }
}
