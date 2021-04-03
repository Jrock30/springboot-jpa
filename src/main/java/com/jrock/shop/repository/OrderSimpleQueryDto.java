package com.jrock.shop.repository;

import com.jrock.shop.domain.Address;
import com.jrock.shop.domain.Order;
import com.jrock.shop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderSimpleQueryDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;


    // DTO 를 하나의 생성자로 만든 것은 중요하지 않다. (크게 문제가 되지 않음)
    public OrderSimpleQueryDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
    }
}
