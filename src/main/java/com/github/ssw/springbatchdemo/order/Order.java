package com.github.ssw.springbatchdemo.order;

import com.github.ssw.springbatchdemo.code.OrderStatus;

import javax.persistence.*;

@Table(name = "TB_ORDER")
@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long no;

    private OrderStatus status;

    public Order(){}

    public Order(OrderStatus status) {
        this.status = status;
    }

    public Long getNo() {
        return no;
    }

    public void setNo(Long no) {
        this.no = no;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Order updateStatus() {
        this.status = OrderStatus.COMPLETED;
        return this;
    }
}
