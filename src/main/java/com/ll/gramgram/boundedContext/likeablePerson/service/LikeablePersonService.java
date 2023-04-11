package com.ll.gramgram.boundedContext.likeablePerson.service;

import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.instaMember.service.InstaMemberService;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.repository.LikeablePersonRepository;
import com.ll.gramgram.boundedContext.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeablePersonService {
    private final LikeablePersonRepository likeablePersonRepository;
    private final InstaMemberService instaMemberService;

    @Transactional
    public RsData<LikeablePerson> like(Member member, String username, int attractiveTypeCode) {
        if ( member.hasConnectedInstaMember() == false ) {
            return RsData.of("F-2", "먼저 본인의 인스타그램 아이디를 입력해야 합니다.");
        }

        if (member.getInstaMember().getUsername().equals(username)) {
            return RsData.of("F-1", "본인을 호감상대로 등록할 수 없습니다.");
        }

        InstaMember fromInstaMember = member.getInstaMember();

        InstaMember toInstaMember = instaMemberService.findByUsernameOrCreate(username).getData();

        List<LikeablePerson> findAll = likeablePersonRepository.findByFromInstaMemberId(fromInstaMember.getId());

        boolean addPossible = checksize(fromInstaMember.getFromLikeablePeople().size());
        if(addPossible){
            return RsData.of("F-1" , "10명을 넘길수 없습니다.");
        }


        for(LikeablePerson lk : findAll){
            if(lk.getToInstaMember().getUsername().equals(toInstaMember.getUsername())){
                if(lk.getAttractiveTypeCode() == attractiveTypeCode)
                    return RsData.of("F-3" ,"중복 발생");
                else{
                    lk.setAttractiveTypeCode(attractiveTypeCode);
                    likeablePersonRepository.save(lk);
                    return RsData.of("S-2" ,"호감 이유 수정");
                }
            }
        }

        LikeablePerson likeablePerson = LikeablePerson
                .builder()
                .fromInstaMember(fromInstaMember) // 호감을 표시하는 사람의 인스타 멤버
                .fromInstaMemberUsername(member.getInstaMember().getUsername()) // 중요하지 않음
                .toInstaMember(toInstaMember) // 호감을 받는 사람의 인스타 멤버
                .toInstaMemberUsername(toInstaMember.getUsername()) // 중요하지 않음
                .attractiveTypeCode(attractiveTypeCode) // 1=외모, 2=능력, 3=성격
                .build();

        likeablePersonRepository.save(likeablePerson); // 저장

        //너가 좋아하는 호감표시 생김
        fromInstaMember.addFromLikeablePerson(likeablePerson);

        //너를 좋아하는 호감표시 생김
        toInstaMember.addToLikeablePerson(likeablePerson);
        return RsData.of("S-1", "입력하신 인스타유저(%s)를 호감상대로 등록되었습니다.".formatted(username), likeablePerson);
    }

    private boolean checksize(int size) {
        if(size >= 10)
            return true;

        return false;
    }

    public List<LikeablePerson> findByFromInstaMemberId(Long fromInstaMemberId) {
        return likeablePersonRepository.findByFromInstaMemberId(fromInstaMemberId);
    }

    public Optional<LikeablePerson> FindById(Long id){
        return this.likeablePersonRepository.findById(id);
    }


    @Transactional
    public RsData delete(LikeablePerson likeablePerson){
        this.likeablePersonRepository.delete(likeablePerson);

        String likeCanceledUsername = likeablePerson.getToInstaMember().getUsername();
        return RsData.of("S-1","호감상대(%s)를 삭제하였습니다.".formatted(likeCanceledUsername));
    }

    public RsData CanActorDelete(Member actor, LikeablePerson likeablePerson) {
        if(likeablePerson == null)
            return RsData.of("F-1","이미 삭제되었습니다.");

        long actorInstaMemeberId = actor.getInstaMember().getId();
        long fromInstaMemberId = likeablePerson.getFromInstaMember().getId();

        if(actorInstaMemeberId != fromInstaMemberId)
            return RsData.of("F-2","권한이 없습니다.");

        return RsData.of("S-1","삭제가능");
    }
}
