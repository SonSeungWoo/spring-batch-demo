package com.github.ssw.springbatchdemo.order;

import com.github.ssw.springbatchdemo.code.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {
    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/order")
    public ResponseEntity<List<Order>> getOrder(){
        return ResponseEntity.ok(orderRepository.findAll());
    }

    @PostMapping(value = "/order", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity saveOrder(){
        orderRepository.save(new Order(OrderStatus.REQUESTED));
        orderRepository.save(new Order(OrderStatus.CANCELED));
        return ResponseEntity.ok("SUCCESS");
    }
}
