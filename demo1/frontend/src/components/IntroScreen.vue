<!-- src/components/IntroScreen.vue -->
<template>
  <div class="intro-section">
    <div class="intro-card">
      <div class="intro-emojis">
        <span v-for="(e, i) in ['🐰', '🐱', '🐲', '🧚']" :key="i" :style="{ animationDelay: `${i * 0.5}s` }">{{ e }}</span>
      </div>
      <h2>동화 만들기를 시작해요! 🌟</h2>
      <p>기 → 승 → 전 → 결, 4단계로 나만의 이야기를 만들어요</p>
      <p class="small">버튼을 눌러서 선택하면 AI가 동화를 완성해줘요!</p>
    </div>

    <div class="step-badges">
      <div v-for="t in ['🌱 이야기 시작', '🌊 이야기 전개', '⚡ 위기 발생', '🌈 해피엔딩']" :key="t" class="badge">
        {{ t }}
      </div>
    </div>

    <button class="start-btn" @click="$emit('start')">
      🚀 동화 만들기 시작!
    </button>
  </div>
</template>

<script setup>
// emit만 사용하므로 별도 로직 없음
</script>

<style scoped>
.intro-section {
  text-align: center;
  animation: slideIn 0.6s ease forwards;
  background: linear-gradient(180deg, rgba(10, 15, 40, 0.9) 0%, rgba(20, 30, 60, 0.8) 100%); /* 연한 → 어두운 그라데이션 배경 */
  min-height: 100vh; /* 전체 화면 채우기 */
  padding: 40px 20px;
}

.intro-card {
  background: rgba(30, 40, 70, 0.85); /* 진한 남색 반투명 */
  border-radius: 32px;
  padding: 48px 40px;
  border: 1px solid rgba(100, 150, 255, 0.35); /* 은은한 파랑 테두리 */
  backdrop-filter: blur(24px); /* 블러 더 강하게 */
  -webkit-backdrop-filter: blur(24px);
  margin-bottom: 32px;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.6); /* 그림자 강화 */
}

.intro-emojis {
  margin-bottom: 24px;
}

.intro-emojis span {
  font-size: 56px;
  margin: 0 12px;
  animation: float 2.5s ease-in-out infinite;
  display: inline-block;
  text-shadow: 
    0 0 16px rgba(255, 255, 255, 0.9), 
    0 0 32px rgba(165, 180, 252, 0.7); /* 빛나는 효과 강화 */
  filter: drop-shadow(0 8px 16px rgba(0, 0, 0, 0.5));
  background: rgba(255, 255, 255, 0.15); /* 이모지 뒤에 살짝 배경 */
  border-radius: 50%;
  padding: 12px;
  line-height: 1;
}

.intro-card h2 {
  color: #e0f2fe; /* 밝은 하늘색 계열 */
  font-size: 32px;
  font-weight: 900;
  margin: 0 0 16px;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.7); /* 그림자 추가 */
}

.intro-card p {
  color: #cbd5e1; /* 밝은 회색 계열 */
  font-size: 18px;
  line-height: 1.7;
  margin: 0 0 12px;
}

.small {
  font-size: 15px;
  color: #94a3b8; /* 조금 더 진한 회색 */
  opacity: 0.9;
}

.step-badges {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 32px;
}

.badge {
  padding: 14px 24px;
  border-radius: 50px;
  background: rgba(50, 60, 90, 0.85); /* 배경 진하게 */
  border: 1px solid rgba(165, 180, 252, 0.4);
  color: #e0e7ff;
  font-size: 16px;
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(8px);
}

.start-btn {
  padding: 20px 60px;
  border-radius: 50px;
  border: none;
  background: linear-gradient(135deg, #fbbf24, #f97316);
  color: #0f172a; /* 진한 남색으로 대비 ↑ */
  font-size: 22px;
  font-weight: 800;
  box-shadow: 0 12px 40px rgba(251, 191, 36, 0.5);
  cursor: pointer;
  transition: all 0.25s ease;
}

.start-btn:hover {
  transform: scale(1.08);
  box-shadow: 0 16px 50px rgba(251, 191, 36, 0.7);
}

@keyframes slideIn {
  from { opacity: 0; transform: translateY(40px) scale(0.92); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-24px) scale(1.12); }
}
</style>