import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.DEV ? 'http://localhost:8080/api' : '/api',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error.response?.data?.message || error.message || 'An unexpected error occurred'
    // You could trigger a global toast here if a toast library was installed
    console.error('API Error:', message)
    return Promise.reject(error)
  }
)

export default api
