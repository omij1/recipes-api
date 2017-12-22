# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table api_key (
  id                            bigint auto_increment not null,
  api_key                       varchar(255),
  version                       bigint not null,
  created                       timestamp not null,
  updated                       timestamp not null,
  constraint pk_api_key primary key (id)
);

create table category (
  id                            bigint auto_increment not null,
  category_name                 varchar(255),
  version                       bigint not null,
  created                       timestamp not null,
  updated                       timestamp not null,
  constraint pk_category primary key (id)
);

create table ingredient (
  id                            bigint auto_increment not null,
  ingredient_name               varchar(255),
  units                         varchar(255),
  version                       bigint not null,
  created                       timestamp not null,
  updated                       timestamp not null,
  constraint pk_ingredient primary key (id)
);

create table recipe (
  id                            bigint auto_increment not null,
  title                         varchar(255),
  steps                         varchar(255),
  time                          varchar(255),
  difficulty                    varchar(11),
  category_id                   bigint,
  version                       bigint not null,
  created                       timestamp not null,
  updated                       timestamp not null,
  constraint ck_recipe_difficulty check ( difficulty in ('Fácil','Muy fácil','Difícil','Muy difícil','Intermedia')),
  constraint pk_recipe primary key (id)
);

create table recipe_ingredient (
  recipe_id                     bigint not null,
  ingredient_id                 bigint not null,
  constraint pk_recipe_ingredient primary key (recipe_id,ingredient_id)
);

create table user (
  id                            bigint auto_increment not null,
  nick                          varchar(255),
  name                          varchar(255),
  surname                       varchar(255),
  city                          varchar(255),
  api_key_id                    bigint,
  version                       bigint not null,
  created                       timestamp not null,
  updated                       timestamp not null,
  constraint uq_user_api_key_id unique (api_key_id),
  constraint pk_user primary key (id)
);

alter table recipe add constraint fk_recipe_category_id foreign key (category_id) references category (id) on delete restrict on update restrict;
create index ix_recipe_category_id on recipe (category_id);

alter table recipe_ingredient add constraint fk_recipe_ingredient_recipe foreign key (recipe_id) references recipe (id) on delete restrict on update restrict;
create index ix_recipe_ingredient_recipe on recipe_ingredient (recipe_id);

alter table recipe_ingredient add constraint fk_recipe_ingredient_ingredient foreign key (ingredient_id) references ingredient (id) on delete restrict on update restrict;
create index ix_recipe_ingredient_ingredient on recipe_ingredient (ingredient_id);

alter table user add constraint fk_user_api_key_id foreign key (api_key_id) references api_key (id) on delete restrict on update restrict;


# --- !Downs

alter table recipe drop constraint if exists fk_recipe_category_id;
drop index if exists ix_recipe_category_id;

alter table recipe_ingredient drop constraint if exists fk_recipe_ingredient_recipe;
drop index if exists ix_recipe_ingredient_recipe;

alter table recipe_ingredient drop constraint if exists fk_recipe_ingredient_ingredient;
drop index if exists ix_recipe_ingredient_ingredient;

alter table user drop constraint if exists fk_user_api_key_id;

drop table if exists api_key;

drop table if exists category;

drop table if exists ingredient;

drop table if exists recipe;

drop table if exists recipe_ingredient;

drop table if exists user;

