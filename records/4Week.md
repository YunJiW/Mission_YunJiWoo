##  4 Week

### 미션 요구사항 분석 & 체크리스트

---

매 주 제공되는 미션 별 요구사항을 기반으로 기능에 대한 분석을 진행한 후, 아래와 같은 체크리스트를 작성합니다.

- ‘어떻게 개발을 진행 할 것인지에 대한 방향성’을 확인하는 과정이기 때문에 최대한 깊이있게 분석 후 진행해주시기 바랍니다.

### N주차 미션 요약

# 필수미션

- [ ] 네이버클라우드플랫폼을 통한 배포, 도메인, HTTPS 까지 적용

- [x] 내가 받은 호감리스트(/usr/likeablePerson/toList)에서 성별 필터링기능 구현

추가미션
- [ ] 젠킨스를 통해서 리포지터리의 main 브랜치에 커밋 이벤트가 발생하면 자동으로 배포가 진행되도록
- [x] 내가 받은 호감리스트(/usr/likeablePerson/toList)에서 호감사유 필터링기능 구현
- [ ] 내가 받은 호감리스트(/usr/likeablePerson/toList)에서 정렬기능


---

**[접근 방법]**

# 필수미션 - 내가 받은 호감리스트(/usr/likeablePerson/toList)에서 성별 필터링기능 구현

첫번째 커밋 생각
- 간단하게 생각해서 현재 ToList가 호감을 표시한 사람들을 모아놔서 List를 뽑아 오는 것으로
- 거기서 그러면 ReQuestParam을 받아서 default값을 ""로 두고 디폴트 값일경우 -> 그냥 likeablePeople을 model에 추가해주고
- 아닐경우 -> toListGender에 gender가 맞는 것들을 다시 한번 뽑아서 model에 추가하는 형식으로 진행하였습니다. (다듬을 예정.)

- 두번째 커밋
- 강사님의 힌트를 통해서 스트림 형식을 통해서 변경 및 호감사유 필터링 까지 구현했습니다.
- 강사님의 힌트 중에서 stream으로 받아와서 requestParam 값이 디폴트 값이 아닐 때만 체크하여 각자 체크를 하는 방식을 통해서 간단하게 구현이 되는 것이였습니다.



**[특이사항]**
