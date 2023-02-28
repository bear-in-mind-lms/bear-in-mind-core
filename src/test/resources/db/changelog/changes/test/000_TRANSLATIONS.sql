SET TIME ZONE 'UTC';

-- Resource             | Identifier range
-- Course               |    1 - 1000
-- Course lesson        | 1001 - 3000
-- Course lesson part   | 3001 - 5000
-- User groups          | 5001 - 6000
INSERT INTO translations(id, identifier, locale, text)
VALUES -- Administration course
       (1, 1, 'en', 'Administration'),
       --> Linux
       (1001, 1001, 'en', 'Linux'),

       -- Java course
       (3, 3, 'en', 'Java'),
       (4, 4, 'en', 'Discover the finest Indonesian coffee'),
       --> Hello World lesson
       (1101, 1101, 'en', 'Hello Java World!'),
       (1102, 1102, 'en', 'World and how to greet it'),
       (3101, 3101, 'en', '<b><i>Hello</i></b> is a salutation or greeting in the English language.'),
       (3103, 3103, 'en', 'It is first attested in writing from 1826.'),
       --> Variables lesson
       (1103, 1103, 'en', 'Variables'),
       (1104, 1104, 'en', 'Data types and how to use them'),
       --> Classes lesson
       (1105, 1105, 'en', 'Classes'),
       (1106, 1106, 'en', 'Classes and how to instantiate them'),

       -- Kotlin course
       (5, 5, 'en', 'Kotlin'),

       -- Spring Framework course
       (7, 7, 'en', 'Spring Framework'),
       (8, 8, 'en', 'Let your server bloom'),

       -- TypeScript course
       (9, 9, 'en', 'TypeScript'),
       (10, 10, 'en', 'Meet the sane brother of JavaScript'),
       --> Hello World lesson
       (1401, 1401, 'en', 'Hello TypeScript World!'),

       -- React course
       (11, 11, 'en', 'React'),
       (12, 12, 'en', 'React aka React.js aka ReactJS from Meta aka Facebook'),

       -- PostgreSQL course
       (13, 13, 'en', 'PostgreSQL'),

       -- Docker course
       (15, 15, 'en', 'Docker'),
       (16, 16, 'en', 'Put the container on the whale''s back'),

       -- Kubernetes course
       (17, 17, 'en', 'Kubernetes'),
       (18, 18, 'en', '/ˌk(j)uːbərˈnɛtɪs, -ˈneɪtɪs, -ˈneɪtiːz, -ˈnɛtiːz/'),

       -- Gamification in Education course
       (19, 19, 'en', 'Gamification in Education'),
       (20, 20, 'en', 'Press any key to teach'),
       --> What is gamification lesson
       (1901, 1901, 'en', 'What is gamification'),

       -- English course
       (21, 21, 'en', 'English'),
       (22, 21, 'es', 'Inglés'),
       (23, 21, 'esES', 'Inglés para españoles'),
       (24, 21, 'esMX', 'Inglés para mexicanos'),
       (25, 21, 'da', 'Engelsk'),
       (26, 21, 'pl', 'Angielski'),

       -- Administrators group
       (5001, 5001, 'en', 'Administrators'),

       -- Teachers group
       (5002, 5002, 'en', 'Teachers'),

       -- Students group
       (5003, 5003, 'en', 'Students')
;

SELECT setval('translations_id_seq', 1000000);
SELECT setval('translations_identifier_seq', 1000000);