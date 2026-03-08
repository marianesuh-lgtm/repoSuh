<!-- src/views/StoryReaderView.vue -->
<template>
  <StoryReader 
    :pages="storyPages" 
    :pageTexts="storyTexts" 
  />
</template>

<script setup>
import StoryReader from '@/components/StoryReader.vue'
import { ref, onMounted } from 'vue'
import { supabase } from '@/lib/supabaseClient'  // 이전에 세팅한 Supabase

const storyPages = ref([])
const storyTexts = ref([])

onMounted(async () => {
  // Supabase나 API에서 데이터 불러오기 예시
  const { data } = await supabase
    .from('stories')
    .select('*')
    .order('created_at', { ascending: false })
    .limit(1)

  if (data?.length) {
    storyPages.value = data[0].pages
    storyTexts.value = parseStoryPages(data[0].story)  // 이전에 만든 파서 함수
  }
})
</script>