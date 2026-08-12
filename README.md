# 光伏电池板缺陷检测系统

基于深度学习的太阳能电池板EL图像缺陷智能检测系统。

## 功能特性

- **图片缺陷检测**：上传光伏电池板EL图像，自动识别缺陷
- **12种缺陷类型**：支持裂纹、黑芯、栅线、粗线、星形裂纹等12种常见缺陷
- **AI智能分析**：集成大语言模型，生成专业缺陷分析报告
- **数据可视化**：实时统计检测数据，图表直观展示
- **历史记录**：检测记录查询与管理
- **用户认证**：JWT安全认证，个人中心管理

## 技术栈

| 分类 | 技术 |
|------|------|
| 前端 | Vue 3 + Element Plus + ECharts |
| 后端 | Spring Boot 2.7 + MyBatis-Plus |
| 数据库 | H2 Database (文件模式) |
| AI服务 | FastAPI + YOLOv11 |
| LLM分析 | Kimi / DeepSeek |

## 快速开始

### 环境要求

- Java 21+
- Node.js 18+
- Python 3.9+
- Maven 3.8+

### 安装运行

```bash
# 1. 启动后端
mvn spring-boot:run

# 2. 启动前端
cd frontend
npm install
npm run dev

# 3. 启动AI服务
cd fastapi
pip install -r requirements.txt
python main.py
```

### 访问地址

- 前端：http://localhost:5173
- 后端API：http://localhost:8080
- FastAPI：http://localhost:8000

**默认账号**：admin / 123456

## 项目结构

```
├── src/                     # 后端代码 (Spring Boot)
│   ├── main/java/.../
│   │   ├── controller/      # 控制器
│   │   ├── service/         # 服务层
│   │   ├── entity/          # 实体类
│   │   ├── mapper/          # 数据访问层
│   │   └── config/          # 配置类
│   └── main/resources/
│       └── application.yml  # 配置文件
├── frontend/                # 前端代码 (Vue 3)
│   └── src/
│       ├── views/           # 页面组件
│       ├── api/             # API接口
│       ├── store/           # 状态管理
│       └── router/          # 路由配置
├── fastapi/                 # AI服务 (Python)
│   ├── services/
│   │   ├── yolo_service.py  # YOLO检测
│   │   └── kimi_service.py  # LLM分析
│   └── main.py              # 服务入口
└── pom.xml                  # Maven配置
```

## 缺陷类型说明

| ID | 缺陷类型 | 中文名称 |
|----|----------|----------|
| 0 | crack | 裂纹 |
| 1 | finger | 栅线 |
| 2 | black_core | 黑芯 |
| 3 | thick_line | 粗线 |
| 4 | star_crack | 星形裂纹 |
| 5 | corner | 边角缺陷 |
| 6 | fragment | 碎片 |
| 7 | scratch | 划痕 |
| 8 | horizontal_dislocation | 水平错位 |
| 9 | vertical_dislocation | 垂直错位 |
| 10 | printing_error | 印刷错误 |
| 11 | short_circuit | 短路 |

## 配置说明

### YOLO模型

将训练好的 `best.pt` 模型文件放置在 `fastapi/services/models/yolov11/` 目录下。

### LLM API密钥

设置环境变量启动Kimi分析功能：

```bash
# Windows
set MOONSHOT_API_KEY=your_api_key_here

# Linux/Mac
export MOONSHOT_API_KEY=your_api_key_here
```

## 许可证

MIT License

## 致谢

- [Ultralytics YOLO](https://github.com/ultralytics/ultralytics)
- [Element Plus](https://element-plus.org/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [FastAPI](https://fastapi.tiangolo.com/)
