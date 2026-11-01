package com.example.demo.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CommonGenerator {

    public String uuid() {
        return UUID.randomUUID().toString();
    }
}
