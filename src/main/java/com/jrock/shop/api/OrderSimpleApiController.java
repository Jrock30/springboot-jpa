package com.jrock.shop.api;

import com.jrock.shop.domain.Address;
import com.jrock.shop.domain.Order;
import com.jrock.shop.domain.OrderStatus;
import com.jrock.shop.repository.OrderRepository;
import com.jrock.shop.repository.OrderSearch;
import com.jrock.shop.repository.OrderSimpleQueryDto;
import com.jrock.shop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * xToOne(ManyToOne, OneToOne)
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {

        /* 빈 객체를 주면 모든 것을 다 들고온다. */
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        /**
         * 참고: 앞에서 계속 강조했듯이 정말 간단한 애플리케이션이 아니면 엔티티를 API 응답으로 외부로 노출하는 것은 좋지 않다.
         * 따라서 Hibernate5Module 를 사용하기 보다는 DTO로 변환해서 반환하는 것이 더 좋은 방법이다.
         *
         * 주의: 지연 로딩(LAZY)을 피하기 위해 즉시 로딩(EARGR)으로 설정하면 안된다!
         *      즉시 로딩 때문에 연관관계가 필요 없는 경우에도 데이터를 항상 조회해서 성능 문제가 발생할 수 있다.
         *      즉시 로딩으로 설정하면 성능 튜닝이 매우 어려워 진다.
         *      항상 지연 로딩을 기본으로 하고,
         *      성능 최적화가 필요한 경우에는 페치 조인(fetch join)을 사용해라!(V3 에서 설명)
         */
        // 강제 지연로딩(LazyLoading) 선택 ( 빈에서 하는 것이 아니고 여기서 함 )
        for (Order order : all) {
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getAddress(); // Lazy 강제 초기화
        }
        return all;
    }

    /**
     * 엔티티를 DTO로 변환하는 일반적인 방법이다.
     * 쿼리가 총 1 + N + N번 실행된다. (v1과 쿼리수 결과는 같다.)
     *   - order 조회 1번(order 조회 결과 수가 N이 된다.)
     *   - order -> member 지연 로딩 조회 N 번
     *   - order -> delivery 지연 로딩 조회 N 번
     *   - 예) order의 결과가 4개면 최악의 경우 1 + 4 + 4번 실행된다.(최악의 경우)
     *     - 지연로딩은 영속성 컨텍스트에서 조회하므로, 이미 조회된 경우 쿼리를 생략한다.
     */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() { // List로 리턴하지 말고 resultMap으로 감싸서 리턴할 것.

        // order 2개 조회
        // 1 + N(2) -> 1 + 회원 N + 배송 N
//        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
//
        // 2개 루프 돌면서 멤버와 딜리버리 2개 조회 그래서 쿼리가 2번 날라감 (member, delivery)
//        List<SimpleOrderDto> result = orders.stream()
//                .map(o -> new SimpleOrderDto(o))
//                .collect(Collectors.toList());
//
//        return result;

        // 리팩토링
        return orderRepository.findAllByString(new OrderSearch()).stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(toList());

    }

    /**
     * 엔티티를 페치 조인(fetch join)을 사용해서 쿼리 1번에 조회
     * 페치 조인으로 order -> member , order -> delivery 는 이미 조회 된 상태 이므로 지연로딩X
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3() {

//        List<Order> orders = orderRepository.finAllWithMemberDelivery();
//
//        List<SimpleOrderDto> result = orders.stream()
//                .map(o -> new SimpleOrderDto(o))
//                .collect(toList());
//
//        return result;

        // 리팩토링
        return orderRepository.finAllWithMemberDelivery().stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(toList());
    }

    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }

    // 밖에서 RequestDto 처럼 요청하는 Dto는 private을 제외하고 static을 넣어주어야 InnerClass를 불러 올 수 있다.
    // 클래스 내부에서만 사용한다면 private으로 사용해도 된다.
    @Data
    private class SimpleOrderDto {
        //    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;


        // DTO 를 하나의 생성자로 만든 것은 중요하지 않다. (크게 문제가 되지 않음)
        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // LAZE 초기화 루프만큼 쿼리 날림(영속성 컨텍스트에 있으면 그대로 가져와서(캐쉬) 쿼리 날리지 않긴 함)
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZE 초기화 루프만큼 쿼리 날림(영속성 컨텍스트에 있으면 그대로 가져와서(캐쉬) 쿼리 날리지 않긴 함)
        }
    }
}
