package com.mrs.shakes.mapper;

import com.mrs.shakes.dto.UserResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    // MyBatis는 XML 파일(mappers/UserMapper.xml)과 연동됩니다.
    List<UserResponseDto> findAllWithStoryCount();
    
    void updateLastLoginAt(@Param("userId") Long userId);
}