CREATE SCHEMA IF NOT EXISTS dbo;

DROP TABLE IF EXISTS dbo.USER_CHANNEL_ROLE CASCADE;
DROP TABLE IF EXISTS dbo.MESSAGE CASCADE;
DROP TABLE IF EXISTS dbo.CHANNEL CASCADE;
DROP TABLE IF EXISTS dbo.REGISTER_INVITATION CASCADE;
DROP TABLE IF EXISTS dbo.CHANNEL_INVITATION CASCADE;
DROP TABLE IF EXISTS dbo.ROLE CASCADE;
DROP TABLE IF EXISTS dbo.TOKEN CASCADE;
DROP TABLE IF EXISTS dbo.USER CASCADE;


CREATE TABLE IF NOT EXISTS dbo.USER(
	id SERIAL PRIMARY KEY,
	email VARCHAR(255) UNIQUE NOT NULL,
	username VARCHAR(255) UNIQUE NOT NULL,
	password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS dbo.TOKEN(
	token VARCHAR(256) UNIQUE NOT NULL,
	user_id int NOT NULL,
    created_at bigint not null,
    last_used_at bigint not null,
	PRIMARY KEY (TOKEN, user_id),
	CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES dbo.USER(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS dbo.CHANNEL(
	id SERIAL PRIMARY KEY,
	name VARCHAR(255) UNIQUE NOT NULL,
	creator_id int NOT NULL,
	visibility VARCHAR(10) NOT NULL CHECK( visibility IN ('PUBLIC', 'PRIVATE')),
	CONSTRAINT fk_creator FOREIGN KEY(creator_id) REFERENCES dbo.USER(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS dbo.CHANNEL_INVITATION(
	id SERIAL PRIMARY KEY,
	role_name VARCHAR(10) NOT NULL CHECK (role_name IN ('READ_ONLY', 'READ_WRITE')),
	used BOOLEAN DEFAULT FALSE NOT NULL,
	channel_id int NOT NULL,
	invited_id int NOT NULL,
	inviter_id int NOT NULL,
    timestamp timestamp not null,
	CONSTRAINT fk_channel FOREIGN KEY(channel_id) REFERENCES dbo.CHANNEL(id) ON DELETE CASCADE,
	CONSTRAINT fk_inviter FOREIGN KEY(inviter_id) REFERENCES dbo.USER(id) ON DELETE CASCADE,
	CONSTRAINT fk_invited FOREIGN KEY(invited_id) REFERENCES dbo.USER(id) ON DELETE CASCADE
	);

CREATE TABLE IF NOT EXISTS dbo.REGISTER_INVITATION(
	id SERIAL PRIMARY KEY,
	role_name VARCHAR(10) CHECK (role_name IN ('READ_ONLY', 'READ_WRITE')),
	used BOOLEAN DEFAULT FALSE NOT NULL,
	channel_id int,
	invited_email VARCHAR(255) NOT NULL,
	inviter_id int NOT NULL,
    timestamp timestamp not null,
	CONSTRAINT fk_channel FOREIGN KEY(channel_id) REFERENCES dbo.CHANNEL(id) ON DELETE CASCADE,
	CONSTRAINT fk_user FOREIGN KEY(inviter_id) REFERENCES dbo.USER(id) ON DELETE CASCADE
);



CREATE TABLE IF NOT EXISTS dbo.MESSAGE(
	id SERIAL PRIMARY KEY,
	creationTime timestamp NOT NULL,
	user_id int NOT NULL,
	channel_id int NOT NULL,
	message VARCHAR(2064) NOT NULL,
	CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES dbo.USER(id) ON DELETE CASCADE,
	CONSTRAINT fk_channel FOREIGN KEY(channel_id) REFERENCES dbo.CHANNEL(id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS dbo.USER_CHANNEL_ROLE(
	user_id int NOT NULL,
	channel_id int NOT NULL,
	role_name VARCHAR(10) NOT NULL CHECK (role_name IN ('READ_ONLY', 'READ_WRITE')),
	PRIMARY KEY(user_id, channel_id, role_name),
	CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES dbo.USER(id) ON DELETE CASCADE,
	CONSTRAINT fk_channel FOREIGN KEY(channel_id) REFERENCES dbo.CHANNEL(id) ON DELETE CASCADE
	);

