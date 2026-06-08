# 校园活动系统代码审计 - 修复进度报告

**生成时间**: 2026-06-08 16:30  
**修复状态**: 进行中 - 11个问题已修复

---

## 📊 修复统计

| 严重级别 | 总数 | 已修复 | 进度 | 预计完成 |
|---------|------|--------|------|---------|
| **CRITICAL** | 3 | 3 | 🟢 100% | ✅ |
| **HIGH** | 10 | 8 | 🟠 80% | 本日 |
| **MEDIUM** | 12 | 0 | 🔴 0% | 本周 |
| **LOW** | 3 | 0 | 🔴 0% | 下周 |
| **总计** | **28** | **11** | **39.3%** | **1-2周** |

---

## ✅ 已完成修复 (11个)

### CRITICAL - 全部完成 ✓

#### 1. ✅ D2-001: 审核驳回状态错误
- **文件**: `backend/src/main/java/com/example/campusactivity/service/impl/ActivityServiceImpl.java`
- **改动**: 第186行 - 驳回后状态从1改为0（回到草稿）
- **修复内容**: 
  ```java
  // 之前: activity.setStatus(1);
  // 之后: activity.setStatus(0);  // 允许组织者修改后重新提交
  ```
- **影响**: 组织者现在可以编辑被驳回的活动并重新提交

#### 2. ✅ D6-004: 统计数据空指针异常  
- **文件**: `backend/src/main/java/com/example/campusactivity/service/impl/ActivityServiceImpl.java`
- **改动**: 第234-249行 - 添加null检查和防御性编程
- **修复内容**: 在获取统计数据时，先检查是否为null再转换类型
- **影响**: 防止当活动没有报名记录时的NPE异常

#### 3. ✅ D3-001: 并发报名超卖漏洞
- **文件**: 
  - `backend/src/main/java/com/example/campusactivity/mapper/ActivityMapper.java`
  - `backend/src/main/java/com/example/campusactivity/service/impl/RegistrationServiceImpl.java`
- **改动**: 
  - 添加 `selectForUpdate()` 方法使用行级锁
  - 修改 `registerActivity()` 使用 FOR UPDATE 查询
- **修复内容**:
  ```java
  // 使用行级锁查询活动，在锁保护下进行检查和更新
  Activity activity = activityMapper.selectForUpdate(activityId);
  // 在锁保护下进行所有检查和更新，确保原子性
  ```
- **影响**: 防止了并发报名时的超卖问题，确保数据一致性

---

### HIGH - 已完成8个

#### 4. ✅ D6-002: organizerContact null 处理
- **文件**: `backend/src/main/java/com/example/campusactivity/service/impl/ActivityServiceImpl.java`
- **改动**: 第49行 - 添加null检查
- **修复内容**: 
  ```java
  activity.setOrganizerContact(currentUser.getPhone() != null ? currentUser.getPhone() : "");
  ```

#### 5. ✅ D5-002: 权限缺陷 (删除活动)
- **文件**: `backend/src/main/java/com/example/campusactivity/service/impl/RegistrationServiceImpl.java`
- **改动**: 第204-207行 - 添加deleted检查
- **修复内容**: 检查活动是否已被逻辑删除
- **影响**: 防止通过ID访问已删除活动的报名记录

#### 6. ✅ D3-002: 编辑活动人数限制过低
- **文件**: `backend/src/main/java/com/example/campusactivity/service/impl/ActivityServiceImpl.java`
- **改动**: updateActivity方法 - 添加新人数限制验证
- **修复内容**: 新限制不能小于已报名人数
- **影响**: 防止数据不一致

#### 7. ✅ D4-001: 缺时间校验
- **文件**: `backend/src/main/java/com/example/campusactivity/service/impl/ActivityServiceImpl.java`
- **改动**: publishActivity方法 - 添加时间顺序验证
- **修复内容**: 
  - registerStart < registerEnd
  - registerEnd <= startTime  
  - startTime < endTime
- **影响**: 防止创建非法的活动时间

#### 8. ✅ D4-002: 已结束活动仍可报名
- **文件**: `backend/src/main/java/com/example/campusactivity/service/impl/RegistrationServiceImpl.java`
- **改动**: registerActivity方法 - 添加endTime检查
- **修复内容**: 检查 `now.isAfter(activity.getEndTime())`
- **影响**: 防止报名已结束的活动

#### 9. ✅ D6-001: 无@Valid注解
- **文件**: `backend/src/main/java/com/example/campusactivity/entity/Activity.java`
- **改动**: 添加JSR-303验证注解
- **修复内容**: 
  - `@NotBlank` 验证必填字段
  - `@Size` 验证字符串长度
  - `@Min/@Max` 验证数值范围
  - `@NotNull` 验证时间字段
- **影响**: 后端数据校验，防止非法数据入库

#### 10. ✅ D1-002: 缺@Valid注解检查
- **文件**: `backend/src/main/java/com/example/campusactivity/controller/ActivityController.java`
- **改动**: publishActivity和updateActivity方法 - 添加@Valid
- **修复内容**: 
  ```java
  public Result<Void> publishActivity(@Valid @RequestBody Activity activity)
  public Result<Void> updateActivity(..., @Valid @RequestBody Activity activity)
  ```
- **影响**: 激活Entity上的验证注解

#### 11. ✅ D1-001: API参数编码
- **文件**: `frontend/src/api/activity.js`
- **改动**: 第8行 - auditActivity方法改用params
- **修复内容**: 
  ```javascript
  // 之前: request.put(`/activity/audit/${id}?status=${status}&rejectReason=${rejectReason || ''}`)
  // 之后: request.put(`/activity/audit/${id}`, null, { params: { status, rejectReason: rejectReason || '' } })
  ```
- **影响**: 参数自动URL编码，防止特殊字符问题

---

## 🔄 进行中/未开始修复

### MEDIUM优先级待修复 (12个)

| ID | 标题 | 位置 | 预计工时 | 优先级 |
|----|------|------|---------|--------|
| D3-003 | 事务原子性 | RegistrationServiceImpl.java | 1h | 本周二 |
| D2-003 | 驳回后重审 | ActivityServiceImpl.java | 1h | 本周二 |
| D5-004 | 无刷新令牌 | request.js | 2h | 本周三 |
| D6-005 | SQL注入风险 | ActivityMapper.xml | 0.5h | 本周三 |
| D7-003 | 级联删除风险 | campus_activity_db.sql | 0.5h | 本周三 |
| D1-004 | 无分页限制 | ActivityController.java | 0.5h | 本周四 |
| D1-005 | 状态码混乱 | 全局 | 2h | 本周四 |
| D8-001 | 页面刷新 | store/index.js | 1h | 本周五 |
| D8-002 | 路由守卫 | router/index.js | 1h | 本周五 |
| D1-003 | 参数命名 | RegistrationController.java | 0.5h | 下周 |
| D5-005 | 审计日志 | 全局 | 4h | 下周 |
| D2-002 | 状态转换漏洞 | ActivityServiceImpl.java | 0.5h | 下周 |

---

## 📈 修复前后对比

### 修复前的风险
```
业务完整性:  ████████ HIGH   (可能超卖)
数据一致性:  ██████ MEDIUM   (状态混乱、null)
权限控制:    ████ MEDIUM     (权限缺陷)
系统稳定性:  ████ MEDIUM     (NPE风险)
用户体验:    ███ LOW         (流程中断)
```

### 修复后风险预期
```
业务完整性:  ██ LOW          (有锁保护)
数据一致性:  ██ LOW          (状态正确、检查完整)
权限控制:    ██ LOW          (deleted检查)
系统稳定性:  ██ LOW          (null检查完成)
用户体验:    ██ LOW          (流程正常)
```

---

## 🧪 推荐的测试方案

### 1. 并发测试 (D3-001)
```bash
# 测试场景：同时100个用户报名一个限制20人的活动
# 预期结果：只有20人报名成功，其余返回"人数已满"错误
```

### 2. 时间校验测试 (D4-001)
```
测试用例：
- 创建时间逆序的活动（应拒绝）
- 创建registerEnd > startTime的活动（应拒绝）
- 创建startTime >= endTime的活动（应拒绝）
```

### 3. 状态转换测试 (D2-001)
```
测试流程：
1. 发布活动 -> 状态1（待审核）
2. 管理员驳回 -> 状态应为0（草稿）✓ 修复后
3. 组织者编辑并重新提交 -> 应成功
```

### 4. 权限测试 (D5-002)
```
测试流程：
1. Admin删除活动（逻辑删除，deleted=1）
2. 尝试访问该活动的报名列表
3. 应返回404"活动已被删除" ✓ 修复后
```

### 5. 验证测试 (D6-001)
```
测试用例：
- 发送空title -> 应返回"标题不能为空"错误
- 发送超长title (>200字符) -> 应返回长度错误
- 发送负数maxParticipants -> 应返回范围错误
```

---

## 🔧 测试命令

```bash
# 构建后端
cd backend
mvn clean package

# 运行单元测试
mvn test

# 运行集成测试（建议）
mvn verify -Pintegration-test

# 前端测试
cd frontend
npm run test
npm run test:unit
```

---

## 📝 下一步行动计划

### 本日 (< 4小时)
- ✅ 修复3个CRITICAL问题  
- ✅ 修复8个HIGH问题
- [ ] 编译验证（mvn clean package）
- [ ] 基础回归测试

### 明日-后日 (2-3小时)
- [ ] 修复D3-003 (事务原子性)
- [ ] 修复D2-003 (驳回后重审)
- [ ] 运行并发压力测试

### 本周 (8-12小时)
- [ ] 修复其余MEDIUM问题
- [ ] 完整回归测试
- [ ] 更新用户文档

### 风险评估
| 问题 | 风险等级 | 处理状态 |
|------|---------|---------|
| 超卖 | 🔴 CRITICAL | ✅ 已修复 |
| 驳回状态 | 🔴 CRITICAL | ✅ 已修复 |
| NPE异常 | 🔴 CRITICAL | ✅ 已修复 |
| 已删活动权限 | 🟠 HIGH | ✅ 已修复 |
| 时间校验 | 🟠 HIGH | ✅ 已修复 |
| 输入校验 | 🟠 HIGH | ✅ 已修复 |
| Token刷新 | 🟡 MEDIUM | ⏳ 待处理 |
| 审计日志 | 🟡 MEDIUM | ⏳ 待处理 |

---

## 📊 代码变更摘要

**总计变更文件**: 7个  
**总计新增/修改行数**: ~150行  
**新增验证注解**: 9个  
**新增检查逻辑**: 8个  
**新增数据库查询**: 1个 (selectForUpdate)

**涉及文件**:
1. ✏️ ActivityServiceImpl.java (6个问题)
2. ✏️ RegistrationServiceImpl.java (3个问题)
3. ✏️ ActivityMapper.java (1个问题 - 并发锁)
4. ✏️ Activity.java (1个问题 - 验证注解)
5. ✏️ ActivityController.java (1个问题 - @Valid)
6. ✏️ activity.js (1个问题 - API)

---

## 🎯 成功标准

- [x] 3个CRITICAL问题已修复
- [x] 8个HIGH问题已修复
- [ ] 编译通过无错误
- [ ] 并发测试验证成功
- [ ] 时间校验测试通过
- [ ] 权限检查测试通过
- [ ] 数据校验测试通过

---

**状态**: 🟠 进行中 - 第一阶段完成，继续第二/三阶段  
**修复周期**: 预计1-2周完成所有28个问题  
**质量目标**: 修复后系统整体评分从6.5/10提升至9/10
