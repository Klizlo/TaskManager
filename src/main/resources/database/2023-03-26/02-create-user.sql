--liquibase formatted sql
--changeset Klizlo:2

create table users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username varchar(100) not null,
  email varchar(255) not null,
  password varchar(255) not null,
  created_at timestamp default current_timestamp,
  updated_at timestamp default current_timestamp on update current_timestamp,
  constraint UC_user_name unique (username),
  constraint UC_user_email unique (email)
);