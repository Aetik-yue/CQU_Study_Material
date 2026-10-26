# RBAC 权限管理系统（rbac-cli）

基于**命令行**的 RBAC（Role-Based Access Control）权限管理系统，纯 JDBC + 手写分层架构实现，
完整覆盖"**用户 — 角色 — 权限 — 资源**"四层映射，含**菜单级 + 操作级双重鉴权**、
**AOP 式审计**、**角色继承**、**权限树**、**动态菜单**、**登录锁定**、**权限缓存**等能力。

> 《Java企业级应用》实验二 提交物（源代码部分）。
> 技术栈：JDK 17+ / Maven 3.8+ / MySQL 8.x / HikariCP / SLF4J + Logback，**不使用 Spring**。

## 0. 界面预览

登录页（RBAC 字符画 LOGO · 真彩渐变 · 圆角面板）：

```text
  ╭──────────────────────────────────────────────────────────────────────────╮
  │                                                                          │
  │                     ██████╗ ██████╗  █████╗  ██████╗                     │
  │                     ██╔══██╗██╔══██╗██╔══██╗██╔════╝                     │
  │                     ██████╔╝██████╔╝███████║██║                          │
  │                     ██╔══██╗██╔══██╗██╔══██║██║                          │
  │                     ██║  ██║██████╔╝██║  ██║╚██████╗                     │
  │                     ╚═╝  ╚═╝╚═════╝ ╚═╝  ╚═╝ ╚═════╝                     │
  │                                                                          │
  │                            权 限 管 理 系 统                             │
  │             R B A C   ·   P E R M I S S I O N   S Y S T E M              │
  │                                                                          │
  ╰──────────────────────────────────────────────────────────────────────────╯

  默认账号 admin / admin123                        MySQL 8 · HikariCP · PBKDF2
  · 输入 q 可退出程序

  » 用户名
  » 密　码
```

主菜单（按权限动态生成，分组 + 双列 + 状态栏）：

```text
  ╭─ ◆ RBAC 权限管理系统 ─────────────────────────────────────────── 主菜单 ─╮
  │  ● admin（超级管理员）                                v1.0.0 · 19:45:04  │
  ├─ 业 务 功 能 ────────────────────────────────────────────────────────────┤
  │  1  用户管理            2  角色管理                                      │
  │  3  权限管理            4  资源管理                                      │
  │  5  权限校验演示        6  审计日志                                      │
  ├─ 账 户 ──────────────────────────────────────────────────────────────────┤
  │  7  修改本人密码        8  我的身份与权限                                │
  │  0  退出登录并关闭程序                                                   │
  ╰──────────────────────────────────────────────────────────────────────────╯
  · 输入编号执行操作                                              · q 退出程序

  » 请输入选项编号：
```

数据列表（带边框表格 · 中文按 2 列宽对齐 · 状态语义色）：

```text
  ┌────┬────────────┬────────────┬──────┬──────┬──────┬────────────┬─────────────────────┐
  │ ID │ 用户名     │ 姓名       │ 邮箱 │ 电话 │ 状态 │ 角色       │ 创建时间            │
  ├────┼────────────┼────────────┼──────┼──────┼──────┼────────────┼─────────────────────┤
  │ 1  │ admin      │ 超级管理员 │ -    │ -    │ 启用 │ 超级管理员 │ 2026-10-20 19:35:04 │
  │ 2  │ manager1   │ 王经理     │ -    │ -    │ 启用 │ 用户管理员 │ 2026-10-20 19:35:04 │
  └────┴────────────┴────────────┴──────┴──────┴──────┴────────────┴─────────────────────┘
  · 共 5 条用户，第 1/1 页
```

> 视觉语言参考了开源 TUI 项目（Charm lipgloss 的圆角边框/真彩/对齐、figlet ANSI Shadow 字符画），
> 使用自封装 ANSI 实现（`Theme` / `Banner` / `ConsolePrinter`），未引入第三方 UI 库。
> 真彩不可用时自动降级为 256 色；`ui.color=false` 可完全关闭着色（纯文本仍保持对齐）。

---

## 1. 功能总览

| 模块 | 功能 |
|---|---|
| 认证 | 登录（失败计数、连续 5 次锁定 10 分钟）、注销、修改本人密码 |
| 用户管理 | 分页/关键字/状态查询、新增、修改、删除（禁止删自己/超管）、分配角色、启用禁用、重置密码 |
| 角色管理 | 查询、新增、修改、删除（内置角色/被绑定/有子角色保护）、分配权限、查看角色权限、角色继承（parent_id） |
| 权限管理 | 权限树、按类型查询、新增、修改、删除（子权限/被引用保护） |
| 资源管理 | 资源增删改查、资源访问校验（RESOURCE 权限） |
| 权限校验 | 动态菜单、方法级注解鉴权（AND/OR）、资源级鉴权、越权拦截演示、我的权限清单 |
| 权限审计 | 登录/登出/增删改/分配/鉴权失败自动记录；多条件分页查询；登录日志 / 操作日志 |
| 特色 | 首次启动自动建库建表初始化、连接池、编程式事务、PBKDF2 加盐哈希、权限缓存与失效、真彩 TUI（圆角面板/渐变 LOGO/双列菜单/带框表格/语义色徽标） |

### 1.1 交互约定（对象工作台模式）

- **列表可选对象**：用户/角色列表首列带"序号"，输入序号直接进入该对象的**工作台**，可在同一对象上连续执行
  "修改资料 / 分配角色 / 启停用 / 重置密码 / 删除"，无需重复输入数据库 ID。
  列表还支持 `n/p` 翻页、`g3` 跳页、`#5` 按 ID 选中。
- **快捷键统一**：操作类输入中 `b` 取消当前操作、`q` 退出程序、回车使用默认值；搜索类输入中 `b/q` 按普通文本处理。
- **表单错误只重填一项**：邮箱/电话/用户名格式错误或唯一性冲突时，只重新询问出错字段，其余已填内容保留。
- **授权用表达式**：给角色/用户分配权限或角色时支持 `7,8,10`、`7-13`、`user:list`、`user:*`、`@menu/@op/@res`、`+5/-10`、`0`、`all`，应用前显示"新增/移除/最终"变更预览。
- **审计查询快捷化**：模块可用编号或名称（模糊匹配）、结果 `s/f`、时间预设 `1/2/3`、"失败日志"一屏直出、查询条件可一键重复。
- **批量操作**：用户列表支持按序号多选（`1,3-5`/`all`）后批量启用/禁用/分配角色/删除，逐条审计并汇总结果。
- 旧菜单入口（如"修改用户（按 ID）"）保留，熟练用户/脚本仍可直接按 ID 操作。

## 2. 快速开始

### 2.1 环境要求

- JDK 17 及以上（编译目标为 17；JDK 21 亦可）
- Maven 3.8+
- MySQL 8.x（本机 3306 端口）

### 2.2 准备 MySQL（二选一）

**方式 A：便携版 MySQL（推荐，免安装）**

本项目配套便携版 MySQL 位于 `..\tools\mysql-8.4.11-winx64`（工作区 tools 目录），
首次使用已通过 `mysqld --initialize-insecure` 初始化，root 密码已设为 `root`。

```bat
:: 启动 / 停止数据库（双击或命令行运行）
E:\严浩睿的珍藏学习资料\java企业级应用\tools\mysql-start.bat
E:\严浩睿的珍藏学习资料\java企业级应用\tools\mysql-stop.bat
```

**方式 B：本机已装 MySQL 8**

修改 `src/main/resources/db.properties` 中的 `jdbc.username` / `jdbc.password` 即可；
**数据库 `rbac_db` 无需手工创建**——程序首次启动会自动创建数据库、建表并初始化数据。

### 2.3 构建与运行

```bat
:: 1) 打包（生成可执行 fat-jar：target\rbac-cli.jar）
build.bat

:: 2) 运行（自动检测/启动配套 MySQL，设置 UTF-8 控制台后启动程序）
run.bat
```

也可以在 IDEA 中直接运行 `com.cqu.rbac.Main`，或使用：

```bat
mvn -q -DskipTests package
java -Dstdin.encoding=UTF-8 -Dstdout.encoding=UTF-8 -jar target\rbac-cli.jar
```

### 2.4 默认账号

| 账号 | 密码 | 角色 | 说明 |
|---|---|---|---|
| `admin` | `admin123` | 超级管理员 | 全部权限 |
| `manager1` | `123456` | 用户管理员 | 仅用户管理 + 权限校验演示入口 |
| `auditor1` | `123456` | 审计员 | 审计日志 + 各模块只读 |
| `auditlead1` | `123456` | 审计主管 | **自身无权限，继承父角色 AUDITOR 全部权限（角色继承演示）** |
| `user1` | `123456` | 普通用户 | 仅登录与修改本人密码 |

> 演示数据可通过 `db.properties` 中 `db.initDemoData=false` 关闭（仅保留 admin）。

## 3. 目录结构

```
rbac-cli/
├── build.bat / run.bat          # 构建 / 运行脚本
├── pom.xml                      # Maven 配置（shade 插件打 fat-jar）
├── sql/
│   ├── schema.sql               # 建表脚本（幂等）
│   └── data.sql                 # 手工初始化参考（程序默认自动初始化）
├── logs/                        # 运行日志（滚动：logs/rbac.log）
├── test/                        # 命令行端到端测试脚本（stdin 驱动）+ 事务自测
└── src/main/
    ├── resources/               # db.properties / logback.xml / schema.sql
    └── java/com/cqu/rbac/
        ├── Main.java            # 入口：初始化 → 登录 → 主菜单
        ├── config/              # 配置管理、HikariCP 数据源（单例）
        ├── common/              # 常量/枚举/异常/返回体/自定义注解/校验工具
        ├── domain/              # 实体(entity)/DTO/VO
        ├── jdbc/                # JdbcTemplate、TransactionTemplate、函数式接口
        ├── dao/                 # BaseDao 泛型接口 + AbstractJdbcDao 模板方法 + 5 个 DAO 实现
        ├── security/            # PBKDF2 密码编码器、权限缓存、权限判定器
        ├── event/               # 事件总线（观察者）+ 审计事件与监听器
        ├── proxy/               # JDK 动态代理：鉴权 + 审计统一拦截
        ├── service/             # 6 个服务接口与实现（注解声明权限与审计）
        ├── support/             # 鉴权外观、数据初始化、服务工厂（组合根）
        └── cli/                 # 控制台输入输出、会话、菜单引擎、各功能屏
```

## 4. 架构与调用链

```
cli(表示层)  →  proxy(鉴权+审计切面)  →  service(业务/事务)  →  dao(单表CRUD)  →  jdbc  →  MySQL
                                        ↑ security/event/support（缓存、事件、外观、初始化）
```

一次"受保护操作"的完整链路：

1. 菜单屏调用 `userService.delete(id)`（拿到的是**代理对象**）；
2. 代理读取实现类方法上的 `@RequiresPermission("user:delete")` 并强制校验（无权限抛 `AuthException` 并记"鉴权失败"审计）；
3. 通过后执行实现：输入校验 → 业务规则 → `TransactionTemplate` 事务 → 缓存失效；
4. 代理按 `@AuditLog(module, operation)` 采集操作人/对象/结果/耗时，投递 `AuditEvent` 到事件总线；
5. 审计监听器落库 `sys_audit_log`；CLI 显示成功/失败结果（彩色）。

## 5. 设计模式落地点（对应任务书 9 类）

| 模式 | 落地点 |
|---|---|
| 单例 Singleton | `ConfigManager`、`DataSourceManager`、`EventBus`、`Pbkdf2PasswordEncoder`、`ServiceLocator` |
| 模板方法 Template Method | `AbstractJdbcDao`（固定 CRUD 骨架，子类提供表名/SQL/映射等原语） |
| 工厂 Factory | `ServiceLocator`（服务工厂/组合根）、`ServiceProxyFactory` |
| 代理 Proxy（JDK 动态代理） | `ServiceProxyFactory` + `AuditAndAuthInvocationHandler` |
| 策略 Strategy | `PasswordEncoder` 实现可换、`PermissionChecker` 的 AND/OR 判定策略 |
| 命令 Command | `MenuItem`（key + title + 动作 Runnable），`MenuEngine` 调度 |
| 观察者 Observer | `EventBus` + `AuditEventListener` + `AuditEvent` |
| 外观 Facade | `AuthorizationFacade`（权限聚合/判定/菜单树/资源校验统一入口） |
| 建造者 Builder | `User.Builder`、`AuditLog.Builder`（静态内部类链式构建） |

## 6. Java 语法元素落地点（对应任务书 10 节）

- **抽象类**：`BaseEntity`（实体基类）、`AbstractJdbcDao`、`AbstractScreen`（界面基类）
- **接口**：`BaseDao<T,ID>`、`RowMapper<T>`、`PasswordEncoder`、`EventListener`、6 个 Service 接口
- **注解**：自定义 `@RequiresPermission`、`@AuditLog`（RUNTIME 保留，反射读取）
- **Lambda / 函数式接口**：`RowMapper`、`TransactionCallback`、`MenuItem` 动作、`EventListener`、分页 Loader
- **泛型**：`BaseDao<T,ID>`、`AbstractJdbcDao<T,ID>`、`Result<T>`、`PageResult<T>`、`JdbcTemplate` 查询
- **内部类**：`User.Builder` / `AuditLog.Builder`（静态内部类）、`TransactionTemplate.TransactionCallback`
- **集合框架**：`LinkedHashMap`（有序菜单/映射）、`HashSet/LinkedHashSet`（继承链去重）、`TreeSet`（权限码排序）、`ConcurrentHashMap`（权限缓存）、`CopyOnWriteArrayList`（监听器）
- **继承/多态**：实体继承 `BaseEntity`、DAO 继承抽象类、`AuthException extends BusinessException`、面向接口编程

## 7. 数据库设计

7 张表：`sys_user`、`sys_role`、`sys_permission`、`sys_resource`、`sys_user_role`、`sys_role_permission`、`sys_audit_log`。
详见 `sql/schema.sql`（幂等）与 `sql/data.sql`。核心关系：

- 用户 ↔ 角色：多对多；角色 ↔ 权限：多对多；权限 → 资源：多对一（可空）
- 权限 `parent_id` 构成权限/菜单树；角色 `parent_id` 构成**继承链**（用户权限 = 直接角色 ∪ 全部祖先角色 的启用权限）

## 8. 配置说明（src/main/resources/db.properties）

| 键 | 默认值 | 说明 |
|---|---|---|
| `jdbc.url` | `jdbc:mysql://localhost:3306/rbac_db?...` | 库不存在时程序自动创建 |
| `jdbc.username` / `jdbc.password` | `root` / `root` | 数据库账号 |
| `db.autoInit` | `true` | 启动时自动建表 + 初始化数据（幂等） |
| `db.initDemoData` | `true` | 是否初始化演示账号（manager1 等） |
| `ui.color` / `ui.clearScreen` | `true` | CLI 彩色 / 清屏（仅交互式终端生效） |
| `ui.trueColor` | `true` | 24 位真彩；终端不支持时改为 `false`（自动降级 256 色） |

> 系统属性（`-Dkey=value`）优先于配置文件，便于临时覆盖连接参数。

## 9. 日志与审计

- **文件日志**：`logs/rbac.log`（按天 + 10MB 滚动，保留 30 天），记录 INFO/WARN/ERROR 全量日志与异常堆栈
- **控制台日志**：仅输出 ERROR 级（避免日志行打断 TUI 界面；界面本身用语义色提示成功/失败）
- **审计表**：`sys_audit_log` 记录"谁在何时对什么做了什么、结果如何、耗时、失败原因"
- 审计字段自动**脱敏**（参数名含 password/pwd/salt 的记录为 `***`）

## 10. 命令行端到端测试（test/）

`test/` 下提供 stdin 驱动的回归脚本（配合 `test/scan.py` 过滤关键结果行），例如：

```bat
:: 以管理员登录，完成 建用户→分配角色→禁用→重置密码→删除 全流程
java -Dstdin.encoding=UTF-8 -Dstdout.encoding=UTF-8 -jar target\rbac-cli.jar < test\a_admin_user_mgmt.txt
```

> 注意：脚本化运行请把重定向直接写在 `java` 命令行上（如上）。
> 不要使用 `run.bat < 脚本文件` 的形式——批处理级的 stdin 重定向与脚本内的
> `chcp` 组合会导致子进程读不到输入（Windows cmd 的已知行为）。交互式使用不受影响。

已覆盖场景（脚本名 → 验证点）：

| 脚本 | 验证点 |
|---|---|
| `smoke.txt` | 首次启动自动建库建表初始化、登录、退出 |
| `a_admin_user_mgmt.txt` | 用户 CRUD、分配角色、启停用、重置密码 |
| `b1/b1b_admin_role`、`b2_admin_permission` | 角色 CRUD 与授权、内置角色/被绑定删除保护、权限删除保护 |
| `b3/c/d_*` | 资源校验、审计查询、动态菜单差异、角色继承、越权拦截 |
| `e1b~e3_*` | 修改本人密码（旧密码校验）并以新密码登录 |
| `f1b/f2_*` | 连续 5 次失败锁定 10 分钟、锁定期登录被拒 |
| `g/g2_*` | 分配角色后权限缓存即时失效（拒绝→允许→拒绝） |
| `v_*`、`w_*` | 模糊查询/状态过滤、禁止删除/禁用自己、审计多条件分页 |
| `y1~y4_*` | 对象工作台（选序号连续操作）、字段级重填、取消语义（b/q）、角色工作台 |
| `z1~z4_*` | 审计快捷查询/重复上次条件、授权表达式与变更预览、批量操作、变更摘要 |
| `tx_selftest.jsh` | 事务模板提交/回滚语义（jshell 脚本） |

## 11. 常见问题

| 问题 | 处理 |
|---|---|
| 启动报"连接数据库失败" | 确认 MySQL 已启动（可运行 `tools\mysql-start.bat`），账号密码与 db.properties 一致 |
| 控制台中文乱码 | 使用 `run.bat` 启动（内部已 `chcp 65001` 并指定 UTF-8 输入输出编码） |
| 界面出现乱码方框/颜色异常 | 终端不支持真彩或字体缺字形：设置 `ui.trueColor=false`；仍异常时设置 `ui.color=false` |
| 控制台看不到 INFO 日志 | 正常：控制台只显示 ERROR，完整日志见 `logs/rbac.log` |
| 权限被锁 10 分钟 | 用管理员登录后"用户管理 → 重置密码"（重置同时解锁） |
| 数据库想从零重来 | `DROP DATABASE rbac_db;` 后重启程序，自动重建并初始化 |
| 想关闭彩色输出 | `db.properties` 中 `ui.color=false` |
