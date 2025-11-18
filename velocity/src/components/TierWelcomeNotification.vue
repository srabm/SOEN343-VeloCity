<template>
  <transition name="fade-scale">
    <div v-if="show" class="tier-welcome-overlay">
      <div class="tier-welcome" :class="tierClass">
        <div class="welcome-content">
        <div v-if="tierIcon" class="welcome-icon">{{ tierIcon }}</div>
        <div class="welcome-text">
          <h3 class="welcome-title">{{ title }}</h3>
          <p class="welcome-message">{{ message }}</p>
        </div>
        <button class="close-btn" type="button" @click="dismiss" aria-label="Close welcome notification">
          ×
        </button>
      </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, watch, computed } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  tier: {
    type: String,
    default: ''
  },
  title: {
    type: String,
    required: true
  },
  message: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['close'])

const show = ref(props.visible)

watch(
  () => props.visible,
  (value) => {
    show.value = value
  },
  { immediate: true }
)

const normalizedTier = computed(() => {
  const tier = props.tier?.toString().trim().toLowerCase()
  if (tier === 'gold') return 'Gold'
  if (tier === 'silver') return 'Silver'
  if (tier === 'bronze') return 'Bronze'
  if (tier === 'notier' || tier === 'no tier' || tier === 'No') return 'NoTier'
  return 'Bronze'
})

const tierClass = computed(() => normalizedTier.value.toLowerCase())

const tierIcon = computed(() => {
  switch (normalizedTier.value) {
    case 'Gold':
      return '👑'
    case 'Silver':
      return '🥈'
    case 'Bronze':
      return '🥉'
    case 'NoTier':
      return '🚵🏻‍♂️'
    default:
      return ''
  }
})

function dismiss() {
  show.value = false
  emit('close')
}
</script>

<style scoped>
.tier-welcome-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.25);
  z-index: 9998;
  padding: 1.5rem;
}

.tier-welcome {
  width: min(520px, 90vw);
  border-radius: 20px;
  padding: 2rem;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.35);
  color: #fff;
  backdrop-filter: blur(10px);
}

.tier-welcome.bronze {
  background: linear-gradient(135deg, #b17f4a 0%, #f9a826 100%);
}

.tier-welcome.silver {
  background: linear-gradient(135deg, #9ca3af 0%, #cbd5f5 100%);
}

.tier-welcome.gold {
  background: linear-gradient(135deg, #f5af19 0%, #f12711 100%);
}

.tier-welcome.notier {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
}

.welcome-content {
  display: flex;
  gap: 1.25rem;
  align-items: flex-start;
}

.welcome-icon {
  font-size: 3rem;
  line-height: 1;
}

.welcome-text {
  flex: 1;
}

.welcome-title {
  margin: 0 0 0.5rem;
  font-size: 1.35rem;
  font-weight: 700;
}

.welcome-message {
  margin: 0;
  font-size: 1rem;
  line-height: 1.6;
  white-space: pre-line;
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  font-size: 1.6rem;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background 0.2s;
  line-height: 1;
  padding: 0;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.35);
}

.fade-scale-enter-active,
.fade-scale-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.fade-scale-enter-from,
.fade-scale-leave-to {
  opacity: 0;
  transform: scale(0.9);
}

@media (max-width: 640px) {
  .tier-welcome {
    padding: 1.5rem;
  }
}
</style>

