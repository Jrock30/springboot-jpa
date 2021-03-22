package com.jrock.shop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    @Embedded // 내장타입 ( 값 타입 ) 여기는 굳이 해줄 필요는 없지만 명확하게 적용하자.
    private Address address;

    @OneToMany
    private List<Order> orders = new ArrayList<>();
}
