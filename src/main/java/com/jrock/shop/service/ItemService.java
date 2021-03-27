package com.jrock.shop.service;

import com.jrock.shop.domain.item.Item;
import com.jrock.shop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    // 위임만 하는 비즈니스 로직은 컨트롤에서 바로 조회해도 상관 없다고 생각함.
    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    // 위임만 하는 비즈니스 로직은 컨트롤에서 바로 조회해도 상관 없다고 생각함.
    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }
}
