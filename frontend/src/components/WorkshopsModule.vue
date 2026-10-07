<template>
  <div class="module-stack">
    <div class="module-toolbar">
      <div>
        <p class="eyebrow">Sucursales</p>
        <h3>Talleres registrados</h3>
      </div>
      <v-btn v-if="canManage" color="primary" prepend-icon="mdi-garage" @click="openCreate">Nuevo taller</v-btn>
    </div>

    <div class="workshop-grid">
      <v-card v-for="workshop in workshops" :key="workshop.id" class="panel-card" elevation="0">
        <v-icon icon="mdi-map-marker-radius" color="primary" size="30" />
        <strong>{{ workshop.name }}</strong>
        <span>{{ workshop.legalName }}</span>
        <span>{{ workshop.street }}, {{ workshop.neighborhood }}, {{ workshop.municipality }}, {{ workshop.state }} {{ workshop.postalCode }}</span>
        <v-chip color="secondary" variant="tonal" size="small">{{ workshop.rfc }}</v-chip>
        <v-btn v-if="canManage" variant="text" prepend-icon="mdi-pencil" @click="openEdit(workshop)">Editar</v-btn>
      </v-card>
    </div>

    <v-dialog v-model="dialog" max-width="920">
      <v-card class="dialog-card">
        <v-card-title>{{ editingId ? 'Editar taller' : 'Nuevo taller' }}</v-card-title>
        <v-card-text>
          <v-form class="data-form" @submit.prevent="saveWorkshop">
            <div class="form-grid">
              <v-text-field v-model="form.name" label="Nombre *" />
              <v-text-field v-model="form.legalName" label="Razon social *" />
              <v-text-field v-model="form.rfc" label="RFC *" maxlength="13" @input="form.rfc = form.rfc.toUpperCase()" />
              <v-text-field v-model="form.phone" label="Telefono *" maxlength="10" />
              <v-text-field v-model="form.email" label="Email *" type="email" />
              <v-text-field v-model="form.postalCode" label="Codigo postal *" maxlength="5" />
              <v-text-field v-model="form.state" label="Estado *" />
              <v-text-field v-model="form.municipality" label="Municipio *" />
              <v-text-field v-model="form.neighborhood" label="Colonia *" />
              <v-text-field v-model="form.street" label="Calle *" />
            </div>

            <div class="photo-uploader">
              <input ref="photoInput" class="hidden-input" type="file" accept="image/*" @change="handlePhoto" />
              <v-btn color="secondary" variant="tonal" prepend-icon="mdi-camera-plus" @click="photoInput?.click()">
                Subir foto
              </v-btn>
              <span>Maximo 15 MB, solo imagenes. Se guarda en disco/volumen.</span>
              <img v-if="preview" :src="preview" alt="Vista previa del taller" />
            </div>

            <div class="dialog-actions">
              <v-btn variant="text" @click="dialog = false">Cancelar</v-btn>
              <v-btn color="primary" type="submit" :loading="saving">Guardar</v-btn>
            </div>
          </v-form>
        </v-card-text>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import Swal from 'sweetalert2'
import { onMounted, reactive, ref } from 'vue'
import { workshopFacade } from '../facades/workshopFacade'

defineProps({
  canManage: { type: Boolean, default: false },
})

const maxPhotoBytes = 15 * 1024 * 1024
const rfcRegex = /^([A-ZÑ&]{3,4})\d{6}[A-Z0-9]{3}$/

const workshops = ref([])
const dialog = ref(false)
const saving = ref(false)
const editingId = ref(null)
const selectedPhoto = ref(null)
const photoInput = ref(null)
const preview = ref('')
const form = reactive(emptyForm())

onMounted(loadWorkshops)

async function loadWorkshops() {
  workshops.value = await workshopFacade.workshops.list()
}

function openCreate() {
  editingId.value = null
  selectedPhoto.value = null
  preview.value = ''
  Object.assign(form, emptyForm())
  dialog.value = true
}

function openEdit(workshop) {
  editingId.value = workshop.id
  selectedPhoto.value = null
  preview.value = ''
  Object.assign(form, { ...emptyForm(), ...workshop })
  dialog.value = true
}

async function saveWorkshop() {
  try {
    validateForm()
    saving.value = true
    const payload = normalizePayload()
    if (editingId.value) {
      await workshopFacade.workshops.update(editingId.value, payload, selectedPhoto.value)
    } else {
      await workshopFacade.workshops.create(payload, selectedPhoto.value)
    }
    await Swal.fire('Taller guardado', 'La sucursal fue registrada correctamente.', 'success')
    dialog.value = false
    await loadWorkshops()
  } catch (error) {
    await Swal.fire('Validacion detenida', error.message, 'warning')
  } finally {
    saving.value = false
  }
}

function handlePhoto(event) {
  const file = event.target.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    Swal.fire('Archivo invalido', 'Solo se permiten imagenes.', 'error')
    event.target.value = ''
    return
  }
  if (file.size > maxPhotoBytes) {
    Swal.fire('Archivo demasiado grande', 'La foto no debe superar 15 MB.', 'error')
    event.target.value = ''
    return
  }
  selectedPhoto.value = file
  preview.value = URL.createObjectURL(file)
}

function validateForm() {
  const required = [
    [form.name, 'Nombre'],
    [form.legalName, 'Razon social'],
    [form.rfc, 'RFC'],
    [form.phone, 'Telefono'],
    [form.email, 'Email'],
    [form.street, 'Calle'],
    [form.neighborhood, 'Colonia'],
    [form.municipality, 'Municipio'],
    [form.state, 'Estado'],
    [form.postalCode, 'Codigo postal'],
  ]
  const missing = required.find(([value]) => !String(value || '').trim())
  if (missing) throw new Error(`${missing[1]} es obligatorio.`)
  if (!rfcRegex.test(form.rfc)) throw new Error('El RFC no tiene un formato valido.')
  if (!/^[0-9]{10}$/.test(form.phone)) throw new Error('El telefono debe contener 10 digitos.')
  if (!/^[0-9]{5}$/.test(form.postalCode)) throw new Error('El codigo postal debe contener 5 digitos.')
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) throw new Error('El email no tiene un formato valido.')
}

function normalizePayload() {
  return {
    name: form.name.trim(),
    legalName: form.legalName.trim(),
    rfc: form.rfc.trim().toUpperCase(),
    phone: form.phone.trim(),
    email: form.email.trim().toLowerCase(),
    street: form.street.trim(),
    neighborhood: form.neighborhood.trim(),
    municipality: form.municipality.trim(),
    state: form.state.trim(),
    postalCode: form.postalCode.trim(),
  }
}

function emptyForm() {
  return {
    name: '',
    legalName: '',
    rfc: '',
    phone: '',
    email: '',
    street: '',
    neighborhood: '',
    municipality: '',
    state: '',
    postalCode: '',
  }
}
</script>
