package com.jrock.shop.api;

import com.jrock.shop.domain.Order;
import com.jrock.shop.repository.OrderRepository;
import com.jrock.shop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
