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
  server: {
    host: '0.0.0.0', // 아이패드 접속 허용
    proxy: {
      // 아이패드가 '/comfy-api'로 요청하면 에이서 노트북으로 전달해줌
      '/comfy-api': {
        target: 'http://172.30.1.90:8188',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/comfy-api/, '')
      },
      '/view': {
        target: 'http://172.30.1.90:8188',
        changeOrigin: true,
      }
    }
  },  
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  }
})