package com.mrs.shakes.handler;

import java.io.IOException;  
import java.sql.CallableStatement;  
import java.sql.PreparedStatement;  
import java.sql.ResultSet;  
import java.sql.SQLException;  
  
import com.fasterxml.jackson.core.JsonProcessingException;  
import com.fasterxml.jackson.databind.DeserializationFeature;  
import com.fasterxml.jackson.databind.ObjectMapper;  
import lombok.extern.slf4j.Slf4j;  
import org.apache.ibatis.exceptions.PersistenceException;  
import org.apache.ibatis.type.BaseTypeHandler;  
import org.apache.ibatis.type.JdbcType;  
import org.apache.ibatis.type.MappedJdbcTypes;
import org.postgresql.util.PGobject;


@Slf4j  
@MappedJdbcTypes(JdbcType.OTHER)  
public class JacksonTypeHandler<T extends Object> extends BaseTypeHandler<T> {  
    
	private static final PGobject jsonObject = new PGobject();
	
	private static ObjectMapper objectMapper;  
    private Class<T> type;  
  
    static {  
        objectMapper = new ObjectMapper();  
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);  
    }  
  
    public JacksonTypeHandler(Class<T> type) {  
        if (log.isTraceEnabled()) {  
            log.trace("JacksonTypeHandler(" + type + ")");  
        }  
        if (null == type) {  
            throw new PersistenceException("Type argument cannot be null");  
        }  
        this.type = type;  
    }  
  
    private T parse(String json) throws SQLException {  
    	
    	try {  
            if (json == null || json.length() == 0) {  
                return null;  
            }  
            return objectMapper.readValue(json, type);  
        } catch (IOException e) {  
            throw new SQLException(e.getMessage());
        }  
    }  
  
    private String toJsonString(T obj) throws SQLException {  
        try {  
            return objectMapper.writeValueAsString(obj);  
        } catch (JsonProcessingException e) {  
            throw new SQLException(e.getMessage());  
        }  
    }  
  
    @Override  
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {  
        return parse(rs.getString(columnName));  
    }  
  
    @Override  
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {  
        return parse(rs.getString(columnIndex));  
    }  
  
    @Override  
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {  
        return parse(cs.getString(columnIndex));  
    }  
  
    @Override  
    public void setNonNullParameter(PreparedStatement ps, int columnIndex, T parameter, JdbcType jdbcType)  
        throws SQLException {  
    	jsonObject.setType("json");
        jsonObject.setValue(toJsonString(parameter));
        ps.setObject(columnIndex, jsonObject);
    }  
}

