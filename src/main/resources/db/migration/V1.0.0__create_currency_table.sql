create sequence users_seq;
create table users (
                       id bigint default nextval('users_seq'),
                       name varchar(255) not null,
                       email varchar(255) not null,
                       password varchar(255) not null,
                       role varchar(255) not null,
                       primary key (id));
create unique index users_unique_email_idx on users (email);
