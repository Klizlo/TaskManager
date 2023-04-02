-- liquibase formatted sql
-- changeset Klizlo:6

CREATE TABLE category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name varchar(255) not null,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp,
    owner_id BIGINT not null,
    constraint category_owner_id_fk FOREIGN KEY (owner_id) REFERENCES `users`(id)
);