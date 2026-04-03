<!-- App.vue -->
<template>
  <div class="app-container">
    <link
      href="https://fonts.googleapis.com/css2?family=Nanum+Gothic:wght@400;700;800&family=Nanum+Pen+Script&display=swap"
      rel="stylesheet"
    />
    <StarField />

    <div class="content-wrapper">
      <div class="header">
        <div class="emoji-book">       <!-- 할머니 마스코트 -->
</div>
        <h1 class="title">        <img 
          src="@/assets/MrsShakespeare2.png"    
          alt="Mrs. Shakespeare"
          class="mascot-image"
        />
나만의 마법 동화 ✨</h1>
        <p class="subtitle">나만의 이야기를 만들어봐요!</p>
      </div>

      <!-- phase를 v-model로 내려보냄 -->
      <router-view v-model:phase="currentPhase" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import StarField from '@/components/StarField.vue'

const currentPhase = ref('intro')  // ← 여기서 phase 관리 시작
</script>

<style scoped>
/* 기존 스타일 유지 */
</style>