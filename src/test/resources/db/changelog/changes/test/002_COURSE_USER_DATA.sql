SET TIME ZONE 'UTC';

-- OWNER = 0, TEACHER = 1, STUDENT = 2
INSERT INTO course_user_data(id, course_id, user_id, role, last_access_date_time)
VALUES -- Administration course
       --> Keith Master, Owner
       (1, 1, 1, 0, timestamp '2021-01-01 09:00'),

       -- Java course
       --> James Gosling, Owner
       (101, 2, 2, 0, timestamp '2021-01-02 09:00'),
       --> Keith Master, Teacher
       (102, 2, 1, 1, timestamp '2021-01-02 10:00'),
       --> Wendell Java, Student
       (103, 2, 6, 2, timestamp '2021-01-02 09:01'),
       --> Wendell John Victor Machine, Student
       (104, 2, 8, 2, timestamp '2021-01-02 09:02'),
       --> Wendell Back End, Student
       (105, 2, 10, 2, timestamp '2021-01-02 09:03'),

       -- Kotlin course
       --> James Gosling, Owner
       (201, 3, 2, 0, timestamp '2021-01-02 10:00'),
       --> Keith Master, Teacher
       (202, 3, 1, 1, timestamp '2021-01-02 11:00'),
       --> Wendell Cotlin, Student
       (203, 3, 7, 2, timestamp '2021-01-02 10:01'),
       --> Wendell John Victor Machine, Student
       (204, 3, 8, 2, timestamp '2021-01-02 10:02'),
       --> Wendell Back End, Student
       (205, 3, 10, 2, timestamp '2021-01-02 10:03'),

       -- Spring Framework course
       --> James Gosling, Owner
       (301, 4, 2, 0, timestamp '2021-01-02 11:00'),
       --> Keith Master, Teacher
       (302, 4, 1, 1, timestamp '2021-01-02 12:00'),
       --> Wendell John Victor Machine, Student
       (303, 4, 8, 2, timestamp '2021-01-02 11:01'),
       --> Wendell Spring, Student
       (304, 4, 9, 2, timestamp '2021-01-02 11:02'),
       --> Wendell Back End, Student
       (305, 4, 10, 2, timestamp '2021-01-02 11:03'),

       -- TypeScript course
       --> Brendan Eich, Owner
       (401, 5, 3, 0, timestamp '2021-01-03 09:00'),
       --> Wendell Type Script, Student
       (402, 5, 11, 2, timestamp '2021-01-03 09:01'),
       --> Wendell Front End, Student
       (403, 5, 13, 2, timestamp '2021-01-03 09:02'),

       -- React course
       --> Brendan Eich, Owner
       (504, 6, 3, 0, timestamp '2021-01-03 10:00'),
       --> Wendell React, Student
       (505, 6, 12, 2, timestamp '2021-01-03 10:01'),
       --> Wendell Front End, Student
       (506, 6, 13, 2, timestamp '2021-01-03 10:02'),

       -- PostgreSQL course
       --> James Gosling, Owner
       (601, 7, 2, 0, timestamp '2021-01-02 12:00'),
       --> Brendan Eich, Teacher
       (602, 7, 3, 1, timestamp '2021-01-02 12:01'),
       --> Brendan Burns, Teacher
       (603, 7, 4, 1, timestamp '2021-01-02 12:02'),
       --> Wendell Data Base, Student
       (604, 7, 14, 2, timestamp '2021-01-02 12:03'),

       -- Docker course
       --> Brendan Burns, Owner
       (701, 8, 4, 0, timestamp '2021-01-04 09:00'),
       --> Keith Master, Student
       (702, 8, 1, 2, timestamp '2021-01-04 10:00'),
       --> Wendell Docker, Student
       (703, 8, 15, 2, timestamp '2021-01-04 09:01'),
       --> Wendell Devante Operations, Student
       (704, 8, 17, 2, timestamp '2021-01-04 09:02'),

       -- Kubernetes course
       --> Brendan Burns, Owner
       (801, 9, 4, 0, timestamp '2021-01-04 10:00'),
       --> Keith Master, Student
       (802, 9, 1, 2, timestamp '2021-01-04 11:00'),
       --> Wendell Kites, Student
       (803, 9, 16, 2, timestamp '2021-01-04 10:01'),
       --> Wendell Devante Operations, Student
       (804, 9, 17, 2, timestamp '2021-01-04 10:02'),

       -- Gamification in Education course
       --> Leonardo da Vinci, Owner
       (901, 10, 5, 0, timestamp '2021-01-05 09:00'),
       --> Keith Master, Student
       (902, 10, 1, 2, timestamp '2021-01-05 10:00'),
       --> James Gosling, Student
       (903, 10, 2, 2, timestamp '2021-01-05 09:01'),
       --> Brendan Eich, Student
       (904, 10, 3, 2, timestamp '2021-01-05 09:02'),
       --> Brendan Burns, Student
       (905, 10, 4, 2, timestamp '2021-01-05 09:03'),

       -- English course
       --> Leonardo da Vinci, Owner
       (1001, 11, 5, 0, timestamp '2021-01-05 10:00')
;

SELECT setval('course_user_data_id_seq', 1000000);