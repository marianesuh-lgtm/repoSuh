import { createRouter, createWebHistory } from 'vue-router'
import StoryReaderView from '../views/StoryReaderView.vue'
// 다른 뷰들도 나중에 추가 가능

const routes = [
  {
    path: '/create',
    name: 'CreateStory',
    component: () => import('@/components/StoryCreate.vue')
  },
  {
    path: '/story/:storyId?',
    name: 'StoryReader',
    component: () => import('@/components/StoryReader.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router