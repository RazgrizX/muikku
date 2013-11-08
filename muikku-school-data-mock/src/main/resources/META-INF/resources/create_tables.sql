/* Clear */

DROP TABLE IF EXISTS GradingScaleItem;
DROP TABLE IF EXISTS GradingScale;
DROP TABLE IF EXISTS UserEmail;
DROP TABLE IF EXISTS UserImage;
DROP TABLE IF EXISTS UserProperty;
DROP TABLE IF EXISTS WorkspaceType;
DROP TABLE IF EXISTS WorkspaceUser;
DROP TABLE IF EXISTS UserWorkspaceRole;
DROP TABLE IF EXISTS Workspace;
DROP TABLE IF EXISTS User;
DROP TABLE IF EXISTS Role;
DROP TABLE IF EXISTS Subject;
DROP TABLE IF EXISTS CourseIdentifier;

/* School Data Course Meta */

CREATE TABLE Subject (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
  name VARCHAR(255) NOT NULL
);

CREATE TABLE CourseIdentifier (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
  code VARCHAR(255) NOT NULL,
  subject_id BIGINT  NOT NULL
);

/* User School Data Bridge */

CREATE TABLE User (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
  firstName VARCHAR(255) NOT NULL,
  lastName VARCHAR(255) NOT NULL,
  role_id BIGINT NOT NULL
);

CREATE TABLE UserEmail (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
  user_id BIGINT  NOT NULL,
  address VARCHAR(255) NOT NULL
);

CREATE TABLE UserImage (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  content_type VARCHAR(255) NOT NULL,
  content BLOB NOT NULL
);

CREATE TABLE UserProperty (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  key VARCHAR(255) NOT NULL,
  value VARCHAR(255) NOT NULL
);

/* Workspace School Data Bridge */

CREATE TABLE Workspace (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
  course_identifier_id BIGINT,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(4096),
  type_id BIGINT NOT NULL
);

CREATE TABLE WorkspaceType (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
  name VARCHAR(255) NOT NULL
);

CREATE TABLE WorkspaceUser (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
  user_school_data_source VARCHAR(255) NOT NULL,
  user_identifier VARCHAR(255) NOT NULL,
  workspace_school_data_source VARCHAR(255) NOT NULL,
  workspace_identifier VARCHAR(255) NOT NULL
);

/* Roles */

CREATE TABLE Role (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(255) NOT NULL
);

CREATE TABLE UserWorkspaceRole (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  workspace_id BIGINT NOT NULL
);

/* Grading Scales */

CREATE TABLE GradingScale (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
  name VARCHAR(255) NOT NULL
);

CREATE TABLE GradingScaleItem (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  grading_scale_id BIGINT NOT NULL
);

ALTER TABLE CourseIdentifier ADD CONSTRAINT FK_COURSEIDENTIFIER_SUBJECT_ID FOREIGN KEY (subject_id)  REFERENCES Subject (id) ON DELETE CASCADE;
ALTER TABLE User ADD CONSTRAINT FK_USER_ROLE_ID FOREIGN KEY (role_id)  REFERENCES Role (id) ON DELETE CASCADE;
ALTER TABLE UserEmail ADD CONSTRAINT FK_USEREMAIL_USER_ID FOREIGN KEY (user_id)  REFERENCES User (id) ON DELETE CASCADE;
ALTER TABLE UserImage ADD CONSTRAINT FK_USERIMAGE_USER_ID FOREIGN KEY (user_id)  REFERENCES User (id) ON DELETE CASCADE;
ALTER TABLE UserProperty ADD CONSTRAINT FK_USERPROPERTY_USER_ID FOREIGN KEY (user_id)  REFERENCES User (id) ON DELETE CASCADE;
ALTER TABLE Workspace ADD CONSTRAINT FK_WORKSPACE_COURSEIDENTIFIER_ID FOREIGN KEY (course_identifier_id)  REFERENCES CourseIdentifier (id) ON DELETE CASCADE;
ALTER TABLE Workspace ADD CONSTRAINT FK_WORKSPACE_TYPE_ID FOREIGN KEY (type_id)  REFERENCES WorkspaceType (id) ON DELETE CASCADE;
ALTER TABLE UserWorkspaceRole ADD CONSTRAINT FK_USERWORKSPACEROLE_USER_ID FOREIGN KEY (user_id)  REFERENCES User (id) ON DELETE CASCADE;
ALTER TABLE UserWorkspaceRole ADD CONSTRAINT FK_USERWORKSPACEROLE_ROLE_ID FOREIGN KEY (role_id)  REFERENCES Role (id) ON DELETE CASCADE;
ALTER TABLE UserWorkspaceRole ADD CONSTRAINT FK_USERWORKSPACEROLE_WORKSPACE_ID FOREIGN KEY (workspace_id)  REFERENCES Workspace (id) ON DELETE CASCADE;
ALTER TABLE GradingScaleItem ADD CONSTRAINT FK_GRADING_SCALE_ITEM_GRADING_SCALE_ID FOREIGN KEY (grading_scale_id)  REFERENCES GradingScale (id) ON DELETE CASCADE;