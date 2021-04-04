package com.jrock.shop.service;

import com.jrock.shop.domain.Member;
import com.jrock.shop.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Transactional : 트랜잭션, 영속성 컨텍스트
 *   - readOnly=true : 데이터의 변경이 없는 읽기 전용 메서드에 사용, 영속성 컨텍스트를 플러시 하지 않으므로 약간의 성능 향상(읽기 전용에는 다 적용)
 *   - 데이터베이스 드라이버가 지원하면 DB에서 성능 향상
 * @Autowired
 *   - 생성자 Injection 많이 사용, 생성자가 하나면 생략 가능
 */
@Service
/**
 * 클래스 레벨에 넣으면 아래의 public 메서드에 다 적용 된다.
 * 클래스 레벨에 주고 메서드에 다른 옵션을 주면 메서드는 해당 옵션에 따른 트랜잭션이 먹힌다.
 * 아래처럼 기본으로 readOnly를 클래스 레벨로 주고 쓰기에 @Transactional을 주는 것도 좋다.(Base readOnly=false) -> 읽기가 많을 떄. 쓰기가 많으면 반대.
 *
 * final 키워드를 추가하면 컴파일 시점에 memberRepository 를 설정하지 않는 오류를 체크할 수 있다. (보통 기본 생성자를 추가할 때 발견)
 *
 */
@Transactional(readOnly = true)
@RequiredArgsConstructor // final 필드만 생성자를 만들어 줌. lombok
public class MemberService {

    private final MemberRepository memberRepository;

    // 회원가입
    @Transactional
    public Long join(Member member) {
        validateDuplicateMember(member); // 중복 회원검증
        memberRepository.save(member);
        return member.getId(); // 영속성 컨텍스트에 존재 (테이블에 들어가지 않은 상태)
    }

    /**
     * 참고: 실무에서는 검증 로직이 있어도 멀티 쓰레드 상황을 고려해서 회원 테이블의 회원명 컬럼에 유니크 제 약 조건을 추가하는 것이 안전하다.
     */
    private void validateDuplicateMember(Member member) {

        // EXCEPTION
        List<Member> findMembers = memberRepository.findByName(member.getName());

        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    // 회원 전체 조회
//    @Transactional(readOnly = true)  // 읽기 전용 트랜잭션 이므로 디비에 부하가 덜 간다.(읽기에는 가급적으로 넣자)
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    // 한건 조회
//    @Transactional(readOnly = true)  // 읽기 전용 트랜잭션 이므로 디비에 부하가 덜 간다.(읽기에는 가급적으로 넣자)
    public Member findOne(Long id) {
//        return memberRepository.findOne(id);
        return memberRepository.findById(id).get(); // Spring Data Jpa
    }

    @Transactional
    public void update(Long id, String name) {
//        Member member = memberRepository.findOne(id);
        Member member = memberRepository.findById(id).get(); // Spring Data Jpa
        member.setName(name);
    }
}
