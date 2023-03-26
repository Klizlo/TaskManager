--liquibase formatted sql
--changeset Klizlo:3

create table user_roles (
    user_id bigint,
    role_id bigint,
    constraint user_role_id
        foreign key (role_id) references `role`(id),
    constraint role_user_id
        foreign key (user_id) references `users`(id)
);