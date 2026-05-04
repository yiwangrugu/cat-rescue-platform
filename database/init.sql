-- 创建数据库
CREATE DATABASE IF NOT EXISTS cat_rescue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE cat_rescue;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    role ENUM('ADMIN', 'RESCUER', 'USER') DEFAULT 'USER',
    avatar VARCHAR(255),
    real_name VARCHAR(50),
    location VARCHAR(100),
    introduction TEXT,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 猫咪信息表
CREATE TABLE IF NOT EXISTS cats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    breed VARCHAR(50),
    age INT,
    gender ENUM('公', '母', '未知'),
    health_status ENUM('健康', '生病', '恢复中', '危重'),
    personality TEXT,
    description TEXT,
    location VARCHAR(100),
    images JSON,
    status ENUM('待领养', '已领养', '照顾中', '已故') DEFAULT '待领养',
    rescuer_id BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (rescuer_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 领养申请表
CREATE TABLE IF NOT EXISTS adoptions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cat_id BIGINT NOT NULL,
    applicant_id BIGINT NOT NULL,
    application_reason TEXT,
    living_condition TEXT,
    experience TEXT,
    status ENUM('待审核', '审核中', '已批准', '已拒绝') DEFAULT '待审核',
    reviewer_id BIGINT,
    review_comment TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (cat_id) REFERENCES cats(id),
    FOREIGN KEY (applicant_id) REFERENCES users(id),
    FOREIGN KEY (reviewer_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 救助需求表
CREATE TABLE IF NOT EXISTS rescues (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    location VARCHAR(100),
    cat_condition TEXT,
    urgency_level ENUM('低', '中', '高', '紧急') DEFAULT '中',
    reporter_id BIGINT NOT NULL,
    rescuer_id BIGINT,
    status ENUM('已报告', '进行中', '已救助', '已关闭') DEFAULT '已报告',
    images JSON,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (reporter_id) REFERENCES users(id),
    FOREIGN KEY (rescuer_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 社区帖子表
CREATE TABLE IF NOT EXISTS posts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    author_id BIGINT NOT NULL,
    category VARCHAR(50),
    images JSON,
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    status ENUM('草稿', '已发布', '已删除') DEFAULT '已发布',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入测试数据
INSERT IGNORE INTO users (username, password, email, role, real_name, location) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVwE.', 'admin@catrescue.com', 'ADMIN', '管理员', '北京市'),
('rescuer1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVwE.', 'rescuer1@catrescue.com', 'RESCUER', '救助员1', '上海市'),
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVwE.', 'user1@catrescue.com', 'USER', '用户1', '广州市');

INSERT IGNORE INTO cats (name, breed, age, gender, health_status, description, location, status, rescuer_id) VALUES
('小橘', '橘猫', 6, '公', '健康', '性格温顺，喜欢与人亲近，适合家庭领养。', '北京市朝阳区', '待领养', 2),
('小黑', '黑猫', 8, '母', '恢复中', '正在恢复中，需要特别照顾，性格独立。', '上海市浦东新区', '照顾中', 2),
('小白', '白猫', 4, '公', '健康', '活泼可爱，喜欢玩耍，适合有活力的家庭。', '广州市天河区', '待领养', 2),
('小花', '三花猫', 12, '母', '健康', '成熟稳重，适合家庭饲养，性格温和。', '深圳市南山区', '待领养', 2),
('咪咪', '狸花猫', 3, '母', '健康', '活泼好动的小猫，适合有经验的养猫人。', '杭州市西湖区', '待领养', 2);

INSERT IGNORE INTO posts (title, content, author_id, category, view_count, like_count, comment_count) VALUES
('求助：发现一只受伤的橘猫需要救助', '在朝阳区XX小区发现一只右腿受伤的橘猫，需要紧急救助。猫咪看起来很虚弱，希望有救助人员能尽快前往。', 2, 'HELP', 156, 15, 8),
('分享：我家猫咪的日常照片', '分享一些我家猫咪的可爱照片，希望大家喜欢！猫咪名字叫小花，是一只三花猫，性格非常温顺。', 1, 'SHARE', 289, 45, 12),
('讨论：如何正确喂养流浪猫', '想和大家讨论一下如何科学地喂养流浪猫，避免对它们造成伤害。有什么好的建议和经验可以分享吗？', 3, 'DISCUSSION', 167, 23, 18),
('新手铲屎官求指导', '刚领养了一只小猫，不知道该怎么照顾，求各位有经验的铲屎官指导一下！', 2, 'HELP', 89, 12, 15),
('猫咪绝育的重要性', '今天想和大家讨论一下猫咪绝育的重要性，以及绝育后的注意事项。', 1, 'DISCUSSION', 234, 67, 25);

INSERT IGNORE INTO rescues (title, description, location, cat_condition, urgency_level, reporter_id, status, rescuer_id) VALUES
('发现受伤的小橘猫需要救助', '在小区花园发现一只受伤的小橘猫，右前腿受伤无法行走，需要紧急救助和治疗。', '北京市朝阳区某某小区花园', '右前腿受伤，无法行走，精神状态良好，愿意接受人类接近。', 'HIGH', 3, 'REPORTED', NULL),
('流浪母猫需要绝育和安置', '公司楼下有一只流浪母猫，已经生过几窝小猫，希望能帮助它进行绝育手术并找到合适的安置。', '上海市浦东新区某某大厦楼下', '健康状态良好，性格温顺，适合领养。', 'MEDIUM', 3, 'IN_PROGRESS', 2),
('被困在屋顶的小黑猫', '邻居家的屋顶上困住了一只小黑猫，已经两天没有下来，需要帮助救援。', '广州市天河区某某小区3栋屋顶', '精神状态尚可，但可能缺水和饥饿。', 'CRITICAL', 3, 'REPORTED', NULL),
('已完成：帮助走失猫咪回家', '帮助一只走失的布偶猫找到了主人，猫咪健康状态良好。', '深圳市南山区某某商业街', '健康状态良好，已安全送回主人身边。', 'LOW', 2, 'RESCUED', 2);

INSERT IGNORE INTO adoptions (cat_id, applicant_id, application_reason, living_condition, experience, status) VALUES
(1, 3, '我一直想养一只温顺的猫咪，家里条件适合养宠物。住在公寓，有足够的空间，家里没有其他宠物。', '之前养过猫，有丰富的养猫经验。', 'PENDING'),
(2, 3, '喜欢黑猫的神秘感，希望能给它一个温暖的家。独居，有稳定的工作和收入，可以给猫咪良好的生活条件。', '第一次养猫，但已经做了很多准备工作。', 'UNDER_REVIEW');