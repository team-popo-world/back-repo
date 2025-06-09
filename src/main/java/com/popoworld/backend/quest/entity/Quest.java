package com.popoworld.backend.quest.entity;


import com.popoworld.backend.quest.enums.QuestState;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name="quest")
public class Quest {
    @Id
    private UUID questId;

    private UUID childId;

    @NotNull
    private QuestType type;

    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private QuestState state;

    private LocalDateTime endDate;

    private LocalDateTime created;

    private boolean isStatic;

    private Integer reward;

    private String imageUrl;

    public enum QuestType{
        PARENT, DAILY
    }

    //상태 변경 메서드
    public void changeState(QuestState newState){
        this.state=newState;
    }
    //일일 퀘스트 생성용 정적 메서드
    public static Quest createDailyQuest(UUID childId, String name, String description, int reward) {
        Quest quest = new Quest();
        quest.questId = UUID.randomUUID();
        quest.childId = childId;
        quest.type = QuestType.DAILY;
        quest.name = name;
        quest.description = description;
        quest.state = QuestState.PENDING_ACCEPT;
        quest.endDate = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);
        quest.created = LocalDateTime.now(ZoneId.of("Asia/Seoul")); // 한국 시간 명시
        quest.isStatic = false;
        quest.reward = reward;
        quest.imageUrl = null;
        return quest;
    }

    // 부모퀘스트 생성용 정적 메서드
    public static Quest createParentQuest(UUID childId, String name, String description,
                                          int reward, LocalDateTime endDate, String imageUrl) {
        Quest quest = new Quest();
        quest.questId = UUID.randomUUID();
        quest.childId = childId;
        quest.type = QuestType.PARENT;
        quest.name = name;
        quest.description = description;
        quest.state = QuestState.PENDING_ACCEPT;
        quest.endDate = endDate;
        quest.created = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        quest.isStatic = false;
        quest.reward = reward;
        quest.imageUrl = imageUrl;
        return quest;
    }
}
