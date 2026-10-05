const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

async function request(path, options = {}) {
  const token = localStorage.getItem('token')
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  }

  let response
  try {
    response = await fetch(`${API_URL}${path}`, {
      ...options,
      headers,
    })
  } catch (cause) {
    throw new Error(`No se pudo conectar con el backend en ${API_URL}. Verifica que Spring Boot este activo.`)
  }

  if (!response.ok) {
    const contentType = response.headers.get('content-type') || ''
    const body = contentType.includes('application/json')
      ? await response.json().catch(() => ({}))
      : { error: await response.text().catch(() => '') }
    const error = new Error(body.error || `Servicio no disponible (${response.status})`)
    error.status = response.status
    throw error
  }

  return response.json()
}

export const api = {
  login(payload) {
    return request('/auth/login', { method: 'POST', body: JSON.stringify(payload) })
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
  createUser(payload) {
    return request('/secretaria/users', { method: 'POST', body: JSON.stringify(payload) })
  },
  listCustomers() {
    return request('/secretaria/clientes')
  },
  createCustomer(payload) {
    return request('/secretaria/clientes', { method: 'POST', body: JSON.stringify(payload) })
  },
  lookupPostalCode(postalCode) {
    return request(`/catalogos/codigos-postales/${encodeURIComponent(postalCode)}`)
  },
}
