package com.github.ssw.springbatchdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderController {
    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/order")
    public ResponseEntity<List<Order>> getOrder(){
        return ResponseEntity.ok(orderRepository.findAll());
    }
}
