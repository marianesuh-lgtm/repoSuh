<template>
  <div class="story-create-container">
    <IntroScreen
      v-if="phase === 'intro'"
      @start="handleStart" 
    />

    <StoryCreateContent
      v-if="phase === 'making' || phase === 'story'"
      :phase="phase"
      @update:phase="handlePhaseUpdate"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue';
import IntroScreen from './IntroScreen.vue';
import StoryCreateContent from './StoryCreateContent.vue'; // 기존 본체 로직

//const phase = ref('intro');


// 1. props에서 phase를 제거하거나, 초기값으로만 사용하세요.
const props = defineProps({
  initialPhase: {
    type: String,
    default: 'intro'
  }
});

// defineProps({
//   phase: {
//     type: String,
//     required: true
//   }
// })

// 2. 내부 상태 변수 phase를 선언합니다.
const phase = ref(props.initialPhase); 

// 3. 부모에게 알릴 emit 선언 (필요한 경우)
const emit = defineEmits(['update:phase']);

// 4. 시작 버튼 클릭 시 실행될 함수
const handleStart = () => {
  console.log("동화 만들기 시작!"); // 작동 확인용
  phase.value = 'making';         // 내부 phase 값을 직접 변경
  emit('update:phase', 'making'); // 부모에게도 알림 (필요 시)
};

// 5. 단계 업데이트 함수
const handlePhaseUpdate = (newPhase) => {
  phase.value = newPhase;
  emit('update:phase', newPhase);
};


//defineEmits(['update:phase'])

</script>
<style scoped>
.story-create-container {
  /* 기존 스타일 유지 */
}
</style>