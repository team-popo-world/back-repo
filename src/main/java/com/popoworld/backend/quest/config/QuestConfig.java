package com.popoworld.backend.quest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration //설정파일 임을 Spring에게 알림
@EnableScheduling // 스케줄러 기능을 켜달라고 Spring에게 요청
public class QuestConfig { //<- 스케줄러 활성화
}
