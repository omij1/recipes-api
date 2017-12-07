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
  id_receta                     bigint auto_increment not null,
  nombre                        varchar(255),
  pasos                         varchar(255),
  tiempo                        varchar(255),
  dificultad                    varchar(11),
  categoria_id_categoria        bigint,
  constraint ck_recipe_dificultad check ( dificultad in ('Fácil','Muy fácil','Difícil','Muy difícil','Intermedia')),
  constraint pk_recipe primary key (id_receta)
);

alter table recipe add constraint fk_recipe_categoria_id_categoria foreign key (categoria_id_categoria) references category (id_categoria) on delete restrict on update restrict;
create index ix_recipe_categoria_id_categoria on recipe (categoria_id_categoria);


# --- !Downs

alter table recipe drop constraint if exists fk_recipe_categoria_id_categoria;
drop index if exists ix_recipe_categoria_id_categoria;

drop table if exists category;

drop table if exists ingredient;

drop table if exists recipe;

