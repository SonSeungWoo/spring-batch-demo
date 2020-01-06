package com.github.ssw.springbatchdemo;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;


@Configuration
public class InactiveOrderJobConfig {

    private static final Logger logger = LoggerFactory.getLogger(InactiveOrderJobConfig.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private static final int CHUNK_SIZE = 1000;

    /**
     * jop 설정
     *
     * @param jobBuilderFactory
     * @param inactiveJobStep
     * @return
     */
    @Bean
    public Job inactiveOrderJob(JobBuilderFactory jobBuilderFactory, Step inactiveJobStep) {
        return jobBuilderFactory.get("inactiveOrderJob")
                .preventRestart() //(2)
                .start(inactiveJobStep) //(3)
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
                .<Order, Order>chunk(10)
                .faultTolerant()
                .retryLimit(3).retry(Exception.class)
                .reader(inactiveOrderJpaReader())
                .processor(inactiveOrderProcessor())
                .writer(inactiveOrderJpaWriter())
                .build();
    }

    /**
     * Reader(읽기) 설정
     *
     * @return
     */
    @Bean
    @StepScope
    public JpaPagingItemReader<Order> inactiveOrderJpaReader() {
        logger.info("JpaPaging Reader Start");
        JpaPagingItemReader<Order> jpaPagingItemReader = new JpaPagingItemReader<>();
        jpaPagingItemReader.setQueryString("select o from Order as o where o.status = :status");

        Map<String, Object> map = new HashMap<>();
        map.put("status", OrderStatus.REQUESTED);

        jpaPagingItemReader.setParameterValues(map);
        jpaPagingItemReader.setEntityManagerFactory(entityManagerFactory);
        jpaPagingItemReader.setPageSize(CHUNK_SIZE);
        return jpaPagingItemReader;
    }

    /*@Bean
    @StepScope
    public QueueItemReader<Order> inactiveOrderReader() {
        logger.info("Reader Start");
        List<Order> oldUsers =
                orderRepository.findAll();
        return new QueueItemReader<>(oldUsers);
    }*/

    /**
     * Processor(처리) 설정
     *
     * @return
     */
    public ItemProcessor<Order, Order> inactiveOrderProcessor() {
        logger.info("Processor Start");
        return order -> order.updateStatus();
    }

    /**
     * Write(쓰기) 설정
     *
     * @return
     */
    private JpaItemWriter<Order> inactiveOrderJpaWriter() {
        logger.info("Jpa Writer Start");
        JpaItemWriter<Order> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }

    /*public ItemWriter<Order> inactiveOrderWriter() {
        logger.info("Writer Start");
        return ((List<? extends Order> orders) -> orderRepository.saveAll(orders));
    }*/

}
