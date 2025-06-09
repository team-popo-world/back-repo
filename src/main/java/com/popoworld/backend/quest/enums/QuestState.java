package com.popoworld.backend.quest.enums;

public enum QuestState {
    PENDING_ACCEPT,    // 수락 대기 (처음 생성된 상태)
    IN_PROGRESS,       // 진행중 (아이가 수락함)
    PENDING_APPROVAL,  // 승인 대기 (아이가 완료, 부모 확인 대기)
    APPROVED,          // 승인 완료 (부모가 승인, 보상받기 가능)
    COMPLETED,         // 완료 (아이가 보상받기 클릭 후 최종 완료)
    EXPIRED            // 만료 (시간 초과)
}