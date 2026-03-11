<!-- src/components/StarField.vue -->
<template>
  <div class="star-field">
    <div
      v-for="i in 50"
      :key="i"
      class="star"
      :style="getStarStyle(i)"
    />
  </div>
</template>

<script setup>
const getStarStyle = () => {
  return {
    position: 'absolute',
    width: Math.random() * 3 + 1 + 'px',
    height: Math.random() * 3 + 1 + 'px',
    borderRadius: '50%',
    background: `hsl(${Math.random() * 60 + 200}, 100%, ${Math.random() * 30 + 70}%)`,
    left: Math.random() * 100 + '%',
    top: Math.random() * 100 + '%',
    animation: `twinkle ${Math.random() * 3 + 2}s ease-in-out infinite`,
    animationDelay: Math.random() * 4 + 's',
    opacity: Math.random() * 0.7 + 0.3,
    pointerEvents: 'none',
  }
}
</script>

<style scoped>
.star-field {
  position: fixed;
  inset: 0;
  overflow: hidden;
  z-index: 0;
  pointer-events: none;
}

@keyframes twinkle {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.5); }
}
</style>