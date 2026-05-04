<template>
  <img 
    :src="currentIconSrc" 
    :class="['shoucang-icon', { 'active': active }]"
    :style="{ width: size + 'px', height: size + 'px' }"
    alt="收藏"
  />
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  active: {
    type: Boolean,
    default: false
  },
  size: {
    type: Number,
    default: 14
  }
})

const currentIconSrc = computed(() => {
  return props.active ? '/icon/shoucang.svg' : '/icon/shoucang-filled.svg'
})
</script>

<style scoped>
.shoucang-icon {
  transition: all 0.3s ease;
  opacity: 0.7;
  vertical-align: -2px;
  margin-right: 2px;
}

.shoucang-icon.active {
  opacity: 1;
}

.shoucang-icon:hover {
  transform: scale(1.1);
}
</style>
