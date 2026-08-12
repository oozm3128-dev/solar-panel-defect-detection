# 光伏电池板缺陷检测系统 - 开发指南

## 系统架构

### 技术栈

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

### 系统架构图

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

### 数据流

1. 用户上传图片到前端
2. 前端将图片发送到后端
3. 后端调用 FastAPI 服务进行缺陷检测
4. FastAPI 使用 YOLOv11 模型识别缺陷
5. 后端保存检测结果到数据库
6. 前端展示检测结果和统计数据

## 项目结构

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

## 核心模块开发

### 1. 前端开发

#### 页面组件

| 页面 | 功能 | 文件位置 |
|------|------|----------|
| 登录页面 | 用户登录 | `frontend/src/views/Login.vue` |
| 注册页面 | 用户注册 | `frontend/src/views/Register.vue` |
| 数据大盘 | 统计数据展示 | `frontend/src/views/Dashboard.vue` |
| 检测工作台 | 图片检测 | `frontend/src/views/Detection.vue` |
| 历史记录 | 检测记录管理 | `frontend/src/views/Records.vue` |
| 个人中心 | 用户信息管理 | `frontend/src/views/Profile.vue` |
| 用户管理 | 管理员功能 | `frontend/src/views/Users.vue` |

#### API 调用

```javascript
// frontend/src/api/index.js
export const authApi = {
  login: (data) => axios.post('/api/auth/login', data),
  register: (data) => axios.post('/api/auth/register', data),
  getCurrentUser: () => axios.get('/api/auth/current')
}

export const detectionApi = {
  detectImage: (formData) => axios.post('/api/detection/image', formData),
  analyzeDefects: (recordId) => axios.post(`/api/detection/analyze/${recordId}`),
  getDetectionRecords: (params) => axios.get('/api/detection/records', { params }),
  getDefectDetails: (recordId) => axios.get(`/api/detection/defects/${recordId}`),
  getAiAnalysisReport: (recordId) => axios.get(`/api/detection/analysis/${recordId}`),
  getDetectionRecord: (recordId) => axios.get(`/api/detection/record/${recordId}`),
  getStatistics: () => axios.get('/api/detection/statistics')
}
```

### 2. 后端开发

#### 控制器

| 控制器 | 功能 | 文件位置 |
|--------|------|----------|
| AuthController | 用户认证 | `src/main/java/com/solar/panel/detection/controller/AuthController.java` |
| DetectionController | 检测管理 | `src/main/java/com/solar/panel/detection/controller/DetectionController.java` |
| UserController | 用户管理 | `src/main/java/com/solar/panel/detection/controller/UserController.java` |

#### 服务层

| 服务 | 功能 | 文件位置 |
|------|------|----------|
| DetectionService | 检测服务 | `src/main/java/com/solar/panel/detection/service/DetectionService.java` |
| SysUserService | 用户服务 | `src/main/java/com/solar/panel/detection/service/SysUserService.java` |

#### 数据模型

| 实体 | 功能 | 文件位置 |
|------|------|----------|
| SysUser | 用户信息 | `src/main/java/com/solar/panel/detection/entity/SysUser.java` |
| DetectionRecord | 检测记录 | `src/main/java/com/solar/panel/detection/entity/DetectionRecord.java` |
| DefectDetail | 缺陷详情 | `src/main/java/com/solar/panel/detection/entity/DefectDetail.java` |
| AiAnalysisReport | AI 分析报告 | `src/main/java/com/solar/panel/detection/entity/AiAnalysisReport.java` |

### 3. AI 服务开发

#### YOLO 服务

```python
# fastapi/services/yolo_service.py
class YoloService:
    def __init__(self):
        self.model_path = r"C:\Users\gl\Downloads\best.pt"
        self.model = self.load_model()
    
    def detect(self, image_path, model_version):
        # 模型预测
        # 处理结果
        # 返回缺陷列表和结果图像
```

#### Kimi 服务

```python
# fastapi/services/kimi_service.py
class KimiService:
    def analyze(self, defects):
        # 构建提示词
        # 调用 Kimi API
        # 处理响应
        # 返回分析报告
```

## 数据库设计

### 表结构

**sys_user 表**
| 字段名 | 数据类型 | 描述 |
|--------|----------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名 |
| password | VARCHAR(100) | 密码（BCrypt 加密） |
| name | VARCHAR(50) | 姓名 |
| avatar | LONGTEXT | 头像（Base64） |
| role | VARCHAR(20) | 角色 |
| created_at | TIMESTAMP | 创建时间 |

**detection_record 表**
| 字段名 | 数据类型 | 描述 |
|--------|----------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户 ID |
| detection_mode | VARCHAR(20) | 检测模式 |
| model_version | VARCHAR(20) | 模型版本 |
| original_image_path | LONGTEXT | 原始图像（Base64） |
| result_image_path | LONGTEXT | 结果图像（Base64） |
| detection_time | TIMESTAMP | 检测时间 |

**defect_detail 表**
| 字段名 | 数据类型 | 描述 |
|--------|----------|------|
| id | BIGINT | 主键 |
| record_id | BIGINT | 检测记录 ID |
| defect_type | VARCHAR(50) | 缺陷类型 |
| confidence | DOUBLE | 置信度 |
| x | INT | 边界框 x 坐标 |
| y | INT | 边界框 y 坐标 |
| w | INT | 边界框宽度 |
| h | INT | 边界框高度 |

**ai_analysis_report 表**
| 字段名 | 数据类型 | 描述 |
|--------|----------|------|
| id | BIGINT | 主键 |
| record_id | BIGINT | 检测记录 ID |
| analysis_content | TEXT | 分析内容 |
| created_at | TIMESTAMP | 创建时间 |

## 开发流程

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

### 2. 前端开发

**安装依赖**：
```bash
cd frontend
npm install
```

**开发模式**：
```bash
npm run dev
```

**构建**：
```bash
npm run build
```

### 3. 后端开发

**编译项目**：
```bash
mvn clean package
```

**运行**：
```bash
mvn spring-boot:run
```

### 4. AI 服务开发

**安装依赖**：
```bash
cd fastapi
pip install -r requirements.txt
```

**运行**：
```bash
python main.py
```

## 代码规范

### 前端

- 使用 ES6+ 语法
- 组件命名使用 PascalCase
- 变量和函数使用 camelCase
- 常量使用 UPPERCASE
- 使用 Prettier 格式化代码

### 后端

- 包名使用小写字母
- 类名使用 PascalCase
- 方法和变量使用 camelCase
- 常量使用 UPPERCASE
- 使用 Lombok 简化代码
- 遵循 Spring Boot 编码规范

### Python

- 文件名使用 snake_case
- 类名使用 PascalCase
- 方法和变量使用 snake_case
- 常量使用 UPPERCASE
- 遵循 PEP 8 编码规范

## 测试

### 前端测试

- 使用 Vitest 进行组件测试
- 使用 Cypress 进行端到端测试

### 后端测试

- 使用 JUnit 进行单元测试
- 使用 Mockito 进行模拟测试

### AI 服务测试

- 使用 pytest 进行单元测试
- 测试模型加载和预测

## 部署

### 本地部署

1. 启动后端服务
2. 启动前端服务
3. 启动 AI 服务
4. 访问前端页面

### 生产部署

1. **前端**：
   - 构建前端项目：`npm run build`
   - 部署到 Nginx 或 Apache

2. **后端**：
   - 打包成 JAR 文件：`mvn clean package`
   - 部署到 Tomcat 或直接运行

3. **AI 服务**：
   - 部署到 Python 环境
   - 使用 Gunicorn 作为 WSGI 服务器

## 监控与维护

### 日志管理

- 后端日志：Spring Boot 日志
- 前端日志：浏览器控制台
- AI 服务日志：FastAPI 日志

### 常见问题

| 问题 | 原因 | 解决方法 |
|------|------|----------|
| 服务启动失败 | 端口被占用 | 检查端口使用情况 |
| 检测失败 | 模型文件不存在 | 检查模型路径 |
| 数据库连接失败 | 数据库文件损坏 | 重建数据库 |
| 前端页面空白 | 依赖缺失 | 重新安装依赖 |

## 扩展建议

### 功能扩展

- **视频检测**：支持视频文件上传和逐帧检测
- **摄像头检测**：实时摄像头流检测
- **批量检测**：支持多图片批量处理
- **缺陷分类**：更详细的缺陷类型分类

### 技术优化

- **模型优化**：使用更轻量的模型提高速度
- **缓存机制**：添加 Redis 缓存提高性能
- **容器化**：使用 Docker 部署简化环境配置
- **CI/CD**：搭建持续集成和部署流程

### 业务扩展

- **报表系统**：生成详细的检测报告
- **API 开放**：提供第三方集成接口
- **移动端**：开发移动应用
- **云端部署**：部署到云服务平台

## 开发工具

- **IDE**：IntelliJ IDEA、VS Code
- **版本控制**：Git
- **构建工具**：Maven、Vite
- **数据库工具**：H2 Console
- **API 测试**：Postman、Insomnia

## 参考文档

- [Vue 3 官方文档](https://v3.vuejs.org/)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [FastAPI 文档](https://fastapi.tiangolo.com/)
- [YOLO 官方文档](https://docs.ultralytics.com/)
- [Element Plus 文档](https://element-plus.org/)
- [ECharts 文档](https://echarts.apache.org/zh/)

## 联系方式

如有技术问题，请联系开发团队。