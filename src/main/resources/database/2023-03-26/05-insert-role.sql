--liquibase formatted sql
--changeset Klizlo:5

insert into role (name) values ('ADMIN');
insert into role (name) values ('USER');