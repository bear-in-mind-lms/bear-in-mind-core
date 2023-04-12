SET TIME ZONE 'UTC';

INSERT INTO courses(id, name_identifier, description_identifier, image, start_date_time, end_date_time, creation_date_time, registration_closing_date_time)
VALUES -- Administration
       (1, 1, null, null, timestamp '2020-01-01 09:00', timestamp '2021-01-01 09:00', timestamp '2020-01-01 09:00', null),
       -- Java
       (2, 3, 4, null, null, null, timestamp '2020-01-02 09:00', null),
       -- Kotlin
       (3, 5, null, null, timestamp '2020-01-02 10:00', null, timestamp '2020-01-02 10:00', null),
       -- Spring Framework
       (4, 7, 8, null, timestamp '2020-01-02 09:00', null, timestamp '2020-01-02 11:00', null),
       -- TypeScript
       (5, 9, 10, null, null, null, timestamp '2020-01-03 09:00', null),
       -- React
       (6, 11, 12, null, timestamp '2020-01-03 10:00', null, timestamp '2020-01-03 10:00', null),
       -- PostgreSQL
       (7, 13, null, null, null, timestamp '2021-01-02 12:00', timestamp '2020-01-02 12:00', null),
       -- Docker
       (8, 15, 16, null, null, current_timestamp + interval '2013' day, timestamp '2020-01-04 09:00', null),
       -- Kubernetes
       (9, 17, 18, null, timestamp '2020-01-04 10:00', timestamp '2021-01-04 10:00', timestamp '2020-01-04 10:00', null),
       -- Gamification in Education
       (10, 19, 20, null, null, null, timestamp '2020-01-05 09:00', null),
       -- English
       (11, 21, null, null, timestamp '2020-01-05 10:00', current_timestamp + interval '365' day, timestamp '2020-01-05 10:00', current_timestamp + interval '7' day)
;

INSERT INTO course_lessons(id, course_id, topic_identifier, description_identifier, ordinal, start_date_time)
VALUES -- Administration course
       --> Linux
       (1, 1, 1001, null, 1, null),

       -- Java course
       --> Hello World
       (101, 2, 1101, 1102, 1, null),
       --> Variables
       (102, 2, 1103, 1104, 2, null),
       --> Classes
       (103, 2, 1105, 1106, 3, null),

       -- TypeScript course
       --> Hello World
       (401, 5, 1401, null, 1, null),

       -- Gamification in Education course
       --> What is gamification
       (901, 10, 1901, null, 1, current_timestamp + interval '1962' day)
;

INSERT INTO course_lesson_parts(id, course_lesson_id, text_identifier, attachments, ordinal)
VALUES -- Java course
       --> Hello World lesson
       (101, 101, 3101, null, 1),
       (102, 101, null, 'Source:https://en.wikipedia.org/wiki/Hello', 2),
       (103, 101, 3103, ':https://www.oed.com/\nModern English-Old High German dictionary:https://www.koeblergerhard.de/germanistischewoerterbuecher/althochdeutscheswoerterbuch/neuenglisch-ahd.pdf', 3)
;

SELECT setval('courses_id_seq', 1000000);
SELECT setval('course_lessons_id_seq', 1000000);
SELECT setval('course_lesson_parts_id_seq', 1000000);