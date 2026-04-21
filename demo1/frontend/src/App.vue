<!-- App.vue -->
<template>
  <div class="app-container">
    <link
      href="https://fonts.googleapis.com/css2?family=Nanum+Gothic:wght@400;700;800&family=Nanum+Pen+Script&display=swap"
      rel="stylesheet"
    />
    <StarField />

    <div class="header">
      <div class="user-menu">
        <template v-if="!isLoggedIn">
          <router-link to="/login" class="menu-item highlight">로그인</router-link>
          <span class="divider">|</span>
        </template>
        <template v-else>
          <div class="user-profile-wrapper">
      <img 
        :src="profileImage || '/default-profile.png'" 
        alt="프로필" 
        class="profile-img"
      />
      <span class="menu-item highlight">{{ userName }}님 ✨</span>
    </div>
          <router-link to="/my-stories" class="menu-item highlight">내가 만든 이야기</router-link>
          <router-link v-if="userRole==='USER'" to="/children" class="menu-item highlight">자녀등록</router-link>
          <button @click="handleLogout" class="logout-btn">로그아웃</button>
        </template>
      </div>
       <div class="emoji-book">       <!-- 할머니 마스코트 -->
       </div>
        <h1 class="title">        <img 
          src="@/assets/MrsShakespeare2.png"    
          alt="Mrs. Shakespeare"
          class="mascot-image"
        />
          나만의 마법 동화 ✨</h1>
        <p class="subtitle">나만의 이야기를 만들어봐요!</p>
      </div>

      <!-- phase를 v-model로 내려보냄 -->
      <router-view v-model:phase="currentPhase" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import StarField from '@/components/StarField.vue'
import { getInfoFromToken } from '@/utils/auth' // 유틸리티 함수 임포트

const currentPhase = ref('intro')  // ← 여기서 phase 관리 시작
const isLoggedIn = ref(false)
const userName = ref('여행자')
const router = useRouter()
const route = useRoute()
const userRole = ref('')
const profileImage = ref('')

// 로그인 상태 체크 함수
const checkLoginStatus = () => {
  const token = localStorage.getItem('accessToken')
  
  if (token) {
    const userData = getInfoFromToken(token)
    if (userData) {
      isLoggedIn.value = true
      // 페이로드 확인: 귀하의 토큰 정보에 sub와 role이 들어있음을 확인했습니다.
      userName.value = userData.nickname || '여행자'
      userRole.value = userData.role || '' 
      profileImage.value = userData.profileImage || ''
      
      // 콘솔 확인 시 .value를 붙여야 명확한 값이 찍힙니다.
      console.log("로그인 상태:", isLoggedIn.value)
      console.log("유저 이름:", userName.value)
      console.log("유저 권한:", userRole.value)
      console.log("프로파일이미지:", profileImage.value)
    }
  } else {
    isLoggedIn.value = false
    userRole.value = ''
  }
}

onMounted(() => {
  checkLoginStatus()
})

// 페이지가 바뀔 때마다 로그인 상태를 다시 확인 (토큰 저장 직후 반영을 위해)
watch(() => route.path, () => {
  checkLoginStatus()
})

const handleLogout = () => {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('userName')
  isLoggedIn.value = false
   console.log(isLoggedIn);
 alert('로그아웃 되었습니다.')
  router.push('/login')
}
</script>

<style scoped>
/* 상단 헤더 전체를 감싸는 컨테이너 */
.header {
  background-color: #170528; /* 하단 카드와 비슷한 다크 네이비 색상 */
  /* 만약 하단 카드처럼 약간의 투명감을 주려면 아래를 사용하세요 */
  /* background: rgba(26, 35, 126, 0.9); */
  
  margin: 20px;            /* 주변 여백 */
  padding: 30px;           /* 안쪽 여백 */
  border-radius: 30px;     /* 하단 카드처럼 둥글게 */
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3); /* 살짝 그림자 추가 */
  
  /* 텍스트 정렬 */
  text-align: left;
  position: relative;
}

/* 제목 텍스트 (나만의 마법 동화) */
.title {
  color: #ffffff;          /* 흰색으로 변경 */
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 15px;
}

/* 서브 타이틀 (나만의 이야기를 만들어봐요!) */
.subtitle {
  color: rgba(255, 255, 255, 0.8); /* 약간 투명한 흰색 */
  margin-left: 5px;
}

/* 로그인/자녀등록 버튼을 헤더 안으로 넣고 싶을 때 */
.logout-btn {
  background: transparent;
  border: 1px solid rgba(255, 215, 0, 0.5); /* 자녀등록과 통일감 */
  color: #ffd700 !important;
  border-radius: 20px;
  padding: 5px 12px;
  cursor: pointer;
}

/* 2. 유저 정보와 이미지를 가로로 정렬 */
.user-profile-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.profile-img {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #ffd700; /* 황금색 테두리 추가 */
}

/* 3. 메뉴 아이템들이 세로로 쌓이지 않게 가로 정렬 강제 */
.user-menu {
  position: absolute;
  top: 25px;
  right: 25px;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8px; /* 아이템이 많아지므로 간격을 살짝 줄임 (기존 12px) */
  z-index: 10;
}

/* 1. 공통 스타일: 로그인과 자녀등록 모두에게 적용됨 */
.menu-item {
  text-decoration: none;
  color: #ffffff !important;   /* 무조건 흰색으로 강제 */
  font-size: 1rem;
  font-weight: 500;
  padding: 5px 10px;
  transition: all 0.3s ease;
  display: inline-block;       /* 영역을 확보함 */
  cursor: pointer;
  z-index: 1000;               /* 별들보다 위에 표시 */
}

/* 2. 마우스를 올렸을 때 효과 */
.menu-item:hover {
  color: #ffd700 !important;   /* 황금색으로 변경 */
  text-shadow: 0 0 10px rgba(255, 215, 0, 0.8);
}

/* 3. 하이라이트 전용 스타일: '자녀등록'처럼 강조하고 싶을 때 */
.menu-item.highlight {
  color: #ffd700 !important;   /* 처음부터 황금색 */
  border: 1px solid rgba(255, 215, 0, 0.5); /* 테두리 추가 */
  border-radius: 20px;
  margin-left: 5px;
  white-space: nowrap;
}

.divider {
  color: rgba(255, 255, 255, 0.3);
}

.user-name {
  color: #fff;
  margin-right: 10px;
}

.btn-logout {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #2b0c0c;
  padding: 4px 10px;
  border-radius: 15px;
  cursor: pointer;
  font-size: 0.8rem;
}

.btn-logout:hover {
  background: rgba(255, 0, 0, 0.2);
}

/* 기존 스타일들... */
</style>