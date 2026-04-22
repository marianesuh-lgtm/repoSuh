<template>
  <div class="story-detail-container">
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>동화책을 펼치는 중...</p>
    </div>

    <div v-else-if="storyData" class="story-content-wrapper">
      <header class="detail-header">
        <button @click="router.back()" class="btn-back">← 목록</button>
        <h1 class="story-title">{{ storyData.title }}</h1>
        <div class="header-spacer"></div>
      </header>

      <div class="swiper-section">
        <swiper-container
          effect="coverflow"
          :slides-per-view="1"
          :centered-slides="true"
          :pagination="{ clickable: true }"
          class="story-swiper"
          @swiperslidechange="handleSlideChange"
        >
          <swiper-slide v-for="(page, index) in pages" :key="index">
            <div class="slide-content">
              <img :src="page.image_url" class="page-image" alt="story-page" />
              
<div class="text-overlay">
  <p class="story-text">
    <span 
      v-for="(char, charIdx) in page.content" 
      :key="charIdx"
      :class="{ 'highlight-char': isCurrentChar(charIdx) }"
    >
      {{ char }}
    </span>
  </p>
</div>
              <div class="page-badge">
                {{ index + 1 }} / {{ pages.length }}
              </div>
            </div>
          </swiper-slide>
        </swiper-container>
      </div>

      <div class="action-buttons">
        <button class="btn-tts" @click="handleTTS" :disabled="isLoadingTTS">
        <span v-if="isLoadingTTS">⏳ 목소리 만드는 중...</span>
        <span v-else>{{ isSpeaking ? '🔇 멈추기' : '🔊 읽어주기' }}</span>
        </button>
        <button class="btn-pdf" @click="saveAsPDF" :disabled="saving">
          {{ saving ? '⏳ 저장 중...' : '💾 PDF 저장' }}
        </button>
      </div>
    </div>

    <div style="position: absolute; left: -9999px; top: 0;">
      <div id="pdf-content" v-if="pages.length > 0" style="width: 210mm; background: white;">
        <div v-for="(page, index) in pages" :key="index" class="pdf-page">
          <img :src="page.image_url" crossOrigin="anonymous" style="width: 100%; height: 100%; object-fit: cover;">
          <div class="pdf-text-box">
            <p class="pdf-text">{{ page.content }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import { register } from 'swiper/element/bundle';
import html2pdf from 'html2pdf.js';
import { getMyStory } from '@/api/storyApi'          // 예: src/api/bookApi.js

register();

const route = useRoute();
const router = useRouter();

const loading = ref(true);
const storyData = ref(null);
const pages = ref([]);
const activeIndex = ref(0);
const isSpeaking = ref(false);
const saving = ref(false);
const isLoadingTTS = ref(false); // 로딩 상태 추가
let audio = null;

// 1. 데이터 로드 및 페이지 변환
onMounted(async () => {
  try {
    const id = route.params.id;
    const storyId = route.params.id;
    console.log("storyId::",storyId);
   const response = await getMyStory(storyId);
    console.log("response::",response[0].pages);
    // 실제 API 주소로 수정 필요
    // const response = await axios.get(`/api/stories/${storyId}`, {
    //   headers: { Authorization: `Bearer ${localStorage.getItem('accessToken')}` }
    // });
//    story.value = response;
    
    storyData.value = response[0];
    console.log("storyData::",storyData);
    
    // 서버 데이터 구조에 따라 pages 배열 매핑 (StoryCreateContent와 맞춤)
    if (response && response.length > 0) {
      storyData.value = { title: response[0].title || '나만의 동화' };
      pages.value = response[0].pages;
       
    } 
    console.log("pages::",pages.value);
    console.log("매핑된 페이지 수:", pages.value.length);

} catch (error) {
    console.error("로드 실패:", error);
  } finally {
    loading.value = false;
  }
});


const isCurrentChar = (index) => {
  if (highlightIndex.value === -1) return false;
  
  const currentPage = pages.value[activeIndex.value];
  const text = currentPage.content;
  
  // 현재 highlightIndex부터 다음 공백(' ')이 나오기 전까지가 하나의 단어
  let wordEnd = text.indexOf(' ', highlightIndex.value);
  if (wordEnd === -1) wordEnd = text.length;
  
  return index >= highlightIndex.value && index < wordEnd;
};


const handleSlideChange = (e) => {
  activeIndex.value = e.detail[0].activeIndex;
};

const highlightIndex = ref(-1); // 현재 읽고 있는 글자의 시작 위치

// 2. TTS 기능 (StoryCreateContent 로직 복제)
const handleTTS = () => {
  if (!('speechSynthesis' in window)) return;

  if (isSpeaking.value) {
    window.speechSynthesis.cancel();
    isSpeaking.value = false;
    return;
  }

  const currentText = pages.value[activeIndex.value]?.content;
  //const textToRead = currentText?.text || currentText?.content;

  if (!currentText) return;

  const utterance = new SpeechSynthesisUtterance(currentText);
  utterance.lang = 'ko-KR';
  utterance.rate = 0.6;
  utterance.pitch = 0.8;
  utterance.onend = () => {isSpeaking.value = false; highlightIndex.value = -1;};
  
  // 💡 단어/문장 경계가 바뀔 때마다 실행됨
  utterance.onboundary = (event) => {
    console.log("148 event:::", event);
    if (event.name === 'word') {
      highlightIndex.value = event.charIndex;
    }
  };

  window.speechSynthesis.speak(utterance);
  isSpeaking.value = true;

};

const handleTTS222 = async () => {
  // 1. 이미 재생 중이면 정지
  if (isSpeaking.value) {
    stopTTS();
    return;
  }

  // 2. 현재 페이지 텍스트 가져오기
  const currentPage = pages.value[activeIndex.value];
  const textToRead = currentPage?.text || currentPage?.content;

  if (!textToRead) return;

  try {
    isLoadingTTS.value = true; // 서버 응답 대기 시작

    // 3. Kokoro TTS API 호출
    const response = await axios.post(
      '/tts-api/v1/audio/speech',
      {
        model: 'kokoro', // 모델명
        input: "currentPage is open", // 읽을 내용
        voice: 'ko', // 목소리 설정 (서버 설정에 따라 변경 가능)
        response_format: 'mp3',
        speed: 1.0
      },
      {
        responseType: 'blob', // 오디오 파일(이진 데이터)로 받기
      }
    );

    // 4. Blob 데이터를 재생 가능한 URL로 변환
    const audioUrl = URL.createObjectURL(response.data);
    
    // 5. 오디오 객체 생성 및 재생
    if (audio) {
      audio.pause();
    }
    
    audio = new Audio(audioUrl);
    
    audio.onplay = () => {
      isSpeaking.value = true;
      isLoadingTTS.value = false;
    };

    audio.onended = () => {
      isSpeaking.value = false;
      URL.revokeObjectURL(audioUrl); // 메모리 해제
    };

    await audio.play();

  } catch (error) {
    console.error("TTS 호출 에러:", error);
    alert("음성 서버에 연결할 수 없습니다.");
    isSpeaking.value = false;
    isLoadingTTS.value = false;
  }
};

const stopTTS = () => {
  if (audio) {
    audio.pause();
    audio.currentTime = 0;
  }
  isSpeaking.value = false;
};



// 3. PDF 저장 기능 (StoryCreateContent 로직 복제)
const saveAsPDF = async () => {
  if (saving.value || pages.value.length === 0) return;
  saving.value = true;

  try {
    await nextTick();
    const element = document.getElementById('pdf-content');
    const opt = {
      margin: 0,
      filename: `${storyData.value.title}.pdf`,
      image: { type: 'jpeg', quality: 1.0 },
      html2canvas: { scale: 2, useCORS: true },
      jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
    };
    await html2pdf().from(element).set(opt).save();
  } catch (err) {
    alert('PDF 생성 실패');
  } finally {
    saving.value = false;
  }
};
</script>

<style scoped>
/* StoryCreateContent.vue의 스타일 가이드 적용 */
.story-detail-container {
  min-height: 100vh;
  background: #0f172a; /* 어두운 배경 */
  padding: 20px;
  color: white;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.story-title {
  font-family: 'Nanum Pen Script', cursive;
  font-size: 2.5rem;
  color: #e0f2fe;
  text-shadow: 0 2px 10px rgba(0,0,0,0.5);
}

.swiper-section {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  aspect-ratio: 3/4; /* 동화책 비율 */
}

.slide-content {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 24px;
  overflow: hidden;
}

.page-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.text-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 50px 30px; /* 텍스트 주변 여백을 늘려 가독성 확보 */
  
  /* 배경 그라데이션을 더 낮고 투명하게 조절하여 이미지를 밝게 유지합니다 */
  background: linear-gradient(to top, 
    rgba(0,0,0,0.5) 0%, 
    rgba(0,0,0,0.1) 40%, 
    transparent 100%
  );
  
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  min-height: 140px; 
}

.story-text span {
  display: inline;
  transition: all 0.1s ease;
  color: #f9f7f7; /* 기본 글자색 */
}

.highlight-char {
  /* 1. 배경색을 더 불투명하고 진한 노란색(#FFD700)으로 변경 */
  background-color: rgba(255, 215, 0, 0.9) !important; 
  
  /* 2. 하이라이트된 단어의 글자색을 검정으로 변경하여 노란 배경과 대비시킴 */
  color: #000000 !important; 
  
  /* 3. 하이라이트 시에는 기존의 흰색 글자 외곽선(text-shadow)을 제거하여 깔끔하게 표시 */
  text-shadow: none !important;
  
  font-weight: bold;
  border-radius: 6px;
  padding: 0 6px;
  
  /* 4. 단어가 약간 커지는 효과와 선명한 그림자로 강조 */
  transform: scale(1.05);
  display: inline-block;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
  transition: all 0.1s ease-in-out;
}

.story-text {
  font-family: 'Nanum Pen Script', cursive;
  text-align: center;
  white-space: pre-line;
  color: #ffffff; /* 기본 글자색은 항상 흰색 */
  margin: 0;
  font-size: 2.7rem; /* 글자 크기를 살짝 키워 시인성 확보 */
  line-height: 1.4;
  
  /* 핵심: 글자 외곽선(Text Shadow)을 다중으로 겹쳐 어떠한 배경에서도 잘 보이게 합니다 */
  text-shadow: 
    -2px -2px 0 #000,  
     2px -2px 0 #000,
    -2px  2px 0 #000,
     2px  2px 0 #000,
     0px  4px 10px rgba(0,0,0,0.8);
}

.page-badge {
  position: absolute;
  top: auto;        /* 위쪽 고정 해제 */
  bottom: 15px;     /* 아래쪽으로 이동 */
  right: 15px;
  background: rgba(0, 0, 0, 0.5); /* 너무 튀지 않게 반투명 검정 */
  padding: 3px 10px;
  font-size: 0.9rem;
  z-index: 10;      /* 텍스트보다 위에 보이게 */
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 30px;
}

.btn-tts, .btn-pdf {
  padding: 12px 25px;
  border-radius: 30px;
  border: none;
  font-weight: bold;
  cursor: pointer;
  transition: 0.3s;
}

.btn-tts { background: #fbbf24; color: #000; }
.btn-pdf { background: #38bdf8; color: #fff; }

/* PDF 전용 스타일 */
.pdf-page {
  width: 210mm;
  height: 296mm; /* A4(297mm)보다 1mm 작게 설정하여 안전하게 한 페이지에 넣음 */
  position: relative;
  overflow: hidden; /* 영역 밖으로 나가는 요소 차단 */
  
  /* 페이지 분할 설정 */
  page-break-after: always; /* 각 페이지 끝에서만 분할 */
  page-break-inside: avoid; /* 페이지 중간에서 잘리지 않게 함 */
  margin: 0;
  padding: 0;
}

.pdf-text-box {
  position: absolute;
  bottom: 0;
  width: 100%;
  padding: 40px;
  background: rgba(255,255,255,0.85);
  text-align: center;
}
.pdf-text {
  font-size: 24pt;
  font-family: 'Nanum Pen Script', cursive;
  color: #333;
}
</style>