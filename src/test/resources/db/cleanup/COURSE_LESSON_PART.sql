DELETE FROM course_lesson_parts clp WHERE clp.id >= 1000000;
SELECT setval('course_lesson_parts_id_seq', 1000000);