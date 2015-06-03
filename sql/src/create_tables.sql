DROP TABLE WORK_EXPR;
DROP TABLE EDUCATIONAL_DETAILS;
DROP TABLE MESSAGE;
DROP TABLE CONNECTION_USR;
DROP TABLE USR;


CREATE TABLE USR(
	userId varchar(50) UNIQUE NOT NULL, 
	password varchar(20) NOT NULL,
	email text NOT NULL,
	name varchar(50),
	date_of_birth date,
	Primary Key(userId));

CREATE TABLE WORK_EXPR(
	userId varchar(50) NOT NULL, 
	company varchar(50) NOT NULL, 
	role varchar(50) NOT NULL,
	location varchar(50),
	start_date date,
	end_date date,
	PRIMARY KEY(userId,company,role,start_date),
	FOREIGN KEY(userId) REFERENCES USR);

CREATE TABLE EDUCATIONAL_DETAILS(
	userId varchar(50) NOT NULL, 
	institution_name varchar(50) NOT NULL, 
	major varchar(50) NOT NULL,
	degree varchar(50) NOT NULL,
	start_date date,
	end_date date,
	PRIMARY KEY(userId,major,degree),
	FOREIGN KEY(userId) REFERENCES USR);

CREATE TABLE MESSAGE(
	msgId integer NOT NULL DEFAULT nextval('msgIDSequence') PRIMARY KEY, 
	senderId varchar(50) NOT NULL,
	receiverId varchar(50) NOT NULL,
	contents varchar(500) NOT NULL,
	send_time timestamp,
	delete_status integer,
	status varchar(30) NOT NULL);

CREATE TABLE CONNECTION_USR(
	userId varchar(50) NOT NULL, 
	connectionId varchar(50) NOT NULL, 
	status varchar(30) NOT NULL,
	PRIMARY KEY(userId,connectionId));
