package com.mrs.shakes.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrs.shakes.dto.PageDTO;

@MappedTypes(List.class)
@Component
public class JsonPageListTypeHandler extends BaseTypeHandler<List<PageDTO>> {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${comfyui.base-url:https://comfyui.myshakes.cc}")  
    private String comfyUrl;


    // 자바 객체를 DB에 넣을 때 (String으로 변환)
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<PageDTO> parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, objectMapper.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting List to JSON String", e);
        }
    }

    // DB에서 값을 가져올 때 (JSON String -> List<PageDTO>)
    @Override
    public List<PageDTO> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public List<PageDTO> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public List<PageDTO> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }


 // 질문하신 parse 메서드 수정 부분
    private List<PageDTO> parse(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            // 1. 먼저 JSON을 List<PageDTO>로 변환합니다.
            List<PageDTO> pages = objectMapper.readValue(json, new TypeReference<List<PageDTO>>() {});

            // 2. 리스트를 돌면서 IMAGE_URL에 Full URL을 입혀줍니다.
            if (pages != null) {
                for (PageDTO page : pages) {
                    if (page.getImageUrl() != null && !page.getImageUrl().startsWith("http")) {
                        // DB에 저장된 값이 상대 경로(/view?...)일 경우에만 baseUrl을 붙여줍니다.
                        page.setImageUrl(page.getImageUrl());
                    }
                    
                 }
            }
            return pages;
            
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting JSON String to List", e);
        }
    }
    
    private List<PageDTO> parse2(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<List<PageDTO>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting JSON String to List", e);
        }
    }
    
    
}