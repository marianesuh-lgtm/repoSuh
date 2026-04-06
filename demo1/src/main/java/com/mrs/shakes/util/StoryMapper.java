package com.mrs.shakes.util;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.mrs.shakes.domain.story.StoryMaster;
import com.mrs.shakes.domain.story.StoryPage;
import com.mrs.shakes.dto.PagedStoryResponse;

@Mapper(componentModel = "spring")
public interface StoryMapper {

	@Mapping(source = "title", target = "title") // 엔티티의 id를 DTO의 storyId로 매핑
    @Mapping(source = "totalPages", target = "totalPages") // 리스트 매핑
	@Mapping(source = "pages", target = "pages")
    PagedStoryResponse toResponse(StoryMaster master);

    // 리스트 안의 각 객체(StoryPage -> PageResponse) 변환 규칙도 정의해줍니다.
    @Mapping(source = "pageNumber", target = "pageNumber")
    @Mapping(source = "rawContent", target = "rawText")
    @Mapping(source = "refinedContent", target = "text")
    @Mapping(source = "imagePrompt", target = "imagePrompt")
        PagedStoryResponse.Page toPageResponse(StoryPage page);	
}
