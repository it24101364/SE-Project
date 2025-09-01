CREATE TABLE users
(
    id       bigint IDENTITY (1, 1) NOT NULL,
    username varchar(255),
    password varchar(255),
    CONSTRAINT pk_users PRIMARY KEY (id)
)
GO