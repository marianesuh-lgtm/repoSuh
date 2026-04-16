<template>
  <div class="admin-wrapper">
    <header class="admin-header">
      <h1>미승인 동화 검수 <span>({{ stories.length }}건)</span></h1>
    </header>

    <div class="story-grid">
      <div v-for="story in stories" :key="story.storyId" class="story-card">
        <div class="card-content">
          <div class="card-header">
            <span class="story-id">#{{ story.storyId }}</span>
            <span class="created-at">{{ story.createdAt }}</span>
          </div>
          <h2 class="story-title">{{ story.title }}</h2>
          <p class="story-summary">{{ story.summary }}</p>
          
          <div class="tag-group">
            <span class="tag loc">📍 {{ story.plaDesc }}</span>
            <span class="tag char">👤 {{ story.charDesc }}</span>
            <span class="tag act">🎬 {{ story.actDesc }}</span>
          </div>
        </div>
        <button @click="goToDetail(story)" class="detail-btn">
          상세 편집 및 승인
        </button>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { getRecentBooks } from '@/api/storyApi'          // 예: src/api/bookApi.js
import { useRouter } from 'vue-router'
import { useStoryStore } from '@/stores/storyStore';

 
// 1. 반드시 ref를 사용하여 'stories'를 정의해야 템플릿에서 접근 가능합니다.
// 초기값을 null이 아닌 빈 배열 []로 설정하면 .length 에러를 방지할 수 있습니다.
const stories = ref([]); 
const router = useRouter();  // ← 추가
const storyStore = useStoryStore();
 
const fetchStories = async () => {
     const cleanedData = {};
     const payload = {
      verifyYn: 'N',  // { 기: {character: "...", ...}, 승: {...}, ... }
      storyId: ''  // { 기: {character: "...", ...}, 승: {...}, ... }
    }

  try {
    const response = await getRecentBooks(payload);
    // 2. ref 변수에 값을 넣을 때는 반드시 .value를 사용해야 합니다.
    const currentStory = response ;
    console.log(response ); // 첫 번째 페이지의 글귀 출력
    console.log(response.length ); // 첫 번째 페이지의 글귀 출력
    //console.log(currentStory.pages[0].content); // 첫 번째 페이지의 글귀 출력

    stories.value = response ; 
  } catch (error) {
    console.error("데이터 로드 실패:", error);
  }
};

const goToDetail = (story) => {
  console.log("story::{}", story);
  storyStore.setStory(story);
  router.push({
    name: 'AdminStoryDtl',
    params: { id: story.storyId } 
    // [핵심] 전체 story 객체(pages 포함)를 state에 담아 전달
    //state: { storyData: story }
  });
};

onMounted(() => {
  fetchStories();
});
</script>

<style scoped>
/* 전체 컨테이너 */
.admin-wrapper {
  padding: 40px;
  background-color: #f8f9fa;
  min-height: 100vh;
  font-family: 'Pretendard', sans-serif;
}

/* 헤더 스타일 */
.admin-header h1 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1d23;
  margin-bottom: 30px;
}
.admin-header h1 span {
  color: #4f46e5;
  font-size: 18px;
}

/* 그리드 레이아웃 */
.story-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

/* 카드 개별 스타일 */
.story-card {
  background: white;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  transition: all 0.2s ease;
  overflow: hidden;
}

.story-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.05);
  border-color: #4f46e5;
}

.card-content {
  padding: 24px;
  flex-grow: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.story-id {
  font-size: 12px;
  font-weight: 800;
  color: #4f46e5;
  background: #eef2ff;
  padding: 2px 8px;
  border-radius: 4px;
}

.created-at {
  font-size: 12px;
  color: #9ca3af;
}

.story-title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.story-summary {
  font-size: 14px;
  color: #4b5563;
  line-height: 1.5;
  margin-bottom: 20px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 태그 스타일 */
.tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  font-size: 11px;
  padding: 4px 10px;
  border-radius: 20px;
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #e5e7eb;
}

/* 버튼 스타일 */
.detail-btn {
  width: 100%;
  padding: 14px;
  background: #111827;
  color: white;
  border: none;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.detail-btn:hover {
  background: #374151;
}
</style>