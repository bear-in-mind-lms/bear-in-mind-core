DELETE FROM courses c WHERE c.id >= 1000000;
SELECT setval('courses_id_seq', 1000000);