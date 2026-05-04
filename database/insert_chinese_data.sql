-- 插入中文测试数据到流浪猫救助平台数据库

USE cat_rescue;

-- 清空现有数据（保留用户数据）
DELETE FROM cats;
DELETE FROM posts;
DELETE FROM rescues;
DELETE FROM adoptions;

-- 1. 插入猫咪数据
INSERT IGNORE INTO cats (name, breed, age, gender, health_status, description, location, status, rescuer_id) VALUES
('小橘', '橘猫', 6, 'MALE', 'HEALTHY', '性格温顺，喜欢与人亲近，适合家庭领养。', '北京市朝阳区', 'WAITING_ADOPTION', 2),
('小黑', '黑猫', 8, 'FEMALE', 'RECOVERING', '正在恢复中，需要特别照顾，性格独立。', '上海市浦东新区', 'UNDER_CARE', 2),
('小白', '白猫', 4, 'MALE', 'HEALTHY', '活泼可爱，喜欢玩耍，适合有活力的家庭。', '广州市天河区', 'WAITING_ADOPTION', 2),
('小花', '三花猫', 12, 'FEMALE', 'HEALTHY', '成熟稳重，适合家庭饲养，性格温和。', '深圳市南山区', 'WAITING_ADOPTION', 2),
('咪咪', '狸花猫', 3, 'FEMALE', 'HEALTHY', '活泼好动的小猫，适合有经验的养猫人。', '杭州市西湖区', 'WAITING_ADOPTION', 2);

-- 2. 插入帖子数据
INSERT IGNORE INTO posts (title, content, author_id, category, view_count, like_count, comment_count) VALUES
('求助：发现一只受伤的橘猫需要救助', '在朝阳区XX小区发现一只右腿受伤的橘猫，需要紧急救助。猫咪看起来很虚弱，希望有救助人员能尽快前往。', 2, 'HELP', 156, 15, 8),
('分享：我家猫咪的日常照片', '分享一些我家猫咪的可爱照片，希望大家喜欢！猫咪名字叫小花，是一只三花猫，性格非常温顺。', 1, 'SHARE', 289, 45, 12),
('讨论：如何正确喂养流浪猫', '想和大家讨论一下如何科学地喂养流浪猫，避免对它们造成伤害。有什么好的建议和经验可以分享吗？', 3, 'DISCUSSION', 167, 23, 18),
('新手铲屎官求指导', '刚领养了一只小猫，不知道该怎么照顾，求各位有经验的铲屎官指导一下！', 2, 'HELP', 89, 12, 15),
('猫咪绝育的重要性', '今天想和大家讨论一下猫咪绝育的重要性，以及绝育后的注意事项。', 1, 'DISCUSSION', 234, 67, 25);

-- 3. 插入救助需求数据
INSERT IGNORE INTO rescues (title, description, location, cat_condition, urgency_level, reporter_id, status, rescuer_id) VALUES
('发现受伤的小橘猫需要救助', '在小区花园发现一只受伤的小橘猫，右前腿受伤无法行走，需要紧急救助和治疗。', '北京市朝阳区某某小区花园', '右前腿受伤，无法行走，精神状态良好，愿意接受人类接近。', 'HIGH', 3, 'REPORTED', NULL),
('流浪母猫需要绝育和安置', '公司楼下有一只流浪母猫，已经生过几窝小猫，希望能帮助它进行绝育手术并找到合适的安置。', '上海市浦东新区某某大厦楼下', '健康状态良好，性格温顺，适合领养。', 'MEDIUM', 3, 'IN_PROGRESS', 2),
('被困在屋顶的小黑猫', '邻居家的屋顶上困住了一只小黑猫，已经两天没有下来，需要帮助救援。', '广州市天河区某某小区3栋屋顶', '精神状态尚可，但可能缺水和饥饿。', 'CRITICAL', 3, 'REPORTED', NULL),
('已完成：帮助走失猫咪回家', '帮助一只走失的布偶猫找到了主人，猫咪健康状态良好。', '深圳市南山区某某商业街', '健康状态良好，已安全送回主人身边。', 'LOW', 2, 'RESCUED', 2);

-- 4. 插入领养申请数据
INSERT IGNORE INTO adoptions (cat_id, applicant_id, application_reason, living_condition, experience, status) VALUES
(1, 3, '我一直想养一只温顺的猫咪，家里条件适合养宠物。住在公寓，有足够的空间，家里没有其他宠物。', '之前养过猫，有丰富的养猫经验。', 'PENDING'),
(2, 3, '喜欢黑猫的神秘感，希望能给它一个温暖的家。独居，有稳定的工作和收入，可以给猫咪良好的生活条件。', '第一次养猫，但已经做了很多准备工作。', 'UNDER_REVIEW');

-- 更新数据库统计信息
UPDATE cats SET rescuer_id = 2 WHERE id IN (3, 5);
UPDATE rescues SET rescuer_id = 2, status = 'IN_PROGRESS' WHERE id = 3;