# 光伏电池板缺陷检测系统 - 项目总结文档

## 项目概述

这是一个完整的**光伏电池板缺陷检测系统**，使用人工智能技术自动识别太阳能电池板的缺陷。系统采用现代化的全栈技术栈，包括前端、后端和AI服务。

**核心功能**：
- 图片上传检测
- YOLOv11 模型缺陷识别
- Kimi 2.5 LLM 缺陷分析
- 历史记录管理
- 数据可视化统计
- 用户认证和个人中心

**技术价值**：
- 自动化缺陷检测，提高检测效率和准确性
- 减少人工检测成本
- 提供专业的缺陷分析报告
- 数据化管理检测记录

## 技术栈

| 分类 | 技术 | 版本 | 用途 |
|------|------|------|------|
| **前端** | Vue 3 | 3.4.21 | 用户界面 |
| | Element Plus | 2.5.6 | UI 组件库 |
| | ECharts | 5.5.0 | 数据可视化 |
| | Pinia | 2.1.7 | 状态管理 |
| | Vue Router | 4.3.0 | 路由管理 |
| **后端** | Spring Boot | 2.7.15 | 业务逻辑 |
| | MyBatis-Plus | 3.5.5 | 数据库操作 |
| | H2 数据库 | 2.1.214 | 数据存储 |
| | JWT | 0.9.1 | 身份认证 |
| **AI 服务** | Python | 3.9+ | 脚本语言 |
| | FastAPI | 最新版 | API 服务 |
| | YOLOv11 | 最新版 | 目标检测 |
| | OpenCV | 最新版 | 图像处理 |

## 系统架构

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│                 │     │                 │     │                 │
│   前端 (Vue 3)   │────▶│  后端 (Spring)  │────▶│  AI 服务 (Python) │
│                 │     │                 │     │                 │
└─────────────────┘     └─────────────────┘     └─────────────────┘
        ▲                       ▲                       │
        │                       │                       │
        └───────────────────────┘                       │
                              │                         │
                              ▼                         │
                     ┌─────────────────┐               │
                     │                 │               │
                     │    H2 数据库    │◀──────────────┘
                     │                 │
                     └─────────────────┘
```

**数据流**：
1. 用户上传图片到前端
2. 前端将图片发送到后端
3. 后端调用 FastAPI 服务进行缺陷检测
4. FastAPI 使用 YOLOv11 模型识别缺陷
5. 后端保存检测结果到数据库
6. 前端展示检测结果和统计数据

## 核心功能模块

### 1. 用户认证模块

**功能**：登录、注册、个人中心

**技术实现**：
- JWT 令牌生成和验证
- BCrypt 密码加密
- 自动用户注册（首次登录）

**文件位置**：
- `src/main/java/com/solar/panel/detection/controller/AuthController.java`
- `src/main/java/com/solar/panel/detection/config/JwtUtils.java`

### 2. 缺陷检测模块

**功能**：图片上传、模型选择、缺陷识别

**技术实现**：
- MultipartFile 处理文件上传
- RestTemplate 调用 FastAPI
- 数据库记录保存

**文件位置**：
- `src/main/java/com/solar/panel/detection/controller/DetectionController.java`
- `src/main/java/com/solar/panel/detection/service/DetectionService.java`

### 3. AI 分析模块

**功能**：缺陷类型识别、AI 分析报告生成

**技术实现**：
- YOLOv11 目标检测
- Kimi 2.5 LLM 分析
- Base64 图片编码

**文件位置**：
- `fastapi/services/yolo_service.py`
- `fastapi/services/kimi_service.py`

### 4. 数据可视化模块

**功能**：实时统计、图表展示

**技术实现**：
- ECharts 图表库
- 后端统计 API
- 前端数据处理

**文件位置**：
- `frontend/src/views/Dashboard.vue`
- `src/main/java/com/solar/panel/detection/controller/DetectionController.java` (统计 API)

### 5. 历史记录模块

**功能**：检测记录查询、详情查看、分析报告

**技术实现**：
- MyBatis-Plus 数据库操作
- 分页查询
- 缺陷详情关联

**文件位置**：
- `frontend/src/views/Records.vue`
- `src/main/java/com/solar/panel/detection/service/DetectionService.java`

## 代码结构

```
├── src/                        # 后端代码
│   ├── main/java/com/solar/panel/detection/
│   │   ├── controller/         # 控制器
│   │   ├── service/            # 服务层
│   │   ├── entity/             # 实体类
│   │   ├── mapper/             # 数据访问
│   │   ├── config/             # 配置
│   │   └── common/             # 公共类
│   └── main/resources/         # 资源文件
│       ├── application.yml     # 配置文件
│       └── db_schema.sql       # 数据库 schema
├── frontend/                   # 前端代码
│   ├── src/
│   │   ├── views/              # 页面组件
│   │   ├── api/                # API 调用
│   │   ├── store/              # 状态管理
│   │   ├── router/             # 路由配置
│   │   └── App.vue             # 根组件
│   ├── package.json            # 依赖管理
│   └── vite.config.js          # Vite 配置
├── fastapi/                    # AI 服务
│   ├── services/               # 服务模块
│   │   ├── yolo_service.py     # YOLO 检测
│   │   └── kimi_service.py     # LLM 分析
│   ├── main.py                 # FastAPI 入口
│   └── requirements.txt        # Python 依赖
├── pom.xml                     # Maven 配置
└── start_all.bat               # 启动脚本
```

## 如何运行系统

### 1. 环境准备

**安装依赖**：
- Java 21 JDK
- Node.js 18+
- Python 3.9+
- Maven
- Git

**克隆项目**：
```bash
git clone <项目地址>
cd solar-panel-detection
```

### 2. 启动服务

**使用一键启动脚本**：
```bash
# Windows
start_all.bat

# Linux/Mac
chmod +x start_all.sh
./start_all.sh
```

**手动启动**：
1. **启动后端**：
   ```bash
   cd solar-panel-detection
   mvn spring-boot:run
   ```

2. **启动前端**：
   ```bash
   cd solar-panel-detection/frontend
   npm install
   npm run dev
   ```

3. **启动 FastAPI**：
   ```bash
   cd solar-panel-detection/fastapi
   pip install -r requirements.txt
   python main.py
   ```

### 3. 访问系统

- **前端**：http://localhost:5173
- **后端 API**：http://localhost:8080
- **FastAPI**：http://localhost:8000

**默认登录**：
- 用户名：admin
- 密码：123456

## 学习路径建议

### 阶段一：前端基础

1. **HTML/CSS/JavaScript**：
   - 学习 HTML5 语义化标签
   - 掌握 CSS3 布局和样式
   - 熟悉 JavaScript 基础语法

2. **Vue 3 核心**：
   - Composition API
   - 组件通信
   - 路由管理
   - 状态管理 (Pinia)

3. **前端工具**：
   - Vite 构建工具
   - npm 包管理
   - 代码规范 (ESLint)

### 阶段二：后端基础

1. **Java 基础**：
   - 面向对象编程
   - 集合框架
   - 异常处理

2. **Spring Boot**：
   - 依赖注入
   - 控制器开发
   - 服务层设计
   - 数据访问 (MyBatis)

3. **数据库**：
   - SQL 基础
   - H2/MySQL 使用
   - 事务管理

### 阶段三：AI 服务

1. **Python 基础**：
   - 语法和数据结构
   - 模块和包管理
   - 异常处理

2. **FastAPI**：
   - API 设计
   - 请求处理
   - 响应格式化

3. **AI 模型**：
   - YOLO 目标检测
   - LLM 应用
   - 图像处理

### 阶段四：全栈整合

1. **API 设计**：
   - RESTful 规范
   - 请求参数验证
   - 错误处理

2. **前后端交互**：
   - Axios 请求
   - 响应处理
   - 状态管理

3. **部署与运维**：
   - 环境配置
   - 服务启动
   - 监控与日志

## 常见问题与解决方案

### 1. 服务启动失败

**问题**：后端启动时报错
**解决方案**：
- 检查 Java 版本（需要 Java 21）
- 检查端口是否被占用
- 查看日志文件分析错误

### 2. 检测功能不工作

**问题**：上传图片后没有检测结果
**解决方案**：
- 检查 FastAPI 服务是否运行
- 检查 YOLO 模型文件是否存在
- 查看后端日志中的错误信息

### 3. 数据库连接问题

**问题**：数据库连接失败
**解决方案**：
- 检查 H2 数据库路径配置
- 确保数据库文件有读写权限
- 尝试删除旧数据库重新初始化

### 4. 前端页面显示异常

**问题**：页面加载失败或样式异常
**解决方案**：
- 清除浏览器缓存
- 检查前端依赖是否安装完整
- 查看浏览器控制台错误

### 5. JWT 认证失败

**问题**：登录后提示 token 无效
**解决方案**：
- 检查 JWT 密钥配置
- 确保 token 没有过期
- 检查前端存储的 token 是否正确

## 项目扩展建议

### 1. 功能扩展

- **视频检测**：支持视频文件上传和逐帧检测
- **摄像头检测**：实时摄像头流检测
- **批量检测**：支持多图片批量处理
- **缺陷分类**：更详细的缺陷类型分类

### 2. 技术优化

- **模型优化**：使用更轻量的模型提高速度
- **缓存机制**：添加 Redis 缓存提高性能
- **容器化**：使用 Docker 部署简化环境配置
- **CI/CD**：搭建持续集成和部署流程

### 3. 业务扩展

- **报表系统**：生成详细的检测报告
- **API 开放**：提供第三方集成接口
- **移动端**：开发移动应用
- **云端部署**：部署到云服务平台

## 学习资源推荐

### 前端学习
- [Vue 3 官方文档](https://v3.vuejs.org/)
- [Element Plus 文档](https://element-plus.org/)
- [ECharts 文档](https://echarts.apache.org/zh/)

### 后端学习
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [MyBatis-Plus 文档](https://baomidou.com/)
- [JWT 认证教程](https://jwt.io/)

### AI 学习
- [FastAPI 文档](https://fastapi.tiangolo.com/)
- [YOLO 官方文档](https://docs.ultralytics.com/)
- [Python 官方教程](https://docs.python.org/3/)

### 全栈学习
- [MDN Web Docs](https://developer.mozilla.org/)
- [Stack Overflow](https://stackoverflow.com/)
- [GitHub 开源项目](https://github.com/)

## 总结

这个光伏电池板缺陷检测系统是一个完整的全栈项目，涵盖了前端、后端、AI 服务和数据库等多个技术领域。通过学习和实践这个项目，你可以：

1. **掌握全栈开发技能**：从前端界面到后端逻辑，再到 AI 服务
2. **理解系统架构设计**：学习如何设计和实现一个完整的应用系统
3. **应用 AI 技术**：了解如何集成和使用 AI 模型
4. **提升问题解决能力**：通过实际项目遇到和解决各种技术问题

作为代码小白，建议你从基础开始，逐步深入各个模块，多动手实践，多查阅文档。全栈开发是一个持续学习的过程，保持好奇心和学习热情，你会不断进步！

---

**项目状态**：基本完成，可投入使用
**后续计划**：视频检测功能、性能优化、部署文档完善

希望这个项目能成为你学习全栈开发的良好起点！