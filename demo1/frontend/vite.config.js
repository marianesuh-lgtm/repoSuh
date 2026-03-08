// vite.config.js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue({
      template: {
        compilerOptions: {
          // 'swiper-'로 시작하는 모든 태그를 커스텀 엘리먼트로 취급합니다.
          isCustomElement: (tag) => tag.startsWith('swiper-')
        }
      }
    }  )],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  }
})