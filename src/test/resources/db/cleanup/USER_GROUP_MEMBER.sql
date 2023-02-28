DELETE FROM user_group_members ugm WHERE ugm.id >= 1000000;
SELECT setval('user_group_members_id_seq', 1000000);