DELETE FROM user_credentials usc WHERE usc.id >= 1000000;
SELECT setval('user_credentials_id_seq', 1000000);