# 光伏电池板缺陷检测系统

## 整体架构与技术栈要求
- **前端**：Vue 3 + Composition API + Vite + Element Plus（或类似 UI 库） + ECharts（用于可视化） + Pinia（状态管理）。
- **业务后端**：Spring Boot 3 + MyBatis-Plus + MySQL + Spring Security/JWT（鉴权）。
- **AI 推理端**：Python + FastAPI（提供 YOLO 目标检测和 DeepSeek LLM 分析的 REST API 接口，供 SpringBoot 调用，以实现解耦）。

## 数据库设计 (MySQL)
核心数据库表结构：
- **用户表 (sys_user)**：用户 ID、账号、密码（需加密）、姓名、头像、角色（管理员/普通用户）、创建时间。
- **检测记录表 (detection_record)**：记录 ID、用户 ID、检测模式（图片/视频/摄像头）、使用模型（YOLOv8/10/11/12）、原始图像路径、结果图像路径、检测时间。
- **缺陷详情表 (defect_detail)**：详情 ID、记录 ID、缺陷类型（black_core, crack, finger, horizontal_dislocation, short_circuit, thick_line）、置信度、边界框坐标 (x, y, w, h)。
- **AI 诊断报告表 (ai_analysis_report)**：报告 ID、记录 ID、DeepSeek 生成的成因分析与工艺改进建议内容。

## Python AI 推理微服务 (FastAPI)
- **模型路由模块**：接收来自 SpringBoot 的图像/视频流数据，以及指定的模型版本（v8/v10/v11/v12）。
- **YOLO 检测模块**：加载对应版本的权重文件，进行推理，返回包含缺陷类型、坐标、置信度及渲染后图片（Base64 或存储路径）的 JSON 结构。
- **DeepSeek 分析模块**：
  - 根据 YOLO 检测出的缺陷类型及数量，构建 Prompt。
  - Prompt 模板："当前工厂正在生产太阳能电池板，EL图像检测出以下缺陷：[缺陷类型]。请以光伏制造专家的身份，分析产线中哪些工艺步骤（如焊接、层压、丝网印刷等）可能存在问题，并给出具体的设备调试或工艺优化建议。"
  - 调用 DeepSeek API 并返回结构化建议。

## SpringBoot 业务后端开发
- **统一响应结构**：定义统一的 Result<T> 类，包含状态码、提示信息和数据负载。
- **认证与用户管理模块**：
  - 实现登录、注册（密码复杂度检测 + BCrypt 加密）。
  - 个人中心：修改信息、上传头像、修改密码。
  - 管理员功能：对用户进行增删改查。
- **检测业务模块**：
  - 处理前端传来的图片、视频或开启摄像头指令。
  - 选择检测模型，通过 RestTemplate 或 WebClient 调用 Python FastAPI 服务。
  - 解析 AI 服务返回的结果，存入 detection_record 和 defect_detail 表。
  - 如果用户请求 AI 分析，则调用 FastAPI 的 DeepSeek 接口，并保存报告。
- **记录与可视化数据模块**：
  - 提供分页查询接口：图片检测记录、视频检测记录、摄像头识别记录。
  - 提供统计数据接口：各类型缺陷数量占比、每日检测数量趋势、各模型使用频率等（供 ECharts 使用）。

## Vue 3 前端开发
- **基础配置**：配置 Vue Router、Axios 拦截器（统一携带 Token、处理 401 和 500 异常）。
- **页面划分**：
  - 登录/注册页：表单验证，响应式设计。
  - 数据大盘 (Dashboard)：使用 ECharts 实现检测数据的可视化呈现。
  - 检测工作台：
    - 模型选择下拉框（YOLOv8 ~ YOLOv12）。
    - Tab 切换：图片上传检测、视频上传检测、开启本地摄像头实时检测。
    - 图片检测完成后，展示检测对比图，并提供“一键获取 DeepSeek 工艺建议”的按钮，在右侧面板通过 Markdown 渲染展示建议。
  - 历史记录管理：提供基于类型（图/文/视）、时间的过滤和表格分页展示。
  - 用户管理中心：针对管理员的表格增删改查；针对普通用户的个人信息修改页面。

## 执行步骤
1. 输出 MySQL 数据库 DDL 语句及初始测试数据。
2. 搭建后端 SpringBoot 基础工程结构（Entity、Mapper 接口及通用 Result 类）。
3. 实现 SpringBoot 认证与用户管理相关接口。
4. 编写 Python FastAPI 服务的骨架及 YOLO/DeepSeek 接口实现逻辑。
5. 完成 SpringBoot 的核心检测业务逻辑（对接 FastAPI 和 MySQL）。
6. 搭建 Vue 3 基础框架及路由配置。
7. 开发前端核心页面（Dashboard 与 检测工作台）。
8. 开发前端其余页面（记录管理、用户中心）。