create table user
(
    id       int auto_increment
        primary key,
    username varchar(16) not null comment '用户名',
    password varchar(32) not null comment '密码',
    constraint id
        unique (id),
    constraint username
        unique (username)
);

