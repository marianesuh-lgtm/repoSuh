import axios from 'axios';

// 1. axios 인스턴스 생성
const restApi = axios.create({
  baseURL: 'https://api.myshakes.cc', // 백엔드 주소
});

// 2. 요청 인터셉터 추가 (요청을 보내기 전 실행됨)
restApi.interceptors.request.use(
  (config) => {
    // 로컬 스토리지에서 토큰을 꺼냅니다.
    const token = localStorage.getItem('accessToken');
    
    if (token) {
      // 헤더에 Authorization 항목 추가
      // Bearer 뒤에 한 칸 띄우는 것(Space)이 매우 중요합니다!
      config.headers['Authorization'] = `Bearer ${token}`;
      console.log("요청 헤더에 토큰 세팅됨:", config.headers['Authorization']);
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default restApi;