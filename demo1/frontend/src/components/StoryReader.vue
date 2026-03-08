<!-- src/components/StoryReader.vue -->
<template>
  <div class="relative h-screen bg-gradient-to-b from-blue-50 to-purple-50 overflow-hidden">
    <swiper-container
      effect="coverflow"
      :slides-per-view="1"
      :centered-slides="true"
      :pagination="{ clickable: true }"
      class="h-full"
    >
      <swiper-slide v-for="(page, index) in pages" :key="index">
        <div class="relative w-full h-full">
          <img
            :src="page.imageUrl"
            class="absolute inset-0 w-full h-full object-cover"
            alt="페이지"
          />
          <div class="absolute bottom-0 left-0 right-0 p-8 bg-white/80 backdrop-blur-sm rounded-t-3xl shadow-xl">
            <p class="text-2xl font-nanum-pen leading-relaxed text-center text-gray-800">
              {{ page.text }}
            </p>
          </div>
          <div class="absolute top-4 right-4 bg-orange-500 text-white px-4 py-2 rounded-full text-lg">
            {{ index + 1 }} / {{ pages.length }}
          </div>
        </div>
      </swiper-slide>
    </swiper-container>
    <button
      @click="saveAsPDF"
      class="absolute top-4 left-4 z-50 px-6 py-3 bg-green-600 text-white rounded-full shadow-lg hover:bg-green-700 flex items-center gap-2"
      :disabled="saving"
    >
      <span v-if="saving">PDF 생성 중...</span>
      <span v-else>PDF로 저장하기 📄</span>
    </button>
  </div>
  <div style="position: absolute; left: -9999px; top: 0;">
  <div id="pdf-content" style="width: 210mm; background: white;">
    <div v-for="(page, index) in props.pages" :key="index" 
         style="width: 210mm; height: 296.8mm; position: relative; overflow: hidden; display: block; clear: both;">
      <img :src="page.imageUrl" style="width: 100%; height: 100%; object-fit: cover; display: block;">
      
      <div style="position: absolute; bottom: 0; width: 100%; padding: 40px; background: rgba(255,255,255,0.9); text-align: center;">
        <p style="font-size: 20pt; color: #333; word-break: keep-all; margin: 0;">{{ page.text }}</p>
      </div>
    </div>
  </div>
</div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import html2pdf from 'html2pdf.js'
import jsPDF from 'jspdf'
import html2canvas from 'html2canvas'
import { nextTick } from 'vue'

const props = defineProps<{
  pages: Array<{ imageUrl: string; pageNumber: number }>
  pageTexts: string[]
}>()

const saving = ref(false)

// URL 이미지를 Base64 데이터로 변환하는 헬퍼 함수
const toBase64 = async (url) => {
  try {
    const response = await fetch(url);
    const blob = await response.blob();
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onloadend = () => resolve(reader.result);
      reader.onerror = reject;
      reader.readAsDataURL(blob);
    });
  } catch (err) {
    console.error("이미지 변환 실패:", url, err);
    return url; // 실패 시 원본 반환
  }
}


const saveAsPDF = async () => {
  if (saving.value || props.pages.length === 0) return;
  saving.value = true;

  try {
    const element = document.getElementById('pdf-content');
    if (!element) return;

    // 1. 이미지 로딩 완료 대기 (중요!)
    const images = element.getElementsByTagName('img');
    const imagePromises = Array.from(images).map(img => {
      if (img.complete) return Promise.resolve();
      return new Promise(resolve => { img.onload = resolve; img.onerror = resolve; });
    });
    await Promise.all(imagePromises);

    // 2. PDF 설정
    const opt = {
      margin: 0,
      filename: '동화책.pdf',
      image: { type: 'jpeg', quality: 0.98 },
      html2canvas: { 
        scale: 2, 
        useCORS: true, 
        logging: true,
        allowTaint: false,
        proxy: null // 이미지 서버 설정에 따라 필요할 수 있음
      },
      jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
    };

    // 3. 실행
    await html2pdf().from(element).set(opt).save();

  } catch (err) {
    console.error('PDF 상세 에러:', err);
    alert('PDF 생성 중 오류가 발생했습니다.');
  } finally {
    saving.value = false;
  }
};

const saveAsPDF222 = async () => {

  console.log(props.pages)
    
  if (saving.value || props.pages.length === 0) return

  saving.value = true
  await nextTick()

  try {
    const slides = document.querySelectorAll('.swiper-slide')
    const tempContainer = document.createElement('div')
    
    // [수정] 화면 밖으로 밀어내되, 투명하게 만들어 레이아웃은 유지
    tempContainer.id = 'pdf-temp-container'
    tempContainer.style.position = 'fixed'
    tempContainer.style.zIndex = '-1'
    tempContainer.style.opacity = '0'
    tempContainer.style.top = '0'
    tempContainer.style.left = '0'
    tempContainer.style.width = '210mm' // A4 너비 강제

    // 1. 모든 슬라이드를 순차적으로 복제 및 이미지 변환
    for (const slide of slides) {
      const clone = slide.cloneNode(true)
      
      // [수정] 인라인 스타일 강제 부여 (CSS 클래스가 복제 안 될 수 있음)
      clone.style.display = 'block'
      clone.style.width = '210mm'
      clone.style.height = '297mm' // A4 높이 강제 (꽉 차게)
      clone.style.overflow = 'hidden'
      clone.style.pageBreakAfter = 'always'
      clone.style.breakAfter = 'page'

      // 2. [핵심] 이미지 Base64 변환 후 소스 교체
      const images = clone.querySelectorAll('img')
      for (const img of images) {
        const base64Data = await toBase64(img.src)
        img.src = base64Data
        img.style.width = '100%' // 이미지 크기 강제 조절
        img.style.height = 'auto'
      }
      
      tempContainer.appendChild(clone)
    }

    document.body.appendChild(tempContainer)

    // [수정] 브라우저가 레이아웃을 다시 그릴 수 있도록 짧은 대기 시간 부여
    await new Promise(resolve => setTimeout(resolve, 500))

    const opt = {
      margin: 0,
      filename: '세현이의_동화책.pdf',
      image: { type: 'jpeg', quality: 0.98 },
      html2canvas: { 
        scale: 2,           // 해상도 2배
        useCORS: true, 
        logging: true,      // 콘솔에서 에러 로그 확인용
        width: 794,         // A4 너비(px) 대략적 고정
        windowWidth: 794    // 캔버스 렌더링 너비 고정
      },
      jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' },
      pagebreak: { mode: 'css' } 
    }

    // 3. PDF 생성 (from에 ID나 Element 직접 전달)
    await html2pdf().from(tempContainer).set(opt).save()

    // 4. 완료 후 제거
    document.body.removeChild(tempContainer)

  } catch (err) {
    console.error('PDF 상세 오류:', err)
  } finally {
    saving.value = false
  }
}

</script>

<style>
/* 필요 시 Swiper 스타일 임포트 (main.ts에서 bundle로 등록했으니 대부분 필요 없음) */
@import 'swiper/css/bundle';  /* 모든 효과 + pagination 한 번에 포함 */
@import 'swiper/css';
@import 'swiper/css/effect-coverflow';
@import 'swiper/css/pagination';
swiper-container {
  --swiper-pagination-color: #f97316;
  --swiper-pagination-bullet-inactive-color: #94a3b8;
}
</style>