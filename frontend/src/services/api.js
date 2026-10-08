const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

async function request(path, options = {}) {
  const token = localStorage.getItem('token')
  const isFormData = options.body instanceof FormData
  const headers = {
    ...(isFormData ? {} : { 'Content-Type': 'application/json' }),
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
  listUsers() {
    return request('/admin/users')
  },
  updateUserRoles(id, roles) {
    return request(`/admin/users/${id}/roles`, { method: 'PATCH', body: JSON.stringify({ roles }) })
  },
  listCustomers(params = {}) {
    return request(`/secretaria/clientes${toQuery(params)}`)
  },
  createCustomer(payload, photo) {
    return request('/secretaria/clientes', { method: 'POST', body: toJsonPhotoFormData(payload, photo) })
  },
  updateCustomer(id, payload, photo) {
    return request(`/secretaria/clientes/${id}`, { method: 'PUT', body: toJsonPhotoFormData(payload, photo) })
  },
  suspendCustomer(id) {
    return request(`/secretaria/clientes/${id}/suspender`, { method: 'PATCH' })
  },
  listCustomerVehicles(customerId) {
    return request(`/secretaria/clientes/${customerId}/vehiculos`)
  },
  createCustomerVehicle(customerId, payload) {
    return request(`/secretaria/clientes/${customerId}/vehiculos`, { method: 'POST', body: JSON.stringify(payload) })
  },
  updateCustomerVehicle(customerId, vehicleId, payload) {
    return request(`/secretaria/clientes/${customerId}/vehiculos/${vehicleId}`, { method: 'PUT', body: JSON.stringify(payload) })
  },
  cancelCustomerVehicle(customerId, vehicleId) {
    return request(`/secretaria/clientes/${customerId}/vehiculos/${vehicleId}`, { method: 'DELETE' })
  },
  listStatuses() {
    return request('/admin/estatus')
  },
  createStatus(payload) {
    return request('/admin/estatus', { method: 'POST', body: JSON.stringify(payload) })
  },
  updateStatus(id, payload) {
    return request(`/admin/estatus/${id}`, { method: 'PUT', body: JSON.stringify(payload) })
  },
  cancelStatus(id) {
    return request(`/admin/estatus/${id}`, { method: 'DELETE' })
  },
  searchVehicleMakes(q = '') {
    return request(`/catalogos/vehiculos/marcas${toQuery({ q })}`)
  },
  searchVehicleModels(make, q = '') {
    return request(`/catalogos/vehiculos/modelos${toQuery({ make, q })}`)
  },
  searchVehicleVersions(make, model, q = '') {
    return request(`/catalogos/vehiculos/versiones${toQuery({ make, model, q })}`)
  },
  listWorkshops() {
    return request('/talleres')
  },
  createWorkshop(payload, photo) {
    return request('/talleres', { method: 'POST', body: toJsonPhotoFormData(payload, photo) })
  },
  updateWorkshop(id, payload, photo) {
    return request(`/talleres/${id}`, { method: 'PUT', body: toJsonPhotoFormData(payload, photo) })
  },
  lookupPostalCode(postalCode) {
    return request(`/catalogos/codigos-postales/${encodeURIComponent(postalCode)}`)
  },
  listPostalStates() {
    return request('/catalogos/codigos-postales/estados')
  },
  listPostalMunicipalities(state) {
    return request(`/catalogos/codigos-postales/municipios${toQuery({ state })}`)
  },
  listPostalSettlements(state, municipality) {
    return request(`/catalogos/codigos-postales/colonias${toQuery({ state, municipality })}`)
  },
  lookupPostalSelection(state, municipality, settlement) {
    return request(`/catalogos/codigos-postales/buscar${toQuery({ state, municipality, settlement })}`)
  },
}

function toQuery(params) {
  const query = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      query.set(key, value)
    }
  })
  const text = query.toString()
  return text ? `?${text}` : ''
}

function toJsonPhotoFormData(payload, photo) {
  const formData = new FormData()
  formData.append('request', new Blob([JSON.stringify(payload)], { type: 'application/json' }))
  if (photo) formData.append('photo', photo)
  return formData
}
