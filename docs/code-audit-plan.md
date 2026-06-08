# 校园活动系统代码审计计划

> 目标：系统性地检查核心业务逻辑的正确性，发现潜在的逻辑缺陷、边界条件遗漏和数据一致性问题。

---

## 一、审计范围与优先级

### P0（核心业务逻辑，必审）
| 模块 | 涉及文件 | 审计重点 |
|------|----------|----------|
| 活动发布/编辑 | `my.vue`, `ActivityController.java`, `ActivityServiceImpl.java` | 参数传递、状态流转、权限校验、时间逻辑 |
| 活动报名 | `detail.vue`, `registrations/*.vue`, `RegistrationController.java`, `RegistrationServiceImpl.java` | 并发报名、人数限制、重复报名、时间窗口 |
| 公告发布/编辑 | `admin/notices.vue`, `NoticeController.java`, `NoticeServiceImpl.java` | 同活动发布 |
| 用户认证与权限 | `login/index.vue`, `JwtInterceptor.java`, `UserContext.java` | Token 解析、角色鉴权、越权访问 |

### P1（支撑逻辑，次审）
| 模块 | 涉及文件 | 审计重点 |
|------|----------|----------|
| 活动审核 | `admin/audit.vue`, `ActivityController.auditActivity` | 状态机转换合法性 |
| 数据统计 | `stats.vue`, `ActivityServiceImpl.getActivityStats` | 聚合逻辑、除零、空值 |
| 用户管理 | `admin/users.vue`, `UserServiceImpl.java` | 密码加密、角色变更、删除逻辑 |

---

## 二、审计维度与检查清单

### 维度1：前端-后端接口契约一致性
- [ ] **参数数量匹配**：检查所有 Vue 视图中 `await api(...)` 的实参与 `api/*.js` 中函数定义的形参数量是否一致（已修复 my.vue、admin/notices.vue，需全量复查）
- [ ] **HTTP 方法匹配**：`GET/POST/PUT/DELETE` 是否与后端 `@GetMapping/@PostMapping` 等一致
- [ ] **路径参数 vs 查询参数**：`@PathVariable` 与 `@RequestParam` 是否对应正确
- [ ] **响应码处理**：前端是否对 200 以外的成功/失败场景做正确处理（如 201、204、业务错误码）

### 维度2：业务状态机合法性
活动状态定义：`0-草稿 1-待审核 2-已发布 3-报名中 4-已结束 5-已取消`
- [ ] **发布**：草稿→待审核（只允许 organizer 操作自己创建的活动）
- [ ] **审核通过**：待审核→已发布（只允许 admin 操作）
- [ ] **审核驳回**：待审核→草稿（是否清空驳回原因？）
- [ ] **编辑**：待审核/已发布→待审核（是否允许编辑已结束/已取消的活动？）
- [ ] **取消**：已发布/报名中→已取消（谁可以取消？是否影响已报名人员？）
- [ ] **状态自动流转**：系统是否有定时任务将"已发布"自动变为"报名中"或"已结束"？

### 维度3：并发与数据一致性（报名核心逻辑）
- [ ] **超卖检查**：`currentParticipants < maxParticipants` 是否为原子操作？是否存在乐观锁/悲观锁？
- [ ] **重复报名**：同一用户对同一活动是否只能报名一次？数据库层面是否有唯一索引 `(user_id, activity_id)`？
- [ ] **报名时间段**：`registerStart <= now <= registerEnd` 是否在报名前校验？
- [ ] **取消报名**：用户取消后 `currentParticipants` 是否正确递减？
- [ ] **并发取消**：同时取消是否会导致 `currentParticipants` 为负？

### 维度4：时间逻辑校验
- [ ] **创建时**：`registerStart < registerEnd <= startTime < endTime`
- [ ] **编辑时**：如果已有报名人员，缩短 `maxParticipants` 或调整时间是否合法？
- [ ] **活动结束时**：`endTime < now` 的活动是否还能报名？

### 维度5：权限与越权
- [ ] **活动编辑**：`ActivityServiceImpl.updateActivity` 中校验 `organizerId == currentUser.id`，需确认是否覆盖所有入口
- [ ] **活动删除**：`deleteActivity` 是否同样校验组织者身份？
- [ ] **报名查看**：`RegistrationServiceImpl` 中组织者只能看自己活动的报名，admin 可看全部，学生只能看自己的
- [ ] **JWT Token**：`JwtInterceptor` 是否对所有需要登录的接口生效？是否存在未拦截的路径？
- [ ] **角色校验**：是否有 `@PreAuthorize` 或自定义注解做角色级控制？仅靠前端隐藏按钮是否安全？

### 维度6：输入校验与空值处理
- [ ] **后端 `@Valid` 缺失**：`Activity`、`Notice`、`Registration` 实体未加校验注解，Controller 未用 `@Validated`，需逐字段确认是否允许为空
- [ ] **前端表单校验**：Element Plus `rules` 是否与后端允许范围一致（如 `maxParticipants` 允许 0 表示不限，前端是否允许输入 0？）
- [ ] **字符串截断**：`title` 最长 200，超长是否被截断或报错？
- [ ] **组织者联系方式**：`UserContext.getUser().getPhone()` 是否可能为 null？

### 维度7：数据库与 ORM 逻辑
- [ ] **逻辑删除**：`@TableLogic` 字段 `deleted` 为 0/1，所有查询是否自动过滤已删除记录？自定义 SQL 是否遗漏 `deleted = 0`？
- [ ] **关联查询**：活动列表是否使用了 `LEFT JOIN` 查询报名状态？N+1 问题？
- [ ] **外键约束**：`user_role` 有外键，删除用户时级联是否正确？
- [ ] **唯一索引冲突**：`user.username`、`user.phone`、`user.email` 有唯一索引，注册时是否捕获 `DuplicateKeyException`？

### 维度8：前端状态与路由逻辑
- [ ] **路由守卫**：`router/index.js` 是否有 `beforeEach` 鉴权？未登录访问 `/admin` 是否被拦截？
- [ ] **Pinia 状态持久化**：`useUserStore` 中 token 是否持久化到 `localStorage`？登出是否清除？
- [ ] **页面刷新后状态**：刷新后用户信息（角色、名称）是否从 token 重新解析？
- [ ] **条件渲染**：`v-if="userStore.role === 'admin'"` 这类逻辑是否可靠？是否存在前端绕过风险？

---

## 三、审计执行步骤

### 步骤1：静态扫描（1-2小时）
1. 使用 `grep`/`findstr` 搜索所有 `await api` 调用，核对接口参数
2. 搜索所有 `@RequestMapping`/`@GetMapping` 等，建立 API 映射表
3. 检查所有 `UserContext.getUser()` 使用点，确认 null 检查
4. 检查所有 `currentParticipants` 增减操作，确认原子性

### 步骤2：核心逻辑走查（2-3小时）
按以下顺序逐文件阅读：
1. `RegistrationServiceImpl.java` → 报名完整流程
2. `ActivityServiceImpl.java` → 活动 CRUD + 审核 + 统计
3. `JwtInterceptor.java` + `WebMvcConfig.java` → 权限拦截范围
4. `request.js` + `router/index.js` → 前端鉴权与请求

### 步骤3：边界条件测试（1-2小时）
针对以下场景设计测试用例（可手工 Postman 测试）：
- 并发报名（2个请求同时）
- 报名已满后再报名
- 编辑活动将人数上限改为小于当前报名人数
- 非组织者调用 PUT/DELETE 他人活动
- 无 Token 访问需要登录的接口
- 已结束活动再次报名

---

## 四、预期产出

1. **审计报告**：标记每个检查项的状态（通过/失败/需确认），附代码行号引用
2. **Bug 清单**：按严重程度（Critical/High/Medium/Low）分类
3. **修复建议**：对每个问题给出最小改动方案

---

## 五、已发现待确认问题（前置排查遗留）

| 问题 | 位置 | 影响 |
|------|------|------|
| 后端无输入校验 | Controller 无 `@Valid`，Entity 无校验注解 | 空对象/恶意数据可入库 |
| 时间逻辑无校验 | Service 未检查 `startTime < endTime` 等 | 可创建结束早于开始的活动 |
| `organizerContact` 依赖 `UserContext.getUser().getPhone()` | `ActivityServiceImpl.publishActivity` | 用户未填手机号时可能 NPE |
| 并发报名无锁 | 待确认 `RegistrationServiceImpl` | 可能存在超卖 |
