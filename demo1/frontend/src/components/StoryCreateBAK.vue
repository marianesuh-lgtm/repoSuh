<!-- src/views/StoryCreate.vue 또는 src/components/StoryCreate.vue -->
<template>
  <div class="min-h-screen bg-gradient-to-b from-blue-50 to-purple-100 flex items-center justify-center p-6">
    <div class="w-full max-w-2xl bg-white rounded-2xl shadow-2xl overflow-hidden">
      <!-- 헤더 -->
      <div class="bg-orange-400 text-white p-8 text-center">
        <h1 class="text-3xl md:text-4xl font-bold">우리 아이만의 동화 만들기</h1>
        <p class="mt-3 text-lg opacity-90">아이의 이름과 좋아하는 것을 알려주세요!</p>
      </div>

      <!-- 폼 영역 -->
      <div class="p-8 md:p-12">
        <form @submit.prevent="createStory" class="space-y-8">
          <!-- 이름 -->
          <div>
            <label class="block text-lg font-medium text-gray-700 mb-2">
              아이 이름 <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.name"
              type="text"
              placeholder="예) 세현"
              required
              class="w-full px-5 py-4 border border-gray-300 rounded-lg text-lg focus:outline-none focus:ring-2 focus:ring-orange-400 focus:border-orange-400 transition"
            />
          </div>

          <!-- 나이 -->
          <div>
            <label class="block text-lg font-medium text-gray-700 mb-2">
              나이 <span class="text-red-500">*</span>
            </label>
            <input
              v-model.number="form.age"
              type="number"
              min="3"
              max="12"
              placeholder="예) 8"
              required
              class="w-full px-5 py-4 border border-gray-300 rounded-lg text-lg focus:outline-none focus:ring-2 focus:ring-orange-400 focus:border-orange-400 transition"
            />
          </div>

          <!-- 성별 -->
          <div>
            <label class="block text-lg font-medium text-gray-700 mb-3">
              성별
            </label>
            <div class="flex gap-6">
              <label class="flex items-center cursor-pointer">
                <input
                  v-model="form.gender"
                  type="radio"
                  value="boy"
                  class="w-5 h-5 text-orange-500 border-gray-300 focus:ring-orange-400"
                />
                <span class="ml-3 text-lg text-gray-700">남자아이</span>
              </label>
              <label class="flex items-center cursor-pointer">
                <input
                  v-model="form.gender"
                  type="radio"
                  value="girl"
                  class="w-5 h-5 text-orange-500 border-gray-300 focus:ring-orange-400"
                />
                <span class="ml-3 text-lg text-gray-700">여자아이</span>
              </label>
            </div>
          </div>

          <!-- 좋아하는 것 -->
          <div>
            <label class="block text-lg font-medium text-gray-700 mb-2">
              좋아하는 것 (콤마로 구분 가능)
            </label>
            <input
              v-model="form.likes"
              type="text"
              placeholder="예) 공룡, 과자, 우주, 토끼"
              class="w-full px-5 py-4 border border-gray-300 rounded-lg text-lg focus:outline-none focus:ring-2 focus:ring-orange-400 focus:border-orange-400 transition"
            />
          </div>

          <!-- 테마 -->
          <div>
            <label class="block text-lg font-medium text-gray-700 mb-2">
              원하는 동화 테마
            </label>
            <input
              v-model="form.theme"
              type="text"
              placeholder="예) 헨젤과 그레텔, 우주 모험, 겨울 왕국, 용감한 기사"
              class="w-full px-5 py-4 border border-gray-300 rounded-lg text-lg focus:outline-none focus:ring-2 focus:ring-orange-400 focus:border-orange-400 transition"
            />
            <p class="mt-2 text-sm text-gray-500">
              자유롭게 적어주세요! (없어도 괜찮아요)
            </p>
          </div>

          <!-- 생성 버튼 -->
          <div class="pt-6">
            <button
              type="submit"
              :disabled="loading || !form.name.trim() || !form.age"
              class="w-full py-5 bg-orange-500 text-white text-xl font-bold rounded-xl shadow-lg hover:bg-orange-600 disabled:opacity-50 disabled:cursor-not-allowed transition transform hover:scale-105"
            >
              {{ loading ? '동화 만드는 중...' : '동화 만들기 시작하기!' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 에러 메시지 -->
    <div v-if="error" class="fixed bottom-8 left-1/2 transform -translate-x-1/2 bg-red-100 border border-red-400 text-red-700 px-6 py-4 rounded-lg shadow-lg max-w-lg text-center">
      {{ error }}
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { generateBook } from '@/api/storyApi'  // 이전에 만든 api 파일

const router = useRouter()

const form = ref({
  name: '',
  age: null,
  gender: 'boy',       // 기본값
  likes: '',
  theme: ''
})

const loading = ref(false)
const error = ref('')

const createStory = async () => {
  error.value = ''
  loading.value = true

  try {
    const payload = {
      name: form.value.name.trim(),
      age: Number(form.value.age),
      gender: form.value.gender,
      likes: form.value.likes.trim(),
      theme: form.value.theme.trim()
    }

    const result = await generateBook(payload)

    if (result.success) {
      // 생성 성공 → 결과 페이지로 이동 (예: StoryReader)
      router.push({
        name: 'StoryReader',
        params: { storyId: result.id }, // 만약 id가 있다면
        query: { data: JSON.stringify(result) } // 또는 쿼리로 전달
      })
      // 또는 상태 관리(Pinia) 사용 시 store에 저장
    } else {
      error.value = result.message || '동화 생성에 실패했습니다'
    }
  } catch (err) {
    error.value = err.message || '서버와 연결할 수 없습니다'
  } finally {
    loading.value = false
  }
}
</script>