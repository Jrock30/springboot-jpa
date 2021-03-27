package com.jrock.shop.service;

import com.jrock.shop.domain.Member;
import com.jrock.shop.repository.MemberRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

/**
 * 현재 사용은 Junit4
 *
 * Junit5
 *   - @SpringBootTest에 @RunWith(SpringRunner.class)가 포함되어 있으므로 생략 가능.
 *   - 예외처리
 *     - @Test(expected) 제거
 *     - IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> memberService.join(memberB));
 * 		 assertEquals("이미 존재하는 회원입니다.", thrown.getMessage());
 * 	   - 위와 같이 assertThrows 를 사용하거나, 직접 예외처리.
 */
// 스프링을 integration 해서 테스트
@RunWith(SpringRunner.class) // Junit 실행할 때 스프링이랑 같이 연동해서 사용할 떄.
@SpringBootTest // 스프링부트를 띄운 상태에서 사용할 떄
@Transactional // 테스트에서 사용할 떄는 테스트 끝나고 다 롤백함.
public class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager em;

    @Test
    @Rollback(value = false)
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("kim");

        //when
        Long saveId = memberService.join(member);

        //then
        /**
         * 영속성 컨텍스트에 같은 객체가 나올 것 임.
         * persist 는 영속성 컨텍스트에 올라가는 것이지 insert 되는 것은 아니다.
         * flush, commit 할 떄 insert 함.
         *
         * 스프링TEST는 기본적으로 @Transactional 을 롤백을 해버린다. , 그래서 JPA 입장에서는 insert 자체를 날리지를 않음. 그래도 롤백은 하니 @Rollback(false) 를 준다.
         * 아니면 아래의 EntityManger 를 불러서 직접 flush 를 해주면 insert를 확인한다. ( 그래도 롤백은 됨 )
         *
         */
//        em.flush();

        assertEquals(member, memberRepository.findOne(saveId));

    }

    @Test(expected = IllegalStateException.class) // expected = IllegalStateException.class -> 해당 Exception 이 발생하면 아래의 예외처리 안해도 됨.
    public void 중복_회원_예약() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("kim1");

        Member member2 = new Member();
        member2.setName("kim1");

        //when
        memberService.join(member1);
        memberService.join(member2); // 에외가 발생해야 한다.
//        try {
//            memberService.join(member2); // 에외가 발생해야 한다.
//
//        } catch (IllegalStateException e) {
//            return;
//        }

        //then
        fail("예외가 발생해야 한다.");
    }

}