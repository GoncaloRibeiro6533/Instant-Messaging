CREATE SCHEMA IF NOT EXISTS dbo;

DROP TABLE IF EXISTS dbo.USER_CHANNEL_ROLE CASCADE;
DROP TABLE IF EXISTS dbo.MESSAGE CASCADE;
DROP TABLE IF EXISTS dbo.CHANNEL CASCADE;
DROP TABLE IF EXISTS dbo.REGISTER_INVITATION CASCADE;
DROP TABLE IF EXISTS dbo.CHANNEL_INVITATION CASCADE;
DROP TABLE IF EXISTS dbo.ROLE CASCADE;
DROP TABLE IF EXISTS dbo.SESSION CASCADE;
DROP TABLE IF EXISTS dbo.USER CASCADE;


CREATE TABLE IF NOT EXISTS dbo.USER(
	id SERIAL PRIMARY KEY,
	email VARCHAR(60) UNIQUE NOT NULL,
	username VARCHAR(60) UNIQUE NOT NULL,
	password VARCHAR(256) NOT NULL
);


CREATE TABLE IF NOT EXISTS dbo.SESSION(
	token VARCHAR(36) UNIQUE NOT NULL,
	user_id int NOT NULL,
    created_at bigint not null,
    last_used_at bigint not null,
	PRIMARY KEY (TOKEN, user_id),
	CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES dbo.USER(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS dbo.ROLE(
	name VARCHAR(60)  PRIMARY KEY NOT NULL CHECK ( name in ('READ_ONLY', 'READ_WRITE')),
	description VARCHAR(500) NOT NULL
);


CREATE TABLE IF NOT EXISTS dbo.CHANNEL(
	id SERIAL PRIMARY KEY,
	name VARCHAR(60) UNIQUE NOT NULL,
	creator_id int NOT NULL,
	visibility VARCHAR(7) NOT NULL CHECK( visibility IN ('PUBLIC', 'PRIVATE')),
	CONSTRAINT fk_creator FOREIGN KEY(creator_id) REFERENCES dbo.USER(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS dbo.CHANNEL_INVITATION(
	id SERIAL PRIMARY KEY,
	role_name VARCHAR(10) NOT NULL,
	used BOOLEAN DEFAULT FALSE NOT NULL,
	channel_id int NOT NULL,
	invited_id int NOT NULL,
	inviter_id int NOT NULL,
	CONSTRAINT fk_channel FOREIGN KEY(channel_id) REFERENCES dbo.CHANNEL(id) ON DELETE CASCADE,
	CONSTRAINT fk_inviter FOREIGN KEY(inviter_id) REFERENCES dbo.USER(id) ON DELETE CASCADE,
	CONSTRAINT fk_invited FOREIGN KEY(invited_id) REFERENCES dbo.USER(id) ON DELETE CASCADE,
	CONSTRAINT fk_role FOREIGN KEY(role_name) REFERENCES dbo.ROLE(name) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS dbo.REGISTER_INVITATION(
	id SERIAL PRIMARY KEY,
	role_name VARCHAR(10),
	used BOOLEAN DEFAULT FALSE NOT NULL,
	channel_id int,
	invited_email VARCHAR(60) NOT NULL,
	inviter_id int NOT NULL,
	CONSTRAINT fk_channel FOREIGN KEY(channel_id) REFERENCES dbo.CHANNEL(id) ON DELETE CASCADE,
	CONSTRAINT fk_user FOREIGN KEY(inviter_id) REFERENCES dbo.USER(id) ON DELETE CASCADE,
	CONSTRAINT fk_role FOREIGN KEY(role_name) REFERENCES dbo.ROLE(name) ON DELETE CASCADE
);



CREATE TABLE IF NOT EXISTS dbo.MESSAGE(
	id SERIAL PRIMARY KEY,
	creationTime TIMESTAMP NOT NULL,
	user_id int NOT NULL,
	channel_id int NOT NULL,
	message VARCHAR(500),
	CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES dbo.USER(id) ON DELETE CASCADE,
	CONSTRAINT fk_channel FOREIGN KEY(channel_id) REFERENCES dbo.CHANNEL(id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS dbo.USER_CHANNEL_ROLE(
	user_id int NOT NULL,
	channel_id int NOT NULL,
	role_name VARCHAR(10) NOT NULL,
	PRIMARY KEY(user_id, channel_id, role_name),
	CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES dbo.USER(id) ON DELETE CASCADE,
	CONSTRAINT fk_channel FOREIGN KEY(channel_id) REFERENCES dbo.CHANNEL(id) ON DELETE CASCADE,
	CONSTRAINT fk_role FOREIGN KEY(role_name) REFERENCES dbo.ROLE(name) ON DELETE CASCADE
);
