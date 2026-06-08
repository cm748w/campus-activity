# 校园活动报名系统 - 项目部署运行文档

## 一、环境准备

### 1.1 必需软件

| 软件 | 版本要求 | 下载地址 |
|------|---------|---------|
| JDK | 17+ | https://adoptium.net/ |
| MySQL | 8.0+ | https://dev.mysql.com/downloads/ |
| Node.js | 18+ | https://nodejs.org/ |
| Maven | 3.8+ | https://maven.apache.org/ (IDEA内置) |
| IntelliJ IDEA | 2023+ | https://www.jetbrains.com/idea/ |
| VS Code | 最新版 | https://code.visualstudio.com/ (可选) |

### 1.2 推荐IDE

- **后端开发**：IntelliJ IDEA（社区版免费）
- **前端开发**：VS Code 或 IntelliJ IDEA

---

## 二、数据库配置

### 2.1 启动MySQL服务

确保MySQL 8.0服务已启动，记住root密码。

### 2.2 导入数据库脚本

#### 方式一：命令行导入

```bash
# 登录MySQL
mysql -u root -p

# 在MySQL命令行中执行
source /path/to/campus_activity_db.sql;

# 或直接在命令行执行
mysql -u root -p < campus_activity_db.sql
```

#### 方式二：使用Navicat/DataGrip等工具

1. 连接到本地MySQL服务器
2. 创建数据库 `campus_activity`
3. 右键数据库 -> 运行SQL文件 -> 选择 `campus_activity_db.sql`

### 2.3 数据库配置说明

如需修改数据库连接信息，编辑后端项目文件：
`backend/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_activity?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root      # 修改为你的MySQL用户名
    password: root      # 修改为你的MySQL密码
```

---

## 三、后端项目启动（Spring Boot）

### 3.1 使用IntelliJ IDEA启动（推荐）

1. 打开IntelliJ IDEA，选择 `Open`，导入 `backend` 文件夹
2. 等待Maven自动下载依赖（右下角有进度条）
3. 找到 `CampusActivityApplication.java` 文件
4. 点击类名旁的 ▶ 运行按钮，或使用快捷键 `Shift + F10`
5. 看到以下日志表示启动成功：

```
Started CampusActivityApplication in x.x seconds
Tomcat started on port(s): 8080
```

### 3.2 使用Maven命令行启动

```bash
# 进入后端项目目录
cd backend

# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run
```

### 3.3 验证后端启动

打开浏览器访问：http://localhost:8080/api/doc.html

应能看到 Knife4j 接口文档页面。

---

## 四、前端项目启动（Vue 3）

### 4.1 使用VS Code启动

1. 打开VS Code，选择 `File -> Open Folder`，导入 `frontend` 文件夹
2. 打开终端（Terminal -> New Terminal）
3. 安装依赖：

```bash
npm install
```

4. 启动开发服务器：

```bash
npm run dev
```

### 4.2 验证前端启动

打开浏览器访问：http://localhost:8081

应能看到登录页面。

---

## 五、系统访问

### 5.1 完整启动顺序

```
1. 启动MySQL服务
2. 启动后端（端口8080）
3. 启动前端（端口8081）
4. 浏览器访问 http://localhost:8081
```

### 5.2 测试账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 系统管理员 | admin | 123456 | 拥有所有权限 |
| 社团负责人 | 2021001 | 123456 | 可发布活动、审核报名 |
| 学生 | 20220101 | 123456 | 可浏览活动、报名 |

---

## 六、常见问题排查

### 6.1 后端启动失败

| 问题 | 解决方案 |
|------|---------|
| 端口8080被占用 | 修改 `application.yml` 中的 `server.port` |
| MySQL连接失败 | 检查MySQL服务是否启动，用户名密码是否正确 |
| Maven依赖下载慢 | 更换阿里云Maven镜像源 |
| JDK版本不对 | 确认使用JDK 17，检查 `java -version` |

### 6.2 前端启动失败

| 问题 | 解决方案 |
|------|---------|
| npm install失败 | 使用 `npm install --registry=https://registry.npmmirror.com` |
| 端口8081被占用 | 修改 `vite.config.js` 中的 `server.port` |
| 接口请求404 | 确认后端已启动，检查代理配置 |

---

## 七、接口文档访问

后端使用 Knife4j（Swagger增强版）自动生成API文档：

- **地址**：http://localhost:8080/api/doc.html
- **描述**：包含所有接口的详细信息、参数说明、响应示例
- **在线调试**：支持在文档页面直接测试接口

---

## 八、技术栈说明

### 后端
- Java 17 + Spring Boot 3.2.5
- MyBatis-Plus 3.5.5（ORM框架）
- MySQL 8.0（数据库）
- JWT（认证授权）
- Knife4j 4.5.0（接口文档）
- Maven（构建工具）

### 前端
- Vue 3.4 + Vite 5
- Element Plus 2.6（UI组件库）
- Vue Router 4（路由）
- Pinia（状态管理）
- Axios（HTTP请求）
- ECharts（图表库）
