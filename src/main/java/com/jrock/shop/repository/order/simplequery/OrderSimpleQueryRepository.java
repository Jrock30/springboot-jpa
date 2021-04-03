package com.jrock.shop.repository.order.simplequery;

import com.jrock.shop.repository.OrderSimpleQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.swing.text.html.parser.Entity;
import java.util.List;

/**
 * 일반적인 SQL을 사용할 때 처럼 원하는 값을 선택해서 조회 new 명령어를 사용해서 JPQL의 결과를 DTO로 즉시 변환
 * SELECT 절에서 원하는 데이터를 직접 선택하므로 DB 애플리케이션 네트웍 용량 최적화(생각보다 미비)
 * 리포지토리 재사용성 떨어짐, API 스펙에 맞춘 코드가 리포지토리에 들어가는 단점
 *
 * 엔티티를 DTO로 변환하거나, DTO로 바로 조회하는 두가지 방법은 각각 장단점이 있다.
 * 둘중 상황에 따라서 더 나은 방법을 선택하면 된다.
 * 엔티티로 조회하면 리포지토리 재사용성도 좋고, 개발도 단순해진다.
 * 따라서 권장하는 방법은 다음과 같다.
 *
 * 쿼리 방식 선택 권장 순서
 * 1. 우선 엔티티를 DTO로 변환하는 방법을 선택한다. v2
 * 2. 필요하면 페치 조인으로 성능을 최적화 한다. 대부분의 성능 이슈가 해결된다. v3
 * 3. 그래도 안되면 DTO로 직접 조회하는 방법을 사용한다. v4
 * 4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접
 *
 * 조회 전용으로 화면에 딱 전용으로 쓰는구나 와 같은 경우는 따로 패키지를 빼서 분리하기도 한다.
 */
@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    private final EntityManager em;

    public List<OrderSimpleQueryDto> findOrderDtos() {
        // new operation 은 엔티티를 바로 넘기는 것이 안된다. (address 처럼 valuetype은 된다)
        return em.createQuery("select new com.jrock.shop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                " from Order o" +
                " join o.member m" +
                " join o.delivery d", OrderSimpleQueryDto.class)
                .getResultList();
    }
}
