drop database if exists chatApp;
create database chatApp;
use chatApp;

create table user(
	account nvarchar(100) primary key,
    password nvarchar(100) not null,
    nickname nvarchar(100) not null,
    signature nvarchar(300) not null,
    image nvarchar(100) not null
)