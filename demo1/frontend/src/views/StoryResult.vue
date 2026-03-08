<template>
  <div class="result-container">
    <div class="book-frame">
      <div class="result-header">
        <img src="@/assets/bori-dog.png" class="mini-bori" alt="보리">
        <div class="congrats-bubble">
          <h3>짜잔! {{ userName }}의 동화가 완성됐어!</h3>
          <p>정말 멋진 모험이었어, 그치?</p>
        </div>
      </div>

      <div class="image-wrapper">
        <transition name="photo-reveal">
          <img v-if="imageUrl" :src="imageUrl" class="final-image" alt="동화 결과물">
          <div v-else class="image-placeholder">
            <p>보리가 마법을 부리는 중...</p>
          </div>
        </transition>
      </div>

      <div class="story-text">
        <p>"{{ userName }}(이)는 오늘 {{ placeName }}에서 {{ dressName }}을(를) 입고 정말 행복했답니다!"</p>
      </div>

      <div class="action-buttons">
        <button @click="downloadImage" class="btn-save">📸 사진첩에 저장하기</button>
        <button @click="resetStory" class="btn-retry">✨ 새 동화 만들기</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';

// 부모 컴포넌트(App.vue 등)에서 결과를 넘겨받음
const props = defineProps({
  userName: String,
  dressName: String,
  placeName: String,
  imageUrl: String
});

const emit = defineEmits(['reset']);

const downloadImage = () => {
  // 실제 저장 로직 (HTML5 Canvas나 라이브러리 사용 가능)
  alert("은서의 소중한 그림을 사진첩에 저장할게!");
};

const resetStory = () => {
  emit('reset');
};
</script>

<style scoped>
.result-container {
  display: flex;
  justify-content: center;
  padding: 20px;
  background: #fdf6e3;
  min-height: 80vh;
}

.book-frame {
  max-width: 600px;
  background: white;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
  border: 15px solid #fff; /* 책 테두리 느낌 */
}

.result-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.mini-bori { width: 60px; }

.congrats-bubble {
  background: #fff9c4;
  padding: 10px 20px;
  border-radius: 15px;
  margin-left: 15px;
  text-align: left;
}

.image-wrapper {
  background: #eee;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: inset 0 0 10px rgba(0,0,0,0.1);
}

.final-image {
  width: 100%;
  display: block;
  border: 5px solid white;
}

.story-text {
  margin: 20px 0;
  font-family: 'Comic Sans MS', cursive;
  font-size: 1.2rem;
  color: #5d4037;
}

.action-buttons {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.btn-save, .btn-retry {
  padding: 15px;
  border: none;
  border-radius: 10px;
  font-weight: bold;
  cursor: pointer;
}

.btn-save { background: #ffeb3b; color: #5d4037; }
.btn-retry { background: #e0e0e0; color: #757575; }

/* 나타나기 애니메이션 */
.photo-reveal-enter-active {
  transition: all 1.5s ease-out;
}
.photo-reveal-enter-from {
  opacity: 0;
  transform: scale(0.9) rotate(-3deg);
}
</style>