# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table category (
  category_id                   bigint auto_increment not null,
  category_name                 varchar(255),
  constraint pk_category primary key (category_id)
);

create table ingredient (
  id_ingrediente                bigint auto_increment not null,
  nombre_ing                    varchar(255),
  unidades                      varchar(255),
  constraint pk_ingredient primary key (id_ingrediente)
);

create table recipe (
  recipe_id                     bigint auto_increment not null,
  title                         varchar(255),
  steps                         varchar(255),
  time                          varchar(255),
  difficulty                    varchar(11),
  category_category_id          bigint,
  constraint ck_recipe_difficulty check ( difficulty in ('Fácil','Muy fácil','Difícil','Muy difícil','Intermedia')),
  constraint pk_recipe primary key (recipe_id)
);

alter table recipe add constraint fk_recipe_category_category_id foreign key (category_category_id) references category (category_id) on delete restrict on update restrict;
create index ix_recipe_category_category_id on recipe (category_category_id);


# --- !Downs

alter table recipe drop constraint if exists fk_recipe_category_category_id;
drop index if exists ix_recipe_category_category_id;

drop table if exists category;

drop table if exists ingredient;

drop table if exists recipe;

