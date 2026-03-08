<!-- src/views/StoryChat.vue -->
<template>
  <div class="min-h-screen bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 flex flex-col">
    <!-- 헤더 + 진행바 -->
    <header class="bg-indigo-700 text-white p-6 shadow-lg">
      <h1 class="text-3xl md:text-4xl font-bold text-center">세현이와 함께 만드는 동화</h1>
      <div class="mt-4 flex justify-center gap-2">
        <div v-for="step in 4" :key="step" 
             class="w-12 h-2 rounded-full transition-all duration-500"
             :class="currentStep >= step ? 'bg-yellow-400' : 'bg-gray-300'">
        </div>
      </div>
      <p class="mt-2 text-center text-sm opacity-90">
        {{ currentStep === 1 ? '기 - 시작' : 
           currentStep === 2 ? '승 - 모험 시작' : 
           currentStep === 3 ? '전 - 위기' : '결 - 마무리' }}
      </p>
    </header>

    <!-- 대화 영역 -->
    <div class="flex-1 overflow-y-auto p-4 md:p-8 space-y-6 pb-32">
      <div v-for="(msg, index) in messages" :key="index" 
           class="flex" :class="msg.role === 'user' ? 'justify-end' : 'justify-start'">
        <div 
          class="max-w-[85%] p-5 rounded-2xl shadow-lg"
          :class="msg.role === 'user' 
            ? 'bg-orange-100 text-gray-900 rounded-br-none border border-orange-200' 
            : 'bg-white text-gray-800 rounded-bl-none border border-gray-200'"
        >
          <p class="text-lg md:text-xl leading-relaxed whitespace-pre-wrap" v-html="msg.content"></p>
          <p class="text-xs mt-2 opacity-70">
            {{ msg.role === 'user' ? '세현이' : '동화 친구' }} · {{ msg.time }}
          </p>
        </div>
      </div>

      <!-- 이미지 미리보기 -->
      <div v-if="latestImage" class="flex justify-center my-8 animate-fade-in">
        <img :src="latestImage" class="max-w-lg rounded-xl shadow-2xl border-4 border-white" alt="현재 장면" />
      </div>
    </div>

    <!-- 입력 & 선택지 영역 -->
    <footer class="bg-white border-t shadow-inner p-6 fixed bottom-0 left-0 right-0 z-10">
      <div class="max-w-4xl mx-auto">
        <!-- 선택지 버튼 (있을 때만) -->
        <div v-if="currentChoices.length > 0" class="flex flex-wrap gap-4 mb-6 justify-center">
          <button
            v-for="(choice, idx) in currentChoices"
            :key="idx"
            @click="selectChoice(choice)"
            class="px-6 py-4 bg-indigo-100 hover:bg-indigo-200 text-indigo-800 font-medium rounded-xl shadow transition transform hover:scale-105"
          >
            {{ choice }}
          </button>
        </div>

        <!-- 자유 입력 -->
        <div class="flex gap-4">
          <input
            v-model="userInput"
            @keyup.enter="sendMessage"
            placeholder="세현이가 뭐라고 할까? 자유롭게 써봐~"
            class="flex-1 px-6 py-4 border border-gray-300 rounded-full text-lg focus:outline-none focus:ring-2 focus:ring-indigo-400"
          />
          <button 
            @click="sendMessage"
            :disabled="!userInput.trim() || loading"
            class="px-8 py-4 bg-indigo-600 text-white rounded-full font-bold hover:bg-indigo-700 disabled:opacity-50 transition"
          >
            {{ loading ? '생각 중...' : '보내기' }}
          </button>
        </div>

        <!-- 액션 버튼 -->
        <div class="mt-4 flex justify-center gap-6 text-sm">
          <button @click="drawScene" :disabled="loading" class="text-indigo-600 hover:underline flex items-center gap-1">
            <span>지금 장면 그림 보기 🎨</span>
          </button>
          <button @click="finishStory" class="text-green-600 hover:underline flex items-center gap-1">
            <span>이야기 완성하기 📖</span>
          </button>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { generateNextScene, generateImage } from '@/api/storyApi' // 백엔드 API 가정

const router = useRouter()
const messages = ref([])
const userInput = ref('')
const loading = ref(false)
const latestImage = ref(null)
const currentStory = ref({ story: '', pages: [] })
const currentChoices = ref([])
const currentStep = ref(1) // 1:기, 2:승, 3:전, 4:결

onMounted(() => {
  startStory()
})

const startStory = () => {
  messages.value.push({
    role: 'ai',
    content: '안녕, 세현아! 오늘은 어떤 재미있는 모험을 해볼까?\n\n세현이가 아침에 눈을 떴을 때, 방 안에 이상한 빛이 나고 있었어.\n책상 위에 빛나는 작은 문이 하나 생겼어! ✨\n\n이 문을 열어볼까? 아니면 무서워서 그냥 덮어둘까?',
    time: new Date().toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
  })

  currentChoices.value = [
    "문을 열어본다!",
    "무서워서 덮어둔다",
    "엄마한테 말해본다"
  ]
}

const selectChoice = async (choice) => {
  messages.value.push({
    role: 'user',
    content: choice,
    time: new Date().toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
  })

  userInput.value = ''
  currentChoices.value = []
  loading.value = true

  try {
    const response = await generateNextScene({
      currentStory: currentStory.value.story,
      userChoice: choice,
      step: currentStep.value
    })

    messages.value.push({
      role: 'ai',
      content: response.nextPart,
      time: new Date().toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
    })

    currentStory.value.story += '\n\n' + response.nextPart
    latestImage.value = response.imageUrl

    // 단계 진행
    if (currentStep.value < 4) currentStep.value++

    // 다음 선택지 업데이트
    currentChoices.value = response.nextChoices || []

  } catch (err) {
    messages.value.push({
      role: 'ai',
      content: '앗, 무슨 일이 생겼나 봐... 다시 선택해줄래?',
      time: new Date().toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
    })
  } finally {
    loading.value = false
  }
}

const sendMessage = () => {
  if (!userInput.value.trim()) return
  selectChoice(userInput.value.trim())
}

const drawScene = async () => {
  loading.value = true
  try {
    const res = await generateImage(currentStory.value.story)
    latestImage.value = res.imageUrl
  } finally {
    loading.value = false
  }
}

const finishStory = () => {
  if (confirm('여기서 이야기 마무리할까?')) {
    router.push({
      name: 'Reader',
      query: { storyData: JSON.stringify(currentStory.value) }
    })
  }
}
</script>

<style>
/* 스크롤바 예쁘게 */
::-webkit-scrollbar {
  width: 8px;
}
::-webkit-scrollbar-track {
  background: #f1f1f1;
}
::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>