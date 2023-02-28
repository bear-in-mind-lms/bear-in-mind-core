SET TIME ZONE 'UTC';

INSERT INTO course_user_groups(id, course_id, group_id)
VALUES -- Administration course
       --> Administrators group
       (1, 1, 1),

       -- Java course
       --> Students group
       (11, 2, 3),

       -- Kotlin course
       --> Students group
       (21, 3, 3),

       -- Spring Framework course
       --> Students group
       (31, 4, 3),

       -- TypeScript course
       --> Students group
       (41, 5, 3),

       -- React course
       --> Students group
       (51, 6, 3),

       -- PostgreSQL course
       --> Administrators group
       (61, 7, 1),
       --> Students group
       (62, 7, 3),

       -- Docker course
       --> Administrators group
       (71, 8, 1),
       --> Students group
       (72, 8, 3),

       -- Kubernetes course
       --> Administrators group
       (81, 9, 1),
       --> Students group
       (82, 9, 3),

       -- Gamification in Education course
       --> Teachers group
       (91, 10, 2),

       -- English course
       --> Teachers group
       (101, 11, 2),
       --> Students group
       (102, 11, 3)
;


SELECT setval('course_user_groups_id_seq', 1000000);