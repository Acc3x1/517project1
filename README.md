# ISO 11820 建筑材料不燃性试验仿真系统

## 项目说明

本项目是根据 ISO 11820 建筑材料不燃性试验标准开发的仿真系统，使用 Java + JavaFX 实现。

## 技术栈

- Java 17+
- JavaFX 17+ (GUI框架)
- Gradle 8.x (构建工具)
- SQLite (数据库)
- Apache POI (Excel导出)
- Apache PDFBox (PDF导出)
- Jackson (JSON配置)
- SLF4J + Logback (日志)

## 项目结构

```
C:\Users\lenovo\ISO11820\
├── build.gradle              # Gradle构建配置
├── settings.gradle           # Gradle项目设置
├── src\main\
│   ├── java\com\iso11820\
│   │   ├── App.java          # 主入口
│   │   ├── GlobalContext.java # 全局单例
│   │   ├── controller\       # 控制器(状态机)
│   │   ├── service\          # 服务层(仿真引擎、数据采集)
│   │   ├── db\               # 数据库层
│   │   ├── model\            # 数据模型
│   │   ├── ui\               # UI控制器
│   │   ├── config\           # 配置加载
│   │   └ util\               # 工具类
│   ├── resources\
│   │   ├── fxml\             # FXML界面文件
│   │   ├── appsettings.json  # 配置文件
│   │   ├── styles.css        # CSS样式
│   │   └ logback.xml         # 日志配置
├── Data\                     # SQLite数据库目录
├── TestData\                 # 试验数据CSV存储
├── Reports\                  # 报告输出目录
├── logs\                     # 日志文件目录
```

## 如何运行

### 前提条件

1. 安装 JDK 17 或更高版本
2. 安装 Gradle 8.x 或使用 Gradle Wrapper

### 运行步骤

```bash
# 进入项目目录
cd C:\Users\lenovo\ISO11820

# 编译项目
gradle build

# 运行项目
gradle run
```

### 登录账号

- **管理员**: admin / 123456
- **试验员**: experimenter / 123456

## 功能说明

### 1. 登录系统
- 选择角色（管理员/试验员）
- 输入密码登录

### 2. 试验控制
- 新建试验：填写样品信息、环境参数
- 开始升温：仿真引擎开始升温
- 开始记录：温度达到稳定后开始记录
- 试验记录：保存试验结果

### 3. 温度仿真
- 5个温度通道：炉温1、炉温2、表面温、中心温、校准温
- 实时曲线图显示
- LED风格温度数值显示

### 4. 数据导出
- CSV文件：每秒温度数据
- Excel报告：试验信息+温度数据+判定结论
- PDF报告：试验概要+曲线图

### 5. 记录查询
- 按日期范围查询
- 按样品编号查询
- 查看试验详情

### 6. 设备校准
- 记录校准温度点
- 保存校准记录
- 查看校准历史

## 状态机

系统有5个状态：
- **Idle（空闲）**: 系统空闲，等待开始
- **Preparing（升温中）**: 系统升温中
- **Ready（就绪）**: 温度已稳定，可以开始记录
- **Recording（记录中）**: 正在记录温度数据
- **Complete（完成）**: 试验完成，等待保存

## 判定标准

根据 ISO 11820 标准：
- 样品温升 ≤ 50°C → 通过
- 失重率 ≤ 50% → 通过
- 火焰持续时间 < 5秒 → 通过

全部通过则判定为"合格"。

## 配置文件

`appsettings.json` 可配置：
- 数据库路径
- 目标温度 (750°C)
- 升温速度 (40°C/s)
- 温度波动范围 (0.5°C)
- 试验时长 (3600秒)

## 作者

AI 生成项目

## 日期

2026-06-14