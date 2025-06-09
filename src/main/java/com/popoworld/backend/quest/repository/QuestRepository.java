package com.popoworld.backend.quest.repository;

import com.popoworld.backend.quest.entity.Quest;
import com.popoworld.backend.quest.enums.QuestState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface QuestRepository extends JpaRepository<Quest, UUID> {
    //기본 조회
    //아이별 퀘스트 조회
    List<Quest> findByChildId(UUID childId);
    //아이별+타입별 퀘스트 조회
    List<Quest> findByChildIdAndType(UUID childId, Quest.QuestType type);

    //스케줄러용
    @Modifying //select가 아니라 db에 변경을 가하는 쿼리임을 나타냄
    @Query("DELETE FROM Quest q WHERE q.type = :type")
    void deleteByType(@Param("type")Quest.QuestType type);

    // 부모퀘스트 만료 처리용 - 이 메서드를 추가하세요!
    @Modifying
    @Query("UPDATE Quest q SET q.state = :expiredState WHERE q.type = :questType AND q.endDate < :now AND q.state NOT IN (:completedState, :expiredState)")
    int updateExpiredParentQuests(
            @Param("now") LocalDateTime now,
            @Param("questType") Quest.QuestType questType,
            @Param("expiredState") QuestState expiredState,
            @Param("completedState") QuestState completedState
    );
}
