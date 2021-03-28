package com.jrock.shop.controller;

import com.jrock.shop.domain.item.Book;
import com.jrock.shop.domain.item.Item;
import com.jrock.shop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items/new")
    public String createForm(Model model) {
        // form 을 지정해 주니까 thymeleaf 에서도 컴파일 에러를 내지 않네?
        model.addAttribute("form", new BookForm());
        return "items/createItemForm";
    }

    @PostMapping("/items/new")
    public String create(BookForm form) {
        // 세터를 제거하고 static 메서드 (생성자 메서드)를 만들어서 사용하는게 깔끔한 설계.(실무에서는 세터를 날림)
        // 싱글 테이블 전략 이므로 나머지 상속 받는 컬럼은 널
        Book book = new Book();
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());

        itemService.saveItem(book);

        return "redirect:/";
    }

    @GetMapping("/items")
    public String list(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);
        return "items/itemList";
    }

    @GetMapping("/items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {
        Book item = (Book) itemService.findOne(itemId);

        BookForm form = new BookForm();
        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setAuthor(item.getAuthor());
        form.setIsbn(item.getIsbn());

        model.addAttribute("form", form);
        return "items/updateItemForm";
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
    @PostMapping("/items/{itemId}/edit")
    public String updateItem(@PathVariable Long itemId, @ModelAttribute("form") BookForm form) {

        // JPA가 식별할 수 있는 ID를 가지고 있는 상태를 준 영속성 엔티티
//        Book book = new Book();
//        book.setId(form.getId());
//        book.setName(form.getName());
//        book.setPrice(form.getPrice());
//        book.setStockQuantity(form.getStockQuantity());
//        book.setAuthor(form.getAuthor());
//        book.setIsbn(form.getIsbn());

//        itemService.saveItem(book);

        // 리팩토링 (merge 를 지우고 영속성 컨텍스트 안에서 수정한다)
        // 업데이트 할 것이 많으면 별도로 DTO를 만든다.
        itemService.updateItem(itemId, form.getPrice(), form.getStockQuantity(), form.getName());

        return "redirect:/items";
    }
}
