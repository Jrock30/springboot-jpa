package com.jrock.shop.api;

import com.jrock.shop.domain.Address;
import com.jrock.shop.domain.Order;
import com.jrock.shop.domain.OrderItem;
import com.jrock.shop.domain.OrderStatus;
import com.jrock.shop.repository.OrderRepository;
import com.jrock.shop.repository.OrderSearch;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * V1. 엔티티 직접 노출
 *   - 엔티티가 변하면 API 스펙이 변한다.
 *   - 트랜잭션 안에서 지연 로딩 필요
 *   - 양방향 연관관계 문제 *
 * V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X)
 *   - 트랜잭션 안에서 지연 로딩 필요
 * V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
 *   - 페이징 시에는 N 부분을 포기해야함(대신에 batch fetch size? 옵션 주면 N -> 1 쿼리로 변경가능) *
 * V4.JPA에서 DTO로 바로 조회, 컬렉션 N 조회 (1+NQuery)
 *   - 페이징 가능
 * V5.JPA에서 DTO로 바로 조회, 컬렉션 1 조회 최적화 버전 (1+1Query)
 *   - 페이징 가능
 * V6. JPA에서 DTO로 바로 조회, 플랫 데이터(1Query) (1 Query)
 *   - 페이징 불가능... *
 */
@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    /**
     * V1. 엔티티 직접 노출
     * - Hibernate5Module 모듈 등록, LAZY=null 처리 * - 양방향 관계 문제 발생 -> @JsonIgnore
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        for (Order order : all) {
            order.getMember().getName(); // Laze 강제 초기화
            order.getDelivery().getAddress(); // Laze 강제 초기화
            List<OrderItem> orderItems = order.getOrderItems();

//            for (OrderItem orderItem : orderItems) {
//                orderItem.getItem().getName(); // Laze 강제 초기화
//            }

            // 리팩토링
            orderItems.stream().forEach((o -> o.getItem().getName())); // Laze 강제 초기화
        }
        return all;
    }

    /**
     * V2. 엔티티를 DTO로 변환
     *
     * 지연 로딩으로 너무 많은 SQL 실행 SQL 실행 수
     * order 1번
     * member , address N번(order 조회 수 만큼) orderItem N번(order 조회 수 만큼)
     * item N번(orderItem 조회 수 만큼)
     *
     * 참고: 지연 로딩은 영속성 컨텍스트에 있으면 영속성 컨텍스트에 있는 엔티티를 사용하고 없으면 SQL을 실행한다.
     * 따라서 같은 영속성 컨텍스트에서 이미 로딩한 회원 엔티티를 추가로 조회하면 SQL을 실행하지 않는다.
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        List<OrderDto> collect = orders.stream()
                .map(OrderDto::new)
                .collect(toList());
//                .map(o -> new OrderDto(o))
//                .collect(Collectors.toList());

        return collect;
    }

    /**
     * 페치 조인으로 SQL이 1번만 실행됨
     * distinct 를 사용한 이유는 1대다 조인이 있으므로 데이터베이스 row가 증가한다.
     * 그 결과 같은 order 엔티티의 조회 수도 증가하게 된다.
     * JPA의 distinct는 SQL에 distinct를 추가하고, 더해서 같은 엔티티가 조회되면, 애플리케이션에서 중복을 걸러준다.
     * 이 예에서 order가 컬렉션 페치 조인 때문에 중복 조회 되는 것을 막아준다.
     * 단점 - 페이징 불가능
     *
     * 참고:
     *   - 컬렉션 페치 조인을 사용하면 페이징이 불가능하다.
     *     하이버네이트는 경고 로그를 남기면서 모든 데이터를 DB에서 읽어오고, 메모리에서 페이징 해버린다(매우 위험하다).
     *     자세한 내용은 자바 ORM 표준 JPA 프로그래밍의 페치 조인 부분을 참고하자.
     *
     *   - 컬렉션 페치 조인은 1개만 사용할 수 있다.
     *     컬렉션 둘 이상에 페치 조인을 사용하면 안된다.
     *     데이터가 부정합하게 조회될 수 있다.
     *     자세한 내용은 자바 ORM 표준 JPA 프로그래밍을 참고하자.
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return result;
    }

    @Data
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address; // Value Object 는 그대로 노출(반환) 해도 된다.
        private List<OrderItemDto> orderItems; // OrderItem 또한 Entity 이므로 이 또한 DTO로 따로 만들어 주어야한다. 따로 랩핑

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .collect(toList());
//                    .map(orderItem -> new OrderItemDto(orderItem))
    //                .collect(Collectors.toList());
        }
    }

    @Getter
    static class OrderItemDto {
        private String itemName; // 상품명
        private int orderPrice; // 주문가격
        private int count; // 주문수량

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getItem().getPrice();
            count = orderItem.getCount();
        }
    }
}
