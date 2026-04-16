<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-b from-blue-50 to-white px-4">
    
    <div class="max-w-md w-full bg-white rounded-[2rem] shadow-2xl shadow-blue-100/50 p-10 text-center border border-blue-50">
      
      <div class="mb-10">
        <div class="inline-block p-4 bg-blue-50 rounded-full mb-6">
          <!-- <img src="@/assets/logo.png" class="w-20 h-20 rounded-full object-cover shadow-inner" alt="로고" /> -->
        </div>
        <h1 class="text-3xl font-black text-gray-800 mb-2 tracking-tight">
          나만의 마법 동화 ✨
        </h1>
        <p class="text-blue-500 font-semibold mb-8">우리 아이만을 위한 특별한 이야기</p>
        
        <div class="space-y-2">
          <h2 class="text-2xl font-bold text-gray-900">우리 아이 동화 세상</h2>
          <p class="text-gray-400 text-sm">로그인하고 맞춤 동화를 만나보세요!</p>
        </div>
      </div>

      <div class="space-y-4">
        <button 
          @click="loginWithSocial('kakao')"
          class="group w-full flex items-center justify-center gap-3 bg-[#FEE500] text-[#3c1e1e] py-4 rounded-2xl font-bold text-lg shadow-md hover:shadow-lg hover:-translate-y-0.5 transition-all"
        >
          <!-- <img src="https://developers.kakao.com/assets/img/lib/logos/kakaotalk_sharing_btn_medium.png" class="w-6 h-6" alt="kakao" /> -->
          카카오로 시작하기
        </button>

        <button 
          @click="loginWithSocial('naver')"
          class="group w-full flex items-center justify-center gap-3 bg-[#03C75A] text-white py-4 rounded-2xl font-bold text-lg shadow-md hover:shadow-lg hover:-translate-y-0.5 transition-all"
        >
          <span class="w-7 h-7 bg-white text-[#03C75A] rounded-full flex items-center justify-center text-sm font-black">N</span>
          네이버로 시작하기
        </button>
      </div>

      <div class="mt-12 text-gray-400 text-xs leading-relaxed">
        아이의 꿈을 키워주는 맞춤형 스토리텔링 서비스 <br/>
        <span class="mt-2 block font-medium text-gray-300">© 2024 나만의 마법 동화. All rights reserved.</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

const loginWithSocial = (platform) => {
  // 실제 운영 시에는 환경 변수(VITE_API_BASE_URL)를 사용하세요.
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://myShakes.ddns.net:8080';
  
  // 백엔드의 소셜 로그인 엔드포인트로 이동
  // 이 요청 이후 백엔드에서 카카오/네이버 로그인 페이지로 리다이렉트 시켜줍니다.
  window.location.href = `${baseUrl}/api/auth/login/${platform}`;
};
</script>
<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(to bottom, #f0f7ff, #ffffff);
  font-family: 'Pretendard', sans-serif;
}

.login-card {
  background: white;
  padding: 40px;
  border-radius: 30px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.05);
  text-align: center;
  width: 100%;
  max-width: 400px;
}

.logo-img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  margin-bottom: 20px;
}

h1 { font-size: 24px; font-weight: 800; margin-bottom: 8px; }

.button-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 30px;
}

.btn-social {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: 16px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: transform 0.2s;
}

.btn-social:active { transform: scale(0.98); }

.kakao { background-color: #FEE500; color: #3c1e1e; }
.naver { background-color: #03C75A; color: white; }

.icon { font-size: 20px; }
</style>