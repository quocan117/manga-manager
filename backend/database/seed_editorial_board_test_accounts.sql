SET XACT_ABORT ON;
SET LOCK_TIMEOUT 10000;
BEGIN TRANSACTION;

DECLARE @EditorialBoardRoleId BIGINT;

SELECT TOP (1) @EditorialBoardRoleId = role_id
FROM roles
WHERE role_name = 'EDITORIAL_BOARD';

IF @EditorialBoardRoleId IS NULL
BEGIN
    INSERT INTO roles (role_name) VALUES ('EDITORIAL_BOARD');
    SET @EditorialBoardRoleId = SCOPE_IDENTITY();
END;

DECLARE @PasswordHash VARCHAR(255) = '$2a$10$0PF.OJUXjBjqgFaKUfF9lOcO7nOdWjOlmuYW4WmvLCl/zS7AEcI8e';

DECLARE @EditorialBoardUsers TABLE (
    username NVARCHAR(255),
    email VARCHAR(255)
);

INSERT INTO @EditorialBoardUsers (username, email)
VALUES
    (N'Editorial Board 1', 'editorial1@manga.test'),
    (N'Editorial Board 2', 'editorial2@manga.test'),
    (N'Editorial Board 3', 'editorial3@manga.test'),
    (N'Editorial Board 4', 'editorial4@manga.test'),
    (N'Editorial Board 5', 'editorial5@manga.test'),
    (N'Editorial Board 6', 'editorial6@manga.test'),
    (N'Editorial Board 7', 'editorial7@manga.test');

MERGE users AS target
USING @EditorialBoardUsers AS source
ON target.email = source.email
WHEN MATCHED THEN
    UPDATE SET
        target.username = source.username,
        target.password = @PasswordHash,
        target.status = 'ACTIVE',
        target.role_id = @EditorialBoardRoleId
WHEN NOT MATCHED THEN
    INSERT (username, email, password, status, created_at, role_id)
    VALUES (source.username, source.email, @PasswordHash, 'ACTIVE', SYSDATETIME(), @EditorialBoardRoleId);

COMMIT TRANSACTION;

SELECT users.username, users.email, users.status, roles.role_name
FROM users
JOIN roles ON roles.role_id = users.role_id
WHERE users.email IN (
    'editorial1@manga.test',
    'editorial2@manga.test',
    'editorial3@manga.test',
    'editorial4@manga.test',
    'editorial5@manga.test',
    'editorial6@manga.test',
    'editorial7@manga.test'
)
ORDER BY users.email;
