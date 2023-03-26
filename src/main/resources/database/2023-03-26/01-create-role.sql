--liquibase formatted sql
--changeset Klizlo:1

create table role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name varchar(100) not null,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp,
    constraint role_name_uq UNIQUE (name)
);