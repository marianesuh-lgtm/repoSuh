import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '@/views/auth/LoginView.vue'
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
  ,
  {
    path: '/admin/stories',
    name: 'AdminStoryList',
    component: () => import('@/views/StoryAdminList.vue')
  },
  {
    path: '/admin/stories/:id', // :id는 동적 파라미터
    name: 'AdminStoryDtl',
    component: () => import('@/views/StoryAdminDtl.vue'),
    props: true // URL의 id를 컴포넌트의 props로 전달
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView
  },
  {
  path: '/signup',
  name: 'Signup',
  component: () => import('@/views/auth/SignupView.vue')
  },
  {
  path: '/login-success',
  name: 'LoginSuccess',
  component: () => import('@/views/auth/LoginSuccess.vue')
}
]

 

const router = createRouter({
  history: createWebHistory(),
  routes
})


router.beforeEach((to, from, next) => {
  const token = to.query.token; // URL의 ?token=... 확인

  if (token) {
    console.log("토큰 발견! 저장 중...");
    localStorage.setItem('accessToken', token);

    // 1. 만약 현재 목적지가 /login-success라면 토큰 저장 후 /home으로 보냄
    if (to.path === '/login-success') {
      next({ path: '/home', query: {} });
    } 
    // 2. 그 외의 페이지라면 토큰만 쿼리에서 지우고 원래 가려던 곳으로 보냄
    else {
      next({ path: to.path, query: {} });
    }
    return;
  }
  
  // 토큰이 없는 일반적인 경우
  next();
});

export default router