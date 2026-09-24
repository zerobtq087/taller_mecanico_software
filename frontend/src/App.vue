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
            Login, registro, recuperacion de contrasena y permisos por rol para gerente,
            secretario/a, auxiliar y mecanico.
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
              <v-tab value="register">Registro</v-tab>
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

              <v-window-item value="register">
                <v-form @submit.prevent="register">
                  <v-text-field v-model="registerForm.name" label="Nombre completo" prepend-inner-icon="mdi-account" />
                  <v-text-field v-model="registerForm.email" label="Correo" type="email" prepend-inner-icon="mdi-email" />
                  <v-text-field
                    v-model="registerForm.password"
                    label="Contrasena segura"
                    type="password"
                    prepend-inner-icon="mdi-lock-check"
                  />
                  <v-btn block size="large" color="primary" type="submit" :loading="loading">
                    Crear usuario
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

            <v-divider class="my-6" />
            <v-btn block variant="tonal" color="secondary" prepend-icon="mdi-account-hard-hat" @click="demoLogin">
              Ver demo como gerente
            </v-btn>
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

            <div class="dashboard-grid">
              <v-card v-for="item in rolePanels" :key="item.title" class="panel-card" elevation="0">
                <v-icon :icon="item.icon" size="30" color="primary" />
                <strong>{{ item.title }}</strong>
                <span>{{ item.text }}</span>
              </v-card>
            </div>

            <v-expansion-panels class="mt-5" variant="accordion">
              <v-expansion-panel title="Cambio de contrasena">
                <v-expansion-panel-text>
                  <v-form @submit.prevent="changePassword">
                    <v-text-field v-model="passwordForm.currentPassword" label="Actual" type="password" />
                    <v-text-field v-model="passwordForm.newPassword" label="Nueva" type="password" />
                    <v-btn color="primary" type="submit" :loading="loading">Actualizar</v-btn>
                  </v-form>
                </v-expansion-panel-text>
              </v-expansion-panel>
            </v-expansion-panels>
          </template>
        </v-card>
      </section>
    </main>
  </v-app>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { api } from './services/api'

const mode = ref('login')
const loading = ref(false)
const notice = ref('')
const resetToken = ref('')
const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

const loginForm = reactive({ email: 'gerente@taller.local', password: 'Paradox87!' })
const registerForm = reactive({ name: '', email: '', password: '' })
const forgotForm = reactive({ email: '' })
const resetForm = reactive({ token: '', newPassword: '' })
const passwordForm = reactive({ currentPassword: '', newPassword: '' })

const metrics = [
  { icon: 'mdi-car-cog', value: '18', label: 'ordenes activas' },
  { icon: 'mdi-account-lock', value: '4', label: 'roles operativos' },
  { icon: 'mdi-database-lock', value: 'BCrypt', label: 'hash + salt' },
]

const rolePanels = computed(() => [
  { icon: 'mdi-view-dashboard', title: 'Gerencia', text: 'Reportes, usuarios, roles y auditoria.' },
  { icon: 'mdi-calendar-check', title: 'Secretaria', text: 'Citas, recepcion, clientes y entregas.' },
  { icon: 'mdi-tools', title: 'Mecanicos', text: 'Diagnosticos, refacciones y avances.' },
  { icon: 'mdi-account-wrench', title: 'Auxiliares', text: 'Apoyo operativo con permisos limitados.' },
])

const title = computed(() => {
  if (user.value) return 'Panel operativo'
  return mode.value === 'login' ? 'Inicio de sesion' : mode.value === 'register' ? 'Nuevo usuario' : 'Recuperar acceso'
})

async function run(action, fallback) {
  loading.value = true
  notice.value = ''
  try {
    await action()
  } catch (error) {
    fallback?.(error)
  } finally {
    loading.value = false
  }
}

function setSession(payload) {
  localStorage.setItem('token', payload.token)
  localStorage.setItem('user', JSON.stringify(payload.user))
  user.value = payload.user
}

function demoLogin() {
  setSession({
    token: 'demo-token',
    user: {
      id: 1,
      name: 'Gerente Demo',
      email: 'gerente@taller.local',
      roles: ['GERENTE', 'SECRETARIO', 'AUXILIAR', 'MECANICO'],
    },
  })
  notice.value = 'Modo demo activo para revisar el diseno sin backend.'
}

function login() {
  run(
    async () => {
      setSession(await api.login(loginForm))
      notice.value = 'Sesion iniciada con JWT.'
    },
    () => {
      demoLogin()
    },
  )
}

function register() {
  run(
    async () => {
      setSession(await api.register(registerForm))
      notice.value = 'Usuario registrado como AUXILIAR.'
    },
    (error) => {
      notice.value = `${error.message}. Backend apagado: mostrando registro demo.`
      setSession({
        token: 'demo-token',
        user: { id: 2, name: registerForm.name || 'Usuario Nuevo', email: registerForm.email, roles: ['AUXILIAR'] },
      })
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
    () => {
      resetToken.value = 'demo-reset-token'
      resetForm.token = resetToken.value
      notice.value = 'Modo demo: token generado localmente.'
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
    () => {
      mode.value = 'login'
      notice.value = 'Modo demo: contrasena cambiada visualmente.'
    },
  )
}

function changePassword() {
  run(
    async () => {
      await api.changePassword(passwordForm)
      notice.value = 'Contrasena actualizada.'
    },
    () => {
      notice.value = 'Modo demo: cambio simulado.'
    },
  )
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  user.value = null
  notice.value = 'Sesion cerrada.'
}
</script>
