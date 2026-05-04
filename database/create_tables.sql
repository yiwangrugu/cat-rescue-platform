-- 创建猫咪信息表
CREATE TABLE IF NOT EXISTS cats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    breed VARCHAR(50),
    age INT,
    gender ENUM('MALE', 'FEMALE', 'UNKNOWN'),
    health_status ENUM('HEALTHY', 'SICK', 'RECOVERING', 'CRITICAL'),
    personality TEXT,
    description TEXT,
    location VARCHAR(100),
    images JSON,
    status ENUM('WAITING_ADOPTION', 'ADOPTED', 'UNDER_CARE', 'DECEASED') DEFAULT 'WAITING_ADOPTION',
    rescuer_id BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (rescuer_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建领养申请表
CREATE TABLE IF NOT EXISTS adoptions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cat_id BIGINT NOT NULL,
    applicant_id BIGINT NOT NULL,
    application_reason TEXT,
    living_condition TEXT,
    experience TEXT,
    status ENUM('PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    reviewer_id BIGINT,
    review_comment TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (cat_id) REFERENCES cats(id),
    FOREIGN KEY (applicant_id) REFERENCES users(id),
    FOREIGN KEY (reviewer_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建救助需求表
CREATE TABLE IF NOT EXISTS rescues (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    location VARCHAR(100),
    cat_condition TEXT,
    urgency_level ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
    reporter_id BIGINT NOT NULL,
    rescuer_id BIGINT,
    status ENUM('REPORTED', 'IN_PROGRESS', 'RESCUED', 'CLOSED') DEFAULT 'REPORTED',
    images JSON,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (reporter_id) REFERENCES users(id),
    FOREIGN KEY (rescuer_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建社区帖子表
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
    status ENUM('DRAFT', 'PUBLISHED', 'DELETED') DEFAULT 'PUBLISHED',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;