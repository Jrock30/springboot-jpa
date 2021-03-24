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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id") // FK 가 member_id 가 된다고 생각하자.
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL) // casecade ALL 을 등록하면 엔티티당 persist에 등록해줘야 하지만 persist를 전파 함으로써 따로 할 필요 없다. 지울 때도 같이 지워짐.
    private List<OrderItem> orderItems = new ArrayList<>();

    /**
     * 1:1 일 떄는 자주 사용 되는 곳에 FK를 두는 것이 좋다.
     * 예를 들어 주문테이블과 배송 테이블이 있을 경우 주문 테이블을 통해 배송의 데이터를 찾듯이.
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL) // 오더를 저장할 때 delivery 도 persist 됨. casecade를 주지 않으면 각자 persist 해줘야함.
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    /**
     * 스프링 부트 신규 설정 (엔티티(필드) 테이블(컬럼))
     * 1. 카멜 케이스 언더스코어(memberPoint member_point)
     * 2. .(점) _(언더스코어)
     * 3. 대문자 소문자
     */
    // Date를 쓰면 @ 달아서 셋팅을 해줘야 했으나 LocalDateTime 은 하이버네이트가 자동으로 셋팅 해줌.
    private LocalDateTime orderDate; // 주문시간, 이 것은 컬럼명이 order_date로 바뀐다.

    @Enumerated(EnumType.STRING) // ORDINAL 안쓰면 기본(1,2,3,4 숫자로 들어감(인덱스), 중간에 다른 상태가 들어가면 망함 그래서 String 으로 사용)
    private OrderStatus status; // 주문상태 (Order, Cencel)

    // 연관관계 메서드
    private void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    // 연관관계 메서드
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }
}
