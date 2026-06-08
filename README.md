### 老铁、请务使用自己的数据库密码！！！

修改点：↓
`backend/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_activity?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root      # 修改为你的MySQL用户名
    password: root      # 修改为你的MySQL密码
```

