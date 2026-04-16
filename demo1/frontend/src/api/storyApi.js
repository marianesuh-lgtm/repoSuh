// src/api/storyApi.js
import axios from 'axios'

const api = axios.create({
  baseURL: '/',                    // ← 이렇게 변경 (proxy 사용을 위해)
  timeout: 0,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    // Bearer 뒤에 한 칸 공백 필수!
    config.headers.Authorization = `Bearer ${token}`;
    console.log("헤더에 토큰 주입됨:", config.headers.Authorization);
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

/**
 * 동화책 생성
 */
export const generateBook = async (payload) => {
  try {
    const response = await api.post('/api/chat/gen-book', payload)
    return response.data
  } catch (error) {
    console.error('동화책 생성 API 호출 실패:', error)
    throw error;
  }
}

/**
 * 최근 생성된 동화 목록 가져오기
 */
export const getRecentBooks = async () => {
  try {
    const response = await api.get('/api/admin/stories')   // ← proxy를 통해 호출됨
    return response.data
  } catch (error) {
    console.error('최근 동화 목록 불러오기 실패:', error)
    return []
  }
}

/**
 * 특정 ID로 동화 가져오기
 */
export const getBookById = async (id) => {
  try {
    const response = await api.get(`/api/admin/stories/${id}`)
    return response.data
  } catch (error) {
    console.error('동화 상세 불러오기 실패:', error)
    return null
  }
}

/**
 * 특정 ID로 동화 텍스트 수정하기
 * @param {string|number} id - 동화 ID
 * @param {object} payload - 수정할 데이터 (예: { content: '수정된 내용' })
 */
export const setBookTextById = async (id, payload) => {
  try {
    // api.put(url, data, config) 구조입니다.
    // 두 번째 인자인 payload가 서버의 Request Body로 전달됩니다.
    const response = await api.put(`/api/admin/stories/content/${id}`, payload);
    return response.data;
  } catch (error) {
    console.error('동화 텍스트 수정 실패:', error);
    return null;
  }
}

/**
 * 특정 ID로 동화 이미지 수정하기
 * @param {string|number} id - 동화 ID
 * @param {object} payload - 수정할 데이터 (예: { content: '수정된 내용' })
 */
export const regenerateImageById = async (id, payload) => {
  try {
    // api.put(url, data, config) 구조입니다.
    // 두 번째 인자인 payload가 서버의 Request Body로 전달됩니다.
    const response = await api.put(`/api/admin/stories/imageUrl/${id}`, payload);
    return response.data;
  } catch (error) {
    console.error('동화 이미지 수정 실패:', error);
    return null;
  }
}

 export default api;
