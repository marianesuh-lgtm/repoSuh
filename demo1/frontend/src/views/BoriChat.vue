<template>
  <div class="bori-container">
    <div class="bori-character">
      <span>🐶</span>
      <div class="bori-bubble">
        <p>{{ currentStepData.message }}</p>
      </div>
    </div>

    <transition name="fade-slide" mode="out-in">
      <div :key="currentStep" class="question-card">
        <h2 class="question-text">{{ currentStepData.question }}</h2>
        
        <div v-if="currentStep === 0" class="input-group">
          <input 
            v-model="storyState.name" 
            placeholder="이름을 알려줘!" 
            @keyup.enter="nextStep"
          />
        </div>

        <div v-else class="options-grid">
          <button 
            v-for="option in currentStepData.options" 
            :key="option.value"
            @click="selectOption(option.value)"
            class="option-button"
          >
            <span class="option-icon">{{ option.icon }}</span>
            <span class="option-label">{{ option.label }}</span>
          </button>
        </div>
      </div>
    </transition>

    <div class="nav-buttons">
      <button v-if="currentStep > 0" @click="prevStep" class="btn-prev">이전으로</button>
      <button v-if="canGoNext" @click="nextStep" class="btn-next">
        {{ currentStep === steps.length - 1 ? '동화 그리기!' : '다음으로' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import axios from 'axios';

const generateFinalPrompt = async () => {
  try {
    // 로딩 상태 시작 (아이에게 "보리가 마법을 부리고 있어!"라고 보여주세요)
    console.log("서버로 데이터 전송 시작:", storyState);

    const response = await axios.post('http://myshakes.ddns.net:8080/api/chat/generate', {
      name: storyState.name,
      dress: storyState.dress,
      place: storyState.place
    });

    if (response.data.status === 'success') {
      alert("동화 그림이 완성되었어! 확인해볼까?");
      // 완성된 이미지 페이지로 이동하거나 결과 보여주기 로직
    }
  } catch (error) {
    console.error("동화 생성 실패:", error);
    alert("보리의 마법 지팡이가 조금 아픈가봐. 다시 시도해볼까?");
  }
};

// 1. 상태 관리
const currentStep = ref(0);
const isAnimating = ref(false);
const storyState = reactive({
  name: '',
  dress: '',
  place: ''
});

// 2. 단계별 데이터 (보리의 대사와 질문)
const steps = [
  {
    message: "안녕! 난 보리야. 오늘 동화 속 주인공은 누구야?",
    question: "주인공의 이름을 알려줘!",
    key: 'name'
  },
  {
    message: (name) => `와, ${name}! 멋진 이름이야. 오늘 어떤 옷을 입고 싶어?`,
    question: "입고 싶은 옷을 골라봐!",
    key: 'dress',
    options: [
      { label: '노란 드레스', value: 'a cozy yellow dress', icon: '👗' },
      { label: '분홍 원피스', value: 'a pink lovely dress', icon: '🌸' },
      { label: '멜빵 바지', value: 'blue dungarees', icon: '👖' }
    ]
  },
  {
    message: () => "어디로 모험을 떠나볼까? 신비로운 곳으로 가보자!",
    question: "가고 싶은 장소를 선택해줘!",
    key: 'place',
    options: [
      { label: '비밀 정원', value: 'a fairy secret garden', icon: '🌿' },
      { label: '과자 나라', value: 'a candy kingdom', icon: '🍭' },
      { label: '구름 나라', value: 'a kingdom above the clouds', icon: '☁️' }
    ]
  }
];

const currentStepData = computed(() => {
  const step = steps[currentStep.value];
  return {
    ...step,
    message: typeof step.message === 'function' ? step.message(storyState.name) : step.message
  };
});

const canGoNext = computed(() => {
  return storyState[steps[currentStep.value].key] !== '';
});

// 3. 로직 함수
const selectOption = (val) => {
  storyState[steps[currentStep.value].key] = val;
  triggerBoriAnimation();
};

const nextStep = () => {
  if (currentStep.value < steps.length - 1) {
    currentStep.value++;
    triggerBoriAnimation();
  } else {
    generateFinalPrompt();
  }
};

const prevStep = () => currentStep.value--;

const triggerBoriAnimation = () => {
  isAnimating.value = true;
  setTimeout(() => isAnimating.value = false, 500);
};

</script>

<style scoped>
/* 아이들이 좋아할만한 파스텔톤 스타일링 */
.bori-container {
  max-width: 500px;
  margin: 0 auto;
  padding: 20px;
  background: #fffdf0;
  border-radius: 30px;
  text-align: center;
}

.bori-character {
  margin-bottom: 30px;
}

.bori-img {
  width: 120px;
  transition: transform 0.3s;
}

.bounce {
  transform: translateY(-20px);
}

.bori-bubble {
  background: white;
  padding: 15px;
  border-radius: 20px;
  border: 3px solid #ffeb3b;
  position: relative;
  margin-top: 10px;
}

.options-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.option-button {
  padding: 20px;
  border: none;
  border-radius: 15px;
  background: white;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  cursor: pointer;
}

.option-button:hover {
  background: #fff9c4;
}

/* 트랜지션 애니메이션 */
.fade-slide-enter-active, .fade-slide-leave-active {
  transition: all 0.3s ease;
}
.fade-slide-enter-from { opacity: 0; transform: translateX(20px); }
.fade-slide-leave-to { opacity: 0; transform: translateX(-20px); }
</style>