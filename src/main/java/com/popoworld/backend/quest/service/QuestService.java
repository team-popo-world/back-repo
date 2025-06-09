package com.popoworld.backend.quest.service;

import com.popoworld.backend.quest.dto.ParentQuestRequest;
import com.popoworld.backend.quest.dto.QuestResponse;
import com.popoworld.backend.quest.dto.QuestStateChangeRequest;
import com.popoworld.backend.quest.entity.Quest;
import com.popoworld.backend.quest.enums.QuestState;
import com.popoworld.backend.quest.repository.QuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestService {
    private final QuestRepository questRepository;

    //타입 별 퀘스트 목록 조회
    public List<QuestResponse> getQuestsByType(UUID childId, String type){
        List<Quest> quests;
        Quest.QuestType questType = Quest.QuestType.valueOf(type.toUpperCase());
        quests = questRepository.findByChildIdAndType(childId, questType);
        return quests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 새로 가입한 아이에게 일일퀘스트 생성 (회원가입 시 호출)
     * 순환 참조 해결을 위해 로직을 QuestService로 이동
     */
    @Transactional
    public void createDailyQuestsForNewChild(UUID childId) {
        List<Quest> newQuests = createDailyQuestsForChild(childId);
        questRepository.saveAll(newQuests);
    }

    /**
     * 특정 아이에게 일일퀘스트 5개 생성 (DailyQuestScheduler에서 이동)
     */
    private List<Quest> createDailyQuestsForChild(UUID childId) {
        List<Quest> dailyQuests = new ArrayList<>();

        // 🎯 정적 팩토리 메서드 사용
        dailyQuests.add(Quest.createDailyQuest(childId, "양치하기", "밥 먹었으면 포포와 양치하자!", 100));
        dailyQuests.add(Quest.createDailyQuest(childId, "장난감 정리하기", "가지고 온 장난감은 스스로 치워볼까?", 100));
        dailyQuests.add(Quest.createDailyQuest(childId, "이불 개기", "일어나면 이불을 예쁘게 개자!", 100));
        dailyQuests.add(Quest.createDailyQuest(childId, "식탁 정리 도와주기", "먹고 난 그릇, 포포랑 정리해보자!", 100));
        dailyQuests.add(Quest.createDailyQuest(childId, "하루 이야기 나누기", "오늘 어땠는지 부모님과 얘기해보자!", 100));

        return dailyQuests;
    }

    // 부모 퀘스트 생성 메서드
    @Transactional
    public QuestResponse createParentQuest(ParentQuestRequest request) {
        LocalDateTime endDateTime = LocalDateTime.parse(request.getEndDate());

        // Quest 엔티티 생성 (imageUrl 포함)
        Quest parentQuest = Quest.createParentQuest(
                request.getChildId(),
                request.getName(),
                request.getDescription(),
                request.getReward(),
                endDateTime,
                request.getImageUrl()
        );

        // 저장
        Quest savedQuest = questRepository.save(parentQuest);

        // DTO로 변환해서 반환
        return convertToDto(savedQuest);
    }

    //상태 변경 메서드
    @Transactional
    public void changeQuestState(QuestStateChangeRequest request){
        //1. 퀘스트 조회 및 검증(받은 퀘스트Id에 해당하는 퀘스트가 디비에 존재하는지?)
        Quest quest = questRepository.findById(request.getQuestId())
                .orElseThrow(() -> new IllegalArgumentException("퀘스트를 찾을 수 없습니다."));

        //2. 요청된 상태 검증
        QuestState newState;
        try{
            newState = QuestState.valueOf(request.getState().toUpperCase());
        }catch (IllegalArgumentException e){
            throw new IllegalArgumentException("유효하지 않은 상태입니다" + request.getState());
        }

        //3. 상태 변경 규칙 검증
        QuestState currentState = quest.getState();
        validateStateTransition(currentState, newState);

        quest.changeState(newState);
    }

    private void validateStateTransition(QuestState current, QuestState target){
        boolean isValidTransition = switch (current){
            case PENDING_ACCEPT -> target == QuestState.IN_PROGRESS;
            case IN_PROGRESS -> target == QuestState.PENDING_APPROVAL;
            case PENDING_APPROVAL -> target == QuestState.APPROVED;
            case APPROVED -> target == QuestState.COMPLETED;
            case COMPLETED, EXPIRED -> false; //최종 상태에선 변경 불가
        };
        if(!isValidTransition){
            throw new IllegalArgumentException(
                    String.format("/%s에서 %s로 변경할 수 없습니다.", current.name(), target.name())
            );
        }
    }

    //Entity를 Dto로 변환
    private QuestResponse convertToDto(Quest quest){
        return QuestResponse.builder()
                .questId(quest.getQuestId())
                .childId(quest.getChildId())
                .type(quest.getType().name().toLowerCase())
                .name(quest.getName())
                .description(quest.getDescription())
                .state(quest.getState().name())
                .endDate(quest.getEndDate())
                .created(quest.getCreated())
                .isStatic(quest.isStatic())
                .reward(quest.getReward())
                .imageUrl(quest.getImageUrl())
                .build();
    }
}