package com.giraone.thymeleaf.service;

import org.springframework.stereotype.Service;

@Service
public class PingService {

    public String getOkString() {
        return "OK";
    }
}
