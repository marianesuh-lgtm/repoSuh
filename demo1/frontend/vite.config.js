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
    allowedHosts: true, // 모든 호스트 허용
    proxy: {
      // 아이패드가 '/comfy-api'로 요청하면 에이서 노트북으로 전달해줌
      '/comfy-api': {
        target: 'http://myshakes.ddns.net:8188',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/comfy-api/, '')
      },
      '/view': {
        target: 'http://myshakes.ddns.net:8188',
        changeOrigin: true,
      },
      // ← 새로 추가: admin API proxy
      // '/admin': {
      //   target: 'http://myshakes.ddns.net:8080',
      //   changeOrigin: true,
      //   secure: false,
      // },
      '/api': {                     // ← 이 부분 추가
       target: 'http://myshakes.ddns.net:8080',
       changeOrigin: true,
       secure: false,
     }
    }
  },  
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  }
})