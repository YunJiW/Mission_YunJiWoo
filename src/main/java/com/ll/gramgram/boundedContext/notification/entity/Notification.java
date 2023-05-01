package com.ll.gramgram.boundedContext.notification.entity;

import com.ll.gramgram.base.baseEntity.BaseEntity;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
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
}
