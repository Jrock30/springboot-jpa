package com.jrock.shop.service;

import com.jrock.shop.domain.item.Book;
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

    /**
     * 준영속 엔티티?
     *   - 영속성 컨텍스트가 더는 관리하지 않는 엔티티를 말한다.
     *   - (여기서는 itemService.saveItem(book) 에서 수정을 시도하는 Book 객체다.
     *   - Book 객체는 이미 DB 에 한번 저장되어서 식별자가 존재한다. 이렇게 임의로 만들어낸 엔티티도 기존 식별자를 가지고 있으면 준 영속 엔티티로 볼 수 있다.)
     *
     *   병합 동작 방식
     *     - 1. merge()를실행한다.
     *     - 2. 파라미터로 넘어온 준영속 엔티티의 식별자 값으로 1차 캐시에서 엔티티를 조회한다.
     *     - 2-1. 만약 1차 캐시에 엔티티가 없으면 데이터베이스에서 엔티티를 조회하고, 1차 캐시에 저장한다.
     *     - 3. 조회한 영속 엔티티( mergeMember )에 member 엔티티의 값을 채워 넣는다. (member 엔티티의 모든 값
     *       을 mergeMember에 밀어 넣는다. 이때 mergeMember의 “회원1”이라는 이름이 “회원명변경”으로 바
     *     - 뀐다.)
     *     - 4. 영속 상태인 mergeMember를 반환한다.
     *
     *   병합시 동작 방식을 간단히 정리
     *     - 1. 준영속 엔티티의 식별자 값으로 영속 엔티티를 조회한다.
     *     - 2. 영속 엔티티의 값을 준영속 엔티티의 값으로 모두 교체한다.(병합한다.)
     *     - 3. 트랜잭션 커밋 시점에 변경 감지 기능이 동작해서 데이터베이스에 UPDATE SQL이 실행
     *
     *   주의: 변경 감지 기능을 사용하면 원하는 속성만 선택해서 변경할 수 있지만,
     *        병합을 사용하면 모든 속성이 변경된다. 병합시 값이 없으면 null 로 업데이트 할 위험도 있다. (병합은 모든 필드를 교체한다.)
     *
     * 가장 좋은 해결 방법
     *   - 엔티티를 변경할 때는 항상 변경 감지를 사용하세요
     *   - 컨트롤러에서 어설프게 엔티티를 생성하지 마세요.
     *   - 트랜잭션이 있는 서비스 계층에 식별자( id )와 변경할 데이터를 명확하게 전달하세요.(파라미터 or dto)
     *   - 트랜잭션이 있는 서비스 계층에서 영속 상태의 엔티티를 조회하고, 엔티티의 데이터를 직접 변경하세요.
     *   - 트랜잭션 커밋 시점에 변경 감지가 실행됩니다.
     */
    @Transactional
    public void updateItem(Long itemId, int price, int stockQuantity, String name) {
        Book book = (Book) itemRepository.findOne(itemId);
        // 아래 처럼 setter를 사용하지 말고 의미있는 메서드를 사용해라
        // ex findItem.change(name,price,stockQuantity)
//        findItem.setPrice(price);
//        findItem.setName(name);
//        findItem.setStockQuantity(stockQuantity);

        book.change(price, stockQuantity, name);
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
