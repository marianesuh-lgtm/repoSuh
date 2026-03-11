<!-- src/components/StoryDisplay.vue -->
<template>
  <div class="story-display">
    <div v-if="isGenerating" class="generating">
      <div class="sparkle">✨</div>
      <p>마법의 이야기를 만들고 있어요...</p>
      <div class="dots">
        <div class="dot" style="animation-delay: 0s"></div>
        <div class="dot" style="animation-delay: 0.2s"></div>
        <div class="dot" style="animation-delay: 0.4s"></div>
      </div>
    </div>

<!-- 이야기 내용 + TTS 버튼 (항상 보이게) -->
    <div class="story-content">
      <div  v-if="story" class="story-text">{{ story }}</div>



      <!-- 로딩 중에도 버튼 보이게 (disabled 상태로) -->
<button
  class="tts-btn"
  @click="$emit('tts')"
>
  {{ isSpeaking ? '⏹️ 멈추기' : '🔊 소리내어 읽기' }}
</button>
    </div>
  </div>
</template>

<script setup>
import { watch } from 'vue'   // ← 이 한 줄 추가!

// console.log('StoryDisplay 렌더링 시점 - isGenerating:', isGenerating)
// console.log('StoryDisplay 렌더링 시점 - story 값:', story)
// console.log('StoryDisplay 렌더링 시점 - 버튼 disabled 될 조건:', isGenerating || !story)

// const props = defineProps({
//   story: String,
//   isGenerating: Boolean,
//   onTTS: Function,
//   isSpeaking: Boolean
// })
const { story, isGenerating, onTTS, isSpeaking } = defineProps({
  story: String,
  isGenerating: Boolean,
  onTts: {
    type: Function,
    required: true
  },
  isSpeaking: Boolean
})

// console.log('StoryDisplay - 받은 isGenerating:', isGenerating)
// console.log('StoryDisplay - 받은 story:', story)

//  console.log('StoryDisplay 렌더링됨')
//  console.log('현재 isGenerating:', isGenerating)
//  console.log('현재 story:', story)
//  console.log('onTTS 함수 존재?', typeof  onTTS === 'function')

//  const handleTTSClick = () => {
//   console.log('버튼 클릭 이벤트 발생!! (StoryDisplay 내부)')
//   console.log('현재 story:', story)
//   console.log('onTTS 타입:', typeof onTTS)
  
//   if (typeof onTTS === 'function') {
//     console.log('onTTS 호출 시도')
//     onTTS()
//   } else {
//     console.error('onTTS가 함수가 아닙니다!', onTTS)
//   }
// }

watch(() => onTTS, (newVal) => {
  console.log('onTTS prop 변경 감지 → 타입:', typeof newVal)
})

console.log('StoryDisplay 초기 렌더링 - onTTS 타입:', typeof onTTS)
 
</script>

<style scoped>
.story-display {
  background: rgba(255,255,255,0.06);
  border-radius: 24px;
  padding: 32px;
  border: 1px solid rgba(255,255,255,0.15);
  backdrop-filter: blur(20px);
  position: relative;
  overflow: hidden;
}

.generating {
  text-align: center;
  padding: 40px;
}

.sparkle {
  font-size: 60px;
  animation: rainbow 2s linear infinite, float 1s ease-in-out infinite;
}

.generating p {
  color: #fde68a;
  font-size: 22px;
  font-weight: 700;
  margin-top: 16px;
}

.dots {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 16px;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #fbbf24;
  animation: bounce 1s ease-in-out infinite;
}

.story-text {
  font-size: 18px;
  line-height: 2;
  color: #e2e8f0;
  white-space: pre-wrap;
}

.tts-btn {
  margin-top: 24px;
  padding: 14px 28px;
  border-radius: 50px;
  border: none;
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
  color: white;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 8px 25px rgba(139,92,246,0.4);
  display: flex;
  align-items: center;
  gap: 8px;
}

.tts-btn:is(:hover) {
  transform: scale(1.03);
}

@keyframes rainbow {
  0% { filter: hue-rotate(0deg); }
  100% { filter: hue-rotate(360deg); }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}
</style>