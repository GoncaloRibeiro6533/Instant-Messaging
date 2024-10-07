drop table if exists users cascade;
drop table if exists channels cascade;
drop table if exists channel_users cascade;
drop table if exists messages cascade;
drop table if exists invitations cascade;

create table users (
    id serial primary key,
    username varchar(255) not null unique,
    password varchar(255) not null,
    email varchar(255) not null unique,
    token uuid not null
);

create table channels (
    id serial primary key,
    name varchar(255) not null unique,
    creator_id integer references users(id),
    visibility varchar(255) not null check ( visibility in ('PUBLIC', 'PRIVATE'))
);

create table channel_users (
    id serial primary key,
    user_id integer references users(id),
    channel_id integer references channels(id),
    role varchar(255) not null check ( role in ('READ_ONLY', 'READ_WRITE'))
);

create table messages (
    id serial primary key,
    sender_id integer references users(id),
    channel_id integer references channels(id),
    content text not null,
    timestamp date not null
);

create table channel_messages (
    id serial primary key,
    channel_id integer references channels(id),
    message_id integer references messages(id)
);

create table invitations (
    id serial primary key,
    sender_id integer references users(id),
    receiver_id integer references users(id),
    channel_id integer references channels(id),
    status boolean not null,
    timestamp date not null
);

