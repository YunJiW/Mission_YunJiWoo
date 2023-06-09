package com.ll.gramgram.boundedContext.likeablePerson.entity;

import com.ll.gramgram.base.baseEntity.BaseEntity;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.standard.util.Ut;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@NoArgsConstructor
@ToString(callSuper = true)
@Entity
@Getter
@SuperBuilder
public class LikeablePerson extends BaseEntity {

    private LocalDateTime modifyUnlockDate;
    @ManyToOne
    @ToString.Exclude
    private InstaMember fromInstaMember; // 호감을 표시한 사람(인스타 멤버)
    private String fromInstaMemberUsername; // 혹시 몰라서 기록
    @ManyToOne
    @ToString.Exclude
    private InstaMember toInstaMember; // 호감을 받은 사람(인스타 멤버)
    private String toInstaMemberUsername; // 혹시 몰라서 기록

    private int attractiveTypeCode; // 매력포인트(1=외모, 2=성격, 3=능력)

    public boolean updateAttractionTypeCode(int attractiveTypeCode) {
        if (this.attractiveTypeCode == attractiveTypeCode) {
            return false;
        }
        toInstaMember.decreaseLikesCount(fromInstaMember.getGender(), this.attractiveTypeCode);
        toInstaMember.increaseLikesCount(fromInstaMember.getGender(), attractiveTypeCode);

        this.attractiveTypeCode = attractiveTypeCode;
        return true;
    }

    public String getAttractiveTypeDisplayName() {
        return switch (attractiveTypeCode) {
            case 1 -> "외모";
            case 2 -> "성격";
            default -> "능력";
        };
    }

    public String getAttractiveTypeDisplayNameWithIcon() {
        return switch (attractiveTypeCode) {
            case 1 -> "<i class=\"fa-solid fa-person-rays\"></i>";
            case 2 -> "<i class=\"fa-regular fa-face-smile\"></i>";
            default -> "<i class=\"fa-solid fa-people-roof\"></i>";
        } + "&nbsp;" + getAttractiveTypeDisplayName();
    }

    public String getJdenticon(){
        return Ut.hash.sha256(fromInstaMember.getId() + "_likes_" + toInstaMember.getId());
    }

    public void modifyAttractiveType(int attractiveTypeCode) {
        this.attractiveTypeCode = attractiveTypeCode;
    }

    public void updatemodifydate(LocalDateTime modifyUnlockDate){
        this.modifyUnlockDate = modifyUnlockDate;
    }

    // 초 단위에서 올림 해주세요.
    public String getModifyUnlockDateRemainStrHuman() {
        int hour = modifyUnlockDate.getHour();
        int min = modifyUnlockDate.getMinute();
        if(min - LocalDateTime.now().getMinute() < 0)
        {
            hour = hour -1;
            min += 60;
        }
        return "%d시간 %d 분".formatted(hour - LocalDateTime.now().getHour(),min- LocalDateTime.now().getMinute());
    }

    public boolean isModifyUnlocked() {
        return modifyUnlockDate.isBefore(LocalDateTime.now());
    }
}
