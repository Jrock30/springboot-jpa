package com.jrock.shop;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

//

/**
 *  스프링에서 기본으로 지원해주는 타입
 *  컴포넌트 대상
 *
 */
@Repository
public class MemberRepository {

    // 엔티티 매니저를 주입시켜준다.(스프링부트가 해줌)
    @PersistenceContext
    private EntityManager em;

    // 저장
    public Long save(Member member) {
        em.persist(member);
        /**
         * 멤버 객체를 통으로 반환하지 않고 ID를 반환하는 스타일
         * 커맨드와 쿼리를 분리해라.
         * 저장을 하고 나면 가급적이면 분리, 커맨드성(쿼리)이기 떄문에 리턴값을 안 만듦)
         */
        return member.getId();
    }

    // 조회
    public Member find(Long id) {
        return em.find(Member.class, id);
    }
}
