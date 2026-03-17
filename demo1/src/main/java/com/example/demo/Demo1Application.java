package com.example.demo;

import java.util.TimeZone;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@MapperScan("com.example.demo.mapper")
public class Demo1Application {

	@PostConstruct
    public void started() {
        // JVM의 기본 시간대를 한국 시간으로 설정
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
	
	public static void main(String[] args) {
        SpringApplication.run(Demo1Application.class, args);
    }

}
