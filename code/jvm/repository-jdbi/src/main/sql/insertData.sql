-- Insert Users
INSERT INTO dbo.USER (email, username, password) VALUES
('user1@example.com', 'user1', '$2a$10$t8H2V8eJaVXcV2evFj7paesW.yS0D.9x3Bxg1vMdL/jYPmAV.R2cW'),
('user2@example.com', 'user2', '$2a$10$t8H2V8eJaVXcV2evFj7paesW.yS0D.9x3Bxg1vMdL/jYPmAV.R2cW'),
('user3@example.com', 'user3', '$2a$10$t8H2V8eJaVXcV2evFj7paesW.yS0D.9x3Bxg1vMdL/jYPmAV.R2cW'),
('user4@example.com', 'user4', '$2a$10$t8H2V8eJaVXcV2evFj7paesW.yS0D.9x3Bxg1vMdL/jYPmAV.R2cW');

-- Insert Tokens (assuming `user_id` is 1 for simplicity)
INSERT INTO dbo.TOKEN (token, user_id, created_at, last_used_at) VALUES
('token_1', 1, 1697755200, 1697758800),
('token_2', 2, 1697755200, 1697758800),
('token_3', 3, 1697755200, 1697758800);

-- Insert Channels
INSERT INTO dbo.CHANNEL (name, creator_id, visibility) VALUES
('General', 1, 'PUBLIC'),
('Private Channel', 2, 'PRIVATE'),
('Project A', 1, 'PRIVATE');

-- Insert Channel Invitations
INSERT INTO dbo.CHANNEL_INVITATION (role_name, used, channel_id, invited_id, inviter_id, timestamp) VALUES
('READ_ONLY', FALSE, 1, 2, 1, NOW()),
('READ_WRITE', FALSE, 2, 3, 2, NOW()),
('READ_ONLY', TRUE, 1, 3, 1, NOW());

-- Insert Register Invitations
INSERT INTO dbo.REGISTER_INVITATION (role_name, used, channel_id, invited_email, inviter_id, timestamp, code) VALUES
('READ_ONLY', FALSE, 1, 'invitee1@example.com', 1, NOW(), 'invite_code_1'),
('READ_WRITE', FALSE, 2, 'invitee2@example.com', 2, NOW(), 'invite_code_2');

-- Insert Messages
INSERT INTO dbo.MESSAGE (creationTime, user_id, channel_id, message) VALUES
(NOW(), 1, 1, 'Hello, welcome to the General channel!'),
(NOW(), 2, 1, 'Thanks! Glad to be here.'),
(NOW(), 3, 2, 'Looking forward to collaborating on the Private Channel.');

-- Insert User-Channel Roles
INSERT INTO dbo.USER_CHANNEL_ROLE (user_id, channel_id, role_name) VALUES
(1, 1, 'READ_WRITE'),
(2, 1, 'READ_ONLY'),
(3, 2, 'READ_ONLY'),
(4, 3, 'READ_WRITE');
