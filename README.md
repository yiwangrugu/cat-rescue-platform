# 🐱 流浪猫救助平台 (Cat Rescue Platform)

一个功能完整的流浪猫救助管理平台，支持猫咪信息管理、领养申请、社区交流、救助需求发布等功能，并集成AI猫咪品种识别技术。

## ✨ 项目特色

- **多端应用**：用户端、救助端、管理端三端分离
- **AI识别**：集成猫咪品种智能识别技术
- **实时通信**：WebSocket实现实时消息推送
- **社区互动**：完整的论坛系统，支持点赞、收藏、评论
- **救助管理**：专业的救助流程管理和跟踪

## 🚀 快速开始

### 环境要求

- **Java**: 22+
- **Node.js**: 18+
- **MySQL**: 8.0+
- **Maven**: 3.6+

### 数据库初始化

1. 创建数据库：
```sql
CREATE DATABASE cat_rescue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 导入表结构：
```bash
mysql -u root -p cat_rescue < database/create_tables.sql
```

3. 导入测试数据：
```bash
mysql -u root -p cat_rescue < database/insert_chinese_data.sql
```

### 后端启动

```bash
cd cat-rescue-backend
mvn clean compile
mvn spring-boot:run
```

后端服务将运行在：`http://localhost:8080`

### 前端启动

```bash
cd cat-rescue-frontend
npm install
npm run dev:all
```

前端服务将运行在：
- 用户端：`http://localhost:3000`
- 救助端：`http://localhost:3001`
- 管理端：`http://localhost:3002`

### 一键启动

Windows系统可以使用提供的批处理文件：
```bash
start-all.bat
```

## 📱 应用功能

### 用户端功能
- 🐱 **猫咪浏览**：查看待领养猫咪信息
- ❤️ **收藏功能**：收藏感兴趣的猫咪
- 📝 **领养申请**：在线提交领养申请
- 💬 **社区交流**：发帖、评论、点赞
- 👤 **个人中心**：个人信息管理

### 救助端功能
- 🏥 **猫咪管理**：添加、编辑猫咪信息
- 🆘 **救助管理**：处理救助需求
- 📊 **数据统计**：救助工作统计
- 👥 **用户管理**：领养申请审核

### 管理端功能
- 👑 **系统管理**：用户、权限管理
- 📈 **数据统计**：平台运营数据
- 🔍 **内容审核**：帖子、评论审核
- ⚙️ **系统设置**：平台配置管理

## 🛠️ 技术架构

### 前端技术栈
- **框架**: Vue 3 + Vite
- **UI组件**: Element Plus
- **路由**: Vue Router
- **状态管理**: Pinia
- **HTTP请求**: Axios
- **构建工具**: Vite

### 后端技术栈
- **框架**: Spring Boot 3.2.11
- **ORM**: MyBatis Plus 3.5.7
- **安全**: Spring Security + JWT
- **数据库**: MySQL 8.0+
- **实时通信**: WebSocket
- **构建工具**: Maven

### AI技术栈
- **图像识别**: 基于机器学习的猫咪品种识别
- **模型训练**: 自定义训练模型
- **预测引擎**: 集成到前端应用

## 📁 项目结构

```
cat-rescue-platform/
├── cat-rescue-backend/          # 后端Spring Boot项目
│   ├── src/main/java/          # Java源代码
│   ├── src/main/resources/     # 配置文件
│   └── pom.xml                # Maven配置
├── cat-rescue-frontend/         # 前端Vue项目
│   ├── src/                    # Vue源代码
│   ├── public/                 # 静态资源
│   └── package.json           # Node.js配置
├── cat-breed-ai/               # AI猫咪识别模块
│   ├── src/                   # Python源代码
│   └── requirements.txt       # Python依赖
├── database/                   # 数据库脚本
│   ├── create_tables.sql      # 表结构创建
│   ├── init.sql               # 数据库初始化
│   └── test_data.sql          # 测试数据
└── README.md                  # 项目说明文档
```

## 🔧 核心模块

### 实体类设计
- **User**: 用户信息
- **Cat**: 猫咪信息
- **Post**: 社区帖子
- **Comment**: 评论信息
- **Adoption**: 领养申请
- **Rescue**: 救助需求

### API接口

#### 猫咪相关
- `GET /api/cats` - 获取猫咪列表
- `GET /api/cats/{id}` - 获取猫咪详情
- `POST /api/cats/favorite` - 收藏猫咪
- `DELETE /api/cats/favorite/{id}` - 取消收藏

#### 社区相关
- `GET /api/community/posts` - 获取帖子列表
- `POST /api/community/posts` - 发布帖子
- `POST /api/community/posts/{id}/like` - 点赞帖子
- `POST /api/community/posts/{id}/comment` - 发表评论

#### 用户相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/users/profile` - 获取用户信息

## 🎯 部署指南

### 生产环境部署

#### 后端部署
1. 打包应用：
```bash
mvn clean package -DskipTests
```

2. 运行jar包：
```bash
java -jar target/cat-rescue-platform-0.0.1-SNAPSHOT.jar
```

#### 前端部署
1. 构建生产版本：
```bash
npm run build
```

2. 部署到Nginx：
```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    location / {
        root /path/to/dist;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8080;
    }
}
```

## 🔒 安全配置

- **JWT认证**：所有API请求需要携带Token
- **权限控制**：基于角色的访问控制(RBAC)
- **密码加密**：使用BCrypt加密存储
- **CORS配置**：跨域资源共享设置
- **SQL注入防护**：使用预编译语句

## 📊 数据库设计

### 核心表结构

#### users表（用户表）
- id, username, password, email, role, status
- 支持管理员、救助人员、普通用户三种角色

#### cats表（猫咪表）
- id, name, breed, age, gender, health_status
- 包含猫咪基本信息、健康状况、领养状态

#### posts表（帖子表）
- id, title, content, author_id, category
- 支持求助、分享、讨论三种帖子类型

## 🐛 故障排除

### 常见问题

1. **端口冲突**：修改application.yml中的端口配置
2. **数据库连接失败**：检查数据库配置和网络连接
3. **前端构建失败**：清除node_modules重新安装依赖
4. **JWT认证失败**：检查Token有效期和签名密钥

### 日志查看

后端日志位置：`cat-rescue-backend/logs/application.log`
前端开发日志：浏览器开发者工具Console面板


---

**让每一只流浪猫都能找到温暖的家** 🏡
