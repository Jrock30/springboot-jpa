package com.jrock.shop.domain;

import com.jrock.shop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Category {

    @Id
    @GeneratedValue
    @Column(name = "category_id")
    private Long id;

    private String name;

    // @ManyToMany 를 쓰는 것이 좋지않다. 예제상 다양하게 쓰기 위해 사용함.
    @ManyToMany
    // 중간 테이블 맵핑 ( 관계형 DB상 중간 테이블 필요 ) 실전에서 쓰는 것은 좋지 않음. 유연하게 변경하기 힘듦.
    @JoinTable(name = "category_item",
            joinColumns = @JoinColumn(name = "category_id"), // 중간 테이블에 있는 category_id
            inverseJoinColumns = @JoinColumn(name = "item_id"))
    private List<Item> items = new ArrayList<>();

    // 카테고리 계층 구조, 내 자신을 부모로 ( 이름만 내 것이지 다른 테이블이라 보자 )
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    // 카테고리 계층 구조, 내 자신을 자식으로 ( 이름만 내 것이지 다른 테이블이라 보자 )
    @OneToMany(mappedBy = "parent")
    private List<Category> child = new ArrayList<>();

    // 연관관계 메서드
    public void addChildCategory(Category child) {
        this.child.add(child);
        child.setParent(this);
    }

}
