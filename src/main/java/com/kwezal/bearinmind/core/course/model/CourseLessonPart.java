package com.kwezal.bearinmind.core.course.model;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "course_lesson_parts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseLessonPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_lesson_id", nullable = false)
    CourseLesson lesson;

    @Column
    Integer textIdentifier;

    /**
     * List of attachments in the following format:
     * <pre>
     * name1:url1\n
     * name2:url2\n
     * ...
     * nameN:urlN
     * </pre>
     * where <code>name</code> is the displayed name (if empty, the URL will be displayed)
     * and <code>url</code> is the address where the attachment can be found.
     */
    @Column
    String attachments;

    @Column(nullable = false)
    Integer ordinal;
}
