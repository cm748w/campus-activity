package com.example.campusactivity.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 3 接口文档配置
 * 使用Knife4j提供美观的API文档界面
 * 访问地址：http://localhost:8080/api/doc.html
 */
@Configuration
public class SwaggerConfig {

    /**
     * 配置OpenAPI基本信息
     * 包含API标题、版本、描述、联系方式等
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("校园活动报名系统 API文档")
                        .version("1.0.0")
                        .description("基于Spring Boot + Vue3的校园活动报名系统后端接口文档")
                        .contact(new Contact()
                                .name("课设导师")
                                .email("tutor@campus.edu.cn"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                // 配置全局JWT认证
                .addSecurityItem(new SecurityRequirement().addList("Authorization"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Authorization",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
