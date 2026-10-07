<template>
  <v-app>
    <main v-if="!user" class="app-shell auth-layout">
      <section class="brand-pane">
        <v-chip class="brand-chip" color="primary" variant="tonal" prepend-icon="mdi-shield-key">
          Gestion segura
        </v-chip>

        <div class="brand-copy">
          <p class="eyebrow">Taller mecanico</p>
          <h1>Control operativo para talleres modernos.</h1>
          <p>
            Administra sucursales, clientes, usuarios y accesos desde una interfaz preparada para operar con seguridad.
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

      <section class="workspace-pane login-pane">
        <v-card class="auth-card login-card" elevation="0">
          <div class="auth-header">
            <div>
              <p class="eyebrow">Acceso protegido</p>
              <h2>{{ title }}</h2>
            </div>
          </div>

          <v-alert v-if="notice" class="mb-4" color="primary" variant="tonal" density="compact">
            {{ notice }}
          </v-alert>

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
        </v-card>
      </section>
    </main>

    <main v-else class="app-dashboard">
      <aside class="sidebar">
        <div class="sidebar-brand">
          <v-icon icon="mdi-car-cog" color="secondary" size="30" />
          <div>
            <strong>Taller mecanico</strong>
            <span>Operacion segura</span>
          </div>
        </div>

        <div class="sidebar-user">
          <v-avatar color="primary" size="40">
            <v-icon icon="mdi-account-cog" />
          </v-avatar>
          <div>
            <strong>{{ user.name }}</strong>
            <span>{{ user.email }}</span>
          </div>
        </div>

        <nav class="sidebar-nav">
          <v-btn
            v-for="item in navItems"
            :key="item.value"
            :color="workspaceMode === item.value ? 'primary' : undefined"
            :variant="workspaceMode === item.value ? 'flat' : 'text'"
            :prepend-icon="item.icon"
            block
            class="sidebar-link"
            @click="workspaceMode = item.value"
          >
            {{ item.label }}
          </v-btn>
        </nav>

        <div class="roles sidebar-roles">
          <v-chip v-for="role in user.roles" :key="role" color="secondary" variant="tonal" size="small">
            {{ role }}
          </v-chip>
        </div>

        <v-btn prepend-icon="mdi-logout" variant="tonal" color="secondary" @click="logout">
          Cerrar sesion
        </v-btn>
      </aside>

      <section class="dashboard-content">
        <div class="content-topbar">
          <div>
            <p class="eyebrow">Panel operativo</p>
            <h2>{{ title }}</h2>
          </div>
          <v-alert v-if="notice" class="topbar-alert" color="primary" variant="tonal" density="compact">
            {{ notice }}
          </v-alert>
        </div>

        <div class="content-card">
          <v-window v-model="workspaceMode">
            <v-window-item value="dashboard">
              <div class="dashboard-grid">
                <v-card v-for="item in rolePanels" :key="item.title" class="panel-card" elevation="0">
                  <v-icon :icon="item.icon" size="26" color="secondary" />
                  <strong>{{ item.title }}</strong>
                  <span>{{ item.text }}</span>
                </v-card>
              </div>
            </v-window-item>

            <v-window-item value="workshops">
              <WorkshopsModule :can-manage="hasRole('GERENTE')" />
            </v-window-item>

            <v-window-item value="customers">
              <CustomersModule :can-suspend="hasRole('GERENTE')" />
            </v-window-item>

            <v-window-item value="users">
              <UsersModule :can-admin-users="hasRole('GERENTE')" />
            </v-window-item>

            <v-window-item value="password">
              <v-form class="data-form narrow-form" @submit.prevent="changePassword">
                <v-text-field v-model="passwordForm.currentPassword" label="Actual" type="password" />
                <v-text-field v-model="passwordForm.newPassword" label="Nueva" type="password" />
                <v-btn color="primary" type="submit" :loading="loading">Actualizar</v-btn>
              </v-form>
            </v-window-item>
          </v-window>
        </div>
      </section>
    </main>
  </v-app>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import CustomersModule from './components/CustomersModule.vue'
import UsersModule from './components/UsersModule.vue'
import WorkshopsModule from './components/WorkshopsModule.vue'
import { workshopFacade } from './facades/workshopFacade'

const mode = ref('login')
const workspaceMode = ref('dashboard')
const loading = ref(false)
const notice = ref('')
const resetToken = ref('')
const user = ref(getStoredUser())

const loginForm = reactive({ email: '', password: '' })
const forgotForm = reactive({ email: '' })
const resetForm = reactive({ token: '', newPassword: '' })
const passwordForm = reactive({ currentPassword: '', newPassword: '' })

const metrics = [
  { icon: 'mdi-garage', value: 'Multi', label: 'sucursales' },
  { icon: 'mdi-account-lock', value: 'Roles', label: 'accesos seguros' },
  { icon: 'mdi-database-lock', value: 'Local', label: 'datos postales' },
]

const rolePanels = computed(() => [
  { icon: 'mdi-view-dashboard', title: 'Gerencia', text: 'Talleres, usuarios, clientes y suspension global.' },
  { icon: 'mdi-calendar-check', title: 'Secretaria', text: 'Alta de clientes y visitas por sucursal.' },
  { icon: 'mdi-tools', title: 'Mecanicos', text: 'Base lista para diagnosticos, refacciones y avances.' },
  { icon: 'mdi-account-wrench', title: 'Auxiliares', text: 'Operacion con permisos limitados.' },
])

const canManageCustomers = computed(() => hasRole('GERENTE') || hasRole('SECRETARIO'))

const navItems = computed(() => [
  { value: 'dashboard', label: 'Panel', icon: 'mdi-view-dashboard' },
  { value: 'workshops', label: 'Talleres', icon: 'mdi-garage' },
  ...(canManageCustomers.value ? [
    { value: 'customers', label: 'Clientes', icon: 'mdi-account-group' },
    { value: 'users', label: 'Usuarios', icon: 'mdi-account-key' },
  ] : []),
  { value: 'password', label: 'Clave', icon: 'mdi-lock-reset' },
])

const title = computed(() => {
  if (user.value) {
    return navItems.value.find((item) => item.value === workspaceMode.value)?.label || 'Panel operativo'
  }
  return mode.value === 'login' ? 'Inicio de sesion' : 'Recuperar acceso'
})

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
      setSession(await workshopFacade.auth.login(loginForm))
      notice.value = 'Sesion iniciada con JWT.'
      workspaceMode.value = 'dashboard'
    },
    (error) => {
      notice.value = error.message
    },
  )
}

function forgotPassword() {
  run(
    async () => {
      const response = await workshopFacade.auth.forgotPassword(forgotForm)
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
      await workshopFacade.auth.resetPassword(resetForm)
      mode.value = 'login'
      resetToken.value = ''
      notice.value = 'Contrasena actualizada. Inicia sesion.'
    },
    (error) => {
      notice.value = error.message
    },
  )
}

function changePassword() {
  run(
    async () => {
      await workshopFacade.auth.changePassword(passwordForm)
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
