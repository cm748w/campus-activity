-- ========================================================
-- 校园活动报名系统 - MySQL 8.0 数据库脚本 (UTF-8修复版)
-- 执行前请确保MySQL客户端编码为UTF-8: SET NAMES utf8mb4;
-- ========================================================

-- 设置客户端编码（Windows下必须）
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 创建数据库
DROP DATABASE IF EXISTS campus_activity;
CREATE DATABASE campus_activity DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_activity;

-- ========================================================
-- 1. 角色表
-- ========================================================
CREATE TABLE role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码（admin/organizer/student）',
    description VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标志（0-未删除，1-已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code),
    KEY idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ========================================================
-- 2. 用户表
-- ========================================================
CREATE TABLE user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名/学号/工号',
    password VARCHAR(100) NOT NULL COMMENT '加密后的密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号码',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    gender TINYINT DEFAULT 0 COMMENT '性别（0-保密，1-男，2-女）',
    department VARCHAR(100) DEFAULT NULL COMMENT '所属院系/部门',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态（0-禁用，1-正常）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标志（0-未删除，1-已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_phone (phone),
    UNIQUE KEY uk_email (email),
    KEY idx_status (status),
    KEY idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ========================================================
-- 3. 用户角色关联表
-- ========================================================
CREATE TABLE user_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES role (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ========================================================
-- 4. 活动表
-- ========================================================
CREATE TABLE activity (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '活动ID',
    title VARCHAR(200) NOT NULL COMMENT '活动标题',
    description TEXT COMMENT '活动详情描述',
    location VARCHAR(200) NOT NULL COMMENT '活动地点',
    start_time DATETIME NOT NULL COMMENT '活动开始时间',
    end_time DATETIME NOT NULL COMMENT '活动结束时间',
    register_start DATETIME NOT NULL COMMENT '报名开始时间',
    register_end DATETIME NOT NULL COMMENT '报名截止时间',
    max_participants INT NOT NULL DEFAULT 0 COMMENT '人数上限（0表示不限）',
    current_participants INT NOT NULL DEFAULT 0 COMMENT '当前报名人数',
    organizer_id BIGINT NOT NULL COMMENT '发布人ID（负责人）',
    organizer_name VARCHAR(50) DEFAULT NULL COMMENT '发布人姓名',
    organizer_contact VARCHAR(50) DEFAULT NULL COMMENT '负责人联系方式',
    activity_type VARCHAR(50) DEFAULT '其他' COMMENT '活动类型（学术/文体/志愿/社团/其他）',
    cover_image VARCHAR(255) DEFAULT NULL COMMENT '活动封面图URL',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '活动状态（0-草稿，1-待审核，2-已发布，3-报名中，4-已结束，5-已取消）',
    reject_reason VARCHAR(255) DEFAULT NULL COMMENT '审核驳回原因',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标志（0-未删除，1-已删除）',
    PRIMARY KEY (id),
    KEY idx_organizer (organizer_id),
    KEY idx_status (status),
    KEY idx_activity_type (activity_type),
    KEY idx_start_time (start_time),
    KEY idx_deleted (deleted),
    KEY idx_register_end (register_end),
    CONSTRAINT fk_activity_user FOREIGN KEY (organizer_id) REFERENCES user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';

-- ========================================================
-- 5. 报名表
-- ========================================================
CREATE TABLE registration (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '报名ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    user_id BIGINT NOT NULL COMMENT '报名用户ID',
    real_name VARCHAR(50) NOT NULL COMMENT '报名人姓名',
    student_no VARCHAR(50) NOT NULL COMMENT '学号',
    phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    department VARCHAR(100) DEFAULT NULL COMMENT '所在院系',
    remark VARCHAR(255) DEFAULT NULL COMMENT '报名备注/自我介绍',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '报名状态（0-待审核，1-已通过，2-已驳回，3-已取消）',
    reject_reason VARCHAR(255) DEFAULT NULL COMMENT '审核意见/驳回原因',
    register_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
    audit_by BIGINT DEFAULT NULL COMMENT '审核人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标志（0-未删除，1-已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_activity_user (activity_id, user_id),
    KEY idx_activity_id (activity_id),
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    KEY idx_deleted (deleted),
    CONSTRAINT fk_reg_activity FOREIGN KEY (activity_id) REFERENCES activity (id),
    CONSTRAINT fk_reg_user FOREIGN KEY (user_id) REFERENCES user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报名表';

-- ========================================================
-- 6. 公告表
-- ========================================================
CREATE TABLE notice (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告内容',
    publisher_id BIGINT NOT NULL COMMENT '发布人ID',
    publisher_name VARCHAR(50) DEFAULT NULL COMMENT '发布人姓名',
    top TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶（0-否，1-是）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '公告状态（0-草稿，1-已发布，2-已下架）',
    view_count INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    publish_time DATETIME DEFAULT NULL COMMENT '发布时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标志（0-未删除，1-已删除）',
    PRIMARY KEY (id),
    KEY idx_publisher (publisher_id),
    KEY idx_status (status),
    KEY idx_top (top),
    KEY idx_deleted (deleted),
    KEY idx_publish_time (publish_time),
    CONSTRAINT fk_notice_user FOREIGN KEY (publisher_id) REFERENCES user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- ========================================================
-- 测试数据插入
-- ========================================================

-- 角色数据（必须在用户数据之前插入，因为用户角色关联表依赖角色表）
INSERT INTO role (id, role_name, role_code, description) VALUES
(1, '系统管理员', 'admin', '系统超级管理员，拥有所有权限'),
(2, '社团/院系负责人', 'organizer', '可发布活动、审核报名、导出名单'),
(3, '普通学生', 'student', '可浏览活动、在线报名、查看公告');

-- 用户数据（密码统一为 123456，BCrypt加密后值）
-- 密码原文：123456
-- 注意：必须先插入用户数据，再插入用户角色关联数据
INSERT INTO user (id, username, password, real_name, phone, email, gender, department, status) VALUES
(1, 'admin', '$2a$10$N.ZGT6PmlGcmejALGtdLvuJzP.VyTO.2m29Z1jVGB3MJbVDT7g4kC', '系统管理员', '13800000001', 'admin@campus.edu.cn', 1, '信息中心', 1),
(2, '2021001', '$2a$10$N.ZGT6PmlGcmejALGtdLvuJzP.VyTO.2m29Z1jVGB3MJbVDT7g4kC', '李明', '13800000002', 'liming@campus.edu.cn', 1, '计算机学院', 1),
(3, '2021002', '$2a$10$N.ZGT6PmlGcmejALGtdLvuJzP.VyTO.2m29Z1jVGB3MJbVDT7g4kC', '张薇', '13800000003', 'zhangwei@campus.edu.cn', 2, '学生工作处', 1),
(4, '2021003', '$2a$10$N.ZGT6PmlGcmejALGtdLvuJzP.VyTO.2m29Z1jVGB3MJbVDT7g4kC', '王浩', '13800000004', 'wanghao@campus.edu.cn', 1, '电子工程学院', 1),
(5, '20220101', '$2a$10$N.ZGT6PmlGcmejALGtdLvuJzP.VyTO.2m29Z1jVGB3MJbVDT7g4kC', '陈思远', '13800000005', 'chensiyuan@stu.campus.edu.cn', 1, '计算机学院', 1),
(6, '20220102', '$2a$10$N.ZGT6PmlGcmejALGtdLvuJzP.VyTO.2m29Z1jVGB3MJbVDT7g4kC', '林雨桐', '13800000006', 'linyutong@stu.campus.edu.cn', 2, '外国语学院', 1),
(7, '20220103', '$2a$10$N.ZGT6PmlGcmejALGtdLvuJzP.VyTO.2m29Z1jVGB3MJbVDT7g4kC', '赵子轩', '13800000007', 'zhaozixuan@stu.campus.edu.cn', 1, '管理学院', 1),
(8, '20220104', '$2a$10$N.ZGT6PmlGcmejALGtdLvuJzP.VyTO.2m29Z1jVGB3MJbVDT7g4kC', '孙艺萌', '13800000008', 'sunyimeng@stu.campus.edu.cn', 2, '艺术学院', 1),
(9, '20220105', '$2a$10$N.ZGT6PmlGcmejALGtdLvuJzP.VyTO.2m29Z1jVGB3MJbVDT7g4kC', '周子涵', '13800000009', 'zhouzihan@stu.campus.edu.cn', 1, '计算机学院', 1),
(10, '20220106', '$2a$10$N.ZGT6PmlGcmejALGtdLvuJzP.VyTO.2m29Z1jVGB3MJbVDT7g4kC', '吴佳怡', '13800000010', 'wujiayi@stu.campus.edu.cn', 2, '医学院', 1),
(11, '20220107', '$2a$10$N.ZGT6PmlGcmejALGtdLvuJzP.VyTO.2m29Z1jVGB3MJbVDT7g4kC', '黄思诚', '13800000011', 'huangsicheng@stu.campus.edu.cn', 1, '电子工程学院', 1),
(12, '20220108', '$2a$10$N.ZGT6PmlGcmejALGtdLvuJzP.VyTO.2m29Z1jVGB3MJbVDT7g4kC', '徐梦洁', '13800000012', 'xumengjie@stu.campus.edu.cn', 2, '人文学院', 1);

-- 用户角色关联数据
INSERT INTO user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 2),
(4, 2),
(5, 3),
(6, 3),
(7, 3),
(8, 3),
(9, 3),
(10, 3),
(11, 3),
(12, 3);

-- 活动数据
INSERT INTO activity (id, title, description, location, start_time, end_time, register_start, register_end, max_participants, current_participants, organizer_id, organizer_name, organizer_contact, activity_type, status) VALUES
(1, '2024春季校园编程马拉松', '面向全校学生的编程竞赛，48小时极限开发，团队协作完成一个完整项目。设立一等奖1名、二等奖2名、三等奖3名，奖金丰厚。参赛团队3-5人，需提前组队报名。', '计算机学院 301实验室', '2024-04-15 09:00:00', '2024-04-16 21:00:00', '2024-03-20 00:00:00', '2024-04-10 23:59:59', 100, 3, 2, '李明', '13800000002', '学术', 3),
(2, '绿动校园环保志愿活动', '组织志愿者在校园内进行垃圾分类宣传、绿植养护、环境清洁等活动。参与满8小时可获得志愿服务认证证书。提供志愿者服装和工具。', '学校中心广场', '2024-03-25 08:00:00', '2024-03-25 17:00:00', '2024-03-10 00:00:00', '2024-03-23 23:59:59', 50, 2, 3, '张薇', '13800000003', '志愿', 3),
(3, '校园十佳歌手大赛初赛', '一年一度的校园歌手大赛，展示你的歌喉！初赛为海选形式，每位选手演唱一首自选歌曲，限时3分钟。评委现场打分，前30名进入复赛。', '大学生活动中心 多功能厅', '2024-04-05 18:30:00', '2024-04-05 22:00:00', '2024-03-15 00:00:00', '2024-04-03 23:59:59', 80, 1, 4, '王浩', '13800000004', '文体', 3),
(4, '人工智能前沿技术讲座', '邀请业界知名专家讲解大语言模型、AIGC、多模态AI等前沿技术发展趋势。讲座结束后设有QA互动环节，欢迎同学们积极提问。', '图书馆 学术报告厅', '2024-03-30 14:00:00', '2024-03-30 17:00:00', '2024-03-20 00:00:00', '2024-03-29 23:59:59', 200, 1, 2, '李明', '13800000002', '学术', 2),
(5, '春季篮球友谊赛', '各院系篮球队之间的友谊赛，增进院系间交流。比赛采用循环赛制，最终决出冠亚季军。欢迎同学们到场加油助威！', '学校体育馆 篮球场', '2024-04-10 16:00:00', '2024-04-20 18:00:00', '2024-03-25 00:00:00', '2024-04-08 23:59:59', 120, 0, 4, '王浩', '13800000004', '文体', 1),
(6, '社团招新大会', '全校社团集中招新活动，涵盖学术科技、文化艺术、体育健身、公益志愿等各大类社团。现场可了解社团详情、报名入社。', '学校田径场', '2024-03-20 10:00:00', '2024-03-20 17:00:00', '2024-03-05 00:00:00', '2024-03-19 23:59:59', 500, 0, 3, '张薇', '13800000003', '社团', 4),
(7, 'Java后端开发技术分享会', '面向计算机学院学生的技术分享活动，内容涵盖Spring Boot微服务架构、数据库优化、Redis缓存实战、性能调优等热门主题。', '计算机学院 205教室', '2024-04-08 19:00:00', '2024-04-08 21:00:00', '2024-04-01 00:00:00', '2024-04-07 23:59:59', 60, 0, 2, '李明', '13800000002', '学术', 0);

-- 报名数据
INSERT INTO registration (id, activity_id, user_id, real_name, student_no, phone, department, remark, status, reject_reason, register_time, audit_time, audit_by) VALUES
(1, 1, 5, '陈思远', '20220101', '13800000005', '计算机学院', '有2年编程经验，希望参加', 1, NULL, '2024-03-21 10:30:00', '2024-03-22 09:00:00', 2),
(2, 1, 9, '周子涵', '20220105', '13800000009', '计算机学院', '对算法竞赛很感兴趣', 1, NULL, '2024-03-22 14:20:00', '2024-03-23 10:00:00', 2),
(3, 1, 11, '黄思诚', '20220107', '13800000011', '电子工程学院', '想锻炼编程能力', 0, NULL, '2024-03-25 09:15:00', NULL, NULL),
(4, 2, 6, '林雨桐', '20220102', '13800000006', '外国语学院', '热爱环保事业', 1, NULL, '2024-03-12 11:00:00', '2024-03-13 15:00:00', 3),
(5, 2, 8, '孙艺萌', '20220104', '13800000008', '艺术学院', '希望为校园环保出力', 0, NULL, '2024-03-18 16:45:00', NULL, NULL),
(6, 3, 10, '吴佳怡', '20220106', '13800000010', '医学院', '学过声乐，热爱唱歌', 1, NULL, '2024-03-20 08:30:00', '2024-03-21 11:00:00', 4),
(7, 4, 7, '赵子轩', '20220103', '13800000007', '管理学院', '对AI技术很感兴趣', 0, NULL, '2024-03-22 13:00:00', NULL, NULL);

-- 公告数据
INSERT INTO notice (id, title, content, publisher_id, publisher_name, top, status, view_count, publish_time) VALUES
(1, '关于2024年春季校园活动报名系统上线的通知', '各位师生：

为提升校园活动管理效率，学校信息中心开发的校园活动报名系统正式上线运行。系统支持活动在线发布、一键报名、审核管理、名单导出等功能。

使用说明：
1. 学生使用学号登录，初始密码为123456
2. 社团负责人可在线提交活动申请
3. 管理员每日17:00前集中审核

如有问题请联系信息中心，电话：010-12345678。', 1, '系统管理员', 1, 1, 256, '2024-03-15 09:00:00'),
(2, '活动报名系统使用指南', '一、学生端功能
1. 查看活动列表：登录后首页展示所有已发布活动
2. 搜索活动：支持按关键词、类型筛选
3. 一键报名：点击报名按钮，填写信息即可
4. 查看报名状态：在个人中心可查看审核进度

二、负责人端功能
1. 发布活动：填写活动详情后提交审核
2. 报名审核：查看报名列表，批量审核
3. 数据导出：导出报名成功名单为Excel

三、管理员功能
1. 用户管理：管理所有系统用户
2. 活动审核：审核活动发布申请
3. 公告管理：发布系统公告', 1, '系统管理员', 1, 1, 189, '2024-03-15 10:00:00'),
(3, '近期学术活动预告', '本月学术类活动安排：

1. 人工智能前沿技术讲座（3月30日）
2. Java后端开发技术分享会（4月8日）
3. 2024春季校园编程马拉松（4月15-16日）

请同学们及时关注系统更新，选择感兴趣的活动报名参加。', 1, '系统管理员', 0, 1, 98, '2024-03-18 14:00:00'),
(4, '文体活动月活动征集通知', '各院系、社团：

为丰富校园文化生活，学校将于4月举办文体活动月。现面向全校征集活动方案，有意向的社团/院系负责人请在系统中提交活动申请，截止日期为3月28日。

征集范围：
- 体育竞赛类（篮球、羽毛球、乒乓球等）
- 文艺表演类（歌唱、舞蹈、乐器等）
- 文化展示类（摄影展、书画展等）', 1, '系统管理员', 0, 1, 76, '2024-03-20 09:30:00'),
(5, '系统维护公告', '各位用户：

系统将于本周日凌晨2:00-4:00进行例行维护，期间可能短暂影响服务，请提前安排使用。

维护内容：
1. 数据库性能优化
2. 安全补丁更新
3. 功能体验优化

感谢您的理解与支持。', 1, '系统管理员', 0, 1, 45, '2024-03-22 16:00:00');
