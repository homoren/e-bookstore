# 项目改进对比与答辩要点

> My-eBookStore 图书商城 —— 从"能跑的课程项目"到"带完整工程化配套的全栈项目"

---

## 一、原本 vs 现在

### 后端

| 维度 | 原本 | 现在 | 价值 |
|---|---|---|---|
| 数据访问 | MyBatis 纯注解手写 SQL | MyBatis-Plus(通用 CRUD + 分页插件) | 重复代码大减,分页/乐观锁开箱即用 |
| 数据库迁移 | 手写 migration.sql 人肉执行 | Flyway(V1 建表 / V2 种子 / V3 索引) | 环境可复现,改表安全 |
| 认证授权 | 手写拦截器 + @CrossOrigin("*") | Spring Security 过滤链 + JWT 过滤器 + @PreAuthorize | 权限模型统一,可扩展 |
| JWT/密码 | 硬编码在源码/yml | 环境变量可覆盖 | 生产安全 |
| 响应/异常 | 手写 Map + try/catch | 统一 Result + 全局异常处理 | 代码整洁,错误语义清晰 |
| 依赖注入 | @Autowired 字段注入 | 构造器注入 | 可测试性、不可变性 |
| DTO 映射 | 手写 setter 拷贝 | MapStruct 编译期映射 | 删掉几百行样板代码 |
| 查询性能 | N+1(循环查明细) | 批量 IN 查询 | 大数据量不卡 |
| 缓存 | 无 | Redis + Spring Cache | 95% 命中率,接口 40 倍提速 |
| 订单状态 | 散落 switch | OrderStatus 状态机 + 非法流转拦截 | 逻辑收敛,防脏数据 |
| 安全 | 无 | 登录防爆破(5 次失败锁 15 分钟) | 安全加分项 |
| 接口文档 | 无 | SpringDoc OpenAPI 3 + Swagger | 前后端契约可同步 |
| 测试 | 仅 contextLoads | 19 个业务单测(状态机/库存/日结) | 敢重构,不怕回归 |

### 前端

| 维度 | 原本 | 现在 | 价值 |
|---|---|---|---|
| 语言 | 纯 JavaScript | TypeScript(vue-tsc 0 错误) | 消灭运行时类型 bug |
| 类型同步 | 前后端人脑对齐 | openapi-typescript 自动生成 | 接口变更即同步 |
| UI 库 | Element Plus 全量(1.1MB) | 按需引入(326KB,-70%) | 首屏更快 |
| 分页 | 前端假分页(全量拉取) | 服务端分页 + 排序 | 数据量大不卡 |
| 购物车全选 | 复选框不联动 | 修复联动 | 交互正确 |
| 测试 | 无 | Vitest + MSW(7 用例) | 回归有保障 |
| 管理端 | 图书管理假实现 | 真实调接口 | 功能完整 |

### 工程化 / 部署

| 维度 | 原本 | 现在 |
|---|---|---|
| CI | 无 | GitHub Actions(后端编译+测试、前端全检查) |
| 部署 | 手动 mvn + npm | Docker Compose 一键起 MySQL+Redis+后端+前端 |
| 配置 | 写死 localhost | 环境变量化(.env + .env.example) |
| 文档 | 无 | 专业 README(架构图 + 技术亮点 + 部署) |
| 代码质量 | 死代码/空方法/注释残留 | 全清理 + IDE 诊断清零 |

---

## 二、修掉的真实 Bug

1. 管理员按状态查订单返回 `null`(前端直接报错)
2. 订单配送截止日期计算了但从未保存
3. 留言无法识别登录用户(全变匿名)
4. 订单状态缺"已汇款"流转(0→1),导致店主永远无法确认收款
5. Redis 缓存列表/PageResult 反序列化失败(读缓存即报错)
6. 登录 403(浏览器 IPv6 来源 + Vite 端口漂移 + CORS 白名单过窄)
7. 前端假分页、购物车全选不联动、管理端图书管理是假的

---

## 三、核心亮点(答辩重点讲)

1. **Redis 多级缓存**:热点数据缓存 + 写操作自动失效,实测命中率 95%,延迟 562ms→13ms(约 40 倍)。
2. **订单状态机**:6 状态 + 合法流转表,统一 `transition()` 校验,非法跳转(0→2、5→5)直接拒绝。
3. **完整安全体系**:SecurityFilterChain + JWT 无状态、管理接口双保险(URL 匹配 + @PreAuthorize)、登录防爆破、BCrypt。
4. **前后端类型全链路同步**:SpringDoc 出契约 → openapi-typescript 生成前端类型,接口变更重新生成即可。
5. **工程化配套**:Flyway 迁移、MyBatis-Plus 通用 CRUD、MapStruct、19 后端单测 + 7 前端单测、GitHub Actions CI、Docker 一键部署。
6. **前端性能**:Element Plus 按需引入,主包 1.1MB → 326KB。

---

## 四、答辩 / 面试可能被问的问题(提前自测)

**后端**
1. JWT 过滤器在 Security 过滤链的什么位置?为什么放在 `UsernamePasswordAuthenticationFilter` 之前?
2. 为什么选 MyBatis-Plus 不用 JPA?分页插件怎么生效的?
3. Redis 缓存了哪些数据?下单为什么清图书缓存?怎么保证一致性?(答:写操作 @CacheEvict 失效 + TTL 兜底)
4. 订单状态机 0→1→2→3→4 各是谁触发的?非法流转会怎样?
5. 全局异常处理怎么做的?业务错误返回什么 HTTP 码?为什么用 200 + success:false?
6. 登录防爆破怎么实现的?内存 Map 有什么局限?(答:单机可用,多实例要换 Redis)

**前端**
7. openapi-typescript 怎么做到"前后端类型同步"?
8. 为什么用 TypeScript?解决了什么实际问题?
9. 服务端分页和前端分页的区别?为什么改服务端分页?
10. Element Plus 按需引入怎么做的?(unplugin-vue-components)

**部署**
11. Docker 部署里 nginx 为什么把 `/api` 代理到后端?(答:同源,避免跨域;静态资源由 nginx 托管)
12. 数据库怎么初始化的?(答:后端启动 Flyway 自动建表)
13. 数据怎么持久化?(答:Docker 数据卷,重建容器不丢)
14. 生产环境怎么更新?(答:git pull + docker compose up -d --build)

---

## 五、简历写法(可直接套用)

### 项目定位

```
项目名称:电子书商城 My-eBookStore(个人全栈项目)
角色:独立开发
技术栈:Java 17 · Spring Boot 3.2 · MyBatis-Plus · MySQL · Redis · Spring Security
        · Vue 3 · TypeScript · Vite · Docker · GitHub Actions
```

### 详版(4-5 条,推荐)

```
- 独立完成图书商城全栈开发:会员、两级分类与检索、购物车、订单(下单→汇款→收款→配送→完成)、
  店主后台(图书管理/进货/日结/客户)等完整业务闭环
- 基于 Spring Security + JWT 构建无状态认证,统一 Result 响应体与全局异常处理;
  引入 Redis + Spring Cache 缓存热点数据并自动失效,实测缓存命中率 95%、接口延迟由 562ms 降至 13ms(约 40 倍)
- 用 OrderStatus 枚举实现订单状态机,统一校验状态流转(0→1→2→3→4),拦截非法跳转;
  接入 Flyway 管理数据库迁移,基于 MyBatis-Plus 通用 CRUD + 分页插件
- 前端 Vue 3 + TypeScript + Element Plus(按需引入,主包 1.1MB→326KB),
  通过 openapi-typescript 实现前后端类型全链路同步
- 编写 19 个后端单测(Mockito,覆盖订单状态机/库存/日结)与 7 个前端单测(Vitest + MSW),
  接入 GitHub Actions CI,并用 Docker Compose 一键部署 MySQL+Redis+后端+nginx
```

### 简版(3 条)

```
- 独立完成电子书商城全栈:会员、图书检索、购物车、订单状态流转、店主后台(进货/日结/客户)完整闭环
- 后端 Spring Boot + MyBatis-Plus + Redis + Spring Security;Redis 缓存实测命中率 95%、
  接口提速约 40 倍;实现订单状态机与登录防爆破
- 前端 Vue 3 + TypeScript,openapi-typescript 前后端类型同步;19 后端 + 7 前端单测、
  GitHub Actions CI、Docker Compose 一键部署
```

### 写简历的 3 条铁律

1. **数字是最强证据**——"95% 命中率""40 倍提速""326KB"这些比形容词有用一万倍
2. **只写你能讲清楚的**——面试官会追着每一条问,答不上来的宁可删掉也不写
3. **千万别写没做的**——"高并发""分布式""微服务"一个都不许出现,写了是减分项
