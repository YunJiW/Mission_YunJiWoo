package com.ll.gramgram.boundedContext.notification.service;

import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.notification.entity.Notification;
import com.ll.gramgram.boundedContext.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public List<Notification> findByToInstaMember(InstaMember toInstaMember) {
        return notificationRepository.findByToInstaMemberOrderByIdDesc(toInstaMember);
    }

    @Transactional
    public RsData<Notification> makeLike(LikeablePerson likeablePerson) {
        return make(likeablePerson, "LIKE", 0, null);
    }


    @Transactional
    public RsData<Notification> makeModifyAttractive(LikeablePerson likeablePerson, int oldAttractiveTypeCode) {
        return make(likeablePerson, "ModifyAttractiveType", oldAttractiveTypeCode, likeablePerson.getFromInstaMember().getGender());
    }

    @Transactional
    private RsData<Notification> make(LikeablePerson likeablePerson, String typeCode, int oldAttractiveType, String oldGender) {
        Notification notification = Notification.builder()
                .typeCode(typeCode)
                .toInstaMember(likeablePerson.getToInstaMember())
                .fromInstaMember(likeablePerson.getFromInstaMember())
                .oldAttractiveTypeCode(oldAttractiveType)
                .oldGender(likeablePerson.getFromInstaMember().getGender())
                .newAttractiveTypeCode(likeablePerson.getAttractiveTypeCode())
                .newGender(likeablePerson.getFromInstaMember().getGender())
                .build();

        notificationRepository.save(notification);

        return RsData.of("S-2", "알림메세지 생성", notification);
    }

    public List<Notification> findByToInstaMember_username(String username){
        return notificationRepository.findByToInstaMember_usernameOrderByIdDesc(username);
    }

    @Transactional
    public RsData markAsRead(List<Notification> notifications){
        notifications
                .stream()
                .filter(notification -> !notification.isRead())
                .forEach(Notification::markAsRead);


        return RsData.of("S-1", "읽음 처리 되었습니다.");
    }

    public boolean countUnreadNotificationsByToInstaMember(InstaMember instaMember) {
        return notificationRepository.countByToInstaMemberAndReadDateNull(instaMember) > 0;
    }
}
