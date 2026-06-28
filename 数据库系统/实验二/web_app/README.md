# 企业员工管理系统 - Web 应用

基于 Flask + Bootstrap + OpenGauss 的企业员工管理系统 Web 界面。

## 功能模块

- **仪表盘**：系统概览、统计图表
- **员工管理**：增删改查员工信息
- **部门管理**：管理部门信息
- **岗位管理**：管理岗位信息
- **薪资管理**：录入和查询薪资
- **考勤管理**：登记和查询考勤
- **搜索功能**：按姓名/邮箱/电话搜索员工

## 环境要求

- Python 3.8+
- OpenGauss 数据库
- 已安装依赖：`flask`, `psycopg2-binary`

## 配置说明

编辑 `app.py` 文件，修改数据库配置：

```python
DB_CONFIG = {
    'host': 'localhost',      # 数据库服务器地址
    'port': 26000,            # OpenGauss 端口
    'database': 'employee_db',
    'user': 'omm',
    'password': 'your_password'  # 数据库密码
}
```

### 如果数据库在远程服务器

**方法 1：直接连接**（需要数据库允许远程访问）
```python
DB_CONFIG = {
    'host': 'your-server-ip',  # 服务器 IP
    'port': 26000,
    'database': 'employee_db',
    'user': 'omm',
    'password': 'your_password'
}
```

**方法 2：SSH 隧道**（推荐）
```bash
# 在本地建立 SSH 隧道
ssh -L 26000:localhost:26000 user@your-server-ip

# 然后 app.py 配置保持 localhost 即可
```

## 启动应用

```bash
cd web_app
python app.py
```

访问：http://localhost:5000

## 项目结构

```
web_app/
├── app.py              # Flask 主程序
├── templates/          # HTML 模板
│   ├── base.html       # 基础模板
│   ├── index.html      # 仪表盘
│   ├── employees.html  # 员工管理
│   ├── departments.html # 部门管理
│   ├── positions.html  # 岗位管理
│   ├── salaries.html   # 薪资管理
│   ├── attendances.html # 考勤管理
│   └── search.html     # 搜索页面
└── static/             # 静态文件（CSS/JS）
```

## 截图演示

启动应用后，访问各页面进行截图，用于实验报告。

### 推荐截图顺序

1. **仪表盘** - http://localhost:5000/
2. **员工管理** - http://localhost:5000/employees
3. **部门管理** - http://localhost:5000/departments
4. **岗位管理** - http://localhost:5000/positions
5. **薪资管理** - http://localhost:5000/salaries
6. **考勤管理** - http://localhost:5000/attendances
7. **搜索功能** - http://localhost:5000/search
