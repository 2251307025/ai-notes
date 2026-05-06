-- auto-generated definition
create table article
(
    id          serial
        primary key,
    title       varchar(30) not null,
    content     text        not null,
    cover_img   varchar(128),
    state       varchar(3) default '草稿'::character varying,
    category_id integer
        constraint fk_article_category
            references category,
    create_user integer     not null
        constraint fk_article_user
            references "user",
    create_time timestamp   not null,
    update_time timestamp   not null
);

comment on table article is '文章表';

comment on column article.id is 'ID';

comment on column article.title is '文章标题';

comment on column article.content is '文章内容';

comment on column article.cover_img is '文章封面';

comment on column article.state is '文章状态: 只能是[已发布] 或者 [草稿]';

comment on column article.category_id is '文章分类ID';

comment on column article.create_user is '创建人ID';

comment on column article.create_time is '创建时间';

comment on column article.update_time is '修改时间';

alter table article
    owner to root;
-- auto-generated definition
create table category
(
    id             serial
        primary key,
    category_name  varchar(32) not null,
    category_alias varchar(32) not null,
    create_user    integer     not null
        constraint fk_category_user
            references "user",
    create_time    timestamp   not null,
    update_time    timestamp   not null
);

comment on table category is '分类表';

comment on column category.id is 'ID';

comment on column category.category_name is '分类名称';

comment on column category.category_alias is '分类别名';

comment on column category.create_user is '创建人ID';

comment on column category.create_time is '创建时间';

comment on column category.update_time is '修改时间';

alter table category
    owner to root;

-- auto-generated definition
create table "user"
(
    id          serial
        primary key,
    username    varchar(20) not null
        unique,
    password    varchar(32),
    nickname    varchar(10)  default ''::character varying,
    email       varchar(128) default ''::character varying,
    user_pic    varchar(128) default ''::character varying,
    create_time timestamp   not null,
    update_time timestamp   not null
);

comment on table "user" is '用户表';

comment on column "user".id is 'ID';

comment on column "user".username is '用户名';

comment on column "user".password is '密码';

comment on column "user".nickname is '昵称';

comment on column "user".email is '邮箱';

comment on column "user".user_pic is '头像';

comment on column "user".create_time is '创建时间';

comment on column "user".update_time is '修改时间';

alter table "user"
    owner to root;


