package com.github.ssw.springbatchdemo.order;

import com.github.ssw.springbatchdemo.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
