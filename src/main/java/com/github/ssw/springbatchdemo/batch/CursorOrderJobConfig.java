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
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.HibernateCursorItemReaderBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static com.github.ssw.springbatchdemo.batch.InactiveOrderJobConfig.JOB_NAME;

/**
 * 커서 job
 */
@Configuration
@ConditionalOnProperty(name = "job.name", havingValue = JOB_NAME, matchIfMissing = true)
public class CursorOrderJobConfig {

    private static final Logger logger = LoggerFactory.getLogger(CursorOrderJobConfig.class);

    private static final int CHUNK_SIZE = 10;

    public static final String JOB_NAME = "cursorOrderJob";

    private final EntityManagerFactory entityManagerFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final JobBuilderFactory jobBuilderFactory;

    private final DataSource dataSource;

    public CursorOrderJobConfig(EntityManagerFactory entityManagerFactory, StepBuilderFactory stepBuilderFactory,
                                JobBuilderFactory jobBuilderFactory, DataSource dataSource){
        this.entityManagerFactory = entityManagerFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
        this.dataSource = dataSource;
    }

    @Bean
    @Primary
    public Job orderJob() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(orderStep())
                .build();
    }

    @Bean
    @JobScope
    public Step orderStep() {
        return stepBuilderFactory.get("orderStep")
                .<Order, Order>chunk(CHUNK_SIZE)
                .reader(orderReader())
                .processor(orderProcessor())
                .writer(writer())
                .build();
    }

    /*@Bean
    @StepScope
    public JdbcCursorItemReader<Order> orderReader() {
        return new JdbcCursorItemReaderBuilder<Order>()
                .sql("SELECT * FROM tb_order o")
                .rowMapper(new BeanPropertyRowMapper<>(Order.class))
                .fetchSize(CHUNK_SIZE)
                .dataSource(dataSource)
                .name("orderReader")
                .build();
    }*/

    @Bean
    @StepScope
    public HibernateCursorItemReader orderReader() {
        logger.info("Reader Start");

        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);

        Map<String, Object> map = new HashMap<>();
        map.put("status", OrderStatus.REQUESTED);

        HibernateCursorItemReader<Order> builder = new HibernateCursorItemReaderBuilder<Order>()
                .name("orderReader")
                .sessionFactory(sessionFactory)
                .queryString("FROM Order o where o.status = :status")
                .parameterValues(map)
                .fetchSize(CHUNK_SIZE)
                .build();

        logger.info("Reader End");
        return builder;
    }

    @Bean
    @StepScope
    public ItemProcessor<Order, Order> orderProcessor() {
        return order -> {
            logger.info("Processor Start order : {}", order);
            order.updateStatus();
            logger.info("Processor End order : {}", order);
            return order;
        };
    }

    @Bean
    @StepScope
    public JpaItemWriter<Order> writer() {
        logger.info("Writer Start");

        JpaItemWriter<Order> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);

        logger.info("Writer End");
        return writer;
    }

}

