<template>
  <div class="module-stack">
    <div class="module-toolbar">
      <div>
        <p class="eyebrow">Usuarios</p>
        <h3>Alta y administracion de cuentas</h3>
      </div>
      <v-btn color="primary" prepend-icon="mdi-account-plus" @click="openCreate">Nuevo usuario</v-btn>
    </div>

    <v-alert v-if="!canAdminUsers" color="secondary" variant="tonal" density="compact">
      Tu rol permite crear usuarios, pero la consulta completa y edicion de roles queda reservada para gerencia.
    </v-alert>

    <v-data-table
      v-if="canAdminUsers"
      :headers="headers"
      :items="users"
      :loading="loading"
      item-value="id"
      class="data-table"
    >
      <template #item.roles="{ item }">
        <div class="chip-row">
          <v-chip v-for="role in item.roles" :key="role" size="small" color="primary" variant="tonal">
            {{ role }}
          </v-chip>
        </div>
      </template>
      <template #item.enabled="{ item }">
        <v-chip :color="item.enabled ? 'success' : 'warning'" size="small">
          {{ item.enabled ? 'Activo' : 'Inactivo' }}
        </v-chip>
      </template>
      <template #item.createdAt="{ item }">
        {{ formatDate(item.createdAt) }}
      </template>
      <template #item.actions="{ item }">
        <v-btn icon="mdi-shield-edit" size="small" variant="text" aria-label="Editar roles" @click="openRoles(item)" />
      </template>
    </v-data-table>

    <v-dialog v-model="createDialog" max-width="760">
      <v-card class="dialog-card">
        <v-card-title>Nuevo usuario</v-card-title>
        <v-card-text>
          <v-form class="data-form" @submit.prevent="createUser">
            <v-text-field v-model="form.name" label="Nombre completo *" prepend-inner-icon="mdi-account" />
            <v-text-field v-model="form.email" label="Correo *" type="email" prepend-inner-icon="mdi-email" />
            <v-text-field
              v-model="form.password"
              label="Contrasena temporal *"
              type="password"
              prepend-inner-icon="mdi-lock-check"
            />
            <v-select v-model="form.roles" :items="availableRoles" label="Roles *" multiple chips />
            <div class="dialog-actions">
              <v-btn variant="text" @click="createDialog = false">Cancelar</v-btn>
              <v-btn color="primary" type="submit" :loading="saving">Crear usuario</v-btn>
            </div>
          </v-form>
        </v-card-text>
      </v-card>
    </v-dialog>

    <v-dialog v-model="rolesDialog" max-width="640">
      <v-card class="dialog-card">
        <v-card-title>Editar roles</v-card-title>
        <v-card-text>
          <div class="user-detail-grid">
            <span>ID</span><strong>{{ selectedUser?.id }}</strong>
            <span>Nombre</span><strong>{{ selectedUser?.name }}</strong>
            <span>Correo</span><strong>{{ selectedUser?.email }}</strong>
            <span>Creado</span><strong>{{ formatDate(selectedUser?.createdAt) }}</strong>
          </div>
          <v-select v-model="roleForm.roles" class="mt-4" :items="managerRoles" label="Roles" multiple chips />
          <div class="dialog-actions mt-4">
            <v-btn variant="text" @click="rolesDialog = false">Cancelar</v-btn>
            <v-btn color="primary" :loading="saving" @click="saveRoles">Guardar roles</v-btn>
          </div>
        </v-card-text>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import Swal from 'sweetalert2'
import { computed, onMounted, reactive, ref } from 'vue'
import { workshopFacade } from '../facades/workshopFacade'

const props = defineProps({
  canAdminUsers: { type: Boolean, default: false },
})

const headers = [
  { title: 'ID', key: 'id', sortable: true },
  { title: 'Nombre', key: 'name', sortable: true },
  { title: 'Correo', key: 'email', sortable: true },
  { title: 'Roles', key: 'roles', sortable: false },
  { title: 'Estado', key: 'enabled', sortable: true },
  { title: 'Creado', key: 'createdAt', sortable: true },
  { title: '', key: 'actions', sortable: false },
]

const managerRoles = ['GERENTE', 'SECRETARIO', 'AUXILIAR', 'MECANICO']
const availableRoles = computed(() => (props.canAdminUsers ? managerRoles : ['SECRETARIO', 'AUXILIAR', 'MECANICO']))

const users = ref([])
const loading = ref(false)
const saving = ref(false)
const createDialog = ref(false)
const rolesDialog = ref(false)
const selectedUser = ref(null)
const form = reactive(emptyForm())
const roleForm = reactive({ roles: [] })

onMounted(loadUsers)

async function loadUsers() {
  if (!props.canAdminUsers) return
  loading.value = true
  try {
    users.value = await workshopFacade.users.list()
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, emptyForm())
  createDialog.value = true
}

async function createUser() {
  try {
    validateUserForm()
    saving.value = true
    const created = await workshopFacade.users.create({
      ...form,
      email: form.email.trim().toLowerCase(),
    })
    await Swal.fire({
      title: 'Usuario registrado',
      text: `${created.name} fue dado de alta correctamente.`,
      icon: 'success',
    })
    createDialog.value = false
    await loadUsers()
  } catch (error) {
    await Swal.fire({
      title: 'No se pudo registrar',
      text: error.message,
      icon: 'error',
    })
  } finally {
    saving.value = false
  }
}

function openRoles(user) {
  selectedUser.value = user
  roleForm.roles = [...(user.roles || [])]
  rolesDialog.value = true
}

async function saveRoles() {
  try {
    if (!roleForm.roles.length) throw new Error('Selecciona al menos un rol.')
    saving.value = true
    await workshopFacade.users.updateRoles(selectedUser.value.id, roleForm.roles)
    await Swal.fire('Roles actualizados', 'Los permisos del usuario fueron actualizados.', 'success')
    rolesDialog.value = false
    await loadUsers()
  } catch (error) {
    await Swal.fire('No se pudieron actualizar roles', error.message, 'error')
  } finally {
    saving.value = false
  }
}

function validateUserForm() {
  if (!form.name.trim()) throw new Error('El nombre completo es obligatorio.')
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) throw new Error('El correo del usuario no tiene un formato valido.')
  if (!form.password || form.password.length < 8) throw new Error('La contrasena temporal debe tener al menos 8 caracteres.')
  if (!form.roles.length) throw new Error('Selecciona al menos un rol.')
}

function emptyForm() {
  return { name: '', email: '', password: '', roles: ['AUXILIAR'] }
}

function formatDate(value) {
  return value ? new Date(value).toLocaleString('es-MX') : 'Sin dato'
}
</script>
