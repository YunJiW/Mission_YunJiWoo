package com.ll.gramgram.boundedContext.likeablePerson.service;

import com.ll.gramgram.base.appConfig.AppConfig;
import com.ll.gramgram.base.event.EventAfterLike;
import com.ll.gramgram.base.event.EventAfterModifyAttractiveType;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.instaMember.service.InstaMemberService;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.repository.LikeablePersonRepository;
import com.ll.gramgram.boundedContext.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeablePersonService {
    private final LikeablePersonRepository likeablePersonRepository;
    private final InstaMemberService instaMemberService;

    private final ApplicationEventPublisher publisher;

    @Transactional
    public RsData<LikeablePerson> like(Member member, String username, int attractiveTypeCode) {
        if (member.hasConnectedInstaMember() == false) {
            return RsData.of("F-2", "먼저 본인의 인스타그램 아이디를 입력해야 합니다.");
        }

        if (member.getInstaMember().getUsername().equals(username)) {
            return RsData.of("F-1", "본인을 호감상대로 등록할 수 없습니다.");
        }

        InstaMember fromInstaMember = member.getInstaMember();

        InstaMember toInstaMember = instaMemberService.findByUsernameOrCreate(username).getData();

        List<LikeablePerson> findFromInstaMember = likeablePersonRepository.findByFromInstaMemberId(fromInstaMember.getId());


        for (LikeablePerson lk : findFromInstaMember) {
            if (lk.getToInstaMember().getUsername().equals(toInstaMember.getUsername())) {
                if (!lk.updateAttractionTypeCode(attractiveTypeCode))
                    return RsData.of("F-3", "중복 발생");
                else {
                    publisher.publishEvent(new EventAfterModifyAttractiveType(this, lk, lk.getAttractiveTypeCode(), attractiveTypeCode));
                    return RsData.of("S-2", "호감 이유 수정");
                }
            }
        }

        boolean addPossible;
        if (findFromInstaMember.isEmpty()) {
            addPossible = false;
        } else {
            addPossible = checksize(fromInstaMember.getFromLikeablePeople().size());
        }

        if (addPossible) {
            return RsData.of("F-1", "%s명을 넘길수 없습니다.".formatted(AppConfig.getLikeablePersonFromMax()));
        }


        LikeablePerson likeablePerson = LikeablePerson
                .builder()
                .fromInstaMember(fromInstaMember) // 호감을 표시하는 사람의 인스타 멤버
                .fromInstaMemberUsername(member.getInstaMember().getUsername()) // 중요하지 않음
                .toInstaMember(toInstaMember) // 호감을 받는 사람의 인스타 멤버
                .toInstaMemberUsername(toInstaMember.getUsername()) // 중요하지 않음
                .attractiveTypeCode(attractiveTypeCode) // 1=외모, 2=능력, 3=성격
                .modifyUnlockDate(AppConfig.genLikeablePersonModifyUnlockDate())
                .build();

        likeablePersonRepository.save(likeablePerson); // 저장

        //너가 좋아하는 호감표시 생김
        fromInstaMember.addFromLikeablePerson(likeablePerson);

        //너를 좋아하는 호감표시 생김
        toInstaMember.addToLikeablePerson(likeablePerson);

        publisher.publishEvent(new EventAfterLike(this, likeablePerson));


        toInstaMember.increaseLikesCount(fromInstaMember.getGender(), attractiveTypeCode);

        return RsData.of("S-1", "입력하신 인스타유저(%s)를 호감상대로 등록되었습니다.".formatted(username), likeablePerson);
    }

    private boolean checksize(int size) {
        if (size >= AppConfig.getLikeablePersonFromMax())
            return true;

        return false;
    }

    public List<LikeablePerson> findByFromInstaMemberId(Long fromInstaMemberId) {
        return likeablePersonRepository.findByFromInstaMemberId(fromInstaMemberId);
    }

    public Optional<LikeablePerson> FindById(Long id) {
        return this.likeablePersonRepository.findById(id);
    }


    @Transactional
    public RsData delete(LikeablePerson likeablePerson) {

        if (likeablePerson.isModifyUnlocked())
            return RsData.of("F-5", "호감표시를 하고 %s 이내에 삭제가 불가능합니다.".formatted(likeablePerson.getModifyUnlockDateRemainStrHuman()));

        publisher.publishEvent(new EventAfterLike(this, likeablePerson));

        likeablePerson.getFromInstaMember().removeFromLikeablePerson(likeablePerson);
        likeablePerson.getToInstaMember().removeToLikeablePerson(likeablePerson);

        this.likeablePersonRepository.delete(likeablePerson);
        likeablePerson.updatemodifydate(AppConfig.genLikeablePersonModifyUnlockDate());

        String likeCanceledUsername = likeablePerson.getToInstaMember().getUsername();
        return RsData.of("S-1", "호감상대(%s)를 삭제하였습니다.".formatted(likeCanceledUsername));
    }

    public RsData CanActorDelete(Member actor, LikeablePerson likeablePerson) {
        if (likeablePerson == null)
            return RsData.of("F-1", "이미 삭제되었습니다.");

        long actorInstaMemeberId = actor.getInstaMember().getId();
        long fromInstaMemberId = likeablePerson.getFromInstaMember().getId();

        if (actorInstaMemeberId != fromInstaMemberId)
            return RsData.of("F-2", "권한이 없습니다.");

        return RsData.of("S-1", "삭제가능");
    }

    public Optional<LikeablePerson> findById(Long id) {
        return likeablePersonRepository.findById(id);
    }

    @Transactional
    public RsData<LikeablePerson> modifyLike(Member actor, Long id, int attractiveTypeCode) {
        LikeablePerson likeablePerson = findById(id).orElseThrow();

        RsData canModifyRsData = canModifyLike(actor, likeablePerson);

        if (canModifyRsData.isFail()) {
            return canModifyRsData;
        }
        if (likeablePerson.isModifyUnlocked())
            return RsData.of("F-5", "호감표시를 하고 %s 이내에 삭제가 불가능합니다.".formatted(likeablePerson.getModifyUnlockDateRemainStrHuman()));

        likeablePerson.modifyAttractiveType(attractiveTypeCode);
        likeablePerson.updatemodifydate(AppConfig.genLikeablePersonModifyUnlockDate());

        return RsData.of("S-1", "호감사유 수정완료!");
    }

    public RsData canModifyLike(Member actor, LikeablePerson likeablePerson) {
        if (!actor.hasConnectedInstaMember()) {
            return RsData.of("F-1", "먼저 본인의 인스타그램아이디를 입력해주세요.");
        }

        InstaMember fromInstaMember = actor.getInstaMember();
        if (!Objects.equals(likeablePerson.getFromInstaMember().getId(), fromInstaMember.getId())) {
            return RsData.of("F-2", "해당 호감표시를 취소할 권한이 없습니다.");
        }

        return RsData.of("S-1", "호감표시취소가 가능합니다.");
    }

    public List<LikeablePerson> findByToInstaMember(InstaMember instaMember, String gender, int attractiveTypeCode, int sortCode) {
        Stream<LikeablePerson> likeablePeopleStream = instaMember.getToLikeablePeople().stream();

        //성별
        if (gender != null) {
            likeablePeopleStream = likeablePeopleStream.filter(likeablePerson -> likeablePerson.getFromInstaMember().getGender().equals(gender));
        }
        //호감 코드
        if (attractiveTypeCode != 0) {
            likeablePeopleStream = likeablePeopleStream.filter(likeablePerson -> likeablePerson.getAttractiveTypeCode() == attractiveTypeCode);
        }
        //순위 정렬
        switch (sortCode) {
            //날짜순 정렬
            case 2:
                likeablePeopleStream = likeablePeopleStream.sorted(Comparator.comparing(LikeablePerson::getId));
                break;
            //인기 내림차순
            case 3:
                likeablePeopleStream = likeablePeopleStream.sorted(Comparator.comparing(lp -> ((LikeablePerson) lp).getFromInstaMember().getLikes()).reversed());
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
        return likeablePeopleStream.toList();
    }
}
