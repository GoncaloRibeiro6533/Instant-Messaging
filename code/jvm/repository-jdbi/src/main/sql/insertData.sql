INSERT INTO dbo.USER (email, username, password)
VALUES
    ('alice@example.com', 'alice', 'hashed_password1'),
    ('bob@example.com', 'bob', 'hashed_password2'),
    ('charlie@example.com', 'charlie', 'hashed_password3');


INSERT INTO dbo.CHANNEL (name, creator_id, visibility)
VALUES
    ('General Chat', 1, 'PUBLIC'),
    ('Project Alpha', 2, 'PRIVATE'),
    ('Fun and Games', 3, 'PUBLIC');


INSERT INTO dbo.CHANNEL_INVITATION (role_name, used, channel_id, invited_id, inviter_id)
VALUES
    ('READ-ONLY', FALSE, 1, 2, 1),  -- Alice invites Bob to General Chat
    ('READ-WRITE', FALSE, 2, 3, 2),  -- Bob invites Charlie to Project Alpha
    ('READ-ONLY', TRUE, 3, 1, 3);  -- Charlie invites Alice to Fun and Games

INSERT INTO dbo.REGISTER_INVITATION (role_name, used, channel_id ,invited_email, inviter_id)
VALUES
    ('READ-WRITE', FALSE, 1, 'dave@example.com', 1),  -- Alice invites Dave to General Chat with Admin role
    ('READ-ONLY', FALSE, 3, 'eve@example.com', 3);  -- Charlie invites Eve to Fun and Games as a Member


INSERT INTO dbo.REGISTER_INVITATION (used, invited_email, inviter_id)
VALUES
   (FALSE, 'eve@example.com', 3);  -- Charlie invites Eve

INSERT INTO dbo.SESSION (token, user_id, expirationDate, lastTimeUsed)
VALUES
    ('550e8400-e29b-41d4-a716-446655440000', 1, '2024-10-20 12:00:00', '2024-10-15 08:30:00'),
    ('550e8400-e29b-41d4-a716-446655440001', 2, '2024-10-21 12:00:00', '2024-10-15 09:00:00'),
    ('550e8400-e29b-41d4-a716-446655440002', 3, '2024-10-22 12:00:00', '2024-10-15 09:30:00');

INSERT INTO dbo.MESSAGE (creationTime, user_id, channel_id, message)
VALUES
    ('2024-10-15 08:35:00', 1, 1, 'Hello everyone in General Chat!'),
    ('2024-10-15 08:40:00', 2, 1, 'Hi Alice, nice to meet you!'),
    ('2024-10-15 09:05:00', 3, 3, 'Can’t wait for the fun event!');

INSERT INTO dbo.USER_CHANNEL_ROLE (user_id, channel_id, role_name)
VALUES
    (1, 1, 'READ-WRITE'),   -- Alice is Admin in General Chat
    (2, 1, 'READ-ONLY'),  -- Bob is Member in General Chat
    (3, 2, 'READ-WRITE'), -- Charlie is Moderator in Project Alpha
    (1, 3, 'READ-ONLY');  -- Alice is Member in Fun and Games

