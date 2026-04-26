// src/api/storyApi.js
import axios from 'axios'

const restApi = axios.create({
  baseURL: '/',                    // ← 이렇게 변경 (proxy 사용을 위해)
  timeout: 0,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
})

restApi.interceptors.request.use((config) => {
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
    const response = await restApi.post(import.meta.env.VITE_API_BASE_URL+'/api/chat/gen-book', payload)
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
    const response = await restApi.get(import.meta.env.VITE_API_BASE_URL+'/api/admin/stories', {
      params: {
        "verifyYn": "N"
    }
    })   // ← proxy를 통해 호출됨
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
    const response = await restApi.get(import.meta.env.VITE_API_BASE_URL+`/api/admin/stories/${id}`)
    return response.data
  } catch (error) {
    console.error('동화 상세 불러오기 실패:', error)
    return null
  }
}

/**
 * 특정 ID로 동화 텍스트 승인하기
 * @param {string|number} id - 동화 ID
 * @param {object} payload - 수정할 데이터 (예: { content: '수정된 내용' })
 */
export const verifyBookStory = async (id, payload) => {
  try {
    // api.put(url, data, config) 구조입니다.
    // 두 번째 인자인 payload가 서버의 Request Body로 전달됩니다.
    const response = await restApi.put(import.meta.env.VITE_API_BASE_URL+`/api/admin/stories/validate/${id}`, payload);
    return response.data;
  } catch (error) {
    console.error('동화 텍스트 승인 실패:', error);
    return null;
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
    const response = await restApi.put(import.meta.env.VITE_API_BASE_URL+`/api/admin/stories/content/${id}`, payload);
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
    const response = await restApi.put(import.meta.env.VITE_API_BASE_URL+`/api/admin/stories/imageUrl/${id}`, payload);
    return response.data;
  } catch (error) {
    console.error('동화 이미지 수정 실패:', error);
    return null;
  }
}

/**
 * 동화책 회원가입
 */
export const signup = async (payload) => {
  try {
    const response = await restApi.post(import.meta.env.VITE_API_BASE_URL+'/api/auth/signup', payload)
    return response.data
  } catch (error) {
    console.error('동화책 회원가입 API 호출 실패:', error)
    throw error;
  }
}

/**
 * 동화책 자녀등록
 */
export const childRegist = async (payload) => {
  try {
    const response = await restApi.post(import.meta.env.VITE_API_BASE_URL+'/api/children/register', payload)
    return response.data
  } catch (error) {
    console.error('동화책 자녀등록 API 호출 실패:', error)
    throw error;
  }
}


/**
 * 동화책 일반로그인
 */
export const loginWithEmail = async (payload) => {
  try {
    //console.log("restApi>>", restApi);
    const response = await restApi.post(import.meta.env.VITE_API_BASE_URL+'/api/auth/login', payload)
    return response.data
  } catch (error) {
    console.error('동화책 일반회원 API 호출 실패:', error)
    throw error;
  }
}

/**
 * 내가 만든 동화 가져오기
 */
export const getMyStories = async () => {
  try {
    const response = await restApi.get(import.meta.env.VITE_API_BASE_URL+`/api/chat/myStories`)
    return response.data
  } catch (error) {
    console.error('내가 만든 동화 불러오기 실패:', error)
    return null
  }
}

/**
 * 내가 만든 동화 가져오기
 */
export const getMyStory = async (id) => {
  try {
    const response = await restApi.get(import.meta.env.VITE_API_BASE_URL+`/api/chat/myStory/${id}`)
    return response.data
  } catch (error) {
    console.error('내가 만든 동화 상세 불러오기 실패:', error)
    return null
  }
}


 export default restApi;
