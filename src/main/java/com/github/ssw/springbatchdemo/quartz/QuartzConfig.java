package com.github.ssw.springbatchdemo.quartz;

import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.HashMap;
import java.util.Map;

import static com.github.ssw.springbatchdemo.batch.CursorOrderJobConfig.JOB_NAME;

/**
 * Quartz 스케줄 설정
 */
@Configuration
public class QuartzConfig {

    private final JobLauncher jobLauncher;

    private final JobLocator jobLocator;

    public QuartzConfig(JobLauncher jobLauncher, JobLocator jobLocator){
        this.jobLauncher = jobLauncher;
        this.jobLocator = jobLocator;
    }

    /**
     * job 등록
     */
    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);

        return jobRegistryBeanPostProcessor;
    }

    /**
     * Batch job 테스크 연결 및 작업의 세부 속성
     */
    @Bean
    public JobDetailFactoryBean jobDetailFactoryBean() {
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(QuartzJobLauncher.class);
        Map<String, Object> map = new HashMap<>();
        map.put("jobName", JOB_NAME);
        map.put("jobLauncher", jobLauncher);
        map.put("jobLocator", jobLocator);

        jobDetailFactoryBean.setJobDataAsMap(map);

        return jobDetailFactoryBean;
    }

    /**
     *  트리거 설정
     */
    @Bean
    public CronTriggerFactoryBean cronTriggerFactoryBean() {
        CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
        cronTriggerFactoryBean.setJobDetail(jobDetailFactoryBean().getObject());
        //run every 3 hour
        cronTriggerFactoryBean.setCronExpression("0 0 0/3 * * ? *");
        //cronTriggerFactoryBean.setCronExpression("*/50 * * * * ? *");

        return cronTriggerFactoryBean;
    }

    /**
     * 작업 및 트리거 등록
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setTriggers(cronTriggerFactoryBean().getObject());

        return schedulerFactoryBean;
    }
}
