<!-- src/components/ChoiceCard.vue -->
<template>
  <div class="choice-card">
    <div class="card-header">
      <span class="emoji">{{ emoji }}</span>
      <h3>{{ label }}</h3>
    </div>

    <div class="options-grid">
      <button
        v-for="(opt, idx) in options"
        :key="idx"
        class="option-btn"
        :class="{ 
          /* ?. 를 사용해서 selected가 없어도 에러가 나지 않게 처리합니다 */
          selected: (opt.code || opt) === (selected?.code || selected) }"
        @click="$emit('select', opt)"
      >
         {{ typeof opt === 'object' ? opt.label : opt }}
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  label: String,
  emoji: String,
  options: Array,
  selected: String
})

defineEmits(['select'])
</script>

<style scoped>
.choice-card {
  margin-bottom: 28px;
  animation: slideIn 0.5s ease forwards;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.emoji {
  font-size: 28px;
  animation: bounce 2s ease-in-out infinite;
}

.card-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  color: #fde68a;
  text-shadow: 0 2px 10px rgba(251, 191, 36, 0.5);
}

.options-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.option-btn {
  padding: 16px 20px;
  border-radius: 20px;
  border: none;
  cursor: pointer;
  text-align: left;
  font-size: 16px;
  font-weight: 700;
  background: rgba(255,255,255,0.08);
  color: #e2e8f0;
  border: 2px solid rgba(255,255,255,0.1);
  transition: all 0.2s ease;
  backdrop-filter: blur(10px);
}

.option-btn.selected {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  color: #1a1a2e;
  border: 2px solid #fde68a;
  transform: scale(1.03);
  box-shadow: 0 8px 30px rgba(251, 191, 36, 0.4);
}

.option-btn:hover:not(.selected) {
  background: rgba(255,255,255,0.15);
  transform: scale(1.02);
}

@keyframes slideIn {
  from { opacity: 0; transform: translateY(30px) scale(0.95); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}
</style>