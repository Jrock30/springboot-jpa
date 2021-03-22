package com.jrock.shop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable // 내장 타입 (값 타입)
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;

}
