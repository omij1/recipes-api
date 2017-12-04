# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table category (
  id_categoria                  bigint auto_increment not null,
  nombre_categoria              varchar(255),
  constraint pk_category primary key (id_categoria)
);

create table ingredient (
  id_ingrediente                bigint auto_increment not null,
  nombre_ing                    varchar(255),
  unidades                      varchar(255),
  constraint pk_ingredient primary key (id_ingrediente)
);

create table recipe (
);


# --- !Downs

drop table if exists category;

drop table if exists ingredient;

drop table if exists recipe;

