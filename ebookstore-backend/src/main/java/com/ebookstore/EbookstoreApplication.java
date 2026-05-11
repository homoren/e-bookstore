package com.ebookstore;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.ebookstore.mapper")
@SpringBootApplication
public class EbookstoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(EbookstoreApplication.class, args);
    }
}