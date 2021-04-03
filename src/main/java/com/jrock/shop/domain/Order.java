package com.jrock.shop.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "orders") // 관례로 order가 됨으로써 이름을 주자
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 생성자를 막음, JPA는 protected까지 지원, 이렇게하면 밖에서 생성자를 호출하는 것을 막는다고 보면 된다. 생성메서드를 통해 호출
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id") // FK 가 member_id 가 된다고 생각하자.
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL) // casecade ALL 을 등록하면 엔티티당 persist에 등록해줘야 하지만 persist를 전파 함으로써 따로 할 필요 없다. 지울 때도 같이 지워짐.
    private List<OrderItem> orderItems = new ArrayList<>();

    /**`
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

    /**
     * 정적 팩토리 메서드(static factory method) static 메서드로 객체 생성을 캡슐화한다
     *  - 객체 생성을 캡슐화하는 기법이다.
     *  - 좀 더 구체적으로는 객체를 생성하는 메소드를 만들고, static으로 선언하는 기법이다.
     *  - 자바로 코딩할 때 흔하게 볼 수 있는 valueOf 메서드가 정적 팩토리 메서드의 한 예라 할 수 있다.
     *    - BigInteger answer = BigInteger.valueOf(42L); // BigInteger 42를 리턴한다
     *    - static으로 선언된 메서드이며, new BigInteger(...)를 은닉하고 있다는 사실을 알 수 있다.
     *    - valueOf, of, getInstance, newInstance, getType, newType
     *  - 장점
     *    - 이름이 있으므로 생성자에 비해 가독성이 좋다.
     *    - 호출할 때마다 새로운 객체를 생성할 필요가 없다.
     *    - 하위 자료형 객체를 반환할 수 있다.
     *    - 형인자 자료형(parameterized type) 객체를 만들 때 편하다.
     *  - 단점
     *    - 정적 팩토리 메서드만 있는 클래스라면, 생성자가 없으므로 하위 클래스를 못 만든다.
     *    - 정적 팩토리 메서드는 다른 정적 메서드와 잘 구분되지 않는다. (문서만으로 확인하기 어려울 수 있음)
     */

    /**
     * 객체지향에서는 객체의 데이터를 가지고 있는 곳에서 해당 데이터를 조작하는 기능을 제공하는 것이 좋습니다.(항상 그런 것은 아닙니다.)
     * JPA를 사용하게 되면 단순히 데이터를 다루는 것을 넘어서 객체를 객체답게 다룰 수 있다는 것이 큰 강점입니다.
     *
     * 밖에서 일일히 set을 하는게 아니라 생성 메서드, 복잡한 생성은 별도의 생성 메서드를 두는게 좋다. (응집해 놓음)
     * 생성하는 지점을 변경하면 이 메소드만 바꾸면 됨.

     */
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);

//        for (OrderItem orderItem : orderItems) {
//            order.addOrderItem(orderItem);
//        }

        // 리팩토링
        Arrays.stream(orderItems).forEach(order::addOrderItem);

        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());

        return order;
    }

    // 비즈니스 로직
    /**
     * 주문 취소
     */
    public void cancel() {
        if (delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }

        this.setStatus(OrderStatus.CANCEL);

//        for (OrderItem orderItem : this.orderItems) { // this. 는 강조할 때만 쓰는. 스타일임
//            // 주문 아이템이 여러개 일 수 있으니 각 각 만들어준다.
//            orderItem.cancel();
//        }

        // 리팩토링
        // this. 는 강조할 때만 쓰는. 스타일임
        // 주문 아이템이 여러개 일 수 있으니 각 각 만들어준다.
        orderItems.forEach(OrderItem::cancel);
    }

    // 조회 로직
    /**
     * 전체 주문 가격 조회
     */
    public int getTotalPrice() {

//        int totalPrice = 0;
//        for (OrderItem orderItem : orderItems) {
//            totalPrice += orderItem.getTotalPrice();
//        }
//        return totalPrice;

        // 리팩토링
        return orderItems.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();
    }
}
