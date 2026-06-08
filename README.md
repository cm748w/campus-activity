### 老铁、请务必修改数据库的密码！

修改点：↓
`backend/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_activity?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root      # 修改为你的MySQL用户名
    password: root      # 修改为你的MySQL密码
```

