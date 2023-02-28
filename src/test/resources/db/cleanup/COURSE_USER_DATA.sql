DELETE FROM course_user_data cud WHERE cud.id >= 1000000;
SELECT setval('course_user_data_id_seq', 1000000);