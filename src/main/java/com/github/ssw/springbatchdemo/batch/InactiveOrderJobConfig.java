package com.github.ssw.springbatchdemo.batch;

import com.github.ssw.springbatchdemo.code.OrderStatus;
import com.github.ssw.springbatchdemo.order.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * 페이징 jop
 */
@Configuration
@ConditionalOnProperty(name = "job.name", havingValue = InactiveOrderJobConfig.JOB_NAME, matchIfMissing = true)
public class InactiveOrderJobConfig {

    private static final Logger logger = LoggerFactory.getLogger(InactiveOrderJobConfig.class);

    private static final int CHUNK_SIZE = 20;

    public static final String JOB_NAME = "inactiveOrderJob";

    private final EntityManagerFactory entityManagerFactory;

    public InactiveOrderJobConfig(EntityManagerFactory entityManagerFactory){
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * jop 설정
     *
     * @param jobBuilderFactory
     * @param inactiveJobStep
     * @return
     */
    @Bean
    @Qualifier("inactiveOrderJob")
    public Job inactiveOrderJob(JobBuilderFactory jobBuilderFactory, Step inactiveJobStep) {
        return jobBuilderFactory.get(JOB_NAME)
                .preventRestart()
                .start(inactiveJobStep)
                .build();
    }

    /**
     * step 설정
     *
     * @param stepBuilderFactory
     * @return
     */
    @Bean
    public Step inactiveJobStep(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory.get("inactiveOrderStep")
                .<Order, Order>chunk(CHUNK_SIZE)
                .faultTolerant()
                .retryLimit(3).retry(Exception.class)
                .reader(inactiveOrderReader())
                .processor(inactiveOrderProcessor())
                .writer(inactiveOrderWriter())
                .build();
    }

    /**
     * Reader(읽기) 설정
     *
     * @return
     */
    @Bean
    @StepScope
    public JpaPagingItemReader<Order> inactiveOrderReader() {
        logger.info("Paging Reader Start");

        JpaPagingItemReader<Order> jpaPagingItemReader = new JpaPagingItemReader<Order>() {
            @Override
            public int getPage() {
                return 0;
            }
        };

        jpaPagingItemReader.setQueryString("select o from Order o where o.status = :status");

        Map<String, Object> map = new HashMap<>();
        map.put("status", OrderStatus.CANCELED);

        jpaPagingItemReader.setParameterValues(map);
        jpaPagingItemReader.setEntityManagerFactory(entityManagerFactory);
        jpaPagingItemReader.setPageSize(CHUNK_SIZE);

        logger.info("Paging Reader End");
        return jpaPagingItemReader;
    }

    /**
     * Processor(처리) 설정
     *
     * @return
     */
    public ItemProcessor<Order, Order> inactiveOrderProcessor() {
        logger.info("Paging Processor Start");
        return order -> order.updateStatus();
    }

    /**
     * Write(쓰기) 설정
     *
     * @return
     */
    private JpaItemWriter<Order> inactiveOrderWriter() {
        logger.info("Paging Writer Start");
        JpaItemWriter<Order> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }

    @Bean
    public JobLauncherTestUtils jobLauncherTestUtils() {
        return new JobLauncherTestUtils();
    }
}
