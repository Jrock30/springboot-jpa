package com.jrock.shop;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class) // JUnit에게 스프링러너로 테스트를 할 것을 알려준다.
@SpringBootTest
public class MemberRepositoryTest {



    @Test
    /**
     * 모든 엔티티의 모든 데이터 변경은 트랜잭션 안에서 이루어져야한다. 하지만 없어서 에러 발생(레파지토리에 걸어도 됨)
     * @Transactional 가 테스트에 있으면 DB가 롤백한다.
     */
    @Transactional
    @Rollback(value = false) // 위의 어노테이션의 기본 롤백을 사용안하려면 false로 주면 값이 남는다.
    public void testMember() throws Exception {

    }
}