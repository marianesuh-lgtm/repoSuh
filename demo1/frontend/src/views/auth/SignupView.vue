<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2>✨ 마법 동화 나라 가입하기</h2>
      <p class="auth-subtitle">계정을 만들고 나만의 이야기를 저장하세요!</p>

      <form @submit.prevent="handleSignup">
        <div class="input-group">
          <label>이메일 주소</label>
          <input 
            v-model="signupData.email" 
            type="email" 
            placeholder="example@mail.com" 
            required 
          />
        </div>

        <div class="input-group">
          <label>비밀번호</label>
          <input 
            v-model="signupData.password" 
            type="password" 
            placeholder="8자 이상 입력하세요" 
            required 
          />
        </div>

        <div class="input-group">
          <label>비밀번호 확인</label>
          <input 
            v-model="passwordConfirm" 
            type="password" 
            placeholder="다시 한번 입력하세요" 
            required 
          />
          <span v-if="passwordError" class="error-msg">{{ passwordError }}</span>
        </div>

        <div class="input-group">
          <label>사용자 이름</label>
          <input 
            v-model="signupData.name" 
            type="text" 
            placeholder="불러드릴 이름을 입력하세요" 
            required 
          />
        </div>

        <button type="submit" class="auth-btn" :disabled="isLoading">
          {{ isLoading ? '신비한 문이 열리는 중...' : '가입하기' }}
        </button>
      </form>

      <div class="auth-footer">
        이미 계정이 있으신가요? <router-link to="/login">로그인하기</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import {signup}  from '@/api/storyApi'

const router = useRouter();
const isLoading = ref(false);

const signupData = ref({
  email: '',
  password: '',
  name: ''
});
const passwordConfirm = ref('');

// 비밀번호 일치 여부 확인
const passwordError = computed(() => {
  if (passwordConfirm.value && signupData.value.password !== passwordConfirm.value) {
    return '비밀번호가 일치하지 않습니다.';
  }
  return '';
});

const handleSignup = async () => {
  if (passwordError.value) return;

  isLoading.value = true;
  try {
    // 백엔드 회원가입 API 주소로 변경하세요
    const response = await signup(signupData.value);
    
    alert("가입을 축하드려요! 이제 로그인해 주세요. 🎉");
    router.push('/login');
  } catch (error) {
    console.error("가입 실패:", error);
    alert(error.response?.data?.message || "가입 중 오류가 발생했습니다.");
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.auth-container {
  position: relative; /* z-index를 쓰기 위해 필요 */
  z-index: 10;        /* 다른 요소보다 높은 숫자로 설정 */
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 20px;
}

.auth-card {
  background: rgba(12, 12, 24, 0.9);
  padding: 40px;
  border-radius: 30px;
  color: rgb(242, 241, 244);
  width: 100%;
  max-width: 450px;
  box-shadow: 0 15px 35px rgba(0,0,0,0.4);
  text-align: center;
}

h2 { margin-bottom: 10px; color: #ffd700; }
.auth-subtitle { margin-bottom: 30px; opacity: 0.8; font-size: 0.9rem; }

.input-group { text-align: left; margin-bottom: 20px; }
.input-group label { display: block; margin-bottom: 8px; font-weight: bold; font-size: 0.9rem; }
.input-group input {
  width: 100%;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.05);
  color: white;
  outline: none;
}
.input-group input:focus { border-color: #ffd700; background: rgba(255, 255, 255, 0.1); }

.error-msg { color: #ff6b6b; font-size: 0.8rem; margin-top: 5px; display: block; }

.auth-btn {
  width: 100%;
  padding: 15px;
  border-radius: 25px;
  border: none;
  background: #ffd700;
  color: #1a237e;
  font-size: 1.1rem;
  font-weight: bold;
  cursor: pointer;
  margin-top: 10px;
}
.auth-btn:disabled { background: #666; cursor: not-allowed; }

.auth-footer { margin-top: 25px; font-size: 0.9rem; opacity: 0.7; }
.auth-footer a { color: #ffd700; text-decoration: none; font-weight: bold; }
</style>