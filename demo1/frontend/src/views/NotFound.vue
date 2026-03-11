<template>
  <div class="not-found">
    <h1>페이지를 찾을 수 없습니다 😢</h1>
    <p>잘못된 주소로 들어오셨네요!</p>
    <button @click="goHome">홈으로 돌아가기</button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()

const goHome = () => router.push({ name: 'Home' })
</script>

<style scoped>
.not-found { text-align: center; padding: 100px; }
</style>