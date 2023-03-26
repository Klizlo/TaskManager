--liquibase formatted sql
--changeset Klizlo:4

create table task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name varchar(255) not null,
    description text null,
    priority enum('LOW', 'MEDIUM', 'HIGH') not null,
    deadline timestamp null,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp on update current_timestamp,
    owner_id BIGINT not null,
    constraint task_owner_id_fk foreign key (owner_id) references `users`(id)
);