const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

async function request(path, options = {}) {
  const token = localStorage.getItem('token')
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  }

  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers,
  })

  if (!response.ok) {
    const body = await response.json().catch(() => ({}))
    throw new Error(body.error || 'Servicio no disponible')
  }

  return response.json()
}

export const api = {
  login(payload) {
    return request('/auth/login', { method: 'POST', body: JSON.stringify(payload) })
  },
  register(payload) {
    return request('/auth/register', { method: 'POST', body: JSON.stringify(payload) })
  },
  forgotPassword(payload) {
    return request('/auth/forgot-password', { method: 'POST', body: JSON.stringify(payload) })
  },
  resetPassword(payload) {
    return request('/auth/reset-password', { method: 'POST', body: JSON.stringify(payload) })
  },
  changePassword(payload) {
    return request('/auth/change-password', { method: 'POST', body: JSON.stringify(payload) })
  },
}
