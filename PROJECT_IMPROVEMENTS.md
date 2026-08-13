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

---

## 六、项目面试问题清单(60 题)

### A. 整体架构(7)

1. 这个项目的整体架构是怎样的?画一下一次完整请求的流程。
2. 为什么做前后端分离?好处和代价?
3. 为什么用单体架构而不是微服务?(答:业务规模小,单体够用;微服务是过度设计)
4. 项目里分了哪些层?controller / service / mapper 各负责什么?
5. 你的项目代码量大约多少?核心业务逻辑在哪?(答:订单/库存/日结)
6. 如果用户量增长十倍,你觉得哪里会先扛不住?怎么优化?
7. 这个项目你做了多久?独立完成还是团队?

### B. Spring 核心(8)

8. Spring Boot 的启动流程是什么?自动配置原理?
9. Spring Security 的过滤链结构?你的 JWT 过滤器加在哪个位置?为什么在 UsernamePasswordAuthenticationFilter 之前?
10. `@PreAuthorize` 和 URL 权限匹配有什么区别?你为什么两个都用?(答:URL 兜底 + 方法级细粒度,双保险)
11. Spring 事务用过吗?`@Transactional` 在什么情况下会失效?(自调用/非 public/异常被吞)
12. 为什么用构造器注入而不是 `@Autowired` 字段注入?
13. 全局异常处理怎么做的?`@RestControllerAdvice` 原理?为什么业务错误返回 HTTP 200 + success:false?
14. Bean 的生命周期?`@Service` 默认单例吗?
15. 为什么用 MapStruct?它和手写 setter、BeanUtils 比好在哪?

### C. 数据库 / MySQL(7)

16. 表结构怎么设计的?订单和订单明细为什么拆两张表?
17. 用 Flyway 的意义?迁移脚本怎么写的?新增一列怎么操作?
18. 索引优化做了什么?怎么分析慢查询?(答:V3 加了 created_at/status 等索引;EXPLAIN)
19. 你遇到过 N+1 查询吗?怎么解决的?(答:订单列表循环查明细→批量 IN 查询)
20. 库存扣减怎么保证并发安全?(答:条件 UPDATE `WHERE stock >= qty`,失败即抛异常回滚)
21. `order` 表名为什么加反引号?(答:MySQL 保留字)
22. MyBatis-Plus 的分页插件原理?(答:拦截器改写 SQL 加 LIMIT + count)

### D. Redis / 缓存(8)

23. 缓存了哪些数据?为什么选这些?(分类/图书列表/详情/公告)
24. 缓存一致性怎么保证?数据更新时怎么处理?(答:@CacheEvict 写操作失效 + TTL 兜底)
25. 缓存穿透 / 击穿 / 雪崩是什么?你的项目怎么应对?
26. 缓存序列化遇到过什么问题?(答:LocalDate 不支持 → 注册 jsr310;List 顶层无类型 → 改 WRAPPER_ARRAY)
27. 为什么用 Caffeine + Redis 多级缓存?单机部署有必要吗?(答:多级缓存是进阶方案,单机 Redis 一层够)
28. Redis 和数据库数据不一致怎么办?最终一致性?
29. 为什么下单要清图书缓存?(答:销量/库存变了,缓存会过期)
30. 你的缓存命中率 95% 是怎么测出来的?

### E. 订单状态机(5)

31. 订单有哪几个状态?状态机为什么用枚举实现?
32. 非法流转怎么拦截?举个例子(0→2 直接确认收款会被拒)
33. 状态机放在 service 层还是 entity?为什么?
34. 你补了"已汇款"0→1 的流转,为什么之前是 bug?
35. 如果以后要加"退款"状态,怎么改?(答:加枚举值 + 流转表)

### F. 安全(7)

36. JWT 认证流程?Token 里放了什么?为什么无状态?
37. JWT 的密钥怎么管理的?泄露了怎么办?(答:环境变量;泄露要换密钥 + 短有效期)
38. 为什么密码用 BCrypt?和 MD5 区别?
39. 登录防爆破怎么做的?内存 Map 有什么局限?(答:单机可用,多实例要换 Redis 集中计数)
40. CSRF 攻击是什么?为什么你的项目关掉了 CSRF?(答:JWT 放 header,非 cookie,无 CSRF 风险)
41. CORS 遇到过什么问题?怎么排查出 IPv6 来源 + 端口漂移的?
42. 你的 401/403 怎么返回给前端?前端怎么处理?

### G. 前端 / TypeScript(8)

43. 为什么把项目从 JS 迁移到 TypeScript?解决了什么实际问题?
44. openapi-typescript 怎么实现前后端类型同步?后端改字段前端怎么感知?
45. 服务端分页和前端分页的区别?为什么改服务端分页?
46. Element Plus 按需引入怎么做的?(答:unplugin-vue-components + resolver)
47. Vue3 和 Vue2 的区别?Composition API 好处?
48. Pinia 和 Vuex 区别?为什么选 Pinia?
49. Vue Router 5 的守卫怎么写?`next()` 为什么废弃了?
50. 组件通信有哪些方式?(props/emit/defineExpose/provide-inject/store)

### H. 测试 / CI / 部署(8)

51. 后端单测怎么写的?Mockito 的 `@Mock` / `@InjectMocks` 原理?为什么要 mock mapper?
52. MSW 是什么?为什么在前端测试里用它?
53. CI 流程包含哪些步骤?为什么后端 job 要带 MySQL 服务?
54. Docker 多阶段构建是什么?为什么前端要 nginx、后端要 JRE 镜像?
55. 数据库在部署环境怎么初始化的?(答:后端启动 Flyway 自动建表)
56. 数据怎么持久化?重建容器会丢数据吗?(答:Docker 数据卷)
57. 部署后怎么更新代码?(答:git pull + docker compose up -d --build)
58. 你部署时踩过哪些坑?(镜像源下载慢、npm 版本不一致、dts 文件缺失)

### I. 排查 / 综合(9)

59. 你在项目里遇到过最难的 bug 是什么?怎么定位的?(推荐讲缓存反序列化或登录 403)
60. 一次完整的 bug 排查流程是怎样的?(复现→看日志→缩小范围→定位→修复→回归测试)

---

## 七、面试问题参考答案

### A. 整体架构

**1. 整体架构 / 一次完整请求流程**
前后端分离。浏览器 → nginx(生产)/Vite 代理(开发)→ Spring Boot Controller → Service(事务)→ Mapper(MyBatis-Plus)→ MySQL;热点读先查 Redis 缓存。例:登录请求 → 前端 axios 带 Authorization → JWT 过滤器校验 → SecurityContext 写入 → Controller → Service 校验密码(BCrypt)→ 签发 JWT 返回。

**2. 为什么前后端分离**
职责清晰、可独立部署扩展、前端可单独优化体验、后端接口可复用。代价:多一层通信、有跨域/联调成本(用 OpenAPI 契约同步缓解)。

**3. 为什么单体不用微服务**
业务规模小、单团队单机够用。微服务引入服务发现/分布式事务/链路追踪等复杂度,对这个体量是过度设计。模块化单体 + 以后按需拆分才是合适的演进路线。

**4. 分层职责**
Controller:参数校验、绑定用户、返回统一 Result;Service:业务逻辑 + 事务 + 状态机校验;Mapper:数据访问(MyBatis-Plus 通用 CRUD + 自定义 SQL)。DTO 与实体分离,避免直接暴露数据库结构。

**5. 核心业务逻辑在哪**
订单模块(状态机流转、库存扣减、配送截止日期)、日结模块(销售额/成本/利润计算)、进货模块(成本核算 + 库存联动)。

**6. 用户量增长十倍哪里先扛不住**
数据库(热点查询)→ 已用 Redis 缓存缓解;其次是 Tomcat 线程(可上虚拟线程/调连接池)。再往上:读写分离、分库分表,但那是大厂场景。

**7. 做了多久 / 独立完成**
(按自己实际说。强调:从需求→设计→实现→测试→部署全链路独立完成。)

### B. Spring 核心

**8. Spring Boot 启动流程 / 自动配置**
main → SpringApplication.run → 创建 ApplicationContext → 扫描 @Component → 执行自动配置(@ConditionalOnXxx 按条件装配 Bean)→ 内嵌 Tomcat 启动。自动配置 = 一堆 AutoConfiguration 类 + @Conditional,按 classpath 和配置决定装配什么。

**9. JWT 过滤器位置**
JwtAuthenticationFilter 放在 UsernamePasswordAuthenticationFilter 之前。因为要**先解析 token、把认证信息放进 SecurityContext**,后续的授权判断(@PreAuthorize / URL 匹配)才能用上。放前面保证认证在授权之前完成。

**10. @PreAuthorize 和 URL 匹配区别**
URL 匹配(hasRole)是粗粒度、集中配置;@PreAuthorize 是方法级细粒度、就近声明。我用在 AdminController 类上双保险:URL 层兜底 + 方法级保证,即使 URL 配置漏了,方法级也拦住。

**11. 事务失效场景**
自调用(同类方法内部调,@Transactional 代理不生效)、非 public 方法、异常被 catch 吞掉、代理未生效(直接 new 对象)。我在 getTodaySettlement 自调用日结方法时也遇到过类似问题,要注意。

**12. 构造器注入 vs 字段注入**
构造器注入:依赖不可变、便于测试(直接 new 传 mock)、Spring 会检测循环依赖;字段注入隐藏依赖、不好测。团队规范更推荐构造器注入。

**13. 全局异常处理 / 为什么业务错误用 200**
@RestControllerAdvice 拦截所有 Controller 异常。业务异常(BusinessException)返回 HTTP 200 + `{success:false, message}`,前端拦截器统一弹提示;**401/403 才用真实 HTTP 状态码**触发前端跳转。好处:前后端约定一套响应体,业务错误不需要 HTTP 语义,前端处理简单统一。

**14. Bean 生命周期 / 单例**
@Service 默认单例(一个实例共享)。生命周期:实例化 → 属性注入 → 初始化(@PostConstruct)→ 使用 → 销毁。单例注意线程安全(我的 LoginAttemptService 用 ConcurrentHashMap 保证)。

**15. 为什么 MapStruct**
编译期生成映射代码,无反射开销、字段名变了编译期报错;比手写 setter 省几百行,比 BeanUtils 反射更快更安全。

### C. 数据库 / MySQL

**16. 表结构 / 为什么订单拆两张表**
订单表(头部:金额/收货人/状态/时间戳)+ 订单明细表(每条商品)。一对多关系,拆开避免冗余、便于单独统计单品销量。1NF 规范化。

**17. Flyway 意义 / 怎么加一列**
Flyway 记录迁移版本(history 表),启动时自动执行未应用的脚本。加列:写 `V4__xxx.sql`(`ALTER TABLE ... ADD COLUMN`),Flyway 按版本顺序执行,团队/环境可复现。

**18. 索引优化 / 慢查询分析**
V3 迁移给 order(created_at/status)、order_item(book_id)、message(created_at) 等加索引,覆盖列表排序、状态过滤、联表场景。用 EXPLAIN 看是否走索引、有没有全表扫描。

**19. N+1 怎么解决**
原来订单列表对每条订单循环查明细(1 + N 次查询)。改为:先查订单列表,再用 `WHERE order_id IN (...)` 一次查出所有明细,Java 侧分组组装 → 2 次查询。

**20. 库存并发安全**
下单扣库存用 `UPDATE book SET stock = stock - n WHERE id = ? AND stock >= n`,条件更新保证"库存够才扣",影响行数为 0 说明不够 → 抛异常回滚。这是数据库层原子操作,避免超卖。

**21. order 为什么加反引号**
`order` 是 MySQL 保留字(ORDER BY),直接写会语法错误,所以 SQL 里用反引号 `` `order` `` 包裹。

**22. MyBatis-Plus 分页原理**
PaginationInnerInterceptor 拦截 SQL,自动改写:先执行 count 查询,再给原 SQL 拼接 LIMIT offset,size。自定义方法传入 Page 参数即可。

### D. Redis / 缓存

**23. 缓存了哪些数据 / 为什么**
分类(1h)、图书列表/分页(30m)、图书详情(30m)、已发布公告(30m)。都是**读多写少**的热点数据,缓存收益最大。

**24. 缓存一致性**
写操作(下单/进货/图书增改/公告增改)标 `@CacheEvict`,操作成功后清缓存;再加 TTL 兜底,即使漏清也会过期自愈。读侧 `@Cacheable` 先查缓存再查库。

**25. 穿透 / 击穿 / 雪崩**
穿透:查不存在的 key 打库 → 缓存空值或布隆过滤器(当前项目对不存在的 key 不缓存,靠前端校验兜底);击穿:热点 key 过期瞬间高并发打库 → 互斥锁/逻辑过期(暂未做,可扩展);雪崩:大量 key 同时过期 → TTL 加随机值(可扩展)。

**26. 缓存序列化的坑**
LocalDate/LocalDateTime 默认不支持 → 注册 jsr310 模块;GenericJackson2JsonRedisSerializer 顶层 List 无类型标注导致反序列化失败 → 改用 EVERYTHING + WRAPPER_ARRAY;PageResult 无无参构造 → 补 @NoArgsConstructor。

**27. 多级缓存**
Caffeine(本地 JVM)L1 + Redis L2:本地快、跨实例共享。但单机部署 L1 缓存更新要手动清(本地节点才知道),复杂度高、收益有限,所以当前用 Redis 单层,多级是进阶方案。

**28. 缓存与数据库不一致**
业务上接受短暂不一致(TTL 兜底),写操作主动清缓存保证下一次读是新的;极端场景可用延迟双删/消息队列,但当前规模不需要。

**29. 为什么下单清图书缓存**
下单扣了库存、销量 +1,缓存里的库存/销量是旧的,不清会显示过期数据。所以 createOrder 标 @CacheEvict 清 bookList/bookDetail。

**30. 命中率怎么测**
清空 Redis → 冷请求(查库)计时 562ms → 连续 20 次热请求平均 13ms → 后端日志里 SQL 查询次数对比(21 次请求仅 1 次查库)→ 命中率 ≈ 95%。

### E. 订单状态机

**31. 状态机设计**
OrderStatus 枚举:待付款(0)/待汇款确认(1)/已收款(2)/已配送(3)/已完成(4)/已取消(5),枚举内定义合法流转表(Map<状态, 可转移集合>),所有状态变更走统一 `transition()` 校验。

**32. 非法流转拦截**
transition() 查流转表,不在表里直接抛"订单状态不正确,当前状态:xxx"。如 0 直接确认收款(0→2,跳过汇款)会被拒;5(已取消)再取消也报错。

**33. 状态机放哪**
枚举放 entity(和 Order 同包),校验逻辑放 Service。实体只描述状态,Service 负责状态变更规则,职责清晰。

**34. 0→1 为什么是 bug**
原代码 createOrder 只置 0,confirmPayment 要求状态==1,但**没有任何地方把 0 改成 1** → 店主永远无法确认收款(状态 1 不可达)。补了 confirmRemittance(0→1)接口 + 前端"我已汇款"按钮。

**35. 加退款状态怎么改**
枚举加 REFUNDING/REFUNDED,流转表里加合法路径(如已完成(4)→退款中),transition 校验自动生效,几乎零改动业务代码。

### F. 安全

**36. JWT 认证流程**
登录成功后用 jjwt 生成 token(sub=username, claim=userId/role, 有效期7天, HS256 签名)→ 前端存 localStorage,请求带 Authorization: Bearer xxx → 过滤器解析校验 → 写 SecurityContext。无状态:服务端不存会话,靠签名验真。

**37. 密钥管理**
JWT 密钥走环境变量 JWT_SECRET(yml 里 `${JWT_SECRET:默认值}`),部署时 .env 注入。泄露应对:换密钥(旧 token 全部失效)+ 缩短有效期 + 必要时候加 refresh token。

**38. 为什么 BCrypt**
BCrypt 自带盐 + 计算成本可调,同样密码每次哈希不同,抗彩虹表;MD5/SHA 是快速哈希,容易被撞库和彩虹表破解。加盐 + 慢哈希才是存密码的正解。

**39. 防爆破实现 / 局限**
LoginAttemptService:ConcurrentHashMap 记录失败次数,连续失败 5 次锁定 15 分钟(可配置),登录成功清零;失败提示统一"用户名或密码错误"防用户名枚举。局限:内存 Map 是**单机状态**,多实例部署各算各的,要换 Redis 集中计数。

**40. 为什么关 CSRF**
CSRF 针对 cookie 会话:攻击者诱导浏览器自动带 cookie 发请求。我的 JWT 放在**请求头 Authorization**,不是 cookie,浏览器不会自动携带 → 无 CSRF 风险,所以关闭。

**41. CORS 排查过程**
现象:登录 403。用带不同 Origin 的 curl 复现 → 发现 `http://[::1]:5173` 403 → 前端 dev 监听 IPv6,浏览器解析 localhost 成 ::1;后来又发现 Vite 端口漂移(5173→5174)。最终 allowedOriginPatterns 改通配 `localhost:* / 127.0.0.1:* / [::1]:*`。

**42. 401/403 返回 / 前端处理**
Security 的 AuthenticationEntryPoint 返回 401 JSON、AccessDeniedHandler 返回 403 JSON。前端 axios 拦截器:401 → 清 token 跳登录;403 → 提示无权访问;其他错误显示后端 message。

### G. 前端 / TypeScript

**43. 为什么转 TypeScript**
纯 JS 时接口返回是 any,字段写错运行期才炸(项目里就遇到过 `res.exists`/`res.data` 手写容错)。TS 后编译期报错,配合后端生成的类型,字段对不上直接红。

**44. openapi-typescript 同步原理**
后端 SpringDoc 自动生成 OpenAPI 契约(JSON)→ 前端 `openapi-typescript` 生成 TS 类型文件(generated.ts)→ api 层引用。后端字段变了,重新生成 + type-check 立刻发现前端哪里没跟上。

**45. 服务端分页 vs 前端分页**
前端分页:一次拉全量内存里切,数据量大了卡且浪费流量;服务端分页:传 page/pageSize,后端 SQL LIMIT + count,只返回一页,数据量大也稳。排序也移到后端避免"只排当前页"的错误。

**46. Element Plus 按需引入**
unplugin-vue-components + ElementPlusResolver:模板里用到哪个 el-xxx 才引入对应组件和样式,ElMessage 等函数用 unplugin-auto-import 自动导入。主包从 1.1MB 降到 326KB。

**47. Vue3 vs Vue2**
Composition API(逻辑复用、代码组织)、响应式用 Proxy(性能更好、支持新增属性)、更小的运行时、Teleport/Fragments 等。

**48. Pinia vs Vuex**
Pinia 更轻、TS 友好、无 mutation(直接改 state)、模块化天然;Vuex 更重、要写 mutation/模块样板。新项目用 Pinia。

**49. Vue Router 5 守卫**
`router.beforeEach((to) => { ... return true / return {name:'xxx'} })`,直接 return 结果;`next()` 回调已废弃(控制台会警告),我改成了 return 写法。

**50. 组件通信**
props 下行、emit 上行、defineExpose 父拿子实例、provide/inject 跨层级、Pinia 全局状态。本项目 Header 用 Pinia 读登录态,父子用 props/emit。

### H. 测试 / CI / 部署

**51. 单测 / Mockito 原理**
@Mock 生成接口假实现(返回默认值),@InjectMocks 把 mock 注入被测类。测 Service 时 mock Mapper,专注测业务逻辑(状态机流转、库存、计算),不依赖真实数据库。原理:JDK 动态代理/字节码生成代理。

**52. MSW 是什么**
Mock Service Worker:在**网络层**拦截 axios 请求,返回预设的 mock 数据,测试不依赖真实后端、也不污染组件逻辑。setupServer(node 环境用)。

**53. CI 步骤 / 为什么带 MySQL**
后端:compile + test(带 MySQL 服务容器,因为 contextLoads + Flyway 要连库验证完整启动);前端:npm ci → type-check → lint → 单测 → build。每次 push 自动验证。

**54. Docker 多阶段构建**
后端:阶段1 maven 镜像编译打包 → 阶段2 JRE 镜像只拷 jar(镜像小);前端:阶段1 node 构建 dist → 阶段2 nginx 托管静态文件 + 反代 /api。前端要 nginx 是因为要托管静态资源 + 做 SPA fallback + 代理接口。

**55. 数据库初始化**
后端启动 Flyway 自动执行 V1~V3:建表 + 种子数据 + 索引,不需要手动建库建表。

**56. 数据持久化**
docker-compose 挂数据卷(mysql-data / redis-data),容器删了重建数据还在。

**57. 部署更新**
服务器上 `git pull` 拉新代码 → `docker compose up -d --build` 重新构建变更的服务并重启。数据卷不删就不丢数据。

**58. 部署踩过的坑**
npm 镜像源(国内镜像 GitHub 拉不到 → 锁文件改官方源)、npm 版本不一致(npm 11 生成的锁 npm 10 不认 → 用 npm 10 重新生成)、dts 文件缺失导致 CI type-check 失败 → 提交生成文件。

### I. 排查 / 综合

**59. 最难 bug(推荐讲缓存反序列化)**
现象:公告/图书列表接口读缓存报错。定位:看日志发现 `Could not read JSON`,发现是 GenericJackson2JsonRedisSerializer 顶层 List 无类型标注 + PageResult 无无参构造。修复:改 EVERYTHING + WRAPPER_ARRAY、补 @NoArgsConstructor、被缓存方法返回 ArrayList。另一个可讲的:登录 403(CORS IPv6/端口漂移)。

**60. 完整排查流程**
复现(稳定触发)→ 看日志定位异常栈 → 缩小范围(前端/后端/网络/缓存)→ 定位根因 → 修复 → 回归测试(现有单测 + 手工验证)→ 必要时补测试防回归。

**61. 项目最大亮点**
(选一个讲透)建议讲 **Redis 缓存**:从设计(哪些数据、怎么失效、怎么保一致)到实测数字(95% 命中率、40 倍提速),既有方案又有数据。

**62. 再给一周想改进什么**
补 Playwright E2E 测试(浏览器走通下单)、上 HTTPS + 域名、加 Redis 集中式登录限流(多实例可扩展)、监控告警。

**63. 为什么这个技术栈**
主流、招人市场大、我熟悉:后端 Spring Boot + MyBatis-Plus(保留 SQL 控制又少写样板),前端 Vue3 + TS,缓存 Redis。换的话 JPA(如果团队偏好领域模型)。

**64. 做得不好的地方**
初期纯 JS 没类型、代码有死代码;缓存序列化配置没提前测 List 场景;后端测试补得晚。都是复盘后意识到,后面通过 TS/测试/缓存修复补齐了。

**65. 多人协作怎么做**
契约先行:先定 OpenAPI 接口文档(本项目正好用 SpringDoc + openapi-typescript),前端按契约开发;数据库用 Flyway 迁移脚本统一;Git 分支 + PR + code review。

**66. 怎么保证代码质量**
分层清晰、统一 Result/异常处理、19 个后端单测 + 7 个前端单测、GitHub Actions CI 每 push 自动验证、接口文档化。前端 lint(type-check/oxlint)纳入 CI。

**67. 能支撑真实用户吗 / 差距**
单机 MySQL + Redis 能支撑中小量用户。和真实生产差距:无集群/高可用、无限流(登录有限流但接口没有)、无监控告警、无备份恢复、无日志平台。
61. 你的项目最大的亮点是什么?为什么?
62. 如果再给你一周,你想改进什么?(答:补 E2E 测试 / 上 HTTPS / 部署监控)
63. 为什么选这个技术栈?换一个你会选什么?
64. 项目里有没有你觉得自己做得不好的地方?
65. 如果两个人一起开发,数据库和接口会怎么协作?(契约先行/文档)
66. 讲讲你怎么保证代码质量?(测试/CI/代码审查意识)
67. 这个项目能支撑真实用户吗?和真实生产系统差在哪?
