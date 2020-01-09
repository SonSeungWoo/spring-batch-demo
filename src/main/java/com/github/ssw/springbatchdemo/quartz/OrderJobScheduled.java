package com.github.ssw.springbatchdemo.quartz;

import com.github.ssw.springbatchdemo.batch.CursorOrderJobConfig;
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
public class OrderJobScheduled implements JobScheduled {

    private static final Logger logger = LoggerFactory.getLogger(OrderJobScheduled.class);

    private final JobLauncher jobLauncher;

    private final Job orderJob;

    public OrderJobScheduled(JobLauncher jobLauncher, @Qualifier("orderJob") Job orderJob){
        this.jobLauncher = jobLauncher;
        this.orderJob = orderJob;
    }

    @Scheduled(cron = "*/50 * * * * *")
    @Override
    public void execute() {
        logger.info("################### OrderJob Start Scheduled");
        JobParameters params = new JobParametersBuilder()
                .addString(CursorOrderJobConfig.JOB_NAME, String.valueOf(System.currentTimeMillis()))
                .toJobParameters();
        try {
            JobExecution jobExecution = jobLauncher.run(orderJob, params);
            logger.info("OrderJob Status: {}", jobExecution.getStatus());
        }catch (Exception ex){
            logger.error(ex.getMessage());
        }
    }
}
