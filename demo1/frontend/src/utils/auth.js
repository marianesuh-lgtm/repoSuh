// src/utils/auth.js

/**
 * JWT 토큰의 Payload를 해독하여 객체로 반환합니다.
 */
export const getInfoFromToken = (token) => {
  if (!token) return null;
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );

    return JSON.parse(jsonPayload);
  } catch (e) {
    console.error('JWT Decode Error:', e);
    return null;
  }
};

/**
 * 로컬 스토리지에 인증 정보를 저장합니다.
 */
export const saveAuthInfo = (token, role) => {
  localStorage.setItem('accessToken', token);
  localStorage.setItem('userRole', role);
};

/**
 * 로컬 스토리지의 인증 정보를 삭제합니다.
 */
export const clearAuthInfo = () => {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('userRole');
};