package com.ebookstore.utils;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Component
public class OrderNoGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();

    public String generate() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        int random = RANDOM.nextInt(10000);
        return timestamp + String.format("%04d", random);
    }
}