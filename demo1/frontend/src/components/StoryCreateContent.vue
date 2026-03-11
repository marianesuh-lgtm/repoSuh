<!-- StoryCreate.vue -->
<template>
  <div>
    <!-- ProgressBar -->
    <ProgressBar :current-step="currentStep" :selections="selections" />

    <!-- 단계별 선택 화면 -->
    <div v-if="phase === 'making'" class="choice-container">
      <div class="step-header">
        <h2>{{ STEP_LABELS[currentStepKey].title }}</h2>
        <p>{{ STEP_LABELS[currentStepKey].desc }}</p>
      </div>

      <ChoiceCard
        v-for="(choice, cat) in currentChoices"
        :key="cat"
        :label="choice.label"
        :emoji="choice.emoji"
        :options="choice.options"
        :selected="currentSelections[cat]"
        @select="handleSelect(cat, $event)"
      />

      <div class="navigation">
        <button class="btn-prev" @click="goPrev">
          ← {{ currentStep === 0 ? '처음으로' : '이전' }}
        </button>

        <button
          v-if="currentStep < STEPS.length - 1"
          class="btn-next"
          :disabled="!allSelectedForStep"
          @click="currentStep++"
        >
          다음 →
        </button>

        <button
          v-else
          class="btn-finish"
          :disabled="!allSelectedForStep"
          @click="generateStory"
        >
          ✨ 동화 완성하기!
        </button>
      </div>
    </div>

    <!-- 이야기 결과 화면 -->
    <div v-else-if="phase === 'story'" class="story-section">
      <h2 v-if="storyTitle" class="story-title">{{ storyTitle }}</h2>

      <!-- <div v-if="illustrationEmojis.length" class="emoji-illust">
        <span
          v-for="(emoji, i) in illustrationEmojis"
          :key="i"
          class="float-emoji"
          :style="{ animationDelay: `${i * 0.3}s` }"
        >{{ emoji }}</span>
      </div> -->
<div v-if="pages.length" > 
    <swiper-container
      effect="coverflow"
      :slides-per-view="1"
      :centered-slides="true"
      :pagination="{ clickable: true }"
      class="h-full"
      @swiperslidechange="handleSlideChange"
    >
      <swiper-slide v-for="(page, index) in pages" :key="index">
        <div class="relative w-full h-full">
          <img
            :src="page.imageUrl"
            class="absolute inset-0 w-full h-full object-cover"
            alt="페이지"
          />
          
          <div class="absolute bottom-0 left-0 right-0 p-8 bg-white/80 backdrop-blur-sm rounded-t-3xl shadow-xl">
            <p class="text-xl font-nanum-pen leading-relaxed text-center text-white whitespace-pre-line drop-shadow-lg">
              {{ page.text }}
            </p>
          </div>
          <div class="absolute top-4 right-4 bg-orange-500 text-white px-4 py-2 rounded-full text-lg">
            {{ index + 1 }} / {{ pages.length }}
          </div>
        </div>
      </swiper-slide>
    </swiper-container>
    </div>
 
      <StoryDisplay @tts="handleTTS"
        :story="story"
        :is-generating="isGenerating"
        :onTTS="handleTTS"
        :is-speaking="isSpeaking"
        :disabled="false"
      />

      <div v-if="!isGenerating && story" class="story-actions">
        <button class="btn-save" @click="saveAsPDF">💾 동화 저장하기</button>
        <button class="btn-reset" @click="handleReset">🌟 새 동화 만들기</button>
      </div>

      <div v-if="!isGenerating && story" class="selection-summary">
        <h3>📝 나의 선택 요약</h3>
        <div class="summary-grid">
          <div v-for="step in STEPS" :key="step" class="summary-item">
            <strong>[{{ step }}단계] {{ STEP_LABELS[step].title }}</strong>
            <div v-for="(val, i) in selections[step]" :key="i" class="summary-line">
              • {{ val }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
 <div style="position: absolute; left: -9999px; top: 0;">
  <div id="pdf-content" v-if="pages.length > 0" style="width: 210mm; background: white;">
    <div v-for="(page, index) in pages" :key="index" 
         style="width: 210mm; 
                height: 296.8mm; /* 297mm보다 아주 살짝 작게 설정하여 안전마진 확보 */
                page-break-after: always; 
                page-break-inside: avoid; /* 페이지 내부에서 잘림 방지 */
                position: relative; 
                overflow: hidden;
                box-sizing: border-box; /* 패딩이 높이에 영향을 주지 않도록 */
                display: block;">
      
      <img :src="page.imageUrl" 
           crossOrigin="anonymous"
           style="width: 100%; height: 100%; object-fit: cover; display: block;">
      
      <div style="position: absolute; bottom: 0; width: 100%; padding: 40px; background: rgba(255,255,255,0.85); text-align: center;">
        <p style="font-size: 24pt; font-family: 'Nanum Pen Script', cursive; color: #333; margin: 0; white-space: pre-line;">
          {{ page.text }}
        </p>
      </div>
    </div>
  </div>
</div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import ProgressBar from './ProgressBar.vue'
import ChoiceCard from './ChoiceCard.vue'
import StoryDisplay from './StoryDisplay.vue'
import { generateBook } from '@/api/storyApi'          // 예: src/api/bookApi.js
import { useRouter } from 'vue-router'
import { nextTick } from 'vue'
import { register } from 'swiper/element/bundle';
import html2pdf from 'html2pdf.js' // 이 줄을 추가하세요!

register();

const activeIndex = ref(0);

// 슬라이드가 변경될 때 실행될 함수
const handleSlideChange = (e) => {
  // swiper-container의 이벤트에서 현재 인덱스를 가져옵니다.
  activeIndex.value = e.detail[0].activeIndex;
};

const pages = ref([]); // 화면에 그려질 페이지 배열
const saving = ref(false)

const router = useRouter()  // ← 추가

const props = defineProps({
  initialPhase: String
})

// 컴포넌트가 마운트/언마운트 상태 체크용 (이 5줄 추가!)
const isMounted = ref(false)

onMounted(() => {
  isMounted.value = true
  console.log('StoryCreateContent 마운트됨')
})

onUnmounted(() => {
  isMounted.value = false
  console.log('StoryCreateContent 언마운트됨')
})

const emit = defineEmits(['update:phase'])

const phase = ref(props.initialPhase || 'making')
const currentStep = ref(0)
const selections = ref({ 기: {}, 승: {}, 전: {}, 결: {} })
const story = ref('')
const isGenerating = ref(false)
const isSpeaking = ref(false)
const storyTitle = ref('')
const illustrationEmojis = ref([])
const utteranceRef = ref(null)

const STEPS = ['기', '승', '전', '결']

const STEP_LABELS = {
  기: { title: '🌱 이야기 시작', desc: '누가, 어디서, 어떤 일이?' },
  승: { title: '🌊 이야기 전개', desc: '어떤 일이 일어났나요?' },
  전: { title: '⚡ 이야기 위기', desc: '무슨 문제가 생겼나요?' },
  결: { title: '🌈 이야기 결말', desc: '어떻게 해결됐나요?' }
}

const CHOICES = {
  기: {
    character: { label: '주인공은 누구인가요?', emoji: '🦸', options: ['용감한 토끼 🐰', '마법사 고양이 🐱✨', '작은 용 🐲', '요정 공주 🧚', '로봇 강아지 🤖🐶', '외계인 친구 👽'] },
    place: { label: '어디서 시작하나요?', emoji: '🏠', options: ['마법의 숲 🌲', '하늘 위 성 🏰☁️', '바닷속 마을 🌊🐟', '우주선 안 🚀', '사탕 왕국 🍬', '비밀 동굴 🕳️'] },
    mood: { label: '오늘 기분은?', emoji: '😊', options: ['신나고 행복해요 😄', '조금 외로워요 😢', '뭔가 찾고 싶어요 🔍', '모험이 하고 싶어요 🗺️'] }
  },
  승: {
    event: { label: '무슨 일이 생겼나요?', emoji: '✨', options: ['마법 편지가 왔어요 📬✨', '새 친구를 만났어요 👫', '신기한 물건을 발견했어요 🪄', '마을이 위험해졌어요 ⚠️', '보물 지도를 찾았어요 🗺️', '마법이 사라졌어요 💫'] },
    companion: { label: '함께하는 친구는?', emoji: '🤝', options: ['말하는 새 🦜', '착한 도깨비 👹💚', '꼬마 요정 🧚', '현명한 할머니 👵', '용감한 기사 ⚔️', '혼자서 해요 💪'] }
  },
  전: {
    problem: { label: '어떤 문제가 생겼나요?', emoji: '😱', options: ['나쁜 마법사가 나타났어요 🧙‍♂️💀', '길을 잃어버렸어요 😰', '마법이 잘못됐어요 🌀', '친구가 사라졌어요 😭', '커다란 괴물이 왔어요 👾', '비밀 문이 잠겼어요 🔒'] },
    tryAction: { label: '어떻게 해결하려 했나요?', emoji: '💡', options: ['용기를 내서 싸웠어요 ⚔️', '지혜롭게 생각했어요 🤔💡', '친구들에게 도움 요청 🙏', '마법 주문을 외웠어요 ✨', '노래를 불렀어요 🎵', '선물을 나눠줬어요 🎁'] }
  },
  결: {
    solution: { label: '어떻게 해결됐나요?', emoji: '🎉', options: ['사랑의 힘으로! 💕', '용기와 지혜로! 🦁💡', '모두 함께 협력해서! 🤝', '마법이 돌아와서! ✨', '예상치 못한 방법으로! 😲', '꿈에서 힌트를 얻어서! 💭'] },
    ending: { label: '결말은 어떻게 됐나요?', emoji: '🌟', options: ['영원히 행복하게 살았어요 🌈', '새로운 모험을 떠났어요 🚀', '집으로 돌아왔어요 🏠❤️', '새 친구들과 함께 살아요 👨‍👩‍👧‍👦', '마을의 영웅이 됐어요 🏆', '멋진 선물을 받았어요 🎁'] }
  }
}

const STORY_IMAGES = {
  기: ['🌲✨', '🏰🌙', '🌊🐠', '🚀⭐', '🍬🌈', '🕳️🔮'],
  승: ['📬💌', '👫🌸', '🪄💫', '⚠️🔥', '🗺️🏆', '💫🌟'],
  전: ['🧙‍♂️⚡', '🌪️😰', '🌀💥', '😭💔', '👾🌑', '🔒🗝️'],
  결: ['💕🌈', '🦁⭐', '🤝🎉', '✨🌟', '😲🎊', '💭🌙']
}

const currentStepKey = computed(() => STEPS[currentStep.value])
const currentChoices = computed(() => CHOICES[currentStepKey.value] || {})
const currentSelections = computed(() => selections.value[currentStepKey.value] || {})

const allSelectedForStep = computed(() => {
  return Object.keys(currentChoices.value).every(k => currentSelections.value[k])
})

const handleSelect = (category, value) => {
  selections.value[currentStepKey.value] = {
    ...selections.value[currentStepKey.value],
    [category]: value
  }
}

const goPrev = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  } else {
    handleReset()
  }
}

const buildPrompt = () => {
  const s = selections.value
  return `당신은 4세~10세 어린이를 위한 동화 작가입니다. 아래 선택들을 바탕으로 따뜻하고 감동적인 동화를 써주세요.

[기 - 이야기 시작]
- 주인공: ${s.기?.character || ''}
- 장소: ${s.기?.place || ''}
- 기분/상황: ${s.기?.mood || ''}

[승 - 이야기 전개]
- 사건: ${s.승?.event || ''}
- 동행: ${s.승?.companion || ''}

[전 - 위기]
- 문제: ${s.전?.problem || ''}
- 시도: ${s.전?.tryAction || ''}

[결 - 결말]
- 해결: ${s.결?.solution || ''}
- 엔딩: ${s.결?.ending || ''}

규칙:
1. 반드시 한국어로 작성
2. 제목은 "제목: [창의적인 제목]" 형식으로 첫 줄에
3. 5~8단락으로 구성, 각 단락은 2~3문장
4. 어린이 눈높이에 맞는 쉽고 재미있는 언어 사용
5. 의성어, 의태어를 적극 활용 (예: 폴짝폴짝, 반짝반짝)
6. 따뜻한 교훈이 자연스럽게 담기도록
7. 500~700자 내외로 작성`
}

const generateStory = async () => {
    console.log("generateStory")
  isGenerating.value = true
 phase.value = 'story'
  emit('update:phase', 'story')

  console.log('로딩 시작 → isGenerating:', isGenerating.value)
  // 서버 호출 대신 임시 더미 데이터 사용
  await new Promise(resolve => setTimeout(resolve, 800))  // 0.8초 로딩 느낌 주기 (선택)
 try {
//await new Promise(resolve => setTimeout(resolve, 2000))

  // 임시로 사용할 더미 데이터 (실제 서버 응답 형식 흉내)
  const fakeResult = {
    title: "반짝이는 별빛 토끼의 모험",
    pages: [
      {
        pageNumber: 1,
        text: "옛날 옛적, 숲속에 반짝반짝 빛나는 작은 토끼가 살았어요.\n하늘에서 휙휙 날아온 마법 편지를 보고 깜짝 놀랐답니다!",
        imagePrompt: "cute sparkling bunny in magical forest, surprised expression, glowing letter flying from sky, children's book illustration, vibrant colors, 16:9"
      },
      {
        pageNumber: 2,
        text: "편지를 열어보니 마법사 고양이가 도와달라는 내용이었어요.\n토끼는 폴짝폴짝 뛰며 친구를 만나러 갔답니다.",
        imagePrompt: "brave bunny jumping through forest, magical cat waiting ahead, soft glowing light, whimsical style, 16:9"
      },
      {
        pageNumber: 3,
        text: "길을 가다 나쁜 마법사가 나타났어요!\n쿵쾅쿵쾅 다가오는 소리에 토끼는 살짝 무서웠지만 용기를 냈어요.",
        imagePrompt: "dark evil wizard approaching cute bunny, bunny looking brave, forest background, dramatic lighting, 16:9"
      },
      {
        pageNumber: 4,
        text: "친구 새와 함께 힘을 모았어요.\n모두가 손을 맞잡고 마법사를 설득했답니다!",
        imagePrompt: "bunny and talking bird holding hands with wizard, reconciliation scene, warm sunset, children's illustration, 16:9"
      },
      {
        pageNumber: 5,
        text: "마법사는 마음을 바꿔 착한 마법사가 되었어요.\n숲은 더 밝고 아름다워졌답니다.",
        imagePrompt: "happy forest with bunny, bird, reformed wizard smiling together, rainbow sky, joyful children's book style, 16:9"
      },
      {
        pageNumber: 6,
        text: "토끼와 친구들은 영원히 행복하게 지냈어요.\n모험은 끝났지만 새로운 이야기가 기다리고 있답니다!",
        imagePrompt: "bunny and friends waving goodbye under starry sky, magical forest at night, warm and happy ending, 16:9"
      }
    ]
  }

    // 1. 사용자 선택 데이터 → 서버에 보낼 payload 준비
    const payload = {
      selections: selections.value,  // { 기: {character: "...", ...}, 승: {...}, ... }
      pageCount: 6,                  // 페이지 수 (앱에서 고정하거나 사용자 선택 가능)
      format: 'paged',               // 서버에 "페이지별 구조로 반환해줘" 요청
      temperature: 0.75,             // 창의성 조절 (옵션)
      maxTokens: 1200                // 전체 토큰 제한 (옵션)
    }

    // 2. 서버 API 호출 (기존 generateBook 재활용)
    const result = await generateBook(payload)
    await nextTick()
 

  // 컴포넌트가 아직 살아있는지 확인 (중요!)
    if (!isMounted.value) return  // 언마운트 됐으면 중단

//   console.log(fakeResult.title);
//   console.log(fakeResult.pages);

  // 결과 데이터 채우기
    // storyTitle.value = fakeResult.title || '마법의 동화'
    // story.value = fakeResult.story || '동화 내용이 생성되었습니다!'

    console.log('서버 응답 받음:', result)

    storyTitle.value = result.title || '마법의 동화'
    story.value = fakeResult.story || '동화 내용이 생성되었습니다!'

    console.log(result.title  || '마법의 동화' );

   console.log(result.title);
   console.log(result.pages);
    
    // fakeResult.pages 데이터를 위에서 선언한 pages ref에 할당
    pages.value = result.pages.map(p => ({
      ...p,
      // 이미지가 없다면 임시 이미지를, 있다면 해당 URL을 사용
      imageUrl: p.imageUrl || `https://picsum.photos/seed/${p.pageNumber}/800/450`
    }));

    console.log("슬라이드 데이터 준비 완료:", pages.value);
    // 이모지 랜덤 선택 (기존 로직 유지)
    // const emojis = STEPS.map(step => {
    //   const arr = STORY_IMAGES[step]
    //   return arr[Math.floor(Math.random() * arr.length)]
    // })
    // illustrationEmojis.value = emojis

    console.log('이야기 생성 완료:', storyTitle.value)

 console.log('데이터 채움 → isGenerating 유지 중:', isGenerating.value)

  await nextTick()
 await nextTick()  // 두 번 하면 더 확실

    console.log('nextTick 후 story 값:', story.value)
    console.log('nextTick 후 isGenerating:', isGenerating.value)
} catch (err) {
    console.error(err)
  } finally {
    // 로딩 종료는 모든 게 끝난 후에
    isGenerating.value = false
    console.log('로딩 종료')
  }
}

const generateStoryBBBB = async () => {
  isGenerating.value = true
  emit('update:phase', 'story')  // 필요하면 유지, 아니면 제거 가능

  try {
    // 1. 사용자 선택 데이터 → 서버에 보낼 payload 준비
    const payload = {
      selections: selections.value,  // { 기: {character: "...", ...}, 승: {...}, ... }
      pageCount: 6,                  // 페이지 수 (앱에서 고정하거나 사용자 선택 가능)
      format: 'paged',               // 서버에 "페이지별 구조로 반환해줘" 요청
      temperature: 0.75,             // 창의성 조절 (옵션)
      maxTokens: 1200                // 전체 토큰 제한 (옵션)
    }

    // 2. 서버 API 호출 (기존 generateBook 재활용)
    const result = await generateBook(payload)
    await nextTick()

    // 3. 서버 응답 처리 (result는 서버에서 반환한 JSON이라고 가정)
    // if (!result.success) {
    //   throw new Error(result.message || '동화 생성에 실패했습니다')
    // }

    // 서버에서 이런 형식으로 온다고 가정
    // {
    //   success: true,
    //   title: "별빛 고양이의 모험",
    //   pages: [
    //     { text: "...", imagePrompt: "cute cat..." },
    //     ...
    //   ]
    // }

    console.log('서버 응답 받음:', result)

    storyTitle.value = result.title || '마법의 동화'

    console.log(result.title  || '마법의 동화' );

    // 페이지별 데이터 저장 (StoryReader에서 사용할 수 있도록)
    // 방법 1: Pinia store에 저장 (추천)
    // useStoryStore().setStory(result)

    if (!router) {
    console.error('router가 없습니다! useRouter import 확인하세요')
    return
    }

    // 방법 2: router 쿼리로 전달 (간단하지만 데이터 크면 제한 있음)
    router.push({
     name: 'StoryReader',
     query: {
             title: encodeURIComponent(result.title || '마법의 동화'),
             pages: encodeURIComponent(JSON.stringify(result.pages || []))
            }
    })

    // 또는 params + 상태 관리 조합
    // router.push({ name: 'StoryReader', params: { storyId: result.id } })

  } catch (err) {
    console.error('동화 생성 실패:', err)
    story.value = '앗! 마법이 잠깐 졸고 있어요 😴\n다시 한번 시도해 주세요!'
    // 또는 alert나 toast로 사용자에게 보여주기
  } finally {
    isGenerating.value = false
  }
}


const handleTTS = () => {
    //console.log("handleTTS here 1")
  if (!('speechSynthesis' in window)) {
    alert('이 브라우저는 읽기 기능을 지원하지 않아요!')
    return
  }

  // 핵심 수정: 현재 슬라이드의 텍스트만 가져오기
  // 만약 pages가 비어있다면 기존 story.value를 백업으로 사용
  const currentText = pages.value.length > 0 
    ? pages.value[activeIndex.value].text 
    : story.value.replace(/제목:.*\n/, '');

  if (!currentText) return;
  // console.log("handleTTS here 2")
//console.log(currentText)

  if (isSpeaking.value) {
    window.speechSynthesis.cancel()
    isSpeaking.value = false
    return
  }


  //console.log("handleTTS here 3")

  const text = story.value.replace(/제목:.*\n/, '')
  const utterance = new SpeechSynthesisUtterance(currentText)
  utterance.lang = 'ko-KR'
  utterance.rate = 0.85
  utterance.pitch = 1.2
  utterance.onend = () => (isSpeaking.value = false)
  utteranceRef.value = utterance

  utterance.onend = () => {
    isSpeaking.value = false;
  };

  window.speechSynthesis.speak(utterance)
  isSpeaking.value = true
}

//console.log('545 부모 handleTTS 함수 존재 여부 (부모):', typeof handleTTS === 'function')

const handleDownload = () => {
  const content = `🌟 나만의 동화 🌟\n\n${story.value}\n\n---\n나의 선택:\n${STEPS.map(s =>
    `[${s}단계] ${Object.values(selections.value[s] || {}).join(', ')}`
  ).join('\n')}`

  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${storyTitle.value || '나만의 동화'}.txt`
  a.click()
  URL.revokeObjectURL(url)
}

const saveAsPDF = async () => {

  if (saving.value || pages.length === 0) return;
  saving.value = true;

  try {
    await nextTick();

    const element = document.getElementById('pdf-content');
   
    if (!element) {
      console.error("PDF 콘텐츠 요소를 찾을 수 없습니다.");
      return;
    }

    // 1. 이미지 로딩 완료 대기 (중요!)
    const images = element.getElementsByTagName('img');
    
    await Promise.all(Array.from(images).map(img => {
      if (img.complete) return Promise.resolve();
      return new Promise(resolve => {
        img.onload = resolve;
        img.onerror = resolve;
      });
    }));

    // 3. PDF 설정 최적화
    const opt = {
      margin: 0,
      filename: `${storyTitle.value || '나만의동화'}.pdf`,
      image: { type: 'jpeg', quality: 1.0 },
      html2canvas: { 
        scale: 2, // 해상도 높임
        useCORS: true, 
        scrollY: 0,
        logging: false,
        letterRendering: true
      },
      jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' },
      pagebreak: { mode: ['css', 'legacy'] }
    };

    // 4. 실행
    await html2pdf().from(element).set(opt).save();

  } catch (err) {
    console.error('PDF 상세 에러:', err);
    alert('PDF 생성 중 오류가 발생했습니다.');
  } finally {
    saving.value = false;
  }
};

const handleReset = () => {
  window.speechSynthesis?.cancel()
  isSpeaking.value = false
  emit('update:phase', 'intro')
  currentStep.value = 0
  selections.value = { 기: {}, 승: {}, 전: {}, 결: {} }
  story.value = ''
  storyTitle.value = ''
  illustrationEmojis.value = []
}

// 컴포넌트 분리용 하위 컴포넌트들 (필요 시 별도 파일로 분리 가능)
// const ProgressBar = defineComponent({ /* ... 원본 그대로 */ })
// const ChoiceCard = defineComponent({ /* ... 원본 그대로 */ })
// const StoryDisplay = defineComponent({ /* ... 원본 그대로 */ })
</script>

<style scoped>
.choice-container  {
  background: rgba(20, 30, 50, 0.85); /* 연한 → 진한 남색 계열 + 투명도 강화 */
  border-radius: 28px;
  padding: 32px;
  border: 1px solid rgba(100, 150, 255, 0.4); /* 은은한 파랑 테두리 */
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4); /* 그림자 추가로 입체감 ↑ */
  color: #e0e7ff; /* 밝은 연보라/하늘색 계열 텍스트로 대비 강화 */
}



.step-header h2 {
  margin: 0 0 8px;
  color: #c7d2fe; /* 연보라 톤으로 부드럽게 */
  font-size: 26px;
  font-weight: 800;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.5); /* 그림자 추가로 선명도 ↑ */
}

.step-header p {
  margin: 0 0 28px;
  color: #cbd5e1; /* 밝은 회색 */
  font-size: 16px;
  line-height: 1.6;
}

.navigation {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-top: 24px;
}

.btn-prev,
.btn-next,
.btn-finish,
.btn-save,
.btn-reset {
  padding: 14px 28px;
  border-radius: 50px;
  font-size: 17px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.btn-prev {
  background: rgba(255, 255, 255, 0.15);
  border: 2px solid rgba(255, 255, 255, 0.4);
  color: #e0e7ff;
}

.btn-prev:hover {
  background: rgba(255, 255, 255, 0.25);
}

.btn-next,
.btn-finish {
  border: none;
  color: #0f172a; /* 진한 남색으로 대비 ↑ */
}

.btn-next {
  background: linear-gradient(135deg, #a5b4fc, #818cf8);
}

.btn-finish {
  background: linear-gradient(135deg, #86efac, #4ade80);
}

.btn-save {
  background: linear-gradient(135deg, #7dd3fc, #38bdf8);
  color: #0f172a;
}

.btn-reset {
  background: linear-gradient(135deg, #f472b6, #ec4899);
  color: #0f172a;
}

[disabled] {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
}

/* 이야기 결과 화면 */
.story-title {
  text-align: center;
  font-size: clamp(28px, 6vw, 44px);
  font-weight: 900;
  color: #e0f2fe; /* 밝은 하늘색 계열 */
  margin: 0 0 24px;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.6);
  font-family: 'Nanum Pen Script', cursive;
}

.story-section {
  background: rgba(15, 23, 42, 0.8); /* 진한 배경으로 텍스트 대비 강화 */
  border-radius: 24px;
  padding: 32px;
  border: 1px solid rgba(165, 180, 252, 0.3);
  backdrop-filter: blur(20px);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5);
}

.story-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin: 32px 0;
  flex-wrap: wrap;
}

/* 요약 섹션도 가독성 높임 */
.selection-summary {
  background: rgba(30, 40, 60, 0.9);
  border-radius: 20px;
  padding: 24px;
  margin-top: 32px;
  border: 1px solid rgba(100, 150, 255, 0.3);
}

.summary-item {
  background: rgba(50, 60, 80, 0.7);
  border-radius: 14px;
  padding: 16px;
  color: #e0e7ff;
}

.summary-line {
  color: #cbd5e1;
  font-size: 14px;
  margin-top: 6px;
}
/* 이모지 일러스트 부분 - 가독성 강화 */
.emoji-illust {
  position: relative;
  display: flex;
  justify-content: center;
  gap: 24px; /* 이모지 간격 조금 더 넓게 */
  margin: 32px 0 40px;
  padding: 20px 0;
  background: rgba(30, 40, 70, 0.75); /* 연한 배경 → 진한 남색 + 투명도 조절 */
  border-radius: 24px;
  border: 1px solid rgba(100, 150, 255, 0.35);
  backdrop-filter: blur(12px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5); /* 더 강한 그림자로 입체감 ↑ */
}

.float-emoji {
  font-size: 60px; /* 이모지 크기 조금 키워서 더 잘 보이게 */
  line-height: 1;
  animation: float 3.2s ease-in-out infinite;
  text-shadow: 
    0 0 12px rgba(255, 255, 255, 0.9),  /* 밝은 흰색 외곽 빛 */
    0 0 24px rgba(165, 180, 252, 0.7);   /* 연보라 빛으로 더 돋보이게 */
  filter: drop-shadow(0 6px 12px rgba(0, 0, 0, 0.6)); /* 그림자 추가 */
  z-index: 1;
}

/* 애니메이션 살짝 부드럽게 조정 */
@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-18px) scale(1.08); }
}

.progress-bar {
  display: flex;
  justify-content: center;
  gap: 16px; /* 아이템 간격 조금 넓게 */
  margin-bottom: 32px;
  flex-wrap: wrap;
  position: relative;
  background: rgba(30, 40, 70, 0.75); /* 연한 배경 → 진한 남색 계열 */
  padding: 16px 24px;
  border-radius: 20px;
  border: 1px solid rgba(100, 150, 255, 0.3);
  backdrop-filter: blur(12px);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.4); /* 그림자 강화 */
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  position: relative;
}

.step-circle {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  font-weight: bold;
  color: white;
  transition: all 0.5s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5);
}

.step-circle.done {
  background: linear-gradient(135deg, #4ade80, #22c55e);
  border: 4px solid #86efac;
  box-shadow: 0 0 20px rgba(74, 222, 128, 0.6);
}

.step-circle.active {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  border: 4px solid #fde68a;
  animation: glow 2s ease-in-out infinite;
  box-shadow: 0 0 30px rgba(251, 191, 36, 0.8);
}

.step-circle:not(.done):not(.active) {
  background: rgba(255, 255, 255, 0.15);
  border: 4px solid rgba(255, 255, 255, 0.3);
  color: rgba(255, 255, 255, 0.6);
}

.step-label {
  font-size: 15px;
  font-weight: 700;
  color: #e0e7ff; /* 밝은 연보라 계열로 가독성 ↑ */
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.6);
}

.step-item:nth-child(1) .step-label { color: #86efac; }
.step-item:nth-child(2) .step-label.active { color: #fde68a; }
.step-item:nth-child(3) .step-label.active { color: #fca5a5; }
.step-item:nth-child(4) .step-label.active { color: #c084fc; }

.connector {
  position: absolute;
  width: 60px;
  height: 4px;
  background: rgba(255, 255, 255, 0.2);
  margin-left: 60px;
  margin-top: -30px;
  border-radius: 2px;
}

.connector.done {
  background: linear-gradient(to right, #4ade80, #86efac);
  box-shadow: 0 0 12px rgba(74, 222, 128, 0.5);
}

@keyframes glow {
  0%, 100% { box-shadow: 0 0 20px rgba(251, 191, 36, 0.4); }
  50% { box-shadow: 0 0 40px rgba(251, 191, 36, 0.8); }
}

/* 1. 섹션 제목 (h2): 질문 텍스트가 명확하게 보이도록 */
h2 {
  /* 기존보다 훨씬 짙은 회색으로 변경 (가독성 핵심) */
  color: #2d3436; 
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: 20px;
  
  /* 배경이 너무 밝을 경우 대비를 위해 약간의 텍스트 그림자 추가 */
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
  
  display: flex;
  align-items: center;
  gap: 10px; /* 이모지와 텍스트 사이 간격 */
}

/* 2. 인트로 이모지 (intro-emoji): 아이콘이 배경에 묻히지 않게 */
.intro-emoji {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  
  /* 이모지 뒤에 연한 배경을 깔아 가독성 확보 */
  background: rgba(255, 255, 255, 0.7); 
  width: 50px;
  height: 50px;
  border-radius: 50%; /* 동그란 배경 */
  
  /* 유리창 효과를 이모지 배경에도 적용 */
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
  
  /* 이모지가 돋보이도록 은은한 그림자 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  
  font-size: 1.5rem;
  margin-bottom: 8px;
}

/* 3. 전체적인 섹션 컨테이너 (이전 질문의 배경 부분 보완) */
.section-card { /* h2와 항목들을 감싸는 부모 요소 */
  background: rgba(255, 255, 255, 0.4); /* 0.05에서 대폭 상향 */
  border: 1px solid rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(15px);
  border-radius: 24px;
  padding: 24px;
  margin-bottom: 32px;
}
</style>