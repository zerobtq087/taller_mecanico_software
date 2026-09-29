<template>
  <v-app>
    <main class="app-shell">
      <section class="brand-pane">
        <v-chip class="brand-chip" color="primary" variant="tonal" prepend-icon="mdi-shield-key">
          Seguridad fullstack
        </v-chip>

        <div class="brand-copy">
          <p class="eyebrow">Taller Paradox</p>
          <h1>Operacion segura para un taller moderno.</h1>
          <p>
            Login, recuperacion de contrasena, alta protegida de usuarios y registro de clientes
            con validaciones, roles y flujo REST con facade.
          </p>
        </div>

        <div class="signal-grid">
          <div v-for="metric in metrics" :key="metric.label" class="metric-card">
            <v-icon :icon="metric.icon" size="28" />
            <strong>{{ metric.value }}</strong>
            <span>{{ metric.label }}</span>
          </div>
        </div>
      </section>

      <section class="workspace-pane">
        <v-card class="auth-card" elevation="0">
          <div class="auth-header">
            <div>
              <p class="eyebrow">Acceso protegido</p>
              <h2>{{ title }}</h2>
            </div>
            <v-btn
              v-if="user"
              icon="mdi-logout"
              variant="text"
              color="primary"
              aria-label="Cerrar sesion"
              @click="logout"
            />
          </div>

          <v-alert v-if="notice" class="mb-4" color="primary" variant="tonal" density="compact">
            {{ notice }}
          </v-alert>

          <template v-if="!user">
            <v-tabs v-model="mode" color="primary" grow>
              <v-tab value="login">Login</v-tab>
              <v-tab value="forgot">Reset</v-tab>
            </v-tabs>

            <v-window v-model="mode" class="mt-6">
              <v-window-item value="login">
                <v-form @submit.prevent="login">
                  <v-text-field v-model="loginForm.email" label="Correo" type="email" prepend-inner-icon="mdi-email" />
                  <v-text-field
                    v-model="loginForm.password"
                    label="Contrasena"
                    type="password"
                    prepend-inner-icon="mdi-lock"
                  />
                  <v-btn block size="large" color="primary" type="submit" :loading="loading">
                    Entrar al taller
                  </v-btn>
                </v-form>
              </v-window-item>

              <v-window-item value="forgot">
                <v-form v-if="!resetToken" @submit.prevent="forgotPassword">
                  <v-text-field v-model="forgotForm.email" label="Correo registrado" type="email" prepend-inner-icon="mdi-email-sync" />
                  <v-btn block size="large" color="primary" type="submit" :loading="loading">
                    Generar recuperacion
                  </v-btn>
                </v-form>
                <v-form v-else @submit.prevent="resetPassword">
                  <v-text-field v-model="resetForm.token" label="Token" prepend-inner-icon="mdi-key-chain" />
                  <v-text-field
                    v-model="resetForm.newPassword"
                    label="Nueva contrasena"
                    type="password"
                    prepend-inner-icon="mdi-lock-reset"
                  />
                  <v-btn block size="large" color="primary" type="submit" :loading="loading">
                    Cambiar contrasena
                  </v-btn>
                </v-form>
              </v-window-item>
            </v-window>
          </template>

          <template v-else>
            <div class="user-row">
              <v-avatar color="primary" size="48">
                <v-icon icon="mdi-account-cog" />
              </v-avatar>
              <div>
                <h3>{{ user.name }}</h3>
                <p>{{ user.email }}</p>
              </div>
            </div>

            <div class="roles">
              <v-chip v-for="role in user.roles" :key="role" color="primary" variant="elevated">
                {{ role }}
              </v-chip>
            </div>

            <v-tabs v-model="workspaceMode" color="primary" grow>
              <v-tab value="dashboard">Panel</v-tab>
              <v-tab v-if="canManageCustomers" value="customers">Clientes</v-tab>
              <v-tab v-if="canManageCustomers" value="users">Usuarios</v-tab>
              <v-tab value="password">Clave</v-tab>
            </v-tabs>

            <v-window v-model="workspaceMode" class="mt-5">
              <v-window-item value="dashboard">
                <div class="dashboard-grid">
                  <v-card v-for="item in rolePanels" :key="item.title" class="panel-card" elevation="0">
                    <v-icon :icon="item.icon" size="30" color="primary" />
                    <strong>{{ item.title }}</strong>
                    <span>{{ item.text }}</span>
                  </v-card>
                </div>
              </v-window-item>

              <v-window-item value="customers">
                <v-form class="data-form" @submit.prevent="createCustomer">
                  <div class="form-grid">
                    <v-text-field v-model="customerForm.fullName" label="Nombre completo" />
                    <v-text-field v-model="customerForm.alternateContactName" label="Contacto alternativo" />
                    <v-text-field v-model.number="customerForm.age" label="Edad" type="number" min="18" max="120" />
                    <v-text-field v-model="customerForm.birthDate" label="Fecha de nacimiento" type="date" />
                    <v-text-field v-model="customerForm.personalPhone" label="Telefono personal" />
                    <v-text-field v-model="customerForm.workPhone" label="Telefono del trabajo" />
                    <v-text-field v-model="customerForm.email" label="Email" type="email" />
                    <v-text-field v-model="customerForm.workEmail" label="Email del trabajo opcional" type="email" />
                    <v-text-field v-model="customerForm.street" label="Calle" />
                    <v-text-field v-model="customerForm.neighborhood" label="Colonia" />
                    <v-text-field v-model="customerForm.municipality" label="Municipio" />
                    <v-text-field v-model="customerForm.state" label="Estado" />
                    <v-text-field v-model="customerForm.postalCode" label="Codigo postal" />
                  </div>

                  <div class="photo-uploader">
                    <input ref="photoInput" class="hidden-input" type="file" accept="image/*" @change="handlePhotoUpload" />
                    <v-btn color="secondary" variant="tonal" prepend-icon="mdi-camera-plus" @click="photoInput?.click()">
                      Subir foto
                    </v-btn>
                    <span>Maximo 20 MB, solo imagenes.</span>
                    <img v-if="photoPreview" :src="photoPreview" alt="Vista previa del cliente" />
                  </div>

                  <v-btn block size="large" color="primary" type="submit" :loading="loading">
                    Registrar cliente
                  </v-btn>
                </v-form>
              </v-window-item>

              <v-window-item value="users">
                <v-form class="data-form" @submit.prevent="createUser">
                  <v-text-field v-model="newUserForm.name" label="Nombre completo" prepend-inner-icon="mdi-account" />
                  <v-text-field v-model="newUserForm.email" label="Correo" type="email" prepend-inner-icon="mdi-email" />
                  <v-text-field
                    v-model="newUserForm.password"
                    label="Contrasena temporal"
                    type="password"
                    prepend-inner-icon="mdi-lock-check"
                  />
                  <v-select v-model="newUserForm.roles" :items="availableRoles" label="Roles" multiple chips />
                  <v-btn block size="large" color="primary" type="submit" :loading="loading">
                    Crear usuario
                  </v-btn>
                </v-form>
              </v-window-item>

              <v-window-item value="password">
                <v-form class="data-form" @submit.prevent="changePassword">
                  <v-text-field v-model="passwordForm.currentPassword" label="Actual" type="password" />
                  <v-text-field v-model="passwordForm.newPassword" label="Nueva" type="password" />
                  <v-btn color="primary" type="submit" :loading="loading">Actualizar</v-btn>
                </v-form>
              </v-window-item>
            </v-window>
          </template>
        </v-card>
      </section>
    </main>
  </v-app>
</template>

<script setup>
import Swal from 'sweetalert2'
import { computed, reactive, ref } from 'vue'
import { api } from './services/api'

const PHOTO_MAX_BYTES = 20 * 1024 * 1024

const mode = ref('login')
const workspaceMode = ref('dashboard')
const loading = ref(false)
const notice = ref('')
const resetToken = ref('')
const photoInput = ref(null)
const photoPreview = ref('')
const user = ref(getStoredUser())

const loginForm = reactive({ email: '', password: '' })
const forgotForm = reactive({ email: '' })
const resetForm = reactive({ token: '', newPassword: '' })
const passwordForm = reactive({ currentPassword: '', newPassword: '' })
const newUserForm = reactive({ name: '', email: '', password: '', roles: ['AUXILIAR'] })
const customerForm = reactive(emptyCustomer())

const metrics = [
  { icon: 'mdi-car-cog', value: 'REST', label: 'servicios protegidos' },
  { icon: 'mdi-account-lock', value: '4', label: 'roles operativos' },
  { icon: 'mdi-database-lock', value: 'Facade', label: 'vista a repository' },
]

const rolePanels = computed(() => [
  { icon: 'mdi-view-dashboard', title: 'Gerencia', text: 'Reportes, usuarios, roles y auditoria.' },
  { icon: 'mdi-calendar-check', title: 'Secretaria', text: 'Citas, recepcion, clientes y entregas.' },
  { icon: 'mdi-tools', title: 'Mecanicos', text: 'Diagnosticos, refacciones y avances.' },
  { icon: 'mdi-account-wrench', title: 'Auxiliares', text: 'Apoyo operativo con permisos limitados.' },
])

const availableRoles = computed(() => {
  const roles = ['SECRETARIO', 'AUXILIAR', 'MECANICO']
  if (hasRole('GERENTE')) roles.unshift('GERENTE')
  return roles
})

const canManageCustomers = computed(() => hasRole('GERENTE') || hasRole('SECRETARIO'))

const title = computed(() => {
  if (user.value) return 'Panel operativo'
  return mode.value === 'login' ? 'Inicio de sesion' : 'Recuperar acceso'
})

function emptyCustomer() {
  return {
    fullName: '',
    alternateContactName: '',
    age: null,
    birthDate: '',
    personalPhone: '',
    workPhone: '',
    email: '',
    workEmail: '',
    photoDataUrl: '',
    street: '',
    neighborhood: '',
    municipality: '',
    state: '',
    postalCode: '',
    workshopId: null,
  }
}

async function run(action, fallback) {
  loading.value = true
  notice.value = ''
  try {
    await action()
  } catch (error) {
    if (error.status === 401 || error.status === 403) {
      clearSession()
      workspaceMode.value = 'dashboard'
      notice.value = 'Sesion expirada o sin autorizacion. Inicia sesion nuevamente.'
    }
    fallback?.(error)
  } finally {
    loading.value = false
  }
}

function hasRole(role) {
  return user.value?.roles?.includes(role) ?? false
}

function setSession(payload) {
  localStorage.setItem('token', payload.token)
  localStorage.setItem('user', JSON.stringify(payload.user))
  user.value = payload.user
}

function clearSession() {
  clearStoredSession()
  user.value = null
}

function clearStoredSession() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
}

function getStoredUser() {
  const token = localStorage.getItem('token')
  const storedUser = localStorage.getItem('user')
  if (!token || !storedUser) {
    clearStoredSession()
    return null
  }
  try {
    return JSON.parse(storedUser)
  } catch {
    clearStoredSession()
    return null
  }
}

function login() {
  run(
    async () => {
      setSession(await api.login(loginForm))
      notice.value = 'Sesion iniciada con JWT.'
    },
    (error) => {
      notice.value = error.message
    },
  )
}

function forgotPassword() {
  run(
    async () => {
      const response = await api.forgotPassword(forgotForm)
      resetToken.value = response.demoResetToken
      resetForm.token = response.demoResetToken
      notice.value = response.message
    },
    (error) => {
      notice.value = error.message
    },
  )
}

function resetPassword() {
  run(
    async () => {
      await api.resetPassword(resetForm)
      mode.value = 'login'
      resetToken.value = ''
      notice.value = 'Contrasena actualizada. Inicia sesion.'
    },
    (error) => {
      notice.value = error.message
    },
  )
}

function createUser() {
  run(
    async () => {
      validateUserForm()
      const payload = {
        ...newUserForm,
        email: newUserForm.email.trim().toLowerCase(),
      }
      const created = await api.createUser(payload)
      await Swal.fire('Usuario registrado', `${created.name} fue dado de alta correctamente.`, 'success')
      Object.assign(newUserForm, { name: '', email: '', password: '', roles: ['AUXILIAR'] })
    },
    async (error) => {
      await Swal.fire('No se pudo registrar', error.message, 'error')
    },
  )
}

function createCustomer() {
  run(
    async () => {
      validateCustomerForm()
      await validateDuplicateCustomer()
      const payload = normalizeCustomerPayload()
      const created = await api.createCustomer(payload)
      await Swal.fire('Cliente registrado', `${created.fullName} fue registrado correctamente.`, 'success')
      Object.assign(customerForm, emptyCustomer())
      photoPreview.value = ''
    },
    async (error) => {
      await Swal.fire('Registro detenido', error.message, 'warning')
    },
  )
}

async function validateDuplicateCustomer() {
  const customers = await api.listCustomers()
  const email = customerForm.email.trim().toLowerCase()
  const phone = customerForm.personalPhone.trim()
  const duplicated = customers.find((customer) => customer.email === email || customer.personalPhone === phone)
  if (duplicated) {
    throw new Error('Los datos ya existen. No se creara un doble registro.')
  }
}

function validateUserForm() {
  if (!newUserForm.name.trim()) throw new Error('El nombre completo es obligatorio.')
  if (!isValidEmail(newUserForm.email)) throw new Error('El correo del usuario no tiene un formato valido.')
  if (!newUserForm.password || newUserForm.password.length < 8) {
    throw new Error('La contrasena temporal debe tener al menos 8 caracteres.')
  }
  if (!newUserForm.roles?.length) throw new Error('Selecciona al menos un rol.')
}

function validateCustomerForm() {
  const requiredFields = [
    [customerForm.fullName, 'Nombre completo'],
    [customerForm.alternateContactName, 'Contacto alternativo'],
    [customerForm.birthDate, 'Fecha de nacimiento'],
    [customerForm.personalPhone, 'Telefono personal'],
    [customerForm.workPhone, 'Telefono del trabajo'],
    [customerForm.street, 'Calle'],
    [customerForm.neighborhood, 'Colonia'],
    [customerForm.municipality, 'Municipio'],
    [customerForm.state, 'Estado'],
    [customerForm.postalCode, 'Codigo postal'],
  ]
  const missing = requiredFields.find(([value]) => !String(value || '').trim())
  if (missing) throw new Error(`${missing[1]} es obligatorio.`)
  if (!customerForm.age || customerForm.age < 18 || customerForm.age > 120) {
    throw new Error('La edad debe estar entre 18 y 120 anos.')
  }
  if (!isValidEmail(customerForm.email)) throw new Error('El email del cliente no tiene un formato valido.')
  if (customerForm.workEmail && !isValidEmail(customerForm.workEmail)) {
    throw new Error('El email del trabajo no tiene un formato valido.')
  }
}

function isValidEmail(value) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(value || '').trim())
}

function normalizeCustomerPayload() {
  return {
    ...customerForm,
    email: customerForm.email.trim().toLowerCase(),
    workEmail: customerForm.workEmail ? customerForm.workEmail.trim().toLowerCase() : null,
    workshopId: customerForm.workshopId || null,
  }
}

function handlePhotoUpload(event) {
  const file = event.target.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    Swal.fire('Archivo invalido', 'Solo se permiten imagenes o fotografias.', 'error')
    event.target.value = ''
    return
  }
  if (file.size > PHOTO_MAX_BYTES) {
    Swal.fire('Archivo demasiado grande', 'La foto no debe superar 20 MB.', 'error')
    event.target.value = ''
    return
  }
  const reader = new FileReader()
  reader.onload = () => {
    customerForm.photoDataUrl = reader.result
    photoPreview.value = reader.result
  }
  reader.readAsDataURL(file)
}

function changePassword() {
  run(
    async () => {
      await api.changePassword(passwordForm)
      notice.value = 'Contrasena actualizada.'
    },
    (error) => {
      notice.value = error.message
    },
  )
}

function logout() {
  clearSession()
  workspaceMode.value = 'dashboard'
  notice.value = 'Sesion cerrada.'
}
</script>
