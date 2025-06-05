package com.popoworld.backend.tests;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "missions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Test1 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String childId;
    private String title;
    private boolean completed;
}