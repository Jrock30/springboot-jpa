package com.jrock.shop.repository;

import com.jrock.shop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * @Repository : 스프링 빈으로 등록, JPA 예외를 스프링 기반 예외로 예외 변환
 * @PersistenceContext : 엔티티 메니저( EntityManager ) 주입
 * @PersistenceUnit : 엔티티 메니터 팩토리( EntityManagerFactory ) 주입
 */
@Repository
@RequiredArgsConstructor
public class MemberRepository {

//    @PersistenceContext // JPA 표준을 지원하는 엔티티 매니저 @ 주입
//    private EntityManager em;
    private final EntityManager em; // 생성자 주입

    public void save(Member member) {
        em.persist(member);
    }

    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }

}
