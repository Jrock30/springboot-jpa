package com.jrock.shop.service;

import com.jrock.shop.domain.Delivery;
import com.jrock.shop.domain.Member;
import com.jrock.shop.domain.Order;
import com.jrock.shop.domain.OrderItem;
import com.jrock.shop.domain.item.Item;
import com.jrock.shop.repository.ItemRepository;
import com.jrock.shop.repository.MemberRepository;
import com.jrock.shop.repository.OrderRepository;
import com.jrock.shop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 참고:
 *   주문 서비스의 주문과 주문 취소 메서드를 보면 비즈니스 로직 대부분이 엔티티에 있다.
 *   서비스 계층 은 단순히 엔티티에 필요한 요청을 위임하는 역할을 한다.
 *   이처럼 엔티티가 비즈니스 로직을 가지고 객체 지 향의 특성을 적극 활용하는 것을 도메인 모델 패턴(http://martinfowler.com/eaaCatalog/ domainModel.html)이라 한다. 반
 *   대로 엔티티에는 비즈니스 로직이 거의 없고 서비스 계층에서 대부분 의 비즈니스 로직을 처리하는 것을 트랜잭션 스크립트 패턴(http://martinfowler.com/eaaCatalog/ transactionScript.html)이라 한다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    /**
     * 주문
     * 주문 할 때 memberId, itemId, 수량을 받아옴
     */
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {

        // 엔티티조회
//        Member member = memberRepository.findOne(memberId);
        Member member = memberRepository.findById(memberId).get(); // Spring Data Jpa
        Item item = itemRepository.findOne(itemId);

        // 배송정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());

        // 주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        // 주문생성
        Order order = Order.createOrder(member, delivery, orderItem);

        // 주문 저장
        // CascadeType.ALL 을 해주었기 때문에 Order를 persist 하면 OrderItem, Delivery도 다 persist를 해주게 설계되어 있다.
        // 다른데서도 OrderItem, Delivery를 사용을 자주 한다면 CascadeType.ALL를 사용하지 말고 별도로 하는 것이 좋다. (현재는 오더만 씀)
        // 따로 쓰다가 익숙해지면 같이 쓰도록 하자.
        orderRepository.save(order);

        return order.getId();

    }

    /**
     *  주문 취소
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        // 주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);
        // 주문 취소  DDD
        order.cancel();

    }

    // 검색
    public List<Order> findOrders(OrderSearch orderSearch) {
        return orderRepository.findAllByString(orderSearch);
    }
}
