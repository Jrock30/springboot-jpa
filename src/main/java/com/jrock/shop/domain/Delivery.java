package com.jrock.shop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class Delivery {

    @Id
    @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    /**
     * 주의: 엔티티를 직접 노출할 때는 양방향 연관관계가 걸린 곳은 꼭! 한곳을 @JsonIgnore 처리 해야 한다.
     * 안그러면 양쪽을 서로 호출하면서 무한 루프가 걸린다.
     */
    @JsonIgnore // 양방향 가는 곳은 @JsonIgnore 를 해주어야한다. 안그러면 무한루프로 떨어진다.
    @OneToOne(mappedBy = "delivery", fetch = FetchType.LAZY) // 1:1 맵핑
    private Order order;

    @Embedded // 내장타입
    private Address address;

    @Enumerated(EnumType.STRING) // ORDINAL 안쓰면 기본(1,2,3,4 숫자로 들어감(인덱스), 중간에 다른 상태가 들어가면 망함 그래서 String 으로 사용)
    private DeliveryStatus status; //READY, COMP
}
