package com.ll.gramgram.boundedContext.likeablePerson.controller;

import com.ll.gramgram.base.rq.Rq;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.repository.LikeablePersonRepository;
import com.ll.gramgram.boundedContext.likeablePerson.service.LikeablePersonService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/usr/likeablePerson")
@RequiredArgsConstructor
public class LikeablePersonController {
    private final Rq rq;
    private final LikeablePersonService likeablePersonService;
    private final LikeablePersonRepository likeablePersonRepository;

    @GetMapping("/like")
    public String showLike() {
        return "usr/likeablePerson/like";
    }

    @AllArgsConstructor
    @Getter
    public static class LikeForm {

        @NotBlank
        @Size(min = 3, max = 30)
        private final String username;

        @NotNull
        @Min(1)
        @Max(3)
        private final int attractiveTypeCode;
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/like")
    public String like(@Valid LikeForm likeForm) {
        RsData<LikeablePerson> createRsData = likeablePersonService.like(rq.getMember(), likeForm.getUsername(), likeForm.getAttractiveTypeCode());

        if (createRsData.isFail()) {
            return rq.historyBack(createRsData);
        }

        return rq.redirectWithMsg("/usr/likeablePerson/list", createRsData);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list")
    public String showLike(Model model) {
        InstaMember instaMember = rq.getMember().getInstaMember();

        // 인스타인증을 했는지 체크
        if (instaMember != null) {
            List<LikeablePerson> likeablePeople = instaMember.getFromLikeablePeople();
            model.addAttribute("likeablePeople", likeablePeople);
        }

        return "usr/likeablePerson/list";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}")
    public String delete(@PathVariable("id") Long id) {
        LikeablePerson likeablePerson = this.likeablePersonService.FindById(id).orElse(null);

        RsData canActorDeleteRsData = likeablePersonService.CanActorDelete(rq.getMember(), likeablePerson);
        if (canActorDeleteRsData.isFail())
            return rq.historyBack(canActorDeleteRsData);


        RsData deleteRsData = likeablePersonService.delete(likeablePerson);
        if (deleteRsData.isFail()) {
            rq.historyBack(deleteRsData);
        }


        return rq.redirectWithMsg("/usr/likeablePerson/list", deleteRsData);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String showModify(@PathVariable Long id, Model model) {
        LikeablePerson likeablePerson = likeablePersonService.findById(id).orElseThrow();

        RsData canModifyRsData = likeablePersonService.canModifyLike(rq.getMember(), likeablePerson);

        if (canModifyRsData.isFail()) {
            return rq.historyBack(canModifyRsData);
        }

        model.addAttribute("likeablePerson", likeablePerson);

        return "usr/likeablePerson/modify";

    }

    @AllArgsConstructor
    @Getter
    public static class ModifyForm {
        @NotNull
        @Min(1)
        @Max(3)
        private final int attractiveTypeCode;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String modify(@PathVariable Long id, @Valid ModifyForm modifyForm) {
        RsData<LikeablePerson> rsData = likeablePersonService.modifyLike(rq.getMember(), id, modifyForm.getAttractiveTypeCode());

        if (rsData.isFail()) {
            return rq.historyBack(rsData);
        }
        return rq.redirectWithMsg("/usr/likeablePerson/list", rsData);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/toList")
    public String showTogender(Model model,String gender,@RequestParam(defaultValue = "0") int attractiveTypeCode,int sortCode){
        InstaMember instaMember = rq.getMember().getInstaMember();

        if(instaMember != null){
            Stream<LikeablePerson> likeablePeopleStream = instaMember.getToLikeablePeople().stream();

            //성별
            if(gender != null)
            {
                likeablePeopleStream = likeablePeopleStream.filter(likeablePerson -> likeablePerson.getFromInstaMember().getGender().equals(gender));
            }
            //호감 코드
            if(attractiveTypeCode != 0){
                likeablePeopleStream = likeablePeopleStream.filter(likeablePerson -> likeablePerson.getAttractiveTypeCode() == attractiveTypeCode);
            }
            //순위 정렬
            switch (sortCode){
                //날짜순 정렬
                case 2:
                    likeablePeopleStream = likeablePeopleStream.sorted(Comparator.comparing(LikeablePerson::getId));
                    break;
                    //인기 내림차순
                case 3:
                    likeablePeopleStream = likeablePeopleStream.sorted(Comparator.comparing(lp -> ((LikeablePerson)lp).getFromInstaMember().getLikes()).reversed());
                    break;
                    //인기 오름차순
                case 4:
                    likeablePeopleStream = likeablePeopleStream.sorted(Comparator.comparing(lp -> lp.getFromInstaMember().getLikes()));
                    break;
                    //성별 순
                case 5:
                    likeablePeopleStream = likeablePeopleStream.sorted(Comparator.comparing((LikeablePerson lp) -> lp.getFromInstaMember().getGender()).reversed());
                    break;
                    //호감 사유 순
                case 6:
                    likeablePeopleStream = likeablePeopleStream.sorted(Comparator.comparing(LikeablePerson::getAttractiveTypeCode).thenComparing(Comparator.comparing(LikeablePerson::getId)).reversed());
                    break;
            }

            List<LikeablePerson> likeablePeople = likeablePeopleStream.collect(Collectors.toList());
            model.addAttribute("likeablePeople",likeablePeople);
        }


        return "usr/likeablePerson/toList";
    }

    //호감 표시한것중 필터링용
    @AllArgsConstructor
    @Getter
    public static class toListForm {
        private String gender;
    }

}
