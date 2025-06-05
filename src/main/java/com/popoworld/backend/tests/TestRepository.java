package com.popoworld.backend.tests;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestRepository extends JpaRepository<Test1, Long> {
    List<Test1> findByChildId(String childId);
}
