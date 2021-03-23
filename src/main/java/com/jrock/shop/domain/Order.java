package com.jrock.shop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders") // 관례로 order가 됨으로써 이름을 주자
@Getter @Setter
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id") // FK 가 member_id 가 된다고 생각하자.
    private Member member;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems = new ArrayList<>();

    /**
     * 1:1 일 떄는 자주 사용 되는 곳에 FK를 두는 것이 좋다.
     * 예를 들어 주문테이블과 배송 테이블이 있을 경우 주문 테이블을 통해 배송의 데이터를 찾듯이.
     */
    @OneToOne
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    // Date를 쓰면 @ 달아서 셋팅을 해줘야 했으나 LocalDateTime 은 하이버네이트가 자동으로 셋팅 해줌.
    private LocalDateTime orderDate; // 주문시간

    @Enumerated(EnumType.STRING) // ORDINAL 안쓰면 기본(1,2,3,4 숫자로 들어감(인덱스), 중간에 다른 상태가 들어가면 망함 그래서 String 으로 사용)
    private OrderStatus status; // 주문상태 (Order, Cencel)


}
