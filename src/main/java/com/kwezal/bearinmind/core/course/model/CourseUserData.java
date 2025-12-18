package com.kwezal.bearinmind.core.course.model;

import com.kwezal.bearinmind.core.course.enumeration.CourseRole;
import com.kwezal.bearinmind.core.user.model.User;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "course_user_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseUserData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    CourseRole role;

    @Column(nullable = false)
    OffsetDateTime lastAccessDateTime;
}
