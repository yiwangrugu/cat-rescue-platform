package com.catrescue.catrescueplatform.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 数据库初始化配置
 * 在应用程序启动时执行数据库初始化操作
 */
@Configuration
public class DatabaseInitializer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 已禁用自动初始化，如需初始化数据库请手动执行SQL脚本
    // @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            // 创建评论表
            String createCommentsTableSql = """
                    CREATE TABLE IF NOT EXISTS comments (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        post_id BIGINT NOT NULL,
                        content TEXT NOT NULL,
                        author_id BIGINT NOT NULL,
                        parent_id BIGINT DEFAULT NULL,
                        like_count INT DEFAULT 0,
                        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                    """;

            try {
                // 为 users 表添加 gender 字段
                try {
                    // 检查 gender 字段是否存在
                    Integer count = jdbcTemplate.queryForObject(
                            "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'gender'",
                            Integer.class);

                    if (count == 0) {
                        jdbcTemplate.execute("ALTER TABLE users ADD COLUMN gender VARCHAR(10) DEFAULT 'unknown'");
                        System.out.println("✅ users 表添加 gender 字段成功！");
                    } else {
                        System.out.println("✅ users 表 gender 字段已存在，跳过添加步骤！");
                    }
                } catch (Exception e) {
                    System.out.println("⚠️  users 表添加 gender 字段失败：" + e.getMessage());
                }

                // 创建评论表
                try {
                    jdbcTemplate.execute(createCommentsTableSql);
                    System.out.println("✅ 评论表创建成功！");

                    // 分别执行创建索引的SQL语句
                    try {
                        Integer indexCount = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_NAME = 'comments' AND INDEX_NAME = 'idx_comments_post_id'",
                                Integer.class);
                        if (indexCount == 0) {
                            jdbcTemplate.execute("CREATE INDEX idx_comments_post_id ON comments(post_id)");
                            System.out.println("✅ 评论表 post_id 索引创建成功！");
                        } else {
                            System.out.println("✅ 评论表 post_id 索引已存在，跳过创建！");
                        }
                    } catch (Exception e) {
                        System.out.println("⚠️  评论表 post_id 索引创建失败：" + e.getMessage());
                    }

                    try {
                        Integer indexCount = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_NAME = 'comments' AND INDEX_NAME = 'idx_comments_author_id'",
                                Integer.class);
                        if (indexCount == 0) {
                            jdbcTemplate.execute("CREATE INDEX idx_comments_author_id ON comments(author_id)");
                            System.out.println("✅ 评论表 author_id 索引创建成功！");
                        } else {
                            System.out.println("✅ 评论表 author_id 索引已存在，跳过创建！");
                        }
                    } catch (Exception e) {
                        System.out.println("⚠️  评论表 author_id 索引创建失败：" + e.getMessage());
                    }

                    try {
                        Integer indexCount = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_NAME = 'comments' AND INDEX_NAME = 'idx_comments_parent_id'",
                                Integer.class);
                        if (indexCount == 0) {
                            jdbcTemplate.execute("CREATE INDEX idx_comments_parent_id ON comments(parent_id)");
                            System.out.println("✅ 评论表 parent_id 索引创建成功！");
                        } else {
                            System.out.println("✅ 评论表 parent_id 索引已存在，跳过创建！");
                        }
                    } catch (Exception e) {
                        System.out.println("⚠️  评论表 parent_id 索引创建失败：" + e.getMessage());
                    }
                } catch (Exception e) {
                    System.err.println("❌ 评论表创建失败：" + e.getMessage());
                }

                // 创建收藏表
                try {
                    String createFavoritesTableSql = """
                            CREATE TABLE IF NOT EXISTS favorites (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                user_id BIGINT NOT NULL,
                                post_id BIGINT NOT NULL,
                                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
                                UNIQUE KEY uk_user_post (user_id, post_id)
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                            """;
                    jdbcTemplate.execute(createFavoritesTableSql);
                    System.out.println("✅ 收藏表创建成功！");

                    // 创建索引
                    try {
                        Integer indexCount = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_NAME = 'favorites' AND INDEX_NAME = 'idx_favorites_user_id'",
                                Integer.class);
                        if (indexCount == 0) {
                            jdbcTemplate.execute("CREATE INDEX idx_favorites_user_id ON favorites(user_id)");
                            System.out.println("✅ 收藏表 user_id 索引创建成功！");
                        } else {
                            System.out.println("✅ 收藏表 user_id 索引已存在，跳过创建！");
                        }
                    } catch (Exception e) {
                        System.out.println("⚠️  收藏表 user_id 索引创建失败：" + e.getMessage());
                    }

                    try {
                        Integer indexCount = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_NAME = 'favorites' AND INDEX_NAME = 'idx_favorites_post_id'",
                                Integer.class);
                        if (indexCount == 0) {
                            jdbcTemplate.execute("CREATE INDEX idx_favorites_post_id ON favorites(post_id)");
                            System.out.println("✅ 收藏表 post_id 索引创建成功！");
                        } else {
                            System.out.println("✅ 收藏表 post_id 索引已存在，跳过创建！");
                        }
                    } catch (Exception e) {
                        System.out.println("⚠️  收藏表 post_id 索引创建失败：" + e.getMessage());
                    }
                } catch (Exception e) {
                    System.err.println("❌ 收藏表创建失败：" + e.getMessage());
                }

                // 为 posts 表添加 favorite_count 字段
                try {
                    // 检查 favorite_count 字段是否存在
                    Integer count = jdbcTemplate.queryForObject(
                            "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_NAME = 'posts' AND COLUMN_NAME = 'favorite_count'",
                            Integer.class);

                    if (count == 0) {
                        jdbcTemplate.execute("ALTER TABLE posts ADD COLUMN favorite_count INT DEFAULT 0");
                        System.out.println("✅ posts 表添加 favorite_count 字段成功！");
                    } else {
                        System.out.println("✅ posts 表 favorite_count 字段已存在，跳过添加步骤！");
                    }
                } catch (Exception e) {
                    System.out.println("⚠️  posts 表添加 favorite_count 字段失败：" + e.getMessage());
                }

            } catch (Exception e) {
                System.err.println("❌ 评论表创建失败：" + e.getMessage());
            }
        };
    }
}
