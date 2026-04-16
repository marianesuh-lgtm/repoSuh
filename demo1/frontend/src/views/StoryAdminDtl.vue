<!-- <template>
  <div v-if="story">
    <h1>{{ story.title }} 상세 편집</h1>
    
    <div v-for="page in story.pages" :key="page.page_no" class="page-editor">
      <img :src="page.image_url" />
      <textarea v-model="page.content"></textarea>
    </div>
  </div>
</template> -->
<template>
  <div v-if="isLoading" class="flex h-screen items-center justify-center">
    로딩 중...
  </div>
  <div v-else class="flex h-screen bg-white">
    </div>
  <div class="flex h-screen bg-white">
    <div class="w-1/4 border-r bg-gray-50 p-6 overflow-y-auto">
      <h2 class="text-xl font-bold mb-6">시나리오 설정</h2>
      <div class="space-y-4">
        <div  class="bg-white p-3 rounded border">
          <p class="text-xs text-gray-400 font-semibold uppercase">제목: {{ story.title }}</p>
          <p class="text-xs text-gray-400 font-semibold uppercase">주인공: {{ story.charDesc }}</p>
          <p class="text-sm text-gray-800">장소:{{ story.plaDesc }}</p>
          <p class="text-sm text-gray-800">기분:{{ story.modDesc }}</p>
          <p class="text-sm text-gray-800">발단:{{ story.eveDesc }}</p>
          <p class="text-sm text-gray-800">동행:{{ story.comDesc }}</p>
          <p class="text-sm text-gray-800">위기:{{ story.proDesc }}</p>
          <p class="text-sm text-gray-800">대응:{{ story.actDesc }}</p>
          <p class="text-sm text-gray-800">해결:{{ story.solDesc }}</p>
          <p class="text-sm text-gray-800">결말:{{ story.endDesc }}</p>
        </div>
      </div>
      
      <div class="mt-8">
        <label class="block text-sm font-bold mb-2">태그 관리</label>
        <input v-model="story.tags" class="w-full border p-2 rounded text-sm" />
      </div>
    </div>

    <div class="flex-1 overflow-y-auto p-8">
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-2xl font-bold">콘텐츠 편집</h2>
        <div class="space-x-2">
          <button @click="goBack" class="px-4 py-2 border rounded">취소</button>
          <button @click="verifyText" class="px-6 py-2 bg-blue-600 text-white rounded font-bold">최종 승인</button>
        </div>
      </div>
      <br/>
      <br/>

      <div v-for="(page, idx) in story.pages" :key="idx" class="mb-12 bg-white rounded-2xl border p-6 shadow-sm">
        <div class="flex gap-8">
          <div class="w-1/2">
            <div class="relative group">
              <img :src="page.image_url" class="w-full aspect-video object-cover rounded-xl border" />
              <div class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center rounded-xl">
                <button @click="regenerateImage(idx)" class="bg-white text-black px-4 py-2 rounded-full font-bold shadow-lg">
                  🔄 이미지 재생성
                </button>
              </div>
            </div>
            <!-- <p class="mt-3 text-xs text-gray-400 font-mono bg-gray-50 p-2 rounded">
              Prompt: {{ page.image_prompt }}
            </p> -->
              <textarea 
                    v-model="page.image_prompt" 
                    class="custom-textarea"
                    placeholder="내용을 입력하세요..."
                  ></textarea>          
          </div>
          
          <div class="w-1/2 flex flex-col">
              <div class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center rounded-xl">
                <button @click="updateText(idx)" class="bg-white text-black px-4 py-2 rounded-full font-bold shadow-lg">
                  🔄 텍스트 변경
                </button>
              </div>
            <label class="text-sm font-bold text-gray-500 mb-2">PAGE {{ idx + 1 }} 텍스트</label>
              <textarea 
                    v-model="page.content" 
                    class="custom-textarea"
                    placeholder="내용을 입력하세요..."
                  ></textarea>          
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { getBookById } from '@/api/storyApi'          // 예: src/api/bookApi.js
import { setBookTextById } from '@/api/storyApi'          // 예: src/api/bookApi.js
import { regenerateImageById } from '@/api/storyApi'          // 예: src/api/bookApi.js
import { verifyBookStory } from '@/api/storyApi'          // 예: src/api/bookApi.js
import { useStoryStore } from '@/stores/storyStore';
import { storeToRefs } from 'pinia'; // 반응성을 유지하며 구조분해할 때 사용
import axios from 'axios'; // axios 임포트 확인

const route = useRoute();
const story = ref({}); 
const storyStore = useStoryStore();
const { selectedStory } = storeToRefs(storyStore);

// 반응형 데이터 설정
//const story = ref({ pages: [] });
const isLoading = ref(true); // 로딩 상태 추가

onMounted(async () => {

   const storyId = route.params.id; 
   //story.value = storyStore.selectedStory;

   console.log("28 storyId:::", storyId  );
   //console.log("28 storyData:::", story.pages  );



  try {
    // 1. 먼저 스토어에 데이터가 있는지 확인
    if (storyStore.selectedStory && storyStore.selectedStory.storyId == storyId) {
      story.value = storyStore.selectedStory;
    } else {
      // 2. 스토어에 없거나 ID가 다르면(새로고침 포함) API 호출
      console.log("데이터를 새로 불러오는 중... ID:", storyId);
      const response = await getBookById(storyId);
      console.log("128 response:::", response  );
      //console.log("133 response.data:::", response.data  );
      
      if (response && response.length==1 ) {
  
        story.value  = response[0]  ;
        // 스토어도 동기화해주면 좋습니다
        storyStore.selectedStory = response[0]  ; 
        console.log("134 storyData:::", story.value  );
      }

     console.log("137 storyData:::", story.value  );
    } 
  } catch (error) {
    console.error("데이터 로드 실패:", error);
    alert("데이터를 불러오는데 실패했습니다.");
    router.push('/'); // 실패 시 메인으로 리다이렉트
  } finally {
    isLoading.value = false;
  }

});

const regenerateImage = async (idx) => {
  // 1. 해당 인덱스의 페이지 데이터 가져오기
  const page = storyStore.selectedStory.pages[idx];
  //const page = story;
  
  // 프롬프트가 비어있는지 확인
  if (!page.image_prompt || page.image_prompt.trim() === '') {
    alert('이미지 프롬프트를 입력해주세요.');
    return;
  }

  // 2. 로딩 상태 시작 (중복 클릭 방지)
  if (page.isGenerating) return;
  page.isGenerating = true;

  try {
    console.log(`페이지 ${idx + 1} 재생성 시작... 프롬프트:`, page );

    // 3. 백엔드 API 호출 (ComfyUI를 실행하는 서버 주소)
      const response = await regenerateImageById(page.story_id, {
      storyId: page.story_id,
      pageId: page.page_id,
      pageNo: page.page_no,
      charImg: page.char_img,
      imagePrompt: page.image_prompt,
      content: page.content // 수정된 프롬프트 전달
    });

    console.log(`response:`, response );
    if (response.data && response.data.image_url) {
      // 4. 성공 시 Pinia 스토어의 이미지 URL 업데이트
      // 스토어의 데이터가 바뀌면 Vue의 반응성 덕분에 화면의 <img>도 즉시 바뀝니다.
      page.image_url = response.data.image_url;
      alert('이미지가 성공적으로 재생성되었습니다!');
    }
  } catch (error) {
    console.error('이미지 재생성 실패:', error);
    alert('이미지 생성 중 오류가 발생했습니다. 서버 로그를 확인하세요.');
  } finally {
    // 5. 로딩 상태 종료
    page.isGenerating = false;
  }
};


const updateText = async () => {
      const page = storyStore.selectedStory.pages[idx];
      //console.log(`페이지 ${idx + 1} 텍스트 변경 시작... 텍스트:`, page.content);
      //console.log(`페이지정보... :`, page);

      const response = await setBookTextById(page.story_id, {
      storyId: page.story_id,
      pageId: page.page_id,
      pageNo: page.page_no,
      content: page.content // 수정된 프롬프트 전달
    });

    if (response) {
    alert('수정이 완료되었습니다!');
    }
     
};

const verifyText = async (idx) => {
      const page = storyStore.selectedStory;
      //console.log(`페이지 ${idx + 1} 텍스트 변경 시작... 텍스트:`, page.content);
      console.log(`페이지정보... :`, page);

      const response = await verifyBookStory(page.storyId, page);

    if (response) {
    alert('수정이 완료되었습니다!');
    }
     
};


</script>

<style scoped>
/* 컨테이너 가로 배치 설정 */
.textarea-container {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  width: 100%;
  margin-bottom: 1.5rem;
}

.page-label {
  white-space: nowrap;
  font-weight: 600;
  color: #4b5563;
  margin-top: 0.75rem; /* 텍스트 시작점과 맞춤 */
}

.custom-textarea {
  flex: 1;
  width: 100%;
  min-height: 120px; /* 👈 중요: 최소 높이를 지정하세요 */
  padding: 1rem;
  border: 1px solid #d1d5db;
  border-radius: 0.75rem;
  font-size: 1.125rem;
  line-height: 1.6;
  resize: none;
  outline: none;
  transition: all 0.2s;
  background-color: #f9fafb; /* 살짝 밝은 배경색 */
}

/* 포커스 되었을 때 */
.custom-textarea:focus {
  border-color: #3b82f6;
  background-color: #ffffff;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
}

/* 스크롤바 커스텀 (이미지처럼 투박한 스크롤 방지) */
.custom-textarea::-webkit-scrollbar {
  width: 8px;
}
.custom-textarea::-webkit-scrollbar-thumb {
  background: #e5e7eb;
  border-radius: 10px;
}
</style>
