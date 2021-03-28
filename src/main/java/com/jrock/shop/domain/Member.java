package com.jrock.shop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @NotEmpty
    private String name;

    @Embedded // 내장타입 ( 값 타입 ) 여기는 굳이 해줄 필요는 없지만 명확하게 적용하자.
    private Address address;

    @OneToMany(mappedBy = "member") // 연관관계의 주인이 아니고 거울이오 ( 오더 테이블에 있는 멤버 필드로 인해서 나는 맵핑 된거야, 읽기 전용 )
    private List<Order> orders = new ArrayList<>();
}
