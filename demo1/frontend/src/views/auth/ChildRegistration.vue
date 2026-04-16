<template>
  <div class="registration-container">
    <div class="registration-card">
      <h2>👶 자녀 정보 등록</h2>
      <p>아이의 정보를 입력하면 맞춤형 동화를 만들어드려요!</p>
      
      <form @submit.prevent="submitForm">
        <div class="input-group">
          <label>아이 이름</label>
          <input v-model="childData.name" type="text" placeholder="이름을 입력하세요" required />
        </div>

        <div class="input-group">
          <label>아이 나이</label>
          <input v-model.number="childData.age" type="number" placeholder="나이를 입력하세요" required />
        </div>

        <div class="input-group">
          <label>성별</label>
          <div class="gender-options">
            <button type="button" :class="{ active: childData.gender === 'BOY' }" @click="childData.gender = 'BOY'">남아</button>
            <button type="button" :class="{ active: childData.gender === 'GIRL' }" @click="childData.gender = 'GIRL'">여아</button>
          </div>
        </div>

        <button type="submit" class="submit-btn" :disabled="isLoading">
          {{ isLoading ? '등록 중...' : '등록하기' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const isLoading = ref(false);

const childData = ref({
  name: '',
  age: null,
  gender: 'BOY' // 기본값
});

const submitForm = async () => {
  const token = localStorage.getItem('accessToken');
  if (!token) {
    alert("로그인이 필요합니다.");
    return;
  }

  isLoading.value = true;
  try {
    // 백엔드 API 엔드포인트에 맞게 수정하세요 (예: /api/children)
    await axios.post('http://your-api-server.com/api/children', childData.value, {
      headers: { Authorization: `Bearer ${token}` }
    });
    
    alert("자녀 등록이 완료되었습니다! ✨");
    router.push('/'); // 등록 후 홈으로 이동
  } catch (error) {
    console.error("등록 실패:", error);
    alert("등록에 실패했습니다. 다시 시도해 주세요.");
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>.registration-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
}

.registration-card {
  background: rgba(53, 48, 56, 0.9); /* 메인 카드와 통일감 */
  padding: 40px;
  border-radius: 30px;
  color: white;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.5);
  text-align: center;
}

.input-group {
  text-align: left;
  margin-bottom: 20px;
}

.input-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
}

.input-group input {
  width: 100%;
  padding: 12px;
  border-radius: 10px;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.gender-options {
  display: flex;
  gap: 10px;
}

.gender-options button {
  flex: 1;
  padding: 10px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  background: none;
  color: white;
  cursor: pointer;
}

.gender-options button.active {
  background: #ffd700;
  color: #1a237e;
  border-color: #ffd700;
  font-weight: bold;
}

.submit-btn {
  width: 100%;
  padding: 15px;
  border-radius: 25px;
  border: none;
  background: #ffd700;
  color: #1a237e;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  margin-top: 20px;
}

.submit-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

</style>