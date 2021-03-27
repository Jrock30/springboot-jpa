package com.jrock.shop.controller;

import com.jrock.shop.domain.Address;
import com.jrock.shop.domain.Member;
import com.jrock.shop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/members/new")
    public String createForm(Model model) {
        // Validation 등을 위해 빈 객체라도 내려 보내자.
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    /**
     * @Valid 를 사용하면 Dto에 정의된 Validation을 해준다.
     * BindingResult 를 사용하면 @Valid에 에러가 있으면 result에 에러 내용이 담겨서 아래 로직이 실행된다.
     * Spring 이랑 Thymeleaf랑 Integration이 잘 되어 있다.
     * <p>
     * Member Entity가 있음에도 불구하고 MemberForm을 따로 쓰는 이유는
     * 도메인이 원하는 Validation과 화면에서 원하는 Validation과 다를 수 있고 필드 값 자체도 다를 수 있으니
     * 깔끔하게 따로 만드는 것이 낫다. ( Entity, DTO 따로 )
     *
     * 실무에서 엔티티는 핵심 비즈니스 로직만 가지고 있고, 화면을 위한 로직은 없어야 한다.
     * 화면이나 API에 맞 는 폼 객체나 DTO를 사용하자. 그래서 화면이나 API 요구사항을 이것들로 처리하고,
     * 엔티티는 최대한 순수 하게 유지하자.
     */
    @PostMapping("/members/new")
    public String create(@Valid MemberForm form, BindingResult result) {

        // 에러나 나도 Form 데이터는 다시 화면으로 내려간다.
        if (result.hasErrors()) {
            return "members/createMemberForm";
        }

        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());

        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        memberService.join(member);

        return "redirect:/";

    }

    @GetMapping("/members")
    public String list(Model model) {
        /**
         * 이것도 위의 상단 주석에 설명했던 것 처럼 복잡해지면 Entity를 Dto로 변환하여 화면에 내려주도록하자.
         * API를 만들떄는 애지간하면 Entity를 외부로 반환하면 안된다고 생각하자.(DTO 변환)
         *
         * 서버사이드는 상황에 따라 Entity를 반환해도 괜찮기는 하지만 그래도 DTO를 쓰도록 하자.
         */
//        List<Member> members = memberService.findMembers();
//        model.addAttribute("members", members);

        model.addAttribute("members", memberService.findMembers());
        return "members/memberList";
    }
}
