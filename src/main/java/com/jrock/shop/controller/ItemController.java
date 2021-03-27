package com.jrock.shop.controller;

import com.jrock.shop.domain.item.Book;
import com.jrock.shop.domain.item.Item;
import com.jrock.shop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
}
