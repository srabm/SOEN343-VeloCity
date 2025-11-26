<template>
  <transition name="slide-down">
    <div v-if="show" class="tier-notification" :class="notificationClass">
      <div class="notification-content">
        <div class="notification-icon">
          <span v-if="isUpgrade">🎉</span>
          <span v-else>📉</span>
        </div>
        <div class="notification-text">
          <h3 class="notification-title">
            {{ isUpgrade ? 'Tier Upgraded!' : 'Tier Changed' }}
          </h3>
          <p class="notification-message">
            You have been {{ isUpgrade ? 'upgraded' : 'downgraded' }} from
            <strong>{{ formatTier(oldTier) }}</strong> to
            <strong>{{ formatTier(newTier) }}</strong> tier.
          </p>
        </div>
        <button @click="dismiss" class="close-btn" aria-label="Close notification">
          ×
        </button>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'

const props = defineProps({
  oldTier: {
    type: String,
    required: true
  },
  newTier: {
    type: String,
    required: true
  },
  autoClose: {
    type: Boolean,
    default: false // Changed to false - only close on X click
  },
  autoCloseDelay: {
    type: Number,
    default: 5000 // 5 seconds
  }
})

const emit = defineEmits(['close'])

const show = ref(false)
let autoCloseTimer = null

const isUpgrade = computed(() => {
    // Normalize tier names for comparison (handle case variations)
    const normalizeTier = (tier) => {
        if (!tier) return 'NoTier'
        const normalized = tier.trim()
        // Handle variations: "NoTier", "No Tier", "noTier", etc.
        if (normalized.toLowerCase().includes('no') && normalized.toLowerCase().includes('tier')) {
            return 'NoTier'
        }
    // Capitalize first letter for other tiers
    return normalized.charAt(0).toUpperCase() + normalized.slice(1).toLowerCase()
  }
  const tierOrder = { 'NoTier': 0, 'Bronze': 1, 'Silver': 2, 'Gold': 3 }
  const oldTierNormalized = normalizeTier(props.oldTier)
  const newTierNormalized = normalizeTier(props.newTier)
  
  const oldOrder = tierOrder[oldTierNormalized] ?? 0
  const newOrder = tierOrder[newTierNormalized] ?? 0
  
  return newOrder > oldOrder
})

const notificationClass = computed(() => {
  return isUpgrade.value ? 'upgrade' : 'downgrade'
})

function formatTier(tier) {
  if (!tier) return 'No Tier'
  // Normalize to handle variations
  const normalized = tier.trim()
  // Check if it's "NoTier" or any variation (case-insensitive)
  const lowerTier = normalized.toLowerCase()
  if (lowerTier === 'notier' || lowerTier === 'no tier' || (lowerTier.includes('no') && lowerTier.includes('tier'))) {
    return 'No Tier'
  }
  // For other tiers, capitalize first letter
  return tier.charAt(0).toUpperCase() + tier.slice(1).toLowerCase()
}

function dismiss() {
  show.value = false
  if (autoCloseTimer) {
    clearTimeout(autoCloseTimer)
    autoCloseTimer = null
  }
  emit('close')
}

watch(() => [props.oldTier, props.newTier], () => {
  if (props.oldTier && props.newTier && props.oldTier !== props.newTier) {
    show.value = true
    
    // Only auto-close if explicitly enabled (default is false)
    // Both upgrades and downgrades stay until user clicks X
    if (props.autoClose === true) {
      if (autoCloseTimer) {
        clearTimeout(autoCloseTimer)
      }
      autoCloseTimer = setTimeout(() => {
        dismiss()
      }, props.autoCloseDelay)
      } else {
        // Ensure no auto-close timer is set
        if (autoCloseTimer) {
            clearTimeout(autoCloseTimer)
            autoCloseTimer = null
        }
    }
  }
}, { immediate: true })

onMounted(() => {
  if (props.oldTier && props.newTier && props.oldTier !== props.newTier) {
    show.value = true

    // Only auto-close if explicitly enabled (default is false)
    // Both upgrades and downgrades stay until user clicks X
    if (props.autoClose === true) {
      autoCloseTimer = setTimeout(() => {
        dismiss()
      }, props.autoCloseDelay)
    } else {
      // Ensure no auto-close timer is set
      if (autoCloseTimer) {
        clearTimeout(autoCloseTimer)
        autoCloseTimer = null
      }
    }
  }
})
</script>

<style scoped>
.tier-notification {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  min-width: 350px;
  max-width: 500px;
  border-radius: 12px;
  background: #fac61e; 
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.tier-notification.upgrade,
.tier-notification.downgrade {
  background: #fac61e !important; 
}

.notification-content {
  display: flex;
  align-items: flex-start;
  padding: 1.25rem;
  color: #0348af;
  gap: 1rem;
}

.notification-icon {
  font-size: 2rem;
  flex-shrink: 0;
  line-height: 1;
}

.notification-text {
  flex: 1;
}

.notification-title {
  margin: 0 0 0.5rem 0;
  font-size: 1.25rem;
  font-weight: 700;
  color: #0348af;
}

.notification-message {
  margin: 0;
  font-size: 0.95rem;
  line-height: 1.5;
  color: #0348af(255, 255, 255, 0.95);
}

.notification-message strong {
  font-weight: 700;
  color: #0348af;
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: #0348af;
  font-size: 1.5rem;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.3s ease;
  line-height: 1;
  padding: 0;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.slide-down-enter-active {
  transition: all 0.3s ease-out;
}

.slide-down-leave-active {
  transition: all 0.35s ease-out;
}

.slide-down-enter-from {
  opacity: 0;
  transform: translateY(-100%);
}

.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

@media (max-width: 640px) {
  .tier-notification {
    top: 10px;
    right: 10px;
    left: 10px;
    min-width: auto;
    max-width: none;
  }
}
</style>
