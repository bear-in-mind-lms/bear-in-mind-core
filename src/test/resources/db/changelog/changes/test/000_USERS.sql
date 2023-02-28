SET TIME ZONE 'UTC';

INSERT INTO user_credentials(id, username, password, role, active)
VALUES -- Mr Keith Master, Administrator
       (1, 'mrmasterkey', '$2a$04$PTBFP2iKcm1CXmjn6uMFk.IbgAPisD3MH.fGvhBN40vF97RuwVYVm', 'ADMINISTRATOR_ROLE_GROUP', true),
       -- PhD James Gosling, Teacher
       (2, 'teacherbackend', '$2a$04$PTBFP2iKcm1CXmjn6uMFk.IbgAPisD3MH.fGvhBN40vF97RuwVYVm', 'TEACHER_ROLE_GROUP', true),
       -- Brendan Eich, Teacher
       (3, 'teacherfrontend', '$2a$04$PTBFP2iKcm1CXmjn6uMFk.IbgAPisD3MH.fGvhBN40vF97RuwVYVm', 'TEACHER_ROLE_GROUP', true),
       -- Brendan Burns, Teacher
       (4, 'teacherdevops', '$2a$04$PTBFP2iKcm1CXmjn6uMFk.IbgAPisD3MH.fGvhBN40vF97RuwVYVm', 'TEACHER_ROLE_GROUP', true),
       -- Leonardo di ser Piero da Vinci, Teacher
       (5, 'teacherofteachers', '$2a$04$PTBFP2iKcm1CXmjn6uMFk.IbgAPisD3MH.fGvhBN40vF97RuwVYVm', 'TEACHER_ROLE_GROUP', true),
       -- Wendell Java, Student
       (6, 'studentjava', '$2a$04$PTBFP2iKcm1CXmjn6uMFk.IbgAPisD3MH.fGvhBN40vF97RuwVYVm', 'STUDENT_ROLE_GROUP', true),
       -- Wendell Cotlin, Student
       (7, 'studentkotlin', '$2a$04$PTBFP2iKcm1CXmjn6uMFk.IbgAPisD3MH.fGvhBN40vF97RuwVYVm', 'STUDENT_ROLE_GROUP', true),
       -- Wendell John Victor Machine, Student
       (8, 'studentjvm', '$2a$04$PTBFP2iKcm1CXmjn6uMFk.IbgAPisD3MH.fGvhBN40vF97RuwVYVm', 'STUDENT_ROLE_GROUP', true),
       -- Wendell Spring, Student
       (9, 'studentspring', '$2a$04$PTBFP2iKcm1CXmjn6uMFk.IbgAPisD3MH.fGvhBN40vF97RuwVYVm', 'STUDENT_ROLE_GROUP', true),
       -- Wendell Back End, Student
       (10, 'studentbackend', '$2a$04$PTBFP2iKcm1CXmjn6uMFk.IbgAPisD3MH.fGvhBN40vF97RuwVYVm', 'STUDENT_ROLE_GROUP', true),
       -- Wendell Type Script, Student
       (11, 'studenttypescript', '$2a$04$PTBFP2iKcm1CXmjn6uMFk.IbgAPisD3MH.fGvhBN40vF97RuwVYVm', 'STUDENT_ROLE_GROUP', true),
       -- Wendell React, Student
       (12, 'studentreact', '$2a$04$PTBFP2iKcm1CXmjn6uMFk.IbgAPisD3MH.fGvhBN40vF97RuwVYVm', 'STUDENT_ROLE_GROUP', true),
       -- Wendell Front End, Student
       (13, 'studentfrontend', '$2a$04$PTBFP2iKcm1CXmjn6uMFk.IbgAPisD3MH.fGvhBN40vF97RuwVYVm', 'STUDENT_ROLE_GROUP', true),
       -- Wendell Data Base, Student
       (14, 'studentdatabase', '$2a$04$PTBFP2iKcm1CXmjn6uMFk.IbgAPisD3MH.fGvhBN40vF97RuwVYVm', 'STUDENT_ROLE_GROUP', true),
       -- Wendell Docker, Student
       (15, 'studentdocker', '$2a$04$PTBFP2iKcm1CXmjn6uMFk.IbgAPisD3MH.fGvhBN40vF97RuwVYVm', 'STUDENT_ROLE_GROUP', true),
       -- Wendell Kites, Student
       (16, 'studentkubernetes', '$2a$04$PTBFP2iKcm1CXmjn6uMFk.IbgAPisD3MH.fGvhBN40vF97RuwVYVm', 'STUDENT_ROLE_GROUP', true),
       -- Wendell Devante Operations, Student
       (17, 'studentdevops', '$2a$04$PTBFP2iKcm1CXmjn6uMFk.IbgAPisD3MH.fGvhBN40vF97RuwVYVm', 'STUDENT_ROLE_GROUP', true)
;

INSERT INTO users(id, first_name, middle_name, last_name, title, email, phone_number, locale, image, registration_date_time, user_credentials_id)
VALUES
       (1, 'Keith', null, 'Master', 'Mr', 'mrmasterkey@bearinmind.kwezal.com', null, 'daDK', null, timestamp '2020-01-01 08:00', 1),
       (2, 'James', null, 'Gosling', 'PhD', 'teacherbackend@bearinmind.kwezal.com', null, 'enCA', null, timestamp '2020-01-02 08:00', 2),
       (3, 'Brendan', null, 'Eich', null, 'teacherfrontend@bearinmind.kwezal.com', null, 'enUS', null, timestamp '2020-01-03 08:00', 3),
       (4, 'Brendan', null, 'Burns', null, 'teacherdevops@bearinmind.kwezal.com', null, 'enUS', null, timestamp '2020-01-04 08:00', 4),
       (5, 'Leonardo', 'di ser Piero', 'da Vinci', null, 'teacherofteachers@bearinmind.kwezal.com', null, 'itIT', null, timestamp '2020-01-05 08:00', 5),
       (6, 'Wendell', null, 'Java', null, 'studentjava@bearinmind.kwezal.com', null, 'en', null, timestamp '2020-01-06 08:00', 6),
       (7, 'Wendell', null, 'Cotlin', null, 'studentkotlin@bearinmind.kwezal.com', null, 'en', null, timestamp '2020-01-07 08:00', 7),
       (8, 'Wendell', 'John Victor', 'Machine', null, 'studentjvm@bearinmind.kwezal.com', null, 'en', null, timestamp '2020-01-08 08:00', 8),
       (9, 'Wendell', null, 'Spring', null, 'studentspring@bearinmind.kwezal.com', null, 'en', null, timestamp '2020-01-09 08:00', 9),
       (10, 'Wendell', 'Back', 'End', null, 'studentbackend@bearinmind.kwezal.com', null, 'en', null, timestamp '2020-01-10 08:00', 10),
       (11, 'Wendell', 'Type', 'Script', null, 'studenttypescript@bearinmind.kwezal.com', null, 'en', null, timestamp '2020-01-11 08:00', 11),
       (12, 'Wendell', null, 'React', null, 'studentreact@bearinmind.kwezal.com', null, 'en', null, timestamp '2020-01-12 08:00', 12),
       (13, 'Wendell', 'Front', 'End', null, 'studentfrontend@bearinmind.kwezal.com', null, 'en', null, timestamp '2020-01-13 08:00', 13),
       (14, 'Wendell', 'Data', 'Base', null, 'studentdatabase@bearinmind.kwezal.com', null, 'en', null, timestamp '2020-01-14 08:00', 14),
       (15, 'Wendell', null, 'Docker', null, 'studentdocker@bearinmind.kwezal.com', null, 'en', null, timestamp '2020-01-15 08:00', 15),
       (16, 'Wendell', null, 'Kites', null, 'studentkubernetes@bearinmind.kwezal.com', null, 'en', null, timestamp '2020-01-16 08:00', 16),
       (17, 'Wendell', 'Devante', 'Operations', null, 'studentdevops@bearinmind.kwezal.com', null, 'en', null, timestamp '2020-01-17 08:00', 17)
;

SELECT setval('user_credentials_id_seq', 1000000);
SELECT setval('users_id_seq', 1000000);