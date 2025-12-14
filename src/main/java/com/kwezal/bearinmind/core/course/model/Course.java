package com.kwezal.bearinmind.core.course.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Integer nameIdentifier;

    @Column
    Integer descriptionIdentifier;

    @Column
    String image;

    @Column
    OffsetDateTime startDateTime;

    @Column
    OffsetDateTime endDateTime;

    @Column(nullable = false)
    OffsetDateTime creationDateTime;

    @Column
    OffsetDateTime registrationClosingDateTime;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    List<CourseLesson> lessons;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    List<CourseUserData> data;
}
