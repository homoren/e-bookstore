# My-eBookStore 图书商城

一个基于 Spring Boot + Vue 3 的前后端分离在线图书商城,覆盖会员、图书、购物车、订单、进货、日结等完整业务闭环,并具备完整的工程化配套(测试、CI、容器化部署、接口文档、类型同步)。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Java 17 · Spring Boot 3.2 · MyBatis-Plus · MySQL 8 · Redis |
| 安全 | Spring Security + JWT(无状态) · BCrypt 密码加密 · 登录防爆破 |
| 中间件 | Redis + Spring Cache(热点缓存) · Flyway(数据库迁移) |
| 后端配套 | MapStruct(DTO 映射) · SpringDoc OpenAPI 3 · 全局异常处理 · 统一响应体 |
| 前端 | Vue 3.5 · TypeScript · Vite 8 · Element Plus · Pinia · Vue Router |
| 前端配套 | openapi-typescript(前后端类型同步) · Vitest + MSW(单测) |
| 工程化 | GitHub Actions CI · Docker Compose 部署 · 多阶段镜像构建 |

## 架构图

```
┌────────────────────────────────────────────────────────────┐
│ GitHub Actions CI                                          │
│  backend: mvn compile + test(含 MySQL)                    │
│  frontend: type-check + lint + vitest + build             │
└────────────────────────────┬───────────────────────────────┘
                             │
┌────────────────────────────▼───────────────────────────────┐
│  Nginx(前端静态资源 + /api 反向代理)                        │
│  Vue 3 + TypeScript + Element Plus + Pinia                 │
└────────────────────────────┬───────────────────────────────┘
                             │  /api
┌────────────────────────────▼───────────────────────────────┐
│  Spring Boot 3 (8080)                                      │
│  ├─ SecurityFilterChain + JWT 过滤器                       │
│  ├─ Controller → Service(事务) → Mapper(MyBatis-Plus)      │
│  ├─ Spring Cache + Redis(分类/图书/公告缓存)                │
│  ├─ Flyway 迁移 / SpringDoc OpenAPI / MapStruct            │
│  └─ 订单状态机(0→1→2→3→4, 0/1→5 合法流转校验)              │
└───────────────┬────────────────────────┬───────────────────┘
                │                        │
        ┌───────▼───────┐        ┌───────▼───────┐
        │   MySQL 8     │        │   Redis 7     │
        └───────────────┘        └───────────────┘
```

## 功能模块

- **会员**:注册 / 登录(JWT)/ 用户名查重 / 防爆破锁定
- **图书**:两级分类、分页列表(服务端分页 + 排序)、详情(三层信息)、关键词搜索
- **购物车**:加购、改数量、批量删除、全选结算
- **订单**:下单扣库存 → 已汇款 → 店主确认收款/配送/完成,订单状态机校验
- **店主后台**:图书管理、进货(自动加库存)、订单管理、客户管理、日结报表、公告、留言
- **留言**:登录用户可选,支持匿名

## 技术亮点

1. **订单状态机**:用 `OrderStatus` 枚举定义 6 状态 + 合法流转表,所有状态变更统一走 `transition()` 校验,非法跳转(如 0→2、5→5)直接拒绝,消除散落的 switch 判断。
2. **Redis 多级缓存**:热点数据(分类 1h、图书列表/详情/公告 30m)走 `@Cacheable`,写操作(下单/进货/图书增改)自动 `@CacheEvict`,保证一致性。**实测:缓存命中率 95%,接口延迟从 562ms 降到 ~13ms(约 40 倍)**。
3. **完整安全体系**:Spring Security 过滤链 + JWT 无状态认证、`/api/admin/**` 需店主角色、登录防爆破(连续 5 次失败锁定 15 分钟)、BCrypt 加密、统一 401/403 JSON。
4. **前后端类型全链路同步**:后端 SpringDoc 生成 OpenAPI 契约,前端 `openapi-typescript` 自动生成类型,接口变更重新生成即可同步,消除手写容错。
5. **工程化配套**:Flyway 数据库迁移、MyBatis-Plus 通用 CRUD + 分页插件、MapStruct 编译期 DTO 映射、GitHub Actions CI、Docker Compose 一键部署。
6. **测试**:后端 19 个单测(状态机/库存/日结业务逻辑),前端 7 个用例(Vitest + MSW 网络层 mock),CI 全绿。
7. **前端性能**:Element Plus 按需引入,主包从 1.1MB 降到 326KB。

## 项目结构

```
ebookstore/
├── ebookstore-backend/          # Spring Boot 后端
│   └── src/main/java/com/ebookstore/
│       ├── common/              # 统一响应体 Result / 业务异常 / 分页
│       ├── config/              # Security / Cache / MyBatisPlus / OpenAPI / Cors
│       ├── controller/          # 接口层(7 个 Controller)
│       ├── dto/                 # 传输对象
│       ├── entity/              # 实体(含 OrderStatus 状态机)
│       ├── mapper/              # MyBatis-Plus Mapper
│       ├── map/                 # MapStruct DTO 映射
│       ├── security/            # JWT 过滤器 / 登录防爆破
│       ├── service/             # 业务层
│       └── utils/               # JWT / 订单号生成
├── ebookstore-frontend/         # Vue 3 前端
│   ├── src/api/                 # 请求层(类型化 + openapi 生成类型)
│   ├── src/components/          # 公共组件
│   ├── src/stores/              # Pinia 状态
│   ├── src/views/               # 页面
│   └── src/test/                # MSW mock + 测试
├── docker-compose.yml           # MySQL + Redis + 后端 + 前端 一键部署
└── .github/workflows/ci.yml     # CI 流水线
```

## 本地运行

前置:JDK 17、Node 20.19+、MySQL 8、Redis。

```bash
# 后端
cd ebookstore-backend
./mvnw spring-boot:run        # 启动时 Flyway 自动建表

# 前端
cd ebookstore-frontend
npm install
npm run dev                    # http://localhost:5173(代理 /api 到 8080)
```

- 接口文档:http://localhost:8080/swagger-ui/index.html

## Docker 部署

```bash
cp .env.example .env           # 修改 MYSQL_PASSWORD / JWT_SECRET
docker compose up -d --build

# 访问 http://服务器IP
```

- MySQL / Redis 数据持久化在数据卷,重建容器不丢数据
- 后端启动时 Flyway 自动初始化表结构

### 部署常见问题与排查

**镜像拉取超时 / DNS 失败(国内常见)**
Docker Hub 在国内经常拉不动。配置镜像加速:
```bash
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<'EOF'
{ "registry-mirrors": ["https://docker.m.daocloud.io", "https://dockerproxy.com"] }
EOF
sudo systemctl restart docker
```
(Maven / npm 构建阶段下载慢同理,可配国内镜像源)

**端口被占用**
- 宿主机 80 被 nginx 占 → 改 `.env` 的 `FRONTEND_PORT=8081` 后 `docker compose up -d`
- 宿主机 8080 被旧 Java 进程占 → `netstat -ano | grep 8080` 找到 PID 后 `kill` 掉,或改 `BACKEND_PORT`
- MySQL 不再映射宿主机端口(后端走 compose 内网),不会和宿主机 MySQL 冲突

**MySQL 密码错误(Access denied)**
> 改 `.env` 的 `MYSQL_PASSWORD` **不会**改已有数据卷里的密码。首次部署后想换密码必须:
> `docker compose down -v`(清空数据库数据)再重新 up,或进旧 MySQL 里 `ALTER USER` 改。

**后端容器反复重启**
多半是连不上数据库:先看日志 `docker compose logs backend`。确认 `DB_PASSWORD` 与 MySQL 一致;compose 已用 `depends_on + healthcheck` 等 MySQL 就绪。

**容器内连数据库/Redis 要用服务名**
后端连接 MySQL 用的是 `mysql:3306`、Redis 是 `redis`,不是 `localhost` 也不是 IP——容器内 `localhost` 指容器自己。compose 已配好,不要手动改成 IP。

**前端页面 404 / 样式旧**
多为浏览器缓存,`Ctrl+Shift+R` 强刷。SPA 路由由 nginx `try_files` 兜底,直接刷新任意路径也能回到页面。

**外部访问不了**
云服务商安全组要放行 `80`(前端)和 `8080`(后端/接口文档)端口。

**前端跨域报错**
生产环境前端由 nginx 托管,`/api` 同源反代到后端,**不存在跨域**。只有直接访问后端 8080 或另起前端才会遇到,正常用 80 端口访问即可。

## 测试与 CI

```bash
# 后端单测(19 个,含业务逻辑)
cd ebookstore-backend && ./mvnw test

# 前端单测 + 类型检查
cd ebookstore-frontend && npm run test && npm run type-check
```

GitHub Actions 每次 push 自动执行:后端编译 + 测试(含 MySQL 服务容器)、前端 type-check + lint + 单测 + 构建。
