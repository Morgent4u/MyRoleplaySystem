----------------------------------------------------------------
-- Created: 2022-07-14
-- Author: Nihar
-- Last updates:
-- 2022-07-14: Add MRS-SQL-Statements to this file.
-- 2022-07-15: Added new tables: mrs_label and mrs_label_enum.
----------------------------------------------------------------

-- UPDv=22.1.0.01
-- Create a table to store the db-version.
CREATE TABLE mrs_dbversion( dbVersion VARCHAR(20) NOT NULL, updTime DATETIME NOT NULL );

INSERT INTO mrs_dbversion ( dbVersion, updTime )
     VALUES ( '22.1.0.01', NOW() );

-- Key table for the key-control.
CREATE TABLE mrs_key ( tableName VARCHAR(40) NOT NULL, lastKey INT DEFAULT 0 );

-- Create a table for the user-main-data.
CREATE TABLE mrs_user (
             user INT(5) NOT NULL PRIMARY KEY
           , name VARCHAR(40) NOT NULL
           , uuid VARCHAR(40) NOT NULL
           , rang INT DEFAULT 999
           , whitelist_yn CHAR(1) DEFAULT 'N'
           , firstConnection DATETIME NOT NULL
           , lastConnection DATETIME NULL);

-- Create a table for specified user-data (for example after the data-protection).
CREATE TABLE mrs_user_data (
             user INT(5)  NOT NULL
           , job INT DEFAULT 999
           , money DECIMAL DEFAULT 0
           , atm DECIMAL DEFAULT 0
           , dataProtection_yn CHAR(1) DEFAULT 'N'
           , dataProtectionTime DATETIME NULL
           , ipAddress VARCHAR(40) NULL);

-- Define a primary-key column for the mrs_user_data-table.
ALTER TABLE mrs_user_data ADD CONSTRAINT fk_mrs_user_data_user FOREIGN KEY (user) REFERENCES mrs_user(user);

-- Add the table mrs_user to the key-control-table.
INSERT INTO mrs_key ( tableName )
     VALUES ( 'mrs_user' );

-- UPDv=22.1.0.02
-- Create the table 'mrs_label' to add many text-labels for every job.
CREATE TABLE mrs_label (
             label INT(5)  NOT NULL
           , label_enum INT(5) NULL
           , text VARCHAR(255) NULL
           , flag VARCHAR(40) NULL
           , sort INT DEFAULT 0);

-- Create the table 'mrs_label_enum' to categorize defined labels in the table mrs_label.
CREATE TABLE mrs_label_enum (
             label_enum INT(5) NOT NULL PRIMARY KEY
           , text VARCHAR(255) NULL
           , flag VARCHAR(40) NULL);

-- Define a primary-key column for the mrs_label-table.
ALTER TABLE mrs_label ADD CONSTRAINT fk_mrs_label_label_enum FOREIGN KEY (label_enum) REFERENCES mrs_label_enum(label_enum);

-- Add the table mrs_label to the key-control-table.
INSERT INTO mrs_key ( tableName, lastKey )
     VALUES ( 'mrs_label', 2 );

-- Add the table mrs_label_enum to the key-control-table.
INSERT INTO mrs_key ( tableName, lastKey )
     VALUES ( 'mrs_label_enum', 1 );

-- UPDv=22.1.0.03
-- Create the view 'mrs_v_user' to get all necessary data for the join-process of a player.
CREATE VIEW mrs_v_user
  AS SELECT mrs_user.user
		  , mrs_user.uuid
		  , mrs_user.rang
		  , mrs_user.whitelist_yn
		  , mrs_user_data.job
		  , mrs_user_data.money
		  , mrs_user_data.atm
		  , dataProtection_yn
		  , ipAddress
	   FROM mrs_user
	      ,	mrs_user_data
	  WHERE mrs_user.user = mrs_user_data.user;

-- UPDv=22.1.0.04
-- Add a default label-enum entry for the DataStore-mechanic.
INSERT INTO mrs_label_enum ( label_enum, text, flag )
     VALUES ( 1, '&9Plot categories', 'plotsystem' );

-- Add some default label-entries for the DataStore-mechanic.
INSERT INTO mrs_label ( label, label_enum, text, sort )
     VALUES ( 1, 1, 'Modern', 1 )
          , ( 2, 1, 'Old', 2 );