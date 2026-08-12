package com.ebookstore;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@MapperScan("com.ebookstore.mapper")
// 纯 JWT 认证,不需要 Spring Boot 默认的内存用户
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class EbookstoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(EbookstoreApplication.class, args);
    }
}

//cd
//.\mvnw spring-boot:run
//cd
//npm run dev