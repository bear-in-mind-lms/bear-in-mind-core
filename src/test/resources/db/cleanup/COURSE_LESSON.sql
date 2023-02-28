DELETE FROM course_lessons cl WHERE cl.id >= 1000000;
SELECT setval('course_lessons_id_seq', 1000000);