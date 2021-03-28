package com.jrock.shop.domain.item;

import com.jrock.shop.domain.Category;
import com.jrock.shop.exception.NotEnoughStockException;
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
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    /**
     * 비즈니스 로직
     * 데이터를 가지고 있는 쪽에 비즈니스 메서드를 가지고 있는 것이 좋다, 그래야 응집력이 있다.
     * 변경할 것이 있으면 의미있는 메서드를 만들어서 넣어라 ( 무분별하게 세터 주입 X )
     */
    /**
     *  stock 증가
     */
    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    /**
     * stock 감소
     */
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if (restStock < 0) {
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = restStock;
    }

    /**
     * 값 변경
     */
    public void change(int price, int stockQuantity, String name) {
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.name = name;
    }
}
