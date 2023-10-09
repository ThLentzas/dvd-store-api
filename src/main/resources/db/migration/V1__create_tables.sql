CREATE TYPE genre AS ENUM (
    'ADVENTURE',
    'CHILDREN_FILM',
    'THRILLER',
    'COMEDY',
    'SOCIOLOGICAL',
    'SCIENCE_FICTION'
);

CREATE TYPE role AS ENUM (
    'ROLE_CUSTOMER',
    'ROLE_EMPLOYEE'
);

CREATE TABLE IF NOT EXISTS app_user (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(30) NOT NULL,
    last_name VARCHAR(30) NOT NULL,
    email VARCHAR(50) NOT NULL,
    password TEXT NOT NULL,
    role role NOT NULL
);

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS dvd (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    genre genre NOT NULL
);