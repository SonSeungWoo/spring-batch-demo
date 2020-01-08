package com.github.ssw.springbatchdemo;

import com.github.ssw.springbatchdemo.code.OrderStatus;
import com.github.ssw.springbatchdemo.order.Order;
import com.github.ssw.springbatchdemo.order.OrderRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

import static com.github.ssw.springbatchdemo.config.InactiveOrderJobConfig.JOB_NAME;

@EnableBatchProcessing
@EnableScheduling
@SpringBootApplication
public class SpringBatchDemoApplication {

    @Autowired
    private JobLauncher jobLauncher;

    @Qualifier("orderJob")
    @Autowired
    private Job orderJob;

    @Qualifier("inactiveOrderJob")
    @Autowired
    private Job inactiveOrderJob;

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchDemoApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner (OrderRepository orderRepository) {
        return args -> {
            for (long i = 0; i < 10; i++) {
                orderRepository.save(new Order(OrderStatus.REQUESTED));
            }
            List<Order> orders = orderRepository.findAll();
            System.out.println(orders);
        };
    }

    //@Scheduled(fixedDelay = 1000 * 60)
    public void perform() throws Exception {
        System.out.println("Start Scheduled");
        JobParameters params = new JobParametersBuilder()
                .addString(JOB_NAME, String.valueOf(System.currentTimeMillis()))
                .toJobParameters();
        jobLauncher.run(inactiveOrderJob, params);
        System.out.println("params : " + params);
    }

    @Scheduled(fixedDelay = 1000 * 30)
    public void perform2() throws Exception {
        System.out.println("Start Scheduled");
        JobParameters params = new JobParametersBuilder()
                .addString(JOB_NAME, String.valueOf(System.currentTimeMillis()))
                .toJobParameters();
        jobLauncher.run(orderJob, params);
        System.out.println("params : " + params);
    }
}
