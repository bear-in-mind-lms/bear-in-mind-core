package com.kwezal.bearinmind.core.course.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "course_lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseLesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    Course course;

    @Column(nullable = false)
    Integer topicIdentifier;

    @Column
    Integer descriptionIdentifier;

    @Column
    String image;

    @Column(nullable = false)
    Integer ordinal;

    @Column
    OffsetDateTime startDateTime;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    List<CourseLessonPart> parts;
}
