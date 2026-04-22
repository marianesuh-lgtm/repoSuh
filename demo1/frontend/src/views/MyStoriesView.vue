<template>
  <div class="my-stories-container">
    <div class="content-card">
      <div class="card-header">
        <h2 class="card-title">📜 내가 만든 이야기 리스트</h2>
        <p class="card-subtitle">지금까지 만든 마법 같은 이야기들을 다시 읽어보세요.</p>
      </div>

      <div v-if="stories.length > 0" class="stories-grid">
        <div 
          v-for="story in stories" 
          :key="story.storyId" 
          class="story-item"
          @click="goToDetail(story.storyId)"
        >
          <div class="story-icon">📖</div>
          <div class="story-info">
            <h3 class="story-name">{{ story.title }}</h3>
            <p class="story-date">{{ story.createdAt }}</p>
          </div>
          <div class="arrow">▶</div>
        </div>
      </div>

      <div v-else class="empty-state">
        <p>아직 만든 이야기가 없어요.</p>
        <router-link to="/" class="start-btn">첫 번째 이야기 만들기 ✨</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyStories } from '@/api/storyApi'          // 예: src/api/bookApi.js

const router = useRouter()
const stories = ref([])

// 임시 데이터 (실제 구현 시 API 호출로 대체)
const fetchStories = async () => {
 
    const response = await getMyStories();
    // 2. ref 변수에 값을 넣을 때는 반드시 .value를 사용해야 합니다.
   // console.log("response::", response);
   stories.value = response ;

    //console.log("stories::", stories);

    // 예시 데이터
//   stories.value = [
//     { id: 1, title: '용감한 토끼의 모험', createdAt: '2024-03-20' },
//     { id: 2, title: '달콤한 과자 나라의 비밀', createdAt: '2024-03-18' },
//     { id: 3, title: '우주선을 탄 고양이', createdAt: '2024-03-15' },
//   ]
}

const goToDetail = (id) => {
    console.log("id:::",id);
  router.push(`/my-story/${id}`);
}

onMounted(() => {
  fetchStories()
})
</script>

<style scoped>
.my-stories-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: 80vh;
  padding: 40px 20px;
}

.content-card {
  width: 100%;
  max-width: 800px;
  background: rgba(23, 5, 40, 0.9); /* App.vue 헤더와 통일된 색상 */
  border-radius: 30px;
  padding: 40px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.card-header {
  text-align: center;
  margin-bottom: 40px;
}

.card-title {
  color: #ffffff;
  font-size: 2rem;
  margin-bottom: 10px;
  font-family: 'Nanum Gothic', sans-serif;
}

.card-subtitle {
  color: rgba(255, 255, 255, 0.7);
}

.stories-grid {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.story-item {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.05);
  padding: 20px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.story-item:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: #ffd700;
  transform: translateY(-3px);
}

.story-icon {
  font-size: 1.5rem;
  margin-right: 20px;
}

.story-info {
  flex-grow: 1;
}

.story-name {
  color: #ffd700;
  font-size: 1.2rem;
  margin: 0;
}

.story-date {
  color: rgba(255, 255, 255, 0.5);
  font-size: 0.9rem;
  margin-top: 5px;
}

.arrow {
  color: #ffd700;
  font-size: 0.8rem;
  opacity: 0.5;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: rgba(255, 255, 255, 0.6);
}

.start-btn {
  display: inline-block;
  margin-top: 20px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #ff8a00, #ffc107);
  color: white;
  text-decoration: none;
  border-radius: 25px;
  font-weight: bold;
  box-shadow: 0 4px 15px rgba(255, 138, 0, 0.3);
}

.start-btn:hover {
  transform: scale(1.05);
}
</style>