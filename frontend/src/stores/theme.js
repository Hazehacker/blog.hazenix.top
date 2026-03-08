import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
    const isDark = ref(localStorage.getItem('theme') === 'dark')

    watch(isDark, (val) => {
        if (val) {
            document.documentElement.classList.add('dark')
            localStorage.setItem('theme', 'dark')
        } else {
            document.documentElement.classList.remove('dark')
            localStorage.setItem('theme', 'light')
        }
    }, { immediate: true })

    function toggleTheme() {
        isDark.value = !isDark.value
    }

    // 带过渡效果的主题切换
    function toggleThemeWithTransition(duration = 1000) {
        // 添加过渡效果
        document.documentElement.style.transition = `background-color ${duration}ms ease, color ${duration}ms ease`
        
        // 切换主题
        setTimeout(() => {
            isDark.value = !isDark.value
            
            // 移除过渡效果
            setTimeout(() => {
                document.documentElement.style.transition = ''
            }, duration)
        }, 50)
    }

    return { isDark, toggleTheme, toggleThemeWithTransition }
})
