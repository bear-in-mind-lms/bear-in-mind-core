DELETE FROM user_groups ug WHERE ug.id >= 1000000;
SELECT setval('user_groups_id_seq', 1000000);