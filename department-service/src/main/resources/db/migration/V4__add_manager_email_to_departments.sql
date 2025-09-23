
create schema if not exists department;

alter table department.departments
    add column if not exists manager_email varchar(200);
