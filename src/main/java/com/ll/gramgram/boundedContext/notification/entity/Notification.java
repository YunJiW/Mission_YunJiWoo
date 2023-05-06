package com.ll.gramgram.boundedContext.notification.entity;

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

@Entity
@Getter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Notification extends BaseEntity {
    private LocalDateTime readDate;

    //메세지 받는사람
    @ManyToOne
    @ToString.Exclude
    private InstaMember toInstaMember;

    //메세지 발생시킨사람
    @ManyToOne
    @ToString.Exclude
    private InstaMember fromInstaMember;

    private String typeCode;

    private String oldGender;

    private int oldAttractiveTypeCode;

    private String newGender;

    private int newAttractiveTypeCode;

    public boolean isRead(){
        return readDate != null;
    }

    public void markAsRead(){
        readDate = LocalDateTime.now();
    }

    public String getCreateDateAfterStrHuman(){
        return Ut.time.diffFormat1Human(LocalDateTime.now(),getCreateDate());
    }

    public boolean isHot() {
        // 만들어진지 60분이 안되었다면 hot 으로 설정
        return getCreateDate().isAfter(LocalDateTime.now().minusMinutes(60));
    }

    public String getOldAttractiveTypeDisplayName() {
        return switch (oldAttractiveTypeCode) {
            case 1 -> "외모";
            case 2 -> "성격";
            default -> "능력";
        };
    }

    public String getNewAttractiveTypeDisplayName() {
        return switch (newAttractiveTypeCode) {
            case 1 -> "외모";
            case 2 -> "성격";
            default -> "능력";
        };
    }

    public String getNewGenderDisplayName() {
        return switch (newGender) {
            case "W" -> "여성";
            default -> "남성";
        };
    }
}
