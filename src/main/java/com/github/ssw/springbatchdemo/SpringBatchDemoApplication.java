package com.github.ssw.springbatchdemo;

import com.github.ssw.springbatchdemo.code.OrderStatus;
import com.github.ssw.springbatchdemo.order.Order;
import com.github.ssw.springbatchdemo.order.OrderRepository;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@EnableBatchProcessing
@EnableScheduling
@SpringBootApplication
public class SpringBatchDemoApplication {

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
}
