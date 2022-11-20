create table model_photo
(
    id       int auto_increment
        primary key,
    photo    longblob         not null,
    model_id int              not null,
    preview  double default 0 null,
    constraint id
        unique (id),
    constraint model_photo_model_id_fk
        foreign key (model_id) references model (id)
);

