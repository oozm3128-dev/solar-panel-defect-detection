import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 60000
})

api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response && error.response.data) {
      const message = error.response.data.message || error.response.data.error || '服务器错误'
      switch (error.response.status) {
        case 401:
          window.location.href = '/login'
          break
        case 500:
          console.error('服务器错误:', message)
          break
        default:
          console.error('请求错误:', message)
      }
    }
    return Promise.reject(error)
  }
)

export const authApi = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
  getCurrentUser: () => api.get('/auth/current')
}

export const userApi = {
  getAllUsers: () => api.get('/users/all'),
  getUserById: (id) => api.get(`/users/${id}`),
  updateUser: (id, data) => api.put(`/users/${id}`, data),
  deleteUser: (id) => api.delete(`/users/${id}`),
  changePassword: (userId, oldPassword, newPassword) => api.post('/users/change-password', {
    userId,
    oldPassword,
    newPassword
  })
}

export const detectionApi = {
  detectImage: (formData) => api.post('/detection/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }),
  analyzeDefects: (recordId) => api.post(`/detection/analyze/${recordId}`),
  markUnrecognized: (formData) => api.post('/detection/unrecognized', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }),
  getDetectionRecords: (params) => api.get('/detection/records', { params }),
  getDetectionRecord: (recordId) => api.get(`/detection/record/${recordId}`),
  getDefectDetails: (recordId) => api.get(`/detection/defects/${recordId}`),
  getAiAnalysisReport: (recordId) => api.get(`/detection/analysis/${recordId}`),
  getStatistics: () => api.get('/detection/statistics')
}

export default api