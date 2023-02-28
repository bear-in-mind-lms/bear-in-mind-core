package com.kwezal.bearinmind.core.course.model;

import com.kwezal.bearinmind.core.user.model.UserGroup;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "course_user_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseUserGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    UserGroup group;
}
