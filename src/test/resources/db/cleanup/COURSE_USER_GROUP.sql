DELETE FROM course_user_group cug WHERE cug.id >= 1000000;
SELECT setval('course_user_groups_id_seq', 1000000);