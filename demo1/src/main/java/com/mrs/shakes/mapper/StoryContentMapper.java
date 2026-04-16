package com.mrs.shakes.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.mrs.shakes.dto.AdminStoryDTO;
import com.mrs.shakes.dto.PageDTO;
import com.mrs.shakes.dto.PagedStoryResponse;
import com.mrs.shakes.dto.StoryChoiceDTO;
import com.mrs.shakes.dto.StoryParameterDTO;

@Mapper
public interface StoryContentMapper {

   List<StoryChoiceDTO> getStoryCodeMaster();

   PagedStoryResponse getStoryContent(StoryParameterDTO dto);
   List<PagedStoryResponse.Page> getStoryPages(StoryParameterDTO dto);
   List<AdminStoryDTO> getAdminStories(StoryParameterDTO dto);
   int validateStoryContent(StoryParameterDTO dto);
   int updateStoryPage(PageDTO dto);
   
}
