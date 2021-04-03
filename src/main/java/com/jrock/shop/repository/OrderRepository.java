package com.jrock.shop.repository;

import com.jrock.shop.domain.Member;
import com.jrock.shop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    // 아래의 동적쿼리 둘다 쓰지 말고 queryDSL을 사용하도록 하자.
    public List<Order> findAllByString(OrderSearch orderSearch) {

        //language=JPAQL
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class).setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }

    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" +
                            orderSearch.getMemberName() + "%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건
        return query.getResultList();
    }

    /**
     * 엔티티를 페치 조인(fetch join)을 사용해서 쿼리 1번에 조회
     * 페치 조인으로 order -> member , order -> delivery 는 이미 조회 된 상태 이므로 지연로딩X
     */
    public List<Order> finAllWithMemberDelivery() {
        // LAZY 무시하고 각 객체에 값을 다 채워서 셀렉트 한다. (Fetch Join)
        return em.createQuery(
                "select o from Order o" +
                          " join fetch o.member m" +
                          " join fetch o.delivery d", Order.class
        ).getResultList();
    }

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
     */
    public List<OrderSimpleQueryDto> findOrderDtos() {
        // new operation 은 엔티티를 바로 넘기는 것이 안된다. (address 처럼 valuetype은 된다)
        return em.createQuery("select new com.jrock.shop.repository.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                " from Order o" +
                " join o.member m" +
                " join o.delivery d", OrderSimpleQueryDto.class)
                .getResultList();
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
    public List<Order> findAllWithItem() {

        return em.createQuery(
                "select distinct o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" +
                        " join fetch oi.item i", Order.class)
//                .setFirstResult(0)
//                .setMaxResults(100)
                .getResultList();
    }
}
