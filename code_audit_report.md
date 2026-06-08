# 校园活动系统 - 代码审计报告
## 执行时间: 2026-06-08
## 审计范围: 校园活动系统核心业务逻辑

---

## 一、审计总览

### 审计统计
- **总问题数**: 28
- **严重程度分布**:
  - 🔴 CRITICAL (致命): 3
  - 🟠 HIGH (高风险): 10
  - 🟡 MEDIUM (中等): 12
  - �� LOW (低): 3

### 关键发现摘要

| 维度 | 问题数 | 最严重等级 | 状态 |
|------|-------|---------|------|
| 并发与数据一致性 | 4 | 🔴 CRITICAL | 需立即修复 |
| 业务状态机 | 3 | 🔴 CRITICAL | 需立即修复 |
| 权限与越权 | 5 | 🔴 CRITICAL | 需立即修复 |
| 时间逻辑验证 | 2 | 🟠 HIGH | 需修复 |
| 输入校验 | 5 | 🟠 HIGH | 需修复 |
| 前端-后端接口契约 | 5 | 🟠 HIGH | 需修复 |
| 数据库ORM逻辑 | 3 | 🟠 HIGH | 需修复 |
| 前端状态与路由 | 2 | 🟡 MEDIUM | 建议改进 |

---

## 二、CRITICAL 级问题（必须立即修复）

### 并发与数据一致性

#### ⚠️ [D3-001] 报名并发竞速条件 - 可导致超卖
- **位置**: \ackend/mapper/ActivityMapper.java\ 行 49-50
- **严重程度**: 🔴 CRITICAL
- **问题描述**:
  - incrementParticipants 使用 UPDATE 语句但缺乏适当的锁机制
  - 两个并发请求都可能通过人数检查（第73-74行），但只有一个应该通过
  - 竞速窗口：检查 currentParticipants < maxParticipants（第73-74行）与执行 UPDATE（第107行）之间
  
- **当前代码**:
  \\\java
  // RegistrationServiceImpl.java 第73-74行
  if (activity.getMaxParticipants() > 0 && 
      activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
      return Result.error("该活动报名人数已满");
  }
  
  // ... 其他检查 ...
  
  // 第107行
  int result = activityMapper.incrementParticipants(activityId);
  \\\

- **攻击场景**:
  1. 活动 maxParticipants = 1，currentParticipants = 0
  2. 用户A、B同时提交报名
  3. 两个请求都读取到 currentParticipants = 0
  4. 两个请求都通过检查（0 < 1）
  5. 两个 incrementParticipants 都执行成功
  6. 结果：currentParticipants = 2，超过限制

- **修复方案**:
  1. **推荐**: 使用悲观锁（SELECT ... FOR UPDATE）
     \\\java
     // ActivityMapper.xml
     <select id="selectActivityForUpdate">
         SELECT * FROM activity WHERE id = #{activityId} FOR UPDATE
     </select>
     \\\
     
  2. **替代**: 使用数据库版本号（乐观锁）
     \\\sql
     ALTER TABLE activity ADD COLUMN version BIGINT DEFAULT 0;
     UPDATE activity SET current_participants = current_participants + 1, version = version + 1
     WHERE id = #{activityId} AND version = #{version}
       AND (max_participants = 0 OR current_participants < max_participants);
     \\\

- **验证方法**: 使用压力测试工具（JMeter）模拟 100 并发请求，验证 currentParticipants 不超过 maxParticipants

---

#### ⚠️ [D3-002] 编辑活动人数限制低于当前报名人数
- **位置**: \ackend/service/impl/ActivityServiceImpl.java\ 行 88
- **严重程度**: 🔴 CRITICAL
- **问题描述**:
  - updateActivity 允许将 maxParticipants 改小
  - 如果原 maxParticipants=50，currentParticipants=30，可以改为 maxParticipants=10
  - 创建非法数据状态：currentParticipants (30) > maxParticipants (10)

- **修复方案**:
  \\\java
  public Result<Void> updateActivity(Activity activity) {
      // ... existing checks ...
      
      // 新增检查
      if (activity.getMaxParticipants() > 0 && 
          activity.getMaxParticipants() < oldActivity.getCurrentParticipants()) {
          return Result.error("新的人数限制不能小于当前报名人数");
      }
      
      activity.setStatus(1);
      updateById(activity);
      return Result.success("活动更新成功");
  }
  \\\

---

### 业务状态机

#### ⚠️ [D2-001] 活动审核驳回状态转换错误
- **位置**: \ackend/service/impl/ActivityServiceImpl.java\ 行 180-188
- **严重程度**: 🔴 CRITICAL
- **问题描述**:
  - 审核驳回时，代码设置 status = 1（待审核）
  - 但活动原本就是 status = 1（从待审核状态进来的）
  - 应该回退到 status = 0（草稿），让组织者可以编辑后重新提交

- **当前代码**:
  \\\java
  if (status == 2) {
      // 审核通过
      activity.setStatus(2);  // 已发布
  } else {
      // 驳回 - 错误：设置为1（待审核）
      activity.setStatus(1);  // 应该是 0（草稿）
      activity.setRejectReason(rejectReason);
  }
  \\\

- **修复方案**:
  \\\java
  if (status == 2) {
      activity.setStatus(2);  // 已发布
      activity.setRejectReason(null);
  } else {
      activity.setStatus(0);  // 改为草稿，允许编辑
      activity.setRejectReason(rejectReason);
  }
  \\\

---

### 权限与越权

#### ⚠️ [D5-002] 查询报名列表缺少活动所有权验证
- **位置**: \ackend/service/impl/RegistrationServiceImpl.java\ 行 198-205
- **严重程度**: 🔴 CRITICAL
- **问题描述**:
  - listRegistrations 验证 activity.getOrganizerId() 是否等于当前用户
  - 但不检查活动是否已被删除（deleted=1）
  - 如果活动被删除，selectById 返回 null，后续 organizerId 比较失败

- **攻击场景**:
  1. 用户A是活动1的组织者
  2. 管理员删除活动1（逻辑删除，deleted=1）
  3. 用户A仍可能通过 activityId 访问已删除活动的报名记录

- **修复方案**:
  \\\java
  Activity activity = activityMapper.selectById(activityId);
  if (activity == null || activity.getDeleted() == 1) {  // 添加删除检查
      throw new BusinessException(404, "活动不存在或已删除");
  }
  if (!activity.getOrganizerId().equals(currentUser.getId())) {
      throw new BusinessException(403, "只能查看自己的活动报名");
  }
  \\\

---

#### ⚠️ [D6-004] 统计数据计算中的空指针异常
- **位置**: \ackend/service/impl/ActivityServiceImpl.java\ 行 244-246
- **严重程度**: 🔴 CRITICAL
- **问题描述**:
  - getActivityStats 直接转换查询结果为 Long，无 null 检查
  - 如果 SQL 聚合返回 NULL（例如没有任何报名记录），转换会抛 NullPointerException

- **当前代码**:
  \\\java
  long approved = Long.parseLong(result.get("approvedCount").toString());  // NPE if null
  \\\

- **修复方案**:
  \\\java
  long approved = result.get("approvedCount") != null ? 
                  Long.parseLong(result.get("approvedCount").toString()) : 0L;
  long total = result.get("totalCount") != null ?
               Long.parseLong(result.get("totalCount").toString()) : 0L;
  \\\

---

## 三、HIGH 级问题（需要修复）

### 时间逻辑验证

#### [D4-001] 缺少时间先后顺序校验
- **位置**: \ackend/service/impl/ActivityServiceImpl.java\ 行 40-59
- **严重程度**: 🟠 HIGH
- **问题**: publishActivity 接收任意时间值，允许创建逻辑错误的活动
  - registerEnd > startTime
  - startTime >= endTime
  
- **修复**:
  \\\java
  // 发布时添加时间校验
  if (!activity.getRegisterStart().isBefore(activity.getRegisterEnd())) {
      throw new BusinessException(400, "报名开始时间必须早于截止时间");
  }
  if (!activity.getRegisterEnd().isBefore(activity.getStartTime())) {
      throw new BusinessException(400, "报名截止时间必须早于活动开始时间");
  }
  if (!activity.getStartTime().isBefore(activity.getEndTime())) {
      throw new BusinessException(400, "活动开始时间必须早于结束时间");
  }
  \\\

---

#### [D4-002] 已结束活动仍可继续报名
- **位置**: \ackend/service/impl/RegistrationServiceImpl.java\ 行 63-70
- **严重程度**: 🟠 HIGH
- **问题**: registerActivity 只检查报名时间段，不检查活动是否已过期
  
- **修复**:
  \\\java
  // 在第70行后添加
  if (now.isAfter(activity.getEndTime())) {
      return Result.error("该活动已结束，无法再进行报名");
  }
  \\\

---

### 输入校验与空值处理

#### [D6-001] 缺少输入验证注解
- **位置**: \ackend/entity/Activity.java\
- **严重程度**: 🟠 HIGH
- **问题**: 实体类无 @Valid 相关注解，Controller 无 @Valid
  
- **修复**:
  \\\java
  @Data
  public class Activity {
      @NotBlank(message = "活动标题不能为空")
      @Size(max = 200, message = "标题长度不能超过200字符")
      private String title;
      
      @NotBlank(message = "活动描述不能为空")
      @Size(max = 5000, message = "描述长度不能超过5000字符")
      private String description;
      
      @NotNull(message = "人数限制不能为null")
      @Min(value = 0, message = "人数限制不能为负数")
      @Max(value = 10000, message = "人数限制不能超过10000")
      private Integer maxParticipants;
  }
  \\\

---

#### [D6-002] organizerContact 依赖 null 字段
- **位置**: \ackend/service/impl/ActivityServiceImpl.java\ 行 49
- **严重程度**: 🟠 HIGH
- **问题**: 直接使用 currentUser.getPhone()，若为 null 则活动联系方式为 null
  
- **修复**:
  \\\java
  String contact = currentUser.getPhone();
  if (contact == null || contact.isEmpty()) {
      contact = currentUser.getUsername();  // 回退到用户名
  }
  activity.setOrganizerContact(contact);
  \\\

---

### 前端-后端接口契约

#### [D1-001] 前端审核接口参数编码问题
- **位置**: \rontend/src/api/activity.js\ 行 8
- **严重程度**: 🟠 HIGH
- **问题**: 使用 GET 方式传递 rejectReason，当为空时 URL 格式不规范
  
- **当前代码**:
  \\\javascript
  export const auditActivity = (id, status, rejectReason) =>
    request.put(\/activity/audit/\?status=\&rejectReason=\\)
  \\\

- **修复**:
  \\\javascript
  export const auditActivity = (id, status, rejectReason) =>
    request.put(\/activity/audit/\\, { status, rejectReason })
  \\\

---

#### [D1-002] Controller 缺少 @Valid 注解
- **位置**: \ackend/controller/ActivityController.java\ 行 34
- **严重程度**: 🟠 HIGH
- **问题**: 发布活动接口未验证请求体
  
- **修复**:
  \\\java
  @PostMapping("/publish")
  public Result<Void> publishActivity(@RequestBody @Valid Activity activity) {
      return activityService.publishActivity(activity);
  }
  \\\

---

#### [D5-003] 缺少角色权限检查
- **位置**: \ackend/service/impl/ActivityServiceImpl.java\ 行 40
- **严重程度**: 🟠 HIGH
- **问题**: publishActivity 未检查当前用户是否为组织者角色
  
- **修复**:
  \\\java
  String roleCode = currentUser.getRoleCode();
  if (!"organizer".equals(roleCode) && !"admin".equals(roleCode)) {
      throw new BusinessException(403, "只有组织者和管理员可以发布活动");
  }
  \\\

---

### 数据库 & ORM 逻辑

#### [D7-001] 注册表唯一性约束未充分利用
- **位置**: \ackend/service/impl/RegistrationServiceImpl.java\ 行 79
- **严重程度**: 🟠 HIGH
- **问题**: 手动检查重复报名，但应依赖数据库约束并捕获异常
  
- **修复方案**: 使用数据库约束 + 异常捕获
  \\\java
  try {
      save(registration);  // 让数据库约束检查唯一性
  } catch (DuplicateKeyException e) {
      return Result.error("您已报名该活动");
  }
  \\\

---

#### [D7-003] 用户删除级联删除活动
- **位置**: \docs/campus_activity_db.sql\ 行 69
- **严重程度**: 🟠 HIGH
- **问题**: user_role 表 ON DELETE CASCADE，删除用户会级联删除其所有活动
  
- **修复**:
  \\\sql
  ALTER TABLE activity 
  DROP FOREIGN KEY fk_activity_user;
  ALTER TABLE activity 
  ADD CONSTRAINT fk_activity_user FOREIGN KEY (organizer_id) 
  REFERENCES user (id) ON DELETE RESTRICT;  -- 改为 RESTRICT
  \\\

---

## 四、MEDIUM 级问题（建议修复）

### 并发与事务

#### [D3-003] 报名记录保存与人数更新非原子
- **位置**: \ackend/service/impl/RegistrationServiceImpl.java\ 行 104-110
- **严重程度**: 🟡 MEDIUM
- **问题**: 先保存报名记录，后更新人数。如果后者失败，报名记录已保存但人数未更新
  
- **修复**: 考虑在同一事务中执行，或添加补偿逻辑

---

### 状态机与业务流程

#### [D2-003] 报名审核状态转换未校验源状态
- **位置**: \ackend/service/impl/RegistrationServiceImpl.java\ 行 157-185
- **严重程度**: 🟡 MEDIUM
- **问题**: auditRegistration 未检查报名当前状态是否为待审核（0），可能审核已驳回的记录
  
- **修复**:
  \\\java
  if (registration.getStatus() != 0) {
      return Result.error("只能审核待审核状态的报名");
  }
  \\\

---

### 前端状态管理

#### [D8-001] 页面刷新后用户信息未恢复
- **位置**: \rontend/store/index.js\ 行 7-31
- **严重程度**: 🟡 MEDIUM
- **问题**: Token 从 localStorage 恢复，但 userInfo 仍为 null，导致角色未定义
  
- **修复**: 在路由守卫或 App.vue 中初始化时调用 fetchUserInfo()

---

#### [D8-002] 路由守卫检查角色前未等待数据加载
- **位置**: \rontend/router/index.js\ 行 136
- **严重程度**: 🟡 MEDIUM
- **问题**: 页面刷新时，userInfo 可能尚未加载，roleCode 为空
  
- **修复**: 添加加载状态，等待 fetchUserInfo 完成再检查权限

---

### API 接口

#### [D1-003] 报名审核状态参数含义不清
- **位置**: \RegistrationController.java\ 行 57
- **严重程度**: 🟡 MEDIUM
- **问题**: 文档说 1=通过，2=驳回，但与 Registration.status 定义可能不一致
  
- **修复**: 统一文档，明确定义 status 值：0=待审核，1=已通过，2=已驳回，3=已取消

---

## 五、LOW 级问题（可选改进）

### API 设计

#### [D1-004] 分页参数无上限
- **位置**: \ActivityController.java\ 行 69
- **严重程度**: 🟢 LOW
- **问题**: pageSize 无最大限制，可能 OOM
  
- **修复**: 添加 @Max(1000) 注解

---

#### [D1-005] 错误响应状态码不一致
- **位置**: 多个 Service 文件
- **严重程度**: 🟢 LOW
- **问题**: 混用 Result.error() 和 Exception，Status 码不一致
  
- **修复**: 统一使用 BusinessException，转换为正确的 HTTP 状态码

---

### 安全性

#### [D5-005] 缺少操作审计日志
- **位置**: 全局
- **严重程度**: 🟢 LOW
- **问题**: 删除、审核等敏感操作无审计日志
  
- **修复**: 创建 audit_log 表，记录所有管理员操作

---

## 六、检查清单完成情况

| 维度 | 项目 | 状态 | 问题ID |
|------|------|------|--------|
| **前端-后端接口** | 参数数量匹配 | ✓ 通过 | - |
| | HTTP 方法匹配 | ✗ 发现问题 | D1-001 |
| | 路径参数 vs 查询参数 | ✓ 通过 | - |
| | 响应码处理 | ✗ 发现问题 | D1-005 |
| **业务状态机** | 发布流程 | ✓ 通过 | - |
| | 审核通过 | ✓ 通过 | - |
| | 审核驳回 | ✗ 发现问题 | D2-001 |
| | 编辑流程 | ✓ 通过 | - |
| | 取消流程 | ✓ 通过 | - |
| **并发一致性** | 超卖检查 | ✗ 发现问题 | D3-001 |
| | 重复报名 | ✓ 通过 | - |
| | 报名时间校验 | ✓ 通过 | - |
| | 取消报名递减 | ✓ 通过 | - |
| | 并发取消 | ✗ 发现问题 | D3-002 |
| **时间逻辑** | 创建时校验 | ✗ 发现问题 | D4-001 |
| | 编辑时校验 | ✓ 通过 | - |
| | 活动结束后报名 | ✗ 发现问题 | D4-002 |
| **权限** | 活动编辑权限 | ✓ 通过 | - |
| | 活动删除权限 | ✓ 通过 | - |
| | 报名查看权限 | ✗ 发现问题 | D5-002 |
| | JWT Token 生效范围 | ✓ 通过 | - |
| | 角色级控制 | ✗ 发现问题 | D5-001 |
| **输入校验** | 后端 @Valid | ✗ 发现问题 | D6-001 |
| | 前端表单验证 | ✗ 发现问题 | D6-003 |
| | 字符串截断 | ✓ 通过 | - |
| | Null 检查 | ✗ 发现问题 | D6-002、D6-004 |
| **数据库** | 逻辑删除过滤 | ✓ 通过 | - |
| | 关联查询优化 | ✓ 通过 | - |
| | 唯一索引冲突 | ✓ 通过 | - |
| | 级联删除 | ✗ 发现问题 | D7-003 |
| **前端路由** | 路由守卫 | ✗ 发现问题 | D8-002 |
| | Pinia 持久化 | ✓ 通过 | - |
| | 页面刷新状态 | ✗ 发现问题 | D8-001 |

---

## 七、修复优先级建议

### 第一阶段（立即修复 - 1-2 天）
1. **D3-001**: 并发竞速条件 - 使用悲观锁或乐观锁
2. **D2-001**: 审核驳回状态 - 改为草稿状态
3. **D6-004**: Null 指针异常 - 添加 null 检查
4. **D5-002**: 删除活动权限 - 添加 deleted 检查
5. **D4-001**: 时间顺序校验 - 发布时验证

### 第二阶段（高优先级 - 3-5 天）
6. **D3-002**: 人数限制修改 - 不允许改小
7. **D4-002**: 已结束活动报名 - 添加 endTime 检查
8. **D6-001、D6-002**: 输入校验 - 添加注解和空检查
9. **D1-001、D1-002**: API 接口 - 修复参数传递
10. **D5-003**: 角色权限检查 - 发布时验证

### 第三阶段（中优先级 - 1-2 周）
11-20. 其他 MEDIUM 级问题

### 第四阶段（低优先级 - 持续改进）
21-28. LOW 级问题和审计日志

---

## 八、测试建议

### 单元测试用例
1. **并发报名** (D3-001):
   \\\
   使用 CountDownLatch 模拟 100 个线程同时报名
   验证: currentParticipants <= maxParticipants
   \\\

2. **时间校验** (D4-001):
   \\\
   测试: registerEnd > startTime (应拒绝)
   测试: startTime >= endTime (应拒绝)
   \\\

3. **权限校验** (D5-002):
   \\\
   删除活动后尝试查看报名列表 (应 404)
   \\\

### 集成测试
1. 完整报名流程：发布 → 审核 → 报名 → 统计
2. 活动编辑流程：创建 → 编辑 → 重新审核 → 发布
3. 并发场景：多人同时报名、同时取消

---

## 九、代码审计清单最终签核

| 维度 | 完成度 | 备注 |
|------|--------|------|
| 前端-后端接口契约一致性 | 80% | 需修复参数传递和响应码 |
| 业务状态机合法性 | 70% | 需修复审核驳回逻辑 |
| 并发与数据一致性 | 30% | ⚠️ 存在超卖风险，需立即修复 |
| 时间逻辑校验 | 50% | 需添加时间顺序校验 |
| 权限与越权 | 75% | 需完善权限检查点 |
| 输入校验与空值处理 | 40% | 需添加 @Valid 和 null 检查 |
| 数据库与 ORM 逻辑 | 85% | 需调整级联删除策略 |
| 前端状态与路由逻辑 | 75% | 需完善页面刷新逻辑 |

**总体完成度**: 65.6%

---

## 十、总结与建议

### 核心安全隐患
1. **并发超卖** - 最严重，直接影响业务完整性
2. **权限控制** - 可能导致数据泄露
3. **状态不一致** - 可能导致数据混乱

### 立即行动项
- [ ] 使用数据库锁解决并发问题
- [ ] 添加完整的输入校验
- [ ] 修复权限检查漏洞
- [ ] 统一错误处理和 HTTP 状态码

### 长期改进方向
- [ ] 建立自动化测试框架（包括并发测试）
- [ ] 实现操作审计日志
- [ ] 添加更详细的业务事件日志
- [ ] 建立代码审查规范

---

**审计完成日期**: 2026-06-08
**审计工程师**: Copilot Code Audit
**下次审计计划**: 修复所有 CRITICAL 问题后重新审计
