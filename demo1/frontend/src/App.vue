<!-- src/App.vue -->
<template>
  <div class="min-h-screen bg-gradient-to-b from-blue-50 to-purple-50 flex flex-col">
    <!-- 헤더 -->
    <header class="bg-orange-400 text-white p-6 shadow-md">
      <h1 class="text-4xl font-bold text-center">세현이의 동화책</h1>
    </header>

    <!-- 메인 콘텐츠 -->
    <main class="flex-1 p-6">
      <!-- 로딩 중 -->
      <div v-if="loading" class="flex flex-col items-center justify-center h-full">
        <div class="animate-spin rounded-full h-16 w-16 border-t-4 border-orange-500 mb-4"></div>
        <p class="text-xl text-gray-700">동화 생성 중... 잠시만 기다려주세요 ✨</p>
      </div>

      <!-- 에러 메시지 -->
      <div v-else-if="error" class="text-center py-20">
        <p class="text-xl text-red-600 mb-6">{{ error }}</p>
        <button 
          @click="createBook" 
          class="px-8 py-4 bg-orange-500 text-white text-lg rounded-full shadow-lg hover:bg-orange-600"
        >
          다시 시도하기
        </button>
      </div>

      <!-- 동화 생성 폼 (처음 화면) -->
      <div v-else-if="!currentStory" class="max-w-2xl mx-auto text-center py-12">
        <h2 class="text-3xl font-bold text-gray-800 mb-8">새 동화 만들기</h2>
        
        <div class="space-y-6">
          <input 
            v-model="form.name" 
            placeholder="아이 이름 (예: 세현)" 
            class="w-full p-4 border rounded-lg text-lg focus:outline-none focus:ring-2 focus:ring-orange-400"
          />
          <input 
            v-model.number="form.age" 
            type="number" 
            placeholder="나이 (예: 8)" 
            class="w-full p-4 border rounded-lg text-lg focus:outline-none focus:ring-2 focus:ring-orange-400"
          />
          <input 
            v-model="form.likes" 
            placeholder="좋아하는 것 (예: 과자, 공룡)" 
            class="w-full p-4 border rounded-lg text-lg focus:outline-none focus:ring-2 focus:ring-orange-400"
          />
          <input 
            v-model="form.theme" 
            placeholder="테마 (예: 헨젤과 그레텔, 우주 모험)" 
            class="w-full p-4 border rounded-lg text-lg focus:outline-none focus:ring-2 focus:ring-orange-400"
          />

          <button 
            @click="createBook" 
            :disabled="loading || !form.name.trim()"
            class="mt-8 px-10 py-5 bg-orange-500 text-white text-xl rounded-full shadow-xl hover:bg-orange-600 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            동화 만들기
          </button>
        </div>
      </div>

      <!-- 생성된 동화 표시 -->
      <div v-else>
        <StoryReader 
          :pages="currentStory.pages" 
          :pageTexts="pageTexts"
        />
        
        <!-- 다시 만들기 버튼 -->
        <div class="text-center mt-8">
          <button 
            @click="reset"
            class="px-8 py-4 bg-gray-600 text-white rounded-full hover:bg-gray-700"
          >
            새로 만들기
          </button>
        </div>
      </div>
    </main>

    <!-- 푸터 -->
    <footer class="bg-gray-800 text-white p-4 text-center text-sm">
      © 2026 동화 생성기
    </footer>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import StoryReader from './components/StoryReader.vue'
import { generateBook } from './api/storyApi'
import { parseStoryPages } from './utils/storyParser'  // 이전에 만든 파서 함수

// 폼 데이터
const form = ref({
  name: '세현',
  age: 8,
  likes: '과자',
  theme: '헨젤과 가르텔'
})

// 상태
const loading = ref(false)
const error = ref('')
const currentStory = ref(null)
const pageTexts = ref([])

// 동화 생성 함수
const createBook = async () => {
  if (!form.value.name.trim()) {
    error.value = '아이 이름을 입력해주세요!'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const result = await generateBook(form.value)
    
    if (result.success) {
      currentStory.value = result
      pageTexts.value = parseStoryPages(result.page)
    } else {
      error.value = result.message || '동화 생성에 실패했습니다'
    }
  } catch (err) {
    error.value = err.message || '알 수 없는 오류가 발생했습니다'
  } finally {
    loading.value = false
  }
}

// 초기화 (새로 만들기)
const reset = () => {
  currentStory.value = null
  pageTexts.value = []
  error.value = ''
}
</script>