// src/main.ts
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'          // ← 여기서 router 가져오기

// Swiper Element 등록
import { register } from 'swiper/element/bundle'
//import { register } from 'swiper/element/bundle.mjs'
//import { register } from 'swiper/swiper-element-bundle.mjs';

register()   // 이 한 줄만으로 모든 Web Component 사용 가능

const app = createApp(App)
app.use(router)                        // ← 이 한 줄이 핵심! 누락되면 router undefined
app.mount('#app')