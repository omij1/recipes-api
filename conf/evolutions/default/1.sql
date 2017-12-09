# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table user (
  id_user                       bigint auto_increment not null,
  nick                          varchar(255),
  name                          varchar(255),
  surname                       varchar(255),
  city                          varchar(255),
  constraint pk_user primary key (id_user)
);


# --- !Downs

drop table if exists user;

