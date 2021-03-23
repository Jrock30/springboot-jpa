package com.jrock.shop.domain.item;

import com.jrock.shop.domain.Category;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // 싱글테이블 전략(객체는 나누어져 있지만 Item 한 테이블에 다 떄려 박는 형식)
@DiscriminatorColumn(name = "dtype") // 상속받는 타입을 지정해준다. ( 한 테이블이기 떄문에 타입 필요 )
@Getter
@Setter
public abstract class Item {

    @Id
    @Getter
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

}
