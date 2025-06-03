package com.popoworld.backend.postgre;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DbTestController {

    private final TestEntityRepository repository;

    public DbTestController(TestEntityRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/jpa-test")
    public List<TestEntity> jpaTest() {
        TestEntity entity = new TestEntity("테스트 데이터");
        repository.save(entity);  // 저장
        return repository.findAll();  // 저장된 전체 조회 후 반환
    }
}
