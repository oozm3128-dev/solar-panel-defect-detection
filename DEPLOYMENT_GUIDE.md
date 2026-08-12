# 光伏电池板缺陷检测系统 - 部署指南

## 部署环境

### 硬件要求

| 配置 | 最低要求 | 推荐配置 |
|------|----------|----------|
| CPU | 4 核 | 8 核或更高 |
| 内存 | 8GB | 16GB 或更高 |
| 存储 | 200GB | 500GB 或更高 |
| GPU | 可选 | NVIDIA GPU（用于 YOLO 模型加速） |

### 软件要求

| 软件 | 版本 | 用途 |
|------|------|------|
| Java | OpenJDK 21 | 运行后端服务 |
| Node.js | v18.17.0+ | 运行前端服务 |
| Python | 3.9+ | 运行 AI 服务 |
| Maven | 3.8.0+ | 构建后端项目 |
| Git | 2.0+ | 版本控制 |

## 部署步骤

### 1. 环境准备

#### 安装 Java

**Windows**：
1. 下载 OpenJDK 21 安装包
2. 运行安装程序，按照提示完成安装
3. 配置环境变量：
   - 新建 `JAVA_HOME` 环境变量，指向 JDK 安装目录
   - 在 `PATH` 环境变量中添加 `%JAVA_HOME%\bin`

**Linux**：
```bash
sudo apt update
sudo apt install openjdk-21-jdk
```

#### 安装 Node.js

**Windows**：
1. 下载 Node.js v18.17.0+ 安装包
2. 运行安装程序，按照提示完成安装

**Linux**：
```bash
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs
```

#### 安装 Python

**Windows**：
1. 下载 Python 3.9+ 安装包
2. 运行安装程序，勾选 "Add Python to PATH"
3. 按照提示完成安装

**Linux**：
```bash
sudo apt update
sudo apt install python3 python3-pip
```

#### 安装 Maven

**Windows**：
1. 下载 Maven 3.8.0+ 压缩包
2. 解压到指定目录
3. 配置环境变量：
   - 新建 `MAVEN_HOME` 环境变量，指向 Maven 安装目录
   - 在 `PATH` 环境变量中添加 `%MAVEN_HOME%\bin`

**Linux**：
```bash
sudo apt update
sudo apt install maven
```

### 2. 项目获取

**克隆项目**：
```bash
git clone <项目地址>
cd solar-panel-detection
```

**或直接下载**：
1. 下载项目压缩包
2. 解压到指定目录
3. 进入项目目录

### 3. 配置修改

#### 后端配置

**修改 `application.yml`**：
```yaml
# src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:h2:file:./data/testdb;MODE=MySQL;CASE_INSENSITIVE_IDENTIFIERS=TRUE
    username: sa
    password:
    driver-class-name: org.h2.Driver

# JWT 配置
jwt:
  secret: solar_panel_detection_secret_key_2024
  expiration: 604800 # 7天

# FastAPI 服务配置
fastapi:
  url: http://localhost:8000
```

**注意**：
- 修改 `fastapi.url` 为实际的 FastAPI 服务地址
- 生产环境中应修改 `jwt.secret` 为更安全的密钥

#### 前端配置

**修改 API 基础地址**：
```javascript
// frontend/src/api/index.js
const axios = axios.create({
  baseURL: 'http://localhost:8080', // 修改为实际的后端地址
  timeout: 60000
})
```

#### AI 服务配置

**修改模型路径**：
```python
# fastapi/services/yolo_service.py
class YoloService:
    def __init__(self):
        self.model_path = r"/path/to/best.pt"  # 修改为实际的模型文件路径
        if not os.path.exists(self.model_path):
            raise FileNotFoundError(f"Model file not found: {self.model_path}")
```

**修改 Kimi API 配置**：
```python
# fastapi/services/kimi_service.py
class KimiService:
    def __init__(self):
        self.api_key = "your-api-key"  # 修改为实际的 Kimi API 密钥
```

### 4. 依赖安装

#### 前端依赖

```bash
cd frontend
npm install
```

#### AI 服务依赖

```bash
cd fastapi
pip install -r requirements.txt
```

### 5. 构建与部署

#### 前端构建

```bash
cd frontend
npm run build
```

构建完成后，`dist` 目录中的文件可以部署到任何静态文件服务器。

#### 后端构建

```bash
cd ..
mvn clean package
```

构建完成后，`target` 目录中会生成 `detection-1.0-SNAPSHOT.jar` 文件。

#### 启动服务

**使用一键启动脚本**：
```bash
# Windows
start_all.bat

# Linux
chmod +x start_all.sh
./start_all.sh
```

**手动启动**：

1. **启动后端服务**：
   ```bash
   java -jar target/detection-1.0-SNAPSHOT.jar
   ```

2. **启动前端服务**（开发模式）：
   ```bash
   cd frontend
   npm run dev
   ```

3. **启动 AI 服务**：
   ```bash
   cd fastapi
   python main.py
   ```

### 6. 生产环境部署

#### 前端部署

**使用 Nginx**：

1. 安装 Nginx
2. 配置 Nginx 虚拟主机：
   ```nginx
   server {
       listen 80;
       server_name detection.example.com;
       
       location / {
           root /path/to/frontend/dist;
           index index.html;
           try_files $uri $uri/ /index.html;
       }
       
       location /api {
           proxy_pass http://localhost:8080;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
       }
   }
   ```

3. 重启 Nginx：
   ```bash
   sudo systemctl restart nginx
   ```

#### 后端部署

**使用 Systemd**：

1. 创建服务文件：
   ```bash
   sudo nano /etc/systemd/system/solar-detection.service
   ```

2. 添加内容：
   ```ini
   [Unit]
   Description=Solar Panel Detection Backend
   After=network.target
   
   [Service]
   User=ubuntu
   WorkingDirectory=/path/to/solar-panel-detection
   ExecStart=/usr/bin/java -jar target/detection-1.0-SNAPSHOT.jar
   Restart=always
   RestartSec=10
   
   [Install]
   WantedBy=multi-user.target
   ```

3. 启动服务：
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl start solar-detection
   sudo systemctl enable solar-detection
   ```

#### AI 服务部署

**使用 Gunicorn**：

1. 安装 Gunicorn：
   ```bash
   pip install gunicorn uvicorn
   ```

2. 启动服务：
   ```bash
   cd fastapi
   gunicorn -w 4 -k uvicorn.workers.UvicornWorker main:app --bind 0.0.0.0:8000
   ```

3. 使用 Systemd 管理：
   ```ini
   [Unit]
   Description=Solar Panel Detection AI Service
   After=network.target
   
   [Service]
   User=ubuntu
   WorkingDirectory=/path/to/solar-panel-detection/fastapi
   ExecStart=/usr/local/bin/gunicorn -w 4 -k uvicorn.workers.UvicornWorker main:app --bind 0.0.0.0:8000
   Restart=always
   RestartSec=10
   
   [Install]
   WantedBy=multi-user.target
   ```

## 数据库配置

### H2 数据库

**默认配置**：
- 数据库文件：`./data/testdb.mv.db`
- 用户名：sa
- 密码：（空）
- 控制台地址：http://localhost:8080/h2-console

**注意**：
- H2 数据库适合开发和测试环境
- 生产环境建议使用 MySQL 或 PostgreSQL

### 数据库迁移

**初始化表结构**：
系统启动时会自动创建表结构，无需手动执行 SQL 脚本。

**数据备份**：
1. 备份 H2 数据库文件：`./data/testdb.mv.db`
2. 或使用 H2 控制台导出数据

## 环境变量配置

### 后端环境变量

| 变量名 | 描述 | 默认值 |
|--------|------|--------|
| `SPRING_DATASOURCE_URL` | 数据库连接 URL | `jdbc:h2:file:./data/testdb;MODE=MySQL` |
| `SPRING_DATASOURCE_USERNAME` | 数据库用户名 | `sa` |
| `SPRING_DATASOURCE_PASSWORD` | 数据库密码 | 空 |
| `JWT_SECRET` | JWT 密钥 | `solar_panel_detection_secret_key_2024` |
| `JWT_EXPIRATION` | JWT 过期时间（秒） | `604800` |
| `FASTAPI_URL` | FastAPI 服务地址 | `http://localhost:8000` |

### 前端环境变量

**创建 `.env` 文件**：
```env
VITE_API_BASE_URL=http://localhost:8080
```

### AI 服务环境变量

| 变量名 | 描述 | 默认值 |
|--------|------|--------|
| `YOLO_MODEL_PATH` | YOLO 模型路径 | `./services/models/yolov11/best.pt` |
| `KIMI_API_KEY` | Kimi API 密钥 | 无 |

## 安全配置

### 生产环境安全建议

1. **修改默认密码**：
   - 更改默认管理员密码
   - 使用强密码策略

2. **JWT 配置**：
   - 使用复杂的 JWT 密钥
   - 设置合理的过期时间

3. **网络安全**：
   - 配置防火墙，只开放必要的端口
   - 使用 HTTPS
   - 限制 API 访问

4. **数据库安全**：
   - 使用非默认端口
   - 配置访问控制
   - 定期备份数据

5. **文件上传安全**：
   - 限制上传文件大小
   - 验证文件类型
   - 存储上传文件到安全位置

## 监控与维护

### 日志管理

**后端日志**：
- 默认日志路径：`./logs/`
- 日志级别：INFO

**前端日志**：
- 浏览器控制台
- 可集成第三方日志服务

**AI 服务日志**：
- 标准输出
- 可配置日志文件

### 健康检查

**后端健康检查**：
```bash
curl http://localhost:8080/actuator/health
```

**前端健康检查**：
```bash
curl http://localhost:5173/
```

**AI 服务健康检查**：
```bash
curl http://localhost:8000/api/detection/analyze -X POST -H "Content-Type: application/x-www-form-urlencoded" -d "defects=[]"
```

### 常见问题排查

| 问题 | 症状 | 排查步骤 |
|------|------|----------|
| 服务启动失败 | 端口被占用 | 检查端口使用情况：`netstat -tulpn` |
| 检测失败 | YOLO 模型加载失败 | 检查模型文件路径和权限 |
| 分析失败 | Kimi API 调用失败 | 检查 API 密钥和网络连接 |
| 数据库连接失败 | 数据库文件损坏 | 重建数据库：删除 `./data/testdb.mv.db` |
| 前端页面空白 | 依赖缺失 | 重新安装前端依赖：`npm install` |
| API 调用失败 | 跨域配置错误 | 检查后端 CORS 配置 |

### 性能优化

1. **前端优化**：
   - 启用 gzip 压缩
   - 代码分割和懒加载
   - 图片优化

2. **后端优化**：
   - 启用缓存
   - 数据库索引优化
   - 异步处理

3. **AI 服务优化**：
   - 使用 GPU 加速
   - 模型量化
   - 批处理

## 升级与回滚

### 升级步骤

1. **备份数据**：
   - 备份数据库文件
   - 备份配置文件

2. **更新代码**：
   - `git pull` 或上传新代码

3. **重新构建**：
   - 前端：`npm run build`
   - 后端：`mvn clean package`

4. **重启服务**：
   - 停止所有服务
   - 启动后端服务
   - 启动前端服务
   - 启动 AI 服务

### 回滚步骤

1. **停止服务**：
   - 停止所有服务

2. **恢复备份**：
   - 恢复数据库文件
   - 恢复配置文件
   - 恢复代码

3. **重启服务**：
   - 启动后端服务
   - 启动前端服务
   - 启动 AI 服务

## 扩展与集成

### 与其他系统集成

**API 集成**：
- 后端提供 RESTful API
- 支持 JSON 格式数据交换
- 支持 JWT 认证

**第三方服务集成**：
- Kimi LLM API
- 其他 AI 模型服务
- 监控系统

### 自定义开发

**添加新功能**：
1. 遵循现有代码结构
2. 按照开发规范编写代码
3. 添加必要的测试
4. 更新文档

**修改现有功能**：
1. 理解现有代码逻辑
2. 进行最小化修改
3. 测试修改后的功能
4. 更新相关文档

## 故障处理

### 紧急故障处理

1. **服务不可用**：
   - 检查服务状态：`systemctl status <service>`
   - 查看日志：`journalctl -u <service>`
   - 重启服务：`systemctl restart <service>`

2. **数据库故障**：
   - 检查数据库文件：`ls -la ./data/`
   - 重建数据库：删除 `testdb.mv.db` 文件
   - 恢复备份：从备份恢复数据库文件

3. **网络故障**：
   - 检查网络连接：`ping <host>`
   - 检查防火墙：`ufw status`
   - 检查端口：`netstat -tulpn`

### 故障预防

1. **定期备份**：
   - 数据库备份
   - 配置文件备份
   - 代码备份

2. **监控**：
   - 服务状态监控
   - 性能监控
   - 错误日志监控

3. **测试**：
   - 定期进行系统测试
   - 测试边界情况
   - 压力测试

## 联系方式

如有部署问题，请联系系统管理员。