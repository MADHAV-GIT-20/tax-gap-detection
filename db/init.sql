-- Tax Gap Detection & Compliance Validation Service - database setup
-- Run this once in MySQL Workbench (or the mysql CLI) as a privileged user.
--
-- It creates the schema and a dedicated application user so you never have to
-- expose your MySQL root password to the application.

CREATE DATABASE IF NOT EXISTS taxgap
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'taxgap_user'@'localhost' IDENTIFIED BY 'taxgap_pass';
GRANT ALL PRIVILEGES ON taxgap.* TO 'taxgap_user'@'localhost';
FLUSH PRIVILEGES;

-- Tables are created automatically by Hibernate (spring.jpa.hibernate.ddl-auto=update)
-- on first application start. Prefilled users and the 3 mandatory rules are
-- inserted by DataSeeder at startup.
