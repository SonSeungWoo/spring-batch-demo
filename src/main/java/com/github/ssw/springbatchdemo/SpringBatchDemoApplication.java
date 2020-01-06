package com.github.ssw.springbatchdemo;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@EnableBatchProcessing
@SpringBootApplication
public class SpringBatchDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchDemoApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner (OrderRepository orderRepository) {
        return args -> {
            orderRepository.save(new Order(OrderStatus.COMPLETED));
            List<Order> orders = orderRepository.findAll();
            System.out.println(orders);
        };
    }
}
