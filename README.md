# CoEdit - 多人在线协作编辑软件

## 项目简介

CoEdit是一款基于Spring Boot 3和Vue 3的多人在线协作编辑软件，支持多人同时在线编辑文档。

## 技术栈

- **后端**: Spring Boot 3.x + Spring Security + WebSocket + MyBatis + Lombok
- **前端**: Vue 3 + Element Plus + Vite + WebSocket + Quill (富文本)
- **数据库**: MySQL 8.0
- **构建工具**: Maven (后端) + Vite (前端)

## 功能特性

### 1. 用户管理模块
- ✅ 账号密码注册登录
- ✅ 密保问题找回密码
- ✅ 个人资料编辑（个人介绍、电话、邮箱）
- ✅ 头像上传

### 2. 文档管理模块
- ✅ 富文本编辑器
- ✅ 文档创建、编辑、删除
- ✅ 文档列表，文件夹分类管理文档
- ✅ 文档搜索（按作者、日期、关键词等），搜索结果排序
- ✅ 自动保存（一有变更就保存）

### 3. 文档实时协作模块
- ✅ 多用户同时编辑
- ✅ 角色定义（owner，admin，editor，viewer）与 访问控制列表（ACL）
        owner：文档的创建者，其权限不可被更改，能够删除文档、编辑文档、管理协作者（添加、删除、修改权限）、发表评论、管理评论（删除评论）；
        admin：文档的管理员，能够编辑文档、管理协作者（添加、删除、修改权限），发表评论，管理评论（删除评论）；
        editor：文档的编辑者，能够编辑文档，发表评论；
        viewer：文档的查看者，能够查看文档，发表评论；
- ✅ 多用户实时光标位置显示
- ✅ 评论与@提及

### 4. 通知与通讯模块
- ✅ 通知列表
- ✅ 通知删除
- ✅ 通知类型包括
        INVITE (邀请协作)
        INVITE_ACCEPTED (接受邀请)
        COLLABORATOR_LEFT (协作者退出)
        REMOVED_FROM_DOC (被移除)
        PERMISSION_UPDATE (权限变更)
        MENTION (评论@提及)

## 配置说明

### 环境要求

- JDK 17+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

### 数据库配置

1. 创建数据库并执行初始化脚本：
```bash
mysql -u root -p123456 < database/init.sql
```

### 后端启动

1. 进入后端目录：
```bash
cd backend
```

2. 修改配置文件 `src/main/resources/application.yml`（如需要）

3. 启动项目：
```bash
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 前端启动

1. 进入前端目录：
```bash
cd frontend
```

2. 安装依赖
```bash
npm install
```

3. 启动开发服务器：
```bash
npm run dev
```

前端服务将在 `http://localhost:3000` 启动

## 默认账号

### 管理员账号
- 账号: `admin`
- 密码: `123456`
- 密保问题: `您的管理员账号是什么？`
- 密保答案: `admin`

### 测试用户
- 账号: `test1`, `test2`, `test3`
- 密码: `123456`
- 密保问题: `您的测试答案是什么？`
- 密保答案: `test`

