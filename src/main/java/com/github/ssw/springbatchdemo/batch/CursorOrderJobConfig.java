package com.github.ssw.springbatchdemo.batch;

import com.github.ssw.springbatchdemo.code.OrderStatus;
import com.github.ssw.springbatchdemo.order.Order;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.HibernateCursorItemReader;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.HibernateCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 커서 job
 */
@Configuration
@ConditionalOnProperty(name = "job.name", havingValue = CursorOrderJobConfig.JOB_NAME, matchIfMissing = true)
public class CursorOrderJobConfig {

    private static final Logger logger = LoggerFactory.getLogger(CursorOrderJobConfig.class);

    private static final int CHUNK_SIZE = 10;

    public static final String JOB_NAME = "cursorOrderJob";

    private final EntityManagerFactory entityManagerFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final JobBuilderFactory jobBuilderFactory;

    private final DataSource dataSource;

    public CursorOrderJobConfig(EntityManagerFactory entityManagerFactory, StepBuilderFactory stepBuilderFactory,
                                JobBuilderFactory jobBuilderFactory, DataSource dataSource) {
        this.entityManagerFactory = entityManagerFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
        this.dataSource = dataSource;
    }

    @Bean
    @Primary
    @Qualifier("cursorOrderJob")
    public Job cursorOrderJob() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(cursorOrderStep())
                .build();
    }

    @Bean
    @JobScope
    public Step cursorOrderStep() {
        return stepBuilderFactory.get("cursorOrderStep")
                .<Order, Order>chunk(CHUNK_SIZE)
                .faultTolerant()
                .retryLimit(3).retry(Exception.class)
                .reader(orderReader())
                .processor(cursorOrderProcessor())
                .writer(cursorOrderWriter())
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<Order> orderReader() {
        return new JdbcCursorItemReaderBuilder<Order>()
                .sql("SELECT * FROM tb_order o where o.status = ?")
                .queryArguments(OrderStatus.REQUESTED.getCode())
                .rowMapper(new BeanPropertyRowMapper<>(Order.class))
                .fetchSize(CHUNK_SIZE)
                .dataSource(dataSource)
                .name("orderReader")
                .build();
    }

    @Bean
    @StepScope
    public HibernateCursorItemReader cursorOrderReader() {
        logger.info("Cursor Order Reader Start");

        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);

        Map<String, Object> map = new HashMap<>();
        map.put("status", OrderStatus.REQUESTED);
        HibernateCursorItemReader<Order> builder = new HibernateCursorItemReaderBuilder<Order>()
                .name("cursorOrderReader")
                .sessionFactory(sessionFactory)

                .queryString("FROM Order o where o.status = :status")
                .parameterValues(map)
                .fetchSize(CHUNK_SIZE)
                .build();

        logger.info("Cursor Order Reader End");
        return builder;
    }

    @Bean
    @StepScope
    public ItemProcessor<Order, Order> cursorOrderProcessor() {
        return order -> {
            logger.info("Cursor Order Processor Start order : {}", order);
            order.updateStatus();
            logger.info("Cursor Order Processor End order : {}", order);
            return order;
        };
    }

    @Bean
    @StepScope
    public JpaItemWriter<Order> cursorOrderWriter() {
        logger.info("Cursor Order Writer Start");

        JpaItemWriter<Order> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);

        logger.info("Cursor Order Writer End");
        return writer;
    }

}

