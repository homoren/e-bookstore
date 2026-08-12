# 技术栈锐评与升级路线图

> 对象:图书商城 ebookstore(Spring Boot + Vue3 前后端分离)
> 评价标准:课程/学习项目是合理的,但按"工程化/生产可用"标准看,差距明显。
> 结论先行:**这不叫垃圾,这叫"能跑但不抗打"。** Vue3 3.5 + Vite 8 + Pinia 3 版本很新,真正的短板在工程化配套:没有 TypeScript、没有测试、没有 CI、后端 ORM 和数据迁移都是"原始社会"。

---

## 一、当前技术栈清单

| 层 | 技术 | 版本 |
|---|---|---|
| 后端语言 | Java | 17 |
| 后端框架 | Spring Boot | 3.2.0 |
| ORM | MyBatis(纯注解 SQL) | mybatis-spring-boot-starter 3.0.3 |
| 数据库 | MySQL | - |
| 认证 | 手写 JWT + 拦截器 + Spring Security 仅用 BCrypt | jjwt 0.11.5 |
| 迁移 | 手写 migration.sql(无自动工具) | - |
| 测试 | 只有一个 contextLoads 空测试 | spring-boot-starter-test |
| 前端框架 | Vue 3(Composition API / script setup) | 3.5.32 |
| 构建 | Vite(rolldown) | 8.0.8 |
| 路由/状态 | Vue Router / Pinia | 5.0.4 / 3.0.4 |
| UI 库 | Element Plus(全量引入) | 2.13.7 |
| HTTP | Axios | 1.15.1 |
| 类型 | **纯 JavaScript,无 TS** | - |
| 前端测试 | **无** | - |
| CI/CD | **无** | - |
| 容器化 | **无** | - |

---

## 二、锐评

### 后端:该有的工程化配套基本没有

1. **MyBatis 注解裸 SQL —— 最扎眼的一个。**
   所有 SQL 以字符串写在 Java 接口里,没有编译期校验、没有 SQL 复用、没有 @ResultMap 的约束,字段一改就静默出错。这个项目的 CRUD 占绝大多数,正好是 ORM 框架的主场,结果却是在手写 SQL。真实项目要么 **MyBatis-Plus**(保留 SQL 控制 + 通用 CRUD/分页/乐观锁),要么 **Spring Data JPA**(领域驱动)。

2. **Spring Security 只被用来做密码加密,认证授权全是手搓。**
   用 `JwtInterceptor + @CrossOrigin` 拼出来的一套鉴权,角色判断散落在拦截器和 controller 里。真实项目应该是一条完整的 **SecurityFilterChain + JWT 过滤器 + 方法级 `@PreAuthorize`**,还要处理 refresh token、token 黑名单、CSRF、登录限流——这些你现在全都没有。

3. **没有数据库迁移工具。**
   `migration.sql` 靠人肉执行,换个环境就得手工跑脚本,改表结构就是一场事故。**Flyway / Liquibase** 是刚需,不是加分项。

4. **没有分页抽象。**
   图书列表是前端把全量数据拉下来再 `slice()`——数据到几百条还行,几千条就开始卡。服务端分页(PageHelper / MyBatis-Plus 分页插件 / Spring Data `Pageable`)是基本功。

5. **DTO 与实体全靠手写 setter 拷贝。**
   `buildOrderDTO()` 那一大坨 `setXxx(getXxx())`,字段多了就是灾难。**MapStruct** 一行解决,还带编译期校验。

6. **没有接口文档。**
   前后端靠人脑对齐字段。上 **SpringDoc OpenAPI 3**,前端还能用 openapi-typescript 自动生成类型,直接消灭一整类 bug。

7. **测试形同虚设。**
   唯一的测试是 `contextLoads()`,空跑。业务逻辑(订单状态机、库存扣减、日结计算)完全没有覆盖。这部分是"未来改代码就胆战心惊"的根源。

8. **事务有自调用陷阱。**
   `getTodaySettlement()` 内部直接调 `generateDailySettlement()`,`@Transactional` 是失效的(Spring 代理机制),这类写法是隐患。

9. **无缓存。**
   首页图书、公告、分类每次请求都打库。**Redis + Spring Cache** 一上,热点数据 QPS 直接翻几个量级。

### 前端:版本很新,但骨架是十年前的后端思维

1. **没有 TypeScript —— 最大的短板。**
   你这次优化时遇到 `res.exists` / `res.data` 这类手写容错,本质就是没有类型。数据从后端回来是 `any`,字段写错编译期不报、运行期炸。对 Vue 3 + Vite 8 这种现代工具链来说,TS 是默认项,不是可选项。

2. **Element Plus 全量引入。**
   构建产物出现 **1.1MB 的单个 chunk**,就是因为 `app.use(ElementPlus)` 把整库打进去了。`unplugin-vue-components + unplugin-auto-import` 按需引入,体积能砍掉 80%。

3. **没有前端测试。**
   没有 Vitest 单测、没有 Playwright E2E。页面是手工点出来的,回归全靠肉眼。

4. **axios 封装是"裸奔"的。**
   没有类型、没有重试、没有请求取消。配合 OpenAPI codegen,能自动生成类型安全的 API 客户端,请求层写错字段会在编译期报错。

5. **server state 和 client state 混在一个 Pinia 里。**
   图书列表、订单列表这些"服务器状态"应该交给 **TanStack Query**(缓存、失效、重试、防抖),Pinia 只该存 token、userInfo 这些"客户端状态"。

6. **表单校验手写。**
   每个页面手写 rules 对象,该用 **VeeValidate + Zod**,声明式 + 类型推导。

### 基础设施:零

- 没有 CI(GitHub Actions),代码合并全靠手。
- 没有 Docker,部署靠 `mvn spring-boot:run` + `npm run dev`。
- 没有环境管理(.env.example、jasypt 加密),数据库密码还躺在 yml 里。

### 要公道的地方

- **版本选型是新的**:Vue 3.5 / Vite 8 / vue-router 5 / Pinia 3 / Spring Boot 3,没有用老掉牙的库。
- **分层是清晰的**:controller / service / mapper 三层,DTO 分层意识有。
- **错误处理在上一轮已统一**:Result + GlobalExceptionHandler 已经比大多数课程项目强了。

---

## 三、升级路线图(按性价比排序)

### P0 —— 收益最大、改动可控,强烈建议

| 方向 | 方案 | 主要收益 |
|---|---|---|
| 前端转 TypeScript | `vue-tsc` + 严格模式,逐步迁移 .vue | 消灭一整类运行时 bug,开发体验质变 |
| Element Plus 按需引入 | `unplugin-vue-components` + `unplugin-auto-import` | 包体从 1.1MB 降到 ~200KB |
| 后端加接口文档 | **SpringDoc OpenAPI 3** + 前端 `openapi-typescript` | 前后端契约自动同步,文档即代码 |
| 数据库迁移 | **Flyway**(SQL 保留,零学习成本) | 环境可复现,改表安全 |
| 服务端分页 | MyBatis-Plus 分页插件 或 PageHelper | 大数据量不卡,前后端职责清晰 |
| Java 21 虚拟线程 | `spring.threads.virtual.enabled=true` 一行开启 | IO 密集型(MySQL/HTTP)天然适配,高并发吞吐提升且几乎零成本 |

### P1 —— 工程质量的分水岭

| 方向 | 方案 | 主要收益 |
|---|---|---|
| ORM 升级 | **MyBatis-Plus**(保留现有 SQL 思路,白捡 CRUD/分页/乐观锁/代码生成) | 重复代码大减,分页/乐观锁开箱即用 |
| DTO 映射 | **MapStruct** | 删掉所有手写 setter 拷贝 |
| 认证完整化 | SecurityFilterChain + JWT 过滤器 + `@PreAuthorize`,替换手写拦截器 | 权限模型统一,可扩展 refresh token |
| 后端测试 | JUnit 5 + **Testcontainers**(真 MySQL) + MockMvc,覆盖订单状态机/日结/库存 | 敢重构,不怕回归 |
| 前端测试 | **Vitest**(单测组件/stores)+ **MSW**(网络层 mock 接口)+ **Playwright**(E2E 冒烟) | 回归有保障 |
| 缓存 | **Redis + Spring Cache**(首页图书、公告、分类) | 热点接口 QPS 数倍提升 |
| CI | **GitHub Actions**:后端 compile+test、前端 build+lint | 每次提交自动验证 |

### P2 —— 生产化与加分项(课程项目可选)

| 方向 | 方案 |
|---|---|
| 容器化 | `Dockerfile` 多阶段构建 + `docker-compose.yml`(MySQL + Redis + 后端 + 前端) |
| 可观测性 | logback JSON 结构化日志 + Micrometer + Prometheus + Grafana |
| 配置安全 | `.env.example` 模板 + 环境变量注入,敏感信息不进仓库 |
| 订单状态机 | 状态流转表/Spring Statemachine 接管 0→1→2→3→4/5 的状态迁移,杜绝非法跳转 |
| 库存并发 | 数据库**乐观锁**(`version` 字段)或 Redis 预扣 + 事务兜底 |
| 事件解耦 | 下单后发事件(本地事务发件箱 + RabbitMQ)驱动扣库存/通知(对单机小店是过度设计,提一嘴) |

### 明确不建议(对你这个体量)

- **微服务 / Spring Cloud**、**分库分表(ShardingSphere)**、**K8s** —— 一个书店没有分布式拆分的理由,纯属给自己加戏。
- **GraphQL** —— 双端都是 REST 心智,换引擎得不偿失。

---

## 四、升级后的目标架构(一页图)

```
┌─────────────────────────────────────────────┐
│  GitHub Actions CI                            │
│  后端: compile → test(Testcontainers) → build │
│  前端: type-check → lint → build → E2E        │
└──────────────┬──────────────────────────────┘
               │
┌──────────────▼──────────────┐   ┌──────────────┐
│  Vue 3 + TypeScript + Vite  │──▶│  SpringDoc   │── 契约即文档
│  Element Plus(按需)          │   │  OpenAPI 3   │
│  Pinia(client state)         │   └──────────────┘
│  TanStack Query(server state)│
│  VeeValidate + Zod           │
│  Vitest / Playwright         │
└──────────────┬──────────────┘
               │  /api (Vite proxy)
┌──────────────▼──────────────────────────────┐
│  Spring Boot 3 + Java 21                     │
│  ├─ SecurityFilterChain + JWT 过滤器 + @PreAuthorize│
│  ├─ MyBatis-Plus(分页 / 乐观锁 / 代码生成)      │
│  ├─ MapStruct(DTO 映射)                      │
│  ├─ Spring Cache + Redis(热点缓存)            │
│  ├─ Flyway(迁移) / SpringDoc(文档)            │
│  └─ JUnit 5 + Testcontainers + MockMvc       │
└──────────────┬──────────────────────────────┘
               │
     ┌─────────▼─────────┐
     │ MySQL │ Redis     │
     └───────────────────┘
```

---

## 五、落地顺序建议(别一口吃成胖子)

1. **本周**:前端转 TS(文件少,趁早转)、Element Plus 按需引入、SpringDoc。
2. **两周内**:Flyway、服务端分页、后端核心流程补测试(订单/库存/日结)。
3. **一个月内**:MyBatis-Plus 或至少把重复 SQL 收敛、MapStruct、Redis 缓存、GitHub Actions。
4. **有余力**:认证完整化(替换手写拦截器)、Docker、Playwright E2E。

> 关键原则:**先把"会让人改代码时心里没底"的部分补齐(TS + 测试 + 迁移 + 文档),再谈炫技。** 技术栈新不等于好,工程化配套才决定一个项目能走多远。
