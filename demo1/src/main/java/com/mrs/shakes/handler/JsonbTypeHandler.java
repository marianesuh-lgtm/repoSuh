package com.mrs.shakes.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.util.Map;

@MappedTypes({Map.class}) // 또는 특정 DTO
public class JsonbTypeHandler extends BaseTypeHandler<Object> {

    private static final ObjectMapper mapper = new ObjectMapper();

    // 1. 파라미터 설정 (Java -> DB)
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setObject(i, mapper.writeValueAsString(parameter), Types.OTHER);
        } catch (Exception e) {
            throw new RuntimeException("JSON writing error", e);
        }
    }

    // 2. 컬럼 이름으로 결과 가져오기 (DB -> Java)
    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toObject(rs.getString(columnName));
    }

    // 3. 컬럼 인덱스로 결과 가져오기 (DB -> Java) - 이 부분이 누락되었을 가능성이 큼!
    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toObject(rs.getString(columnIndex));
    }

    // 4. 프로시저 등에서 호출 결과 가져오기 (DB -> Java)
    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toObject(cs.getString(columnIndex));
    }

    private Object toObject(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        try {
            return mapper.readValue(content, Object.class);
        } catch (Exception e) {
            throw new RuntimeException("JSON reading error", e);
        }
    }
}