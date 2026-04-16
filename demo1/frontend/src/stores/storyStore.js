// src/stores/storyStore.js
import { defineStore } from 'pinia';

export const useStoryStore = defineStore('story', {
  // 1. 저장할 상태 값 (데이터)
  state: () => ({
    selectedStory: null
  }),

  // 2. 데이터를 변경하거나 조작하는 함수
  actions: {
    setStory(story) {
      this.selectedStory = story; // 넘겨받은 story 객체를 저장소에 담음
    },
    clearStory() {
      this.selectedStory = null; // 필요 시 데이터 비우기
    }
  }
});