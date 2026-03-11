<template>
  <div style="padding: 50px; text-align: center;">
    <h1>StoryReader 페이지 도착!</h1>
    <p>여기까지 왔어요 🎉</p>
    <pre>받은 쿼리: {{ route.query }}</pre>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
const route = useRoute()
</script>