package com.github.ssw.springbatchdemo.quartz;

import com.github.ssw.springbatchdemo.SpringBatchDemoApplication;
import com.github.ssw.springbatchdemo.batch.CursorOrderJobConfig;
import com.github.ssw.springbatchdemo.batch.InactiveOrderJobConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class JobScheduled {

    private static final Logger logger = LoggerFactory.getLogger(SpringBatchDemoApplication.class);

    private final JobLauncher jobLauncher;

    @Qualifier("orderJob")
    private final Job orderJob;

    @Qualifier("inactiveOrderJob")
    private final Job inactiveOrderJob;

    public JobScheduled(JobLauncher jobLauncher, Job orderJob, Job inactiveOrderJob){
        this.jobLauncher = jobLauncher;
        this.orderJob = orderJob;
        this.inactiveOrderJob = inactiveOrderJob;
    }

    //@Scheduled(fixedDelay = 1000 * 30)
    public void executeInactiveOrderJob() {
        System.out.println("InactiveOrderJob Start Scheduled");
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

    //@Scheduled(fixedDelay = 1000 * 60)
    public void executeOrderJob() {
        System.out.println("OrderJob Start Scheduled");
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
