package com.jrock.shop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class Delivery {

    @Id
    @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    @OneToOne(mappedBy = "delivery") // 1:1 맵핑
    private Order order;

    @Embedded // 내장타입
    private Address address;

    @Enumerated(EnumType.STRING) // ORDINAL 안쓰면 기본(1,2,3,4 숫자로 들어감(인덱스), 중간에 다른 상태가 들어가면 망함 그래서 String 으로 사용)
    private DeliveryStatus status; //READY, COMP
}
