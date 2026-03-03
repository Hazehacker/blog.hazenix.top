// 載入 Google Identity Services 腳本並提供初始化/登入方法
let googleScriptLoaded = false
let googleInitInProgress = null

function loadGoogleScript() {
	if (googleScriptLoaded) return Promise.resolve()
	if (googleInitInProgress) return googleInitInProgress
	googleInitInProgress = new Promise((resolve, reject) => {
		const script = document.createElement('script')
		script.src = 'https://accounts.google.com/gsi/client'
		script.async = true
		script.defer = true
		script.onload = () => {
			googleScriptLoaded = true
			resolve()
		}
		script.onerror = () => reject(new Error('Google Identity Services 腳本載入失敗'))
		document.head.appendChild(script)
	})
	return googleInitInProgress
}

/**
 * 以 Google Identity Services 前端流程取得 id_token
 * @param {Object} options
 * @param {string} options.clientId - Google OAuth 2.0 Client ID
 * @param {boolean} [options.useOneTap=false] - 是否啟用 One Tap（可選）
 * @returns {Promise<string>} idToken
 */
export async function getGoogleIdToken({ clientId, useOneTap = false }) {
	if (!clientId) {
		throw new Error('缺少 Google Client ID（VITE_GOOGLE_CLIENT_ID）')
	}
	await loadGoogleScript()

	// 以 Promise 形式包裝 callback
	return new Promise((resolve, reject) => {
		try {
			window.google.accounts.id.initialize({
				client_id: clientId,
				auto_select: false,
				callback: (response) => {
					// response.credential 即為 id_token(JWT)
					if (response && response.credential) {
						resolve(response.credential)
					} else {
						reject(new Error('未取得 Google id_token'))
					}
				},
			})

			// One Tap（可選）
			if (useOneTap) {
				window.google.accounts.id.prompt() // 非阻塞
			}

			// 使用 Popup/按鈕提示（透過 select_popup 可避免整頁跳轉）
			window.google.accounts.id.prompt((notification) => {
				// 若需要可根據 notification 進行 UI 調整
				// console.log('GIS prompt status:', notification)
			})

			// 也支援直接開啟 select_account 的快顯
			window.google.accounts.id.renderButton?.(document.createElement('div'), {
				type: 'standard',
				theme: 'outline',
				size: 'large',
				text: 'signin_with',
				shape: 'rectangular',
			})

			// 備用方案：若需要強制彈窗選擇帳戶，可使用這個介面
			window.google.accounts.id.prompt()
		} catch (e) {
			reject(e)
		}
	})
}

export default {
	getGoogleIdToken,
}


