import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: {
      id: null,
      username: '',
      name: '',
      avatar: '',
      role: ''
    },
    token: ''
  }),
  actions: {
    // 登录
    login(userInfo, token) {
      this.user = userInfo
      this.token = token
      // 存储到本地存储
      localStorage.setItem('user', JSON.stringify(userInfo))
      localStorage.setItem('token', token)
    },
    // 退出登录
    logout() {
      this.user = {
        id: null,
        username: '',
        name: '',
        avatar: '',
        role: ''
      }
      this.token = ''
      // 从本地存储移除
      localStorage.removeItem('user')
      localStorage.removeItem('token')
    },
    // 从本地存储加载用户信息
    loadUserFromStorage() {
      const user = localStorage.getItem('user')
      const token = localStorage.getItem('token')
      if (user && token) {
        this.user = JSON.parse(user)
        this.token = token
      }
    },
    // 更新用户信息
    updateUser(userInfo) {
      this.user = { ...this.user, ...userInfo }
      // 更新本地存储
      localStorage.setItem('user', JSON.stringify(this.user))
    }
  }
})
