<!-- src/components/ProgressBar.vue -->
<template>
  <div class="progress-bar">
    <div v-for="(step, i) in STEPS" :key="step" class="step-item">
      <div
        class="step-circle"
        :class="{
          done: i < currentStep,
          active: i === currentStep
        }"
      >
        {{ i < currentStep ? '✓' : STEP_LABELS[step].title.split(' ')[0] }}
      </div>
      <span class="step-label">{{ step }}단계</span>

      <div v-if="i < STEPS.length - 1" class="connector" :class="{ done: i < currentStep - 1 }" />
    </div>
  </div>
</template>

<script setup>
defineProps({
  currentStep: Number,
  selections: Object
})

const STEPS = ['기', '승', '전', '결']
const STEP_LABELS = {
  기: { title: '🌱 이야기 시작' },
  승: { title: '🌊 이야기 전개' },
  전: { title: '⚡ 이야기 위기' },
  결: { title: '🌈 이야기 결말' }
}
</script>

<style scoped>
.progress-bar {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 32px;
  flex-wrap: wrap;
  position: relative;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  position: relative;
}

.step-circle {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  transition: all 0.5s ease;
}

.step-circle.done {
  background: linear-gradient(135deg, #4ade80, #22c55e);
  border: 3px solid #86efac;
}

.step-circle.active {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  border: 3px solid #fde68a;
  animation: glow 2s ease-in-out infinite;
}

.step-circle:not(.done):not(.active) {
  background: rgba(255,255,255,0.1);
  border: 3px solid rgba(255,255,255,0.2);
}

.step-label {
  font-size: 13px;
  font-weight: 700;
  color: rgba(255,255,255,0.4);
}

.step-item:nth-child(1) .step-label { color: #86efac; }
.step-item:nth-child(2) .step-label.active { color: #fde68a; }

.connector {
  position: absolute;
  width: 40px;
  height: 2px;
  background: rgba(255,255,255,0.15);
  margin-left: 68px;
  margin-top: -42px;
}

.connector.done {
  background: #4ade80;
}

@keyframes glow {
  0%, 100% { box-shadow: 0 0 20px rgba(255, 200, 50, 0.3); }
  50% { box-shadow: 0 0 40px rgba(255, 200, 50, 0.8), 0 0 80px rgba(255, 150, 50, 0.4); }
}
</style>