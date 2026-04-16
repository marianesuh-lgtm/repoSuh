<script setup>
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getInfoFromToken, saveAuthInfo } from '@/utils/auth';

// import { useAuthStore } from '@/stores/authStore'; // Pinia 스토어가 있다면 사용

const route = useRoute();
const router = useRouter();

onMounted(() => {
  const token = route.query.token;
      console.log("onMounted");
      console.log(token);
      alert(token);
  
  if (token) {
    const userData = getInfoFromToken(token);

      console.log("17");
      console.log(userData);
      alert(userData);
    
    if (userData) {
      // 토큰 내부의 키값(sub, auth 등)은 백엔드 설정에 따라 확인 필요!
      const role = userData.auth || userData.role || 'USER';
      const userEmail = userData.sub;
      
      // 유틸리티 함수로 저장
      saveAuthInfo(token, role);
      console.log("userEmail::",userEmail);
      localStorage.setItem('userName', userEmail);
      
      setTimeout(() => {
        router.push('/');
      }, 5000);
      
      // 메인으로 이동
      //router.push('/');
    } else {
      alert('유효하지 않은 접근입니다.');
      router.push('/login');
    }
  }
});
</script>

<template>
  <div class="min-h-screen flex items-center justify-center">
    <div class="text-center">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
      <p class="mt-4 text-gray-600">로그인 처리 중입니다...</p>
    </div>
  </div>
</template>