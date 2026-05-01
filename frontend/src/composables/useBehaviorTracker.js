import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useUserStore } from '@/stores/user'
import { reportBehavior } from '@/api/recommend'

export function useBehaviorTracker(articleId) {
    const startTime = ref(null)
    const userStore = useUserStore()

    const sendBehavior = (duration) => {
        if (!userStore.token || !articleId) return
        reportBehavior(Number(articleId), duration).catch(() => {})
    }

    onMounted(() => {
        startTime.value = Date.now()
    })

    onBeforeUnmount(() => {
        if (startTime.value) {
            const duration = Math.floor((Date.now() - startTime.value) / 1000)
            if (duration > 0) {
                sendBehavior(duration)
            }
        }
    })

    return { startTime }
}