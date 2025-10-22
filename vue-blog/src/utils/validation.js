/**
 * 表单验证工具函数
 */

/**
 * 验证规则
 */
export const validationRules = {
    // 必填验证
    required: (value) => {
        if (value === null || value === undefined || value === '') {
            return '此字段为必填项'
        }
        return true
    },

    // 邮箱验证
    email: (value) => {
        if (!value) return true // 空值由required规则处理
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        return emailRegex.test(value) || '请输入有效的邮箱地址'
    },

    // 手机号验证
    phone: (value) => {
        if (!value) return true
        const phoneRegex = /^1[3-9]\d{9}$/
        return phoneRegex.test(value) || '请输入有效的手机号码'
    },

    // 密码验证
    password: (value) => {
        if (!value) return true
        if (value.length < 6) return '密码长度不能少于6位'
        if (value.length > 20) return '密码长度不能超过20位'
        return true
    },

    // 确认密码验证
    confirmPassword: (password, confirmPassword) => {
        if (!confirmPassword) return true
        return password === confirmPassword || '两次输入的密码不一致'
    },

    // 用户名验证
    username: (value) => {
        if (!value) return true
        if (value.length < 2) return '用户名长度不能少于2位'
        if (value.length > 20) return '用户名长度不能超过20位'
        const usernameRegex = /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/
        return usernameRegex.test(value) || '用户名只能包含字母、数字、下划线和中文'
    },

    // URL验证
    url: (value) => {
        if (!value) return true
        try {
            new URL(value)
            return true
        } catch {
            return '请输入有效的URL地址'
        }
    },

    // 数字验证
    number: (value) => {
        if (!value) return true
        return !isNaN(value) || '请输入有效的数字'
    },

    // 整数验证
    integer: (value) => {
        if (!value) return true
        return Number.isInteger(Number(value)) || '请输入有效的整数'
    },

    // 正整数验证
    positiveInteger: (value) => {
        if (!value) return true
        const num = Number(value)
        return Number.isInteger(num) && num > 0 || '请输入有效的正整数'
    },

    // 长度验证
    length: (min, max) => (value) => {
        if (!value) return true
        if (value.length < min) return `长度不能少于${min}位`
        if (value.length > max) return `长度不能超过${max}位`
        return true
    },

    // 最小长度验证
    minLength: (min) => (value) => {
        if (!value) return true
        return value.length >= min || `长度不能少于${min}位`
    },

    // 最大长度验证
    maxLength: (max) => (value) => {
        if (!value) return true
        return value.length <= max || `长度不能超过${max}位`
    },

    // 最小值验证
    min: (min) => (value) => {
        if (!value) return true
        const num = Number(value)
        return num >= min || `值不能小于${min}`
    },

    // 最大值验证
    max: (max) => (value) => {
        if (!value) return true
        const num = Number(value)
        return num <= max || `值不能大于${max}`
    },

    // 范围验证
    range: (min, max) => (value) => {
        if (!value) return true
        const num = Number(value)
        return num >= min && num <= max || `值必须在${min}到${max}之间`
    },

    // 正则表达式验证
    pattern: (regex, message) => (value) => {
        if (!value) return true
        return regex.test(value) || message
    },

    // 自定义验证
    custom: (validator, message) => (value) => {
        if (!value) return true
        return validator(value) || message
    }
}

/**
 * 验证表单数据
 * @param {object} data - 表单数据
 * @param {object} rules - 验证规则
 * @returns {object} 验证结果
 */
export function validateForm(data, rules) {
    const errors = {}
    let isValid = true

    for (const field in rules) {
        const fieldRules = rules[field]
        const value = data[field]

        for (const rule of fieldRules) {
            const result = rule(value, data)
            if (result !== true) {
                errors[field] = result
                isValid = false
                break
            }
        }
    }

    return { isValid, errors }
}

/**
 * 验证单个字段
 * @param {any} value - 字段值
 * @param {Array} rules - 验证规则数组
 * @param {object} data - 完整表单数据（用于关联验证）
 * @returns {string|true} 验证结果
 */
export function validateField(value, rules, data = {}) {
    for (const rule of rules) {
        const result = rule(value, data)
        if (result !== true) {
            return result
        }
    }
    return true
}

/**
 * 创建验证器
 * @param {object} rules - 验证规则
 * @returns {Function} 验证函数
 */
export function createValidator(rules) {
    return (data) => validateForm(data, rules)
}

/**
 * 异步验证
 * @param {any} value - 字段值
 * @param {Function} validator - 异步验证函数
 * @returns {Promise<string|true>} 验证结果
 */
export async function validateAsync(value, validator) {
    try {
        const result = await validator(value)
        return result === true ? true : result
    } catch (error) {
        return '验证失败'
    }
}

/**
 * 防抖验证
 * @param {Function} validator - 验证函数
 * @param {number} delay - 延迟时间
 * @returns {Function} 防抖验证函数
 */
export function debounceValidate(validator, delay = 300) {
    let timeoutId = null

    return (value, callback) => {
        clearTimeout(timeoutId)
        timeoutId = setTimeout(async () => {
            const result = await validator(value)
            callback(result)
        }, delay)
    }
}

/**
 * 常用验证规则组合
 */
export const commonRules = {
    // 用户名规则
    username: [
        validationRules.required,
        validationRules.username
    ],

    // 邮箱规则
    email: [
        validationRules.required,
        validationRules.email
    ],

    // 手机号规则
    phone: [
        validationRules.required,
        validationRules.phone
    ],

    // 密码规则
    password: [
        validationRules.required,
        validationRules.password
    ],

    // 确认密码规则
    confirmPassword: (password) => [
        validationRules.required,
        (value) => validationRules.confirmPassword(password, value)
    ],

    // 文章标题规则
    articleTitle: [
        validationRules.required,
        validationRules.length(1, 100)
    ],

    // 文章内容规则
    articleContent: [
        validationRules.required,
        validationRules.minLength(10)
    ],

    // 分类名称规则
    categoryName: [
        validationRules.required,
        validationRules.length(1, 50)
    ],

    // 标签名称规则
    tagName: [
        validationRules.required,
        validationRules.length(1, 20)
    ],

    // URL规则
    url: [
        validationRules.required,
        validationRules.url
    ],

    // 数字规则
    number: [
        validationRules.required,
        validationRules.number
    ],

    // 正整数规则
    positiveInteger: [
        validationRules.required,
        validationRules.positiveInteger
    ]
}

/**
 * 表单验证Hook（Vue 3 Composition API）
 * @param {object} initialData - 初始数据
 * @param {object} rules - 验证规则
 * @returns {object} 验证相关状态和方法
 */
export function useFormValidation(initialData = {}, rules = {}) {
    const data = reactive({ ...initialData })
    const errors = reactive({})
    const touched = reactive({})
    const isValidating = ref(false)

    // 验证单个字段
    const validateField = (field) => {
        const fieldRules = rules[field]
        if (!fieldRules) return true

        const result = validateField(data[field], fieldRules, data)
        if (result === true) {
            delete errors[field]
        } else {
            errors[field] = result
        }
        return result === true
    }

    // 验证所有字段
    const validateAll = () => {
        const result = validateForm(data, rules)
        Object.assign(errors, result.errors)
        return result.isValid
    }

    // 重置表单
    const resetForm = () => {
        Object.assign(data, initialData)
        Object.keys(errors).forEach(key => delete errors[key])
        Object.keys(touched).forEach(key => delete touched[key])
    }

    // 设置字段值
    const setFieldValue = (field, value) => {
        data[field] = value
        touched[field] = true
        validateField(field)
    }

    // 设置字段错误
    const setFieldError = (field, error) => {
        if (error) {
            errors[field] = error
        } else {
            delete errors[field]
        }
    }

    // 清除字段错误
    const clearFieldError = (field) => {
        delete errors[field]
    }

    // 清除所有错误
    const clearAllErrors = () => {
        Object.keys(errors).forEach(key => delete errors[key])
    }

    // 检查字段是否被触摸过
    const isFieldTouched = (field) => {
        return touched[field] || false
    }

    // 检查字段是否有错误
    const hasFieldError = (field) => {
        return !!errors[field]
    }

    // 检查表单是否有错误
    const hasErrors = computed(() => {
        return Object.keys(errors).length > 0
    })

    // 检查表单是否有效
    const isValid = computed(() => {
        return Object.keys(errors).length === 0
    })

    return {
        data,
        errors,
        touched,
        isValidating,
        validateField,
        validateAll,
        resetForm,
        setFieldValue,
        setFieldError,
        clearFieldError,
        clearAllErrors,
        isFieldTouched,
        hasFieldError,
        hasErrors,
        isValid
    }
}
