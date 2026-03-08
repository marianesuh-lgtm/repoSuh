// src/main.ts
import { createApp } from 'vue'
import App from './App.vue'

// Swiper Element 등록
import { register } from 'swiper/element/bundle'
//import { register } from 'swiper/element/bundle.mjs'
//import { register } from 'swiper/swiper-element-bundle.mjs';

register()   // 이 한 줄만으로 모든 Web Component 사용 가능

createApp(App).mount('#app')