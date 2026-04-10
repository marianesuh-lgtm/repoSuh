<template>
  <div class="flex h-screen bg-white">
    <div class="w-1/4 border-r bg-gray-50 p-6 overflow-y-auto">
      <h2 class="text-xl font-bold mb-6">시나리오 설정</h2>
      <div class="space-y-4">
        <div v-for="attr in storyAttributes" :key="attr.label" class="bg-white p-3 rounded border">
          <p class="text-xs text-gray-400 font-semibold uppercase">{{ attr.label }}</p>
          <p class="text-sm text-gray-800">{{ attr.value }}</p>
        </div>
      </div>
      
      <div class="mt-8">
        <label class="block text-sm font-bold mb-2">태그 관리</label>
        <input v-model="story.tags" class="w-full border p-2 rounded text-sm" />
      </div>
    </div>

    <div class="flex-1 overflow-y-auto p-8">
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-2xl font-bold">콘텐츠 편집</h2>
        <div class="space-x-2">
          <button @click="goBack" class="px-4 py-2 border rounded">취소</button>
          <button @click="approve" class="px-6 py-2 bg-blue-600 text-white rounded font-bold">최종 승인</button>
        </div>
      </div>

      <div v-for="(page, idx) in storyPages" :key="idx" class="mb-12 bg-white rounded-2xl border p-6 shadow-sm">
        <div class="flex gap-8">
          <div class="w-1/2">
            <div class="relative group">
              <img :src="page.imageUrl" class="w-full aspect-video object-cover rounded-xl border" />
              <div class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center rounded-xl">
                <button @click="regenerateImage(idx)" class="bg-white text-black px-4 py-2 rounded-full font-bold shadow-lg">
                  🔄 이미지 재생성
                </button>
              </div>
            </div>
            <p class="mt-3 text-xs text-gray-400 font-mono bg-gray-50 p-2 rounded">
              Prompt: {{ page.imagePrompt }}
            </p>
          </div>
          
          <div class="w-1/2 flex flex-col">
            <label class="text-sm font-bold text-gray-500 mb-2">PAGE {{ idx + 1 }} 텍스트</label>
            <textarea v-model="page.content" 
                      class="flex-1 w-full p-4 border rounded-xl resize-none focus:ring-2 focus:ring-blue-500 outline-none text-lg leading-relaxed"></textarea>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>