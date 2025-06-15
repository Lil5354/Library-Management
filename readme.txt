USE master;
GO
--Chạy Create lại DB hỗ trợ tiếng việt
IF DB_ID('LibraryDB') IS NOT NULL
BEGIN
    ALTER DATABASE LibraryDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE LibraryDB;
    PRINT 'Database LibraryDB cũ đã được xóa.';
END
GO
CREATE DATABASE LibraryDB
COLLATE Latin1_General_100_CI_AI_SC_UTF8;
GO

PRINT 'Database LibraryDB đã được tạo thành công với Collation hỗ trợ UTF-8.';
USE master;
GO
IF NOT EXISTS (SELECT name FROM sys.server_principals WHERE name = 'springuser')

BEGIN

    CREATE LOGIN springuser WITH PASSWORD = '123456', DEFAULT_DATABASE = LibraryDB, CHECK_POLICY = OFF;

    PRINT 'Login springuser created.';

END
ELSE
BEGIN
    PRINT 'Login springuser already exists.';
END
GO

-- 2. Create User in LibraryDB and map to Login (run in LibraryDB context)
USE LibraryDB;
GO
IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = 'springuser')
BEGIN

    CREATE USER springuser FOR LOGIN springuser;

    PRINT 'User springuser created in LibraryDB.';
END
ELSE
BEGIN
    PRINT 'User springuser already exists in LibraryDB.';
END
GO
-- 3. Grant db_owner role to user in LibraryDB (run in LibraryDB context)
USE LibraryDB;
GO
ALTER ROLE db_owner ADD MEMBER springuser;
PRINT 'User springuser added to db_owner role in LibraryDB.';
GO
PRINT 'Setup complete for springuser.';
