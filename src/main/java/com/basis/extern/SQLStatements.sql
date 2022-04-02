----------------------------------------------------------------
-- Created: 2022-04-02
-- Author: Nihar
-- Last updates:
-- 2022-04-02: Added create database and table functions.
-- 2022-04-02: Tested the current script.

----------------------------------------------------------------

-- UPDv=22.1.0.01
CREATE TABLE mrs_dbversion( dbVersion VARCHAR(20) NOT NULL, updTime DATETIME NOT NULL );

INSERT INTO mrs_dbversion ( dbVersion, updTime )
     VALUES ( '22.1.0.01', NOW() );

-- Key table for the key-control.
CREATE TABLE mrs_key ( tableName VARCHAR(40) NOT NULL, lastKey INT DEFAULT 0 );

-- Default user table.
CREATE TABLE mrs_user (
             user INT(5) NOT NULL PRIMARY KEY
           , name VARCHAR(40) NOT NULL
           , uuid VARCHAR(40) NOT NULL
           , rang INT DEFAULT 999
           , job INT DEFAULT 999
           , money INT DEFAULT 0
           , atm INT DEFAULT 0
           , lastConnection DATETIME NOT NULL );

-- Update the key-control table.
INSERT INTO mrs_key ( tableName )
     VALUES ( 'mrs_user' );