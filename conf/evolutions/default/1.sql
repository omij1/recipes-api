# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table api_key (
  id                            bigint auto_increment not null,
  api_key                       varchar(255),
  constraint pk_api_key primary key (id)
);

create table user (
  id_user                       bigint auto_increment not null,
  nick                          varchar(255),
  name                          varchar(255),
  surname                       varchar(255),
  city                          varchar(255),
  api_key_id                    bigint,
  constraint uq_user_api_key_id unique (api_key_id),
  constraint pk_user primary key (id_user)
);

alter table user add constraint fk_user_api_key_id foreign key (api_key_id) references api_key (id) on delete restrict on update restrict;


# --- !Downs

alter table user drop constraint if exists fk_user_api_key_id;

drop table if exists api_key;

drop table if exists user;

