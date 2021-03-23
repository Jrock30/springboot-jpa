package com.jrock.shop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

// 값은 imuutable 하다. 값이 변경 되면 안됨. 그래서 setter No
@Embeddable // 내장 타입 (값 타입)
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;

    /**
     *  @Embeddable 은 기본 생성자가 있어야 한다.
     *  JPA가 이런 제약을 두는 이유는 JPA 구현 라이브러리가 객체를 생성할 때 리플랙션 같은 기술을 사용할 수 있도록 지원해야 하기 떄문이다.
     */
    protected Address() {
    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
