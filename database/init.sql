CREATE DATABASE IF NOT EXISTS coedit DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE coedit;

SET FOREIGN_KEY_CHECKS = 0;

-- Users table
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(255),
    intro TEXT,
    security_question VARCHAR(255),
    security_answer VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Documents table
DROP TABLE IF EXISTS documents;
CREATE TABLE documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content LONGTEXT,
    owner_id BIGINT NOT NULL,
    version BIGINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id)
);

-- Document permissions/collaborators
DROP TABLE IF EXISTS document_collaborators;
CREATE TABLE document_collaborators (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    permission ENUM('ADMIN', 'EDITOR', 'VIEWER') NOT NULL DEFAULT 'VIEWER',
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_doc_user (document_id, user_id)
);

-- Notifications
DROP TABLE IF EXISTS notifications;
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(50), -- e.g., 'INVITE'
    related_id BIGINT, -- e.g., document_id
    is_read BOOLEAN DEFAULT FALSE,
    is_accepted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Folders table
DROP TABLE IF EXISTS folders;
CREATE TABLE folders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- User Document Settings table (for folder organization per user)
DROP TABLE IF EXISTS user_document_settings;
CREATE TABLE user_document_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    folder_id BIGINT DEFAULT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE,
    FOREIGN KEY (folder_id) REFERENCES folders(id) ON DELETE SET NULL,
    UNIQUE KEY unique_user_doc (user_id, document_id)
);

-- Comments table
DROP TABLE IF EXISTS comments;
CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

SET FOREIGN_KEY_CHECKS = 1;

-- Initialize default users
-- Admin: admin/123456
INSERT INTO users (account, name, password, security_question, security_answer, email, phone, intro) 
VALUES ('admin', 'Admin', '123456', '您的管理员账号是什么？', 'admin', 'admin@coedit.com', '13800138000', 'I am the system administrator.');

-- Test users: test1, test2, test3 / 123456
INSERT INTO users (account, name, password, security_question, security_answer, email, phone, intro) 
VALUES ('test1', 'Test1', '123456', '您的测试答案是什么？', 'test', 'test1@example.com', '13911112222', 'Hello, I am Test User 1.');

INSERT INTO users (account, name, password, security_question, security_answer, email, phone, intro) 
VALUES ('test2', 'Test2', '123456', '您的测试答案是什么？', 'test', 'test2@example.com', '13933334444', 'Coding enthusiast.');

INSERT INTO users (account, name, password, security_question, security_answer, email, phone, intro) 
VALUES ('test3', 'Test3', '123456', '您的测试答案是什么？', 'test', 'test3@example.com', '13955556666', 'Just a simple user.');
