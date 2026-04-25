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
        target: 'https://comfyui.myShakes.cc',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/comfy-api/, '')
      },
      '/view': {
        target: 'https://comfyui.myShakes.cc',
        changeOrigin: true,
      },
// ✅ Kokoro TTS Proxy 추가
      '/tts-api': {
        target: 'https://tts.myShakes.cc',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/tts-api/, '')
      },
      '/api': {                     // ← 이 부분 추가
       target: 'https://api.myShakes.cc',
       changeOrigin: true,
       secure: false,
      //  ws: true,
      //  configure: (proxy, _options) => {
      //     proxy.on('error', (err, _req, _res) => {
      //       console.log('proxy error', err);
      //     });
      //   },
     }
    }
  },  
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  }
})