// src/api/storyApi.js
import axios from 'axios'

export const generateNextScene = (data) => axios.post('/api/story/next', data)
export const generateImage = (data) => axios.post('/api/generate-image', data)

const api = axios.create({
  baseURL: 'http://172.30.1.99:8080',  // Spring Boot 서버 주소 (필요 시 변경)
  timeout: 0,                    // 이미지 생성이 오래 걸릴 수 있으니 60초로 여유롭게
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
})

// 인터페이스 정의 (타입스크립트 사용 시 유용, JS에서도 주석으로 활용 가능)
/**
 * @typedef {Object} GenerateBookRequest
 * @property {string} name
 * @property {number} age
 * @property {string} likes
 * @property {string} theme
 */

/**
 * @typedef {Object} PageItem
 * @property {string} imageUrl
 * @property {number} pageNumber
 * @property {string|null} promptUsed
 * @property {string|null} text
 */

/**
 * @typedef {Object} GenerateBookResponse
 * @property {boolean} success
 * @property {string} message
 * @property {string} story
 * @property {PageItem[]} pages
 */

/**
 * 동화책 생성 요청 (POST /api/generate-book)
 * @param {GenerateBookRequest} payload
 * @returns {Promise<GenerateBookResponse>}
 */
export const generateBook = async (payload) => {
  try {
    const response = await api.post('/api/generate-book', payload)
    return response.data
  } catch (error) {
    console.error('동화책 생성 API 호출 실패:', error)
    if (error.response) {
      // 서버 응답 에러 (4xx, 5xx)
      throw new Error(error.response.data?.message || '서버에서 오류가 발생했습니다')
    } else if (error.request) {
      // 요청은 보냈으나 응답 없음 (네트워크 문제, 타임아웃 등)
      throw new Error('서버와 연결할 수 없습니다. 서버가 실행 중인지 확인해주세요.')
    } else {
      throw new Error(error.message || '알 수 없는 오류가 발생했습니다')
    }
  }
}

/**
 * 최근 생성된 동화 목록 가져오기 (GET /api/stories/recent) - 필요 시 사용
 * @returns {Promise<GenerateBookResponse[]>}
 */
export const getRecentBooks = async () => {
  try {
    const response = await api.get('/api/stories/recent')
    return response.data
  } catch (error) {
    console.error('최근 동화 목록 불러오기 실패:', error)
    return []
  }
}

// 필요 시 추가 API
// 예: 특정 ID로 동화 가져오기
export const getBookById = async (id) => {
  try {
    const response = await api.get(`/api/stories/${id}`)
    return response.data
  } catch (error) {
    console.error('동화 상세 불러오기 실패:', error)
    return null
  }
}