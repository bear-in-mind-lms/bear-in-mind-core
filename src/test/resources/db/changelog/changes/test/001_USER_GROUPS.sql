SET TIME ZONE 'UTC';

INSERT INTO user_groups(id, name_identifier, image, creation_date_time)
VALUES -- Administrators
       (1, 5001, null, timestamp '2020-01-01 08:01'),
       -- Teachers
       (2, 5002, null, timestamp '2020-01-05 08:01'),
       -- Students
       (3, 5003, null, timestamp '2020-01-06 08:01')
;

-- OWNER = 0, MEMBER = 1, INVITED = 2, APPLICANT = 3
INSERT INTO user_group_members(id, group_id, user_id, role, registration_date_time)
VALUES -- Administrators group
       --> Keith Master, Owner
       (1, 1, 1, 0, timestamp '2020-01-01 08:01'),

       -- Teachers group
       --> Leonardo da Vinci, Owner
       (101, 2, 5, 0, timestamp '2020-01-05 08:01'),
       --> Keith Master, Member
       (102, 2, 1, 1, timestamp '2020-01-05 08:02'),
       --> James Gosling, Member
       (103, 2, 2, 1, timestamp '2020-01-05 08:03'),
       --> Brendan Eich, Member
       (104, 2, 3, 1, timestamp '2020-01-05 08:04'),
       --> Brendan Burns, Member
       (105, 2, 4, 1, timestamp '2020-01-05 08:05'),

       -- Students group
       --> Wendell Java, Owner
       (201, 3, 6, 0, timestamp '2020-01-06 08:01'),
       --> Wendell Cotlin, Member
       (202, 3, 7, 1, timestamp '2020-01-06 08:02'),
       --> Wendell John Victor Machine, Member
       (203, 3, 8, 1, timestamp '2020-01-06 08:03'),
       --> Wendell Spring, Member
       (204, 3, 9, 1, timestamp '2020-01-06 08:04'),
       --> Wendell Back End, Member
       (205, 3, 10, 1, timestamp '2020-01-06 08:05'),
       --> Wendell Type Script, Member
       (206, 3, 11, 1, timestamp '2020-01-06 08:06'),
       --> Wendell React, Member
       (207, 3, 12, 1, timestamp '2020-01-06 08:07'),
       --> Wendell Front End, Member
       (208, 3, 13, 1, timestamp '2020-01-06 08:08'),
       --> Wendell Data Base, Member
       (209, 3, 14, 1, timestamp '2020-01-06 08:09'),
       --> Wendell Docker, Member
       (210, 3, 15, 1, timestamp '2020-01-06 08:10'),
       --> Wendell Kites, Member
       (211, 3, 16, 1, timestamp '2020-01-06 08:11'),
       --> Wendell Devante Operations, Member
       (212, 3, 17, 1, timestamp '2020-01-06 08:12')
;

SELECT setval('user_groups_id_seq', 1000000);
SELECT setval('user_group_members_id_seq', 1000000);