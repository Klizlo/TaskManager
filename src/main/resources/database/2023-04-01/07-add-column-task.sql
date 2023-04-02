-- liquibase formatted sql
-- changeset Klizlo:7

ALTER TABLE task
    ADD category_id BIGINT null default null;

ALTER TABLE task
    ADD CONSTRAINT task_category_id_fk FOREIGN KEY (category_id) REFERENCES category(id);