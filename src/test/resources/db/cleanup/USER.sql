DELETE FROM users us WHERE us.id >= 1000000;
SELECT setval('users_id_seq', 1000000);