create table model
(
    id      int auto_increment
        primary key,
    title   varchar(32) not null,
    user_id int         not null,
    constraint id
        unique (id),
    constraint model_user_id_fk
        foreign key (user_id) references user (id)
);

