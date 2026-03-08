export function parseStoryPages(storyText) {
  if (!storyText) return []
  
  const pages = []
  const sections = storyText.split('\n\n')
  
  for (const section of sections) {
    if (!section.trim()) continue
    const textPart = section.split('[Prompt:')[0].trim()
    const cleaned = textPart.replace(/^페이지 \d+:\s*/, '').trim()
    if (cleaned) pages.push(cleaned)
  }
  
  return pages
}