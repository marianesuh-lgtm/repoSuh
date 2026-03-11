// src/lib/supabaseClient.js
import { createClient } from '@supabase/supabase-js'

const supabaseUrl = import.meta.env.VITE_SUPABASE_URL
const supabaseAnonKey = import.meta.env.VITE_SUPABASE_ANON_KEY

console.log('VITE_SUPABASE_URL:', supabaseUrl)
console.log('VITE_SUPABASE_ANON_KEY:', supabaseAnonKey ? '있음 (길이:' + supabaseAnonKey.length + ')' : '없음')

if (!supabaseUrl) {
  throw new Error('supabaseUrl is required. .env 파일에 VITE_SUPABASE_URL을 확인하세요.')
}
if (!supabaseAnonKey) {
  throw new Error('supabaseAnonKey is required. .env 파일에 VITE_SUPABASE_ANON_KEY을 확인하세요.')
}

export const supabase = createClient(supabaseUrl, supabaseAnonKey)