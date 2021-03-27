package com.jrock.shop.repository;

import com.jrock.shop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;


@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) {
        if (item.getId() == null) { // 저장하기 전까지 id 값이 없으니 새로 생성한 객체 (신규등록)
            em.persist(em);
        } else {
            em.merge(item); // update 비슷한 ( DB에 id 값이 있으므로 수정 )
        }
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }
}
