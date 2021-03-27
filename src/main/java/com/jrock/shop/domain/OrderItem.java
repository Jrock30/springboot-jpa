package com.jrock.shop.domain;

import com.jrock.shop.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 생성자를 막음, JPA는 protected까지 지원, 이렇게하면 밖에서 생성자를 호출하는 것을 막는다고 보면 된다. 생성메서드를 통해 호출
public class OrderItem {

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice; // 주문가격
    private int count; // 주문수량

    // 생성 메서드
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        // 쿠폰 값 이라던지 바뀔 수 있기 떄문에
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        // 주문 만큼 재고를 까준다.
        item.removeStock(count);
        return orderItem;
    }

    // 비즈니스 로직
    public void cancel() {
        // 재고 수량을 원복
        getItem().addStock(count);

    }

    /**
     * 주문상품 전체 가격 조회
     */
    // 조회 로직
    public int getTotalPrice() {
        return getOrderPrice() * getCount();
    }
}
