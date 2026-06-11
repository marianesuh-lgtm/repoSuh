package com.mrs.shakes.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 프런트엔드에서 /images/로 시작하는 주소로 요청하면 그램 로컬 폴더와 매핑
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:///C:/shakes_images/") // 로컬 저장 경로
                .setCachePeriod(0); // 테스트할 때는 캐시를 끄는 것이 좋습니다.
    }
}
