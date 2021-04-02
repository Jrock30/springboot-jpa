package com.jrock.shop;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);

    }

    /**
     * 엔티티를 직접 노출하는 것은 좋지 않다. ( 엔티티 발생할 경우에 추가, 엔티티를 직접 리턴하지말 것 )
     * order member 와 order address 는 지연 로딩이다. 따라서 실제 엔티티 대신에 프록시 존재
     * jackson 라이브러리는 기본적으로 이 프록시 객체를 json으로 어떻게 생성해야 하는지 모름 -> 예외발생
     * Hibernate5Module 을 스프링 빈으로 등록하면 해결(스프링 부트 사용중)
     * 지연로딩 무시
     *
     * 참고: 앞에서 계속 강조했듯이 정말 간단한 애플리케이션이 아니면 엔티티를 API 응답으로 외부로 노출하는 것은 좋지 않다.
     * 따라서 Hibernate5Module 를 사용하기 보다는 DTO로 변환해서 반환하는 것이 더 좋은 방법이다.
     */
    @Bean
    Hibernate5Module hibernate5Module() {
        // 지연로딩 하지 않고 조회
//        return new Hibernate5Module();
        // 다음과 같이 설정하면 강제로 지연 로딩 가능 ( 사실 아래 처럼 쓰면 좋지 않음. 결과적으로 엔티티를 반환하지 말자(노출하지말자))
        Hibernate5Module hibernate5Module = new Hibernate5Module();
        // 강제 지연로딩(LazyLoading)
//        hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true);

        return hibernate5Module;
    }
}
