<template>
  <img 
    :src="currentIconSrc" 
    :class="['dianzan-icon', { 'active': active }]"
    :style="{ width: size + 'px', height: size + 'px' }"
    alt="点赞"
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
  return props.active ? '/icon/dianzan-filled.svg' : '/icon/dianzan.svg'
})
</script>

<style scoped>
.dianzan-icon {
  transition: all 0.3s ease;
  opacity: 0.7;
  vertical-align: -2px;
  margin-right: 2px;
}

.dianzan-icon.active {
  opacity: 1;
}

.dianzan-icon:hover {
  transform: scale(1.1);
}
</style>
