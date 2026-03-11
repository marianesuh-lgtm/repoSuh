import { createRouter, createWebHistory } from 'vue-router'
//import StoryReaderView from '../views/StoryReaderView.vue'
// 다른 뷰들도 나중에 추가 가능

const routes = [
  {
    path: '/',
    redirect: '/home'   // ← / 로 들어오면 자동으로 /home으로 이동
    // 또는 직접 컴포넌트 매핑
    // component: () => import('@/components/StoryCreate.vue')
  },
  {
    path: '/home',
    name: 'CreateStory',
    component: () => import('@/components/StoryCreate.vue')
  },
   {
    path: '/story-reader',
    name: 'StoryReader',
    component: () => import('@/components/StoryReader.vue')  // 실제 컴포넌트 경로 맞춰주세요
  },
  {
  path: '/test-story-reader',
  name: 'TestStoryReader',
  component: () => import('@/components/StoryReader.vue')
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router