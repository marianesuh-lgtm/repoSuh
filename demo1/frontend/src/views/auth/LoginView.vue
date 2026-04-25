<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2>✨ 동화 나라 입장하기</h2>
      <p class="auth-subtitle">소셜 계정으로 간편하게 시작하세요!</p>

      <div class="social-login-group">
        <button @click="loginWithSocial('kakao')" class="social-btn kakao">
          <img src="@/assets/kakao-icon.png" alt="K"  class="social-icon" /> 카카오로 시작하기
        </button>
        <button @click="loginWithSocial('naver')" class="social-btn naver">
          <img src="@/assets/naver-icon.png" alt="N"  class="social-icon" /> 네이버로 시작하기
        </button>
      </div>

      <div class="divider-text">또는</div>

      <form @submit.prevent="handleEmailLogin" class="email-login-form">
        <input v-model="loginData.email" type="email" placeholder="이메일 주소" required />
        <input v-model="loginData.password" type="password" placeholder="비밀번호" required />
        <button type="submit" class="auth-btn">로그인</button>
      </form>

      <div class="auth-footer">
        아직 회원이 아니신가요? 
        <router-link to="/signup" class="signup-link">회원가입 하기</router-link>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import {loginWithEmail}  from '@/api/storyApi'
import axios from 'axios';

const router = useRouter();


const loginWithSocial = (platform) => {
  // 실제 운영 시에는 환경 변수(VITE_API_BASE_URL)를 사용하세요.
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'https://api.myShakes.cc';
  
  // 백엔드의 소셜 로그인 엔드포인트로 이동
  // 이 요청 이후 백엔드에서 카카오/네이버 로그인 페이지로 리다이렉트 시켜줍니다.
  console.log(${baseUrl}/api/auth/login/${platform});
  
  window.location.href = `${baseUrl}/api/auth/login/${platform}`;
};

// 1. loginData를 객체로 초기화해야 합니다.
const loginData = ref({
  email: '',
  password: ''
});


const handleEmailLogin = async () => {

    
  try {

    const response = await loginWithEmail({
      email: loginData.value.email,
      password: loginData.value.password
    });

console.log("65::response::",response);

    const token = response.token; // 서버에서 준 JWT
console.log("69l::token::",token);
    
    // 1. 브라우저에 저장
    localStorage.setItem('accessToken', token);
    
    // 2. 이후 axios 요청 시 기본 헤더로 설정
     axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;

    alert("환영합니다! ✨");
    router.push('/'); // 메인 페이지로 이동
  } catch (error) {
    console.error("실제 에러 원인:", error);
    alert("로그인 정보가 올바르지 않습니다.");
  }
};

</script>
<style scoped>
/* 1. 상단 카드 영역 (기존 스타일 유지) */
.story-card {
  background: rgba(26, 35, 126, 0.9);
  padding: 30px;
  border-radius: 30px;
  color: white;
  margin-bottom: 20px;
  position: relative;
  box-shadow: 0 10px 20px rgba(0,0,0,0.3);
}

.profile-img-container {
  display: flex;
  align-items: center;
  gap: 20px;
}

.profile-img {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  object-fit: cover;
}

.card-content h1 { font-size: 24px; margin-bottom: 10px; }
.card-content p { font-size: 14px; opacity: 0.8; }

.login-btn-top {
  position: absolute;
  top: 20px;
  right: 30px;
  background: none;
  border: 1px solid #ffd700;
  color: #ffd700;
  padding: 6px 16px;
  border-radius: 15px;
  cursor: pointer;
  font-size: 14px;
}

/* 2. 로그인 섹션 (여기가 문제의 핵심 수정 부분) */
.login-section {
  padding: 20px 10px;
  text-align: left; /* 왼쪽 정렬 */
}

.section-title { font-size: 20px; margin-bottom: 10px; display: flex; align-items: center; gap: 8px; }
.section-subtitle { font-size: 14px; color: #555; margin-bottom: 30px; }

/* 3. 소셜 로그인 버튼 그룹 (flex-direction 추가) */
.social-login-group {
  display: flex;
  flex-direction: column; /* 세로로 배치 */
  gap: 12px; /* 버튼 사이 간격 */
  margin-bottom: 30px;
  width: 100%;
}

/* 4. 소셜 로그인 버튼 공통 (이미지 겹침 해결) */
.social-btn {
  display: flex;
  align-items: center; /* 세로 중앙 정렬 */
  justify-content: flex-start; /* 아이콘을 왼쪽에 배치 */
  width: 100%; /* 너비 100% */
  height: 50px;
  border-radius: 12px;
  border: none;
  font-weight: bold;
  font-size: 16px;
  cursor: pointer;
  padding: 0 20px; /* 양옆 여백 */
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  transition: transform 0.2s, box-shadow 0.2s;
}

.social-btn:hover { transform: translateY(-2px); box-shadow: 0 6px 8px rgba(0,0,0,0.15); }

/* 소셜 아이콘 스타일 */
.social-icon {
  width: 24px; /* 아이콘 크기 고정 */
  height: 24px;
  margin-right: 15px; /* 글자와 간격 */
  object-fit: contain;
}

/* 각 소셜 버튼 색상 (기존 유지) */
.kakao { background-color: #FEE500; color: #3c1e1e; }
.naver { background-color: #03C75A; color: white; }

/* 5. 이메일 로그인 및 회원가입 (기존 스타일 유지 및 보강) */
.email-login-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 25px;
  width: 100%;
}

.email-login-form input {
  padding: 12px 15px;
  border-radius: 10px;
  border: 1px solid #ddd;
  font-size: 14px;
}

.auth-btn {
  width: 100%;
  padding: 12px;
  border-radius: 20px;
  border: none;
  background: #f0f0f0;
  color: #333;
  font-size: 14px;
  cursor: pointer;
  margin-top: 5px;
}

.auth-footer { font-size: 14px; color: #555; text-align: left; }
.signup-link { color: #ffd700; text-decoration: underline; margin-left: 5px; font-weight: bold; }
</style>