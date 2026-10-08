<template>
  <div class="module-stack">
    <div class="module-toolbar">
      <v-select v-model="filters.workshopId" :items="workshopOptions" label="Filtrar por taller" clearable density="comfortable" />
      <div class="inline-actions">
        <v-btn v-if="canSuspend" color="secondary" variant="tonal" prepend-icon="mdi-list-status" @click="openStatuses">
          Estatus
        </v-btn>
        <v-btn color="primary" prepend-icon="mdi-account-plus" @click="openCreate">Nuevo cliente</v-btn>
      </div>
    </div>

    <v-data-table-server
      v-model:items-per-page="table.itemsPerPage"
      :headers="headers"
      :items="customers"
      :items-length="table.totalItems"
      :loading="loading"
      item-value="id"
      class="data-table customers-table"
      density="compact"
      @update:options="loadCustomers"
    >
      <template #item.fullName="{ item }">
        <div class="customer-name-cell">
          <v-avatar color="primary" size="34">
            <v-icon icon="mdi-account" size="18" />
          </v-avatar>
          <div>
            <strong>{{ item.fullName }}</strong>
            <span>{{ item.email }}</span>
          </div>
        </div>
      </template>
      <template #item.status="{ item }">
        <v-chip :color="item.status === 'ACTIVO' ? 'success' : 'warning'" size="small">{{ item.status }}</v-chip>
      </template>
      <template #item.workshops="{ item }">
        <div class="workshop-cell">
          <v-chip size="small" color="secondary" variant="tonal">
            {{ primaryWorkshopName(item) }}
          </v-chip>
          <span v-if="extraWorkshopCount(item)" class="muted-count">+{{ extraWorkshopCount(item) }}</span>
        </div>
      </template>
      <template #item.vehicleCount="{ item }">
        <v-btn
          size="small"
          variant="tonal"
          color="info"
          prepend-icon="mdi-car-multiple"
          class="vehicle-count-btn"
          @click="openVehicles(item)"
        >
          {{ item.vehicleCount || 0 }}
        </v-btn>
      </template>
      <template #item.actions="{ item }">
        <v-menu location="bottom end">
          <template #activator="{ props: menuProps }">
            <v-btn v-bind="menuProps" size="small" variant="tonal" color="secondary" append-icon="mdi-chevron-down" class="actions-btn">
              Acciones
            </v-btn>
          </template>
          <v-list class="actions-menu" density="compact">
            <v-list-item prepend-icon="mdi-pencil" title="Editar" @click="openEdit(item)" />
            <v-list-item prepend-icon="mdi-car" title="Vehiculos" @click="openVehicles(item)" />
            <v-list-item prepend-icon="mdi-history" title="Historial" @click="openHistory(item)" />
            <v-list-item
              v-if="canSuspend && item.status === 'ACTIVO'"
              prepend-icon="mdi-account-cancel"
              title="Suspender"
              class="warning-action"
              @click="suspendCustomer(item)"
            />
          </v-list>
        </v-menu>
      </template>
    </v-data-table-server>

    <v-dialog v-model="formDialog" max-width="1040">
      <v-card class="dialog-card">
        <v-card-title>{{ editingId ? 'Editar cliente' : 'Alta de cliente' }}</v-card-title>
        <v-card-text>
          <v-form class="data-form" @submit.prevent="saveCustomer">
            <div class="form-grid">
              <v-text-field v-model="form.firstName" label="Nombre *" />
              <v-text-field v-model="form.lastName" label="Apellido paterno *" />
              <v-text-field v-model="form.secondLastName" label="Apellido materno *" />
              <v-text-field v-model="form.alternateContactName" label="Contacto alternativo *" />
              <v-text-field v-model="form.birthDate" label="Fecha de nacimiento *" type="date" />
              <v-text-field v-model="form.curp" label="CURP *" maxlength="18" @input="form.curp = form.curp.toUpperCase()" />
              <v-text-field v-model="form.rfc" label="RFC *" maxlength="13" @input="form.rfc = form.rfc.toUpperCase()" />
              <v-text-field v-model="form.contactPhone" label="Telefono de contacto *" maxlength="10" />
              <v-text-field v-model="form.workPhone" label="Telefono del trabajo *" maxlength="10" />
              <v-text-field v-model="form.email" label="Email *" type="email" />
              <v-text-field v-model="form.workEmail" label="Email del trabajo" type="email" />
              <v-text-field
                v-model="form.postalCode"
                label="Codigo postal *"
                maxlength="5"
                :loading="postalLoading"
                append-inner-icon="mdi-map-search"
                @click:append-inner="lookupPostalCode"
              />
              <v-autocomplete v-model="form.state" :items="states" label="Estado *" clearable @update:model-value="loadMunicipalities" />
              <v-autocomplete v-model="form.municipality" :items="municipalities" label="Municipio *" clearable @update:model-value="loadSettlements" />
              <v-autocomplete
                v-model="form.neighborhood"
                :items="settlementNames"
                label="Colonia *"
                clearable
                @update:model-value="lookupPostalSelection"
              />
              <v-text-field v-model="form.street" label="Calle *" />
              <v-select
                v-if="!editingId"
                v-model="form.currentWorkshopId"
                :items="workshopOptions"
                label="Taller actual *"
              />
              <v-select
                v-else
                v-model="form.workshopIds"
                :items="workshopOptions"
                label="Talleres asociados *"
                multiple
                chips
              />
            </div>
            <div class="photo-uploader compact-photo">
              <input ref="photoInput" class="hidden-input" type="file" accept="image/*" @change="handlePhoto" />
              <v-btn color="secondary" variant="tonal" prepend-icon="mdi-camera-plus" @click="photoInput?.click()">
                Foto del cliente
              </v-btn>
              <span>{{ editingId ? 'Opcional al editar. Maximo 15 MB.' : 'Obligatoria. Maximo 15 MB, solo imagenes.' }}</span>
              <img v-if="photoPreview" :src="photoPreview" alt="Vista previa del cliente" />
            </div>
            <div class="dialog-actions">
              <v-btn variant="text" @click="formDialog = false">Cancelar</v-btn>
              <v-btn color="primary" type="submit" :loading="saving">Guardar</v-btn>
            </div>
          </v-form>
        </v-card-text>
      </v-card>
    </v-dialog>

    <v-dialog v-model="historyDialog" max-width="760">
      <v-card class="dialog-card">
        <v-card-title>Historial de talleres</v-card-title>
        <v-card-text>
          <v-list lines="two">
            <v-list-item v-for="visit in selectedHistory" :key="visit.workshopId">
              <v-list-item-title>{{ visit.workshopName }}</v-list-item-title>
              <v-list-item-subtitle>
                Primera visita: {{ formatDate(visit.firstVisitAt) }} · Ultima visita: {{ formatDate(visit.lastVisitAt) }}
              </v-list-item-subtitle>
            </v-list-item>
          </v-list>
        </v-card-text>
      </v-card>
    </v-dialog>

    <v-dialog v-model="vehiclesDialog" max-width="1040">
      <v-card class="dialog-card">
        <v-card-title>Vehiculos de {{ selectedCustomer?.fullName }}</v-card-title>
        <v-card-text>
          <v-alert v-if="selectedCustomer?.status !== 'ACTIVO'" type="warning" variant="tonal" class="mb-4">
            El cliente no esta activo. No se pueden agregar vehiculos nuevos.
          </v-alert>
          <div class="module-toolbar vehicle-toolbar">
            <h3>Lista de vehiculos</h3>
            <v-btn
              color="primary"
              prepend-icon="mdi-car-plus"
              :disabled="selectedCustomer?.status !== 'ACTIVO'"
              @click="openVehicleForm()"
            >
              Agregar vehiculo
            </v-btn>
          </div>
          <v-data-table :headers="vehicleHeaders" :items="vehicles" class="data-table" density="compact">
            <template #item.status="{ item }">
              <v-chip :color="item.status === 'ACTIVO' ? 'success' : 'warning'" size="small">{{ item.status }}</v-chip>
            </template>
            <template #item.actions="{ item }">
              <div class="inline-actions">
                <v-btn icon="mdi-pencil" size="small" variant="text" @click="openVehicleForm(item)" />
                <v-btn icon="mdi-cancel" size="small" variant="text" color="warning" @click="cancelVehicle(item)" />
              </div>
            </template>
          </v-data-table>
        </v-card-text>
      </v-card>
    </v-dialog>

    <v-dialog v-model="vehicleFormDialog" max-width="860">
      <v-card class="dialog-card">
        <v-card-title>{{ editingVehicleId ? 'Editar vehiculo' : 'Agregar vehiculo' }}</v-card-title>
        <v-card-text>
          <v-form class="data-form" @submit.prevent="saveVehicle">
            <div class="form-grid">
              <v-text-field v-model="vehicleForm.vin" label="VIN *" maxlength="17" @input="vehicleForm.vin = vehicleForm.vin.toUpperCase()" />
              <v-text-field v-model="vehicleForm.plate" label="Placas *" maxlength="10" @input="vehicleForm.plate = vehicleForm.plate.toUpperCase()" />
              <v-autocomplete
                v-model="vehicleForm.make"
                :items="vehicleMakeOptions"
                label="Marca *"
                :loading="vehicleCatalogLoading"
                clearable
                @update:search="searchVehicleMakes"
                @update:model-value="onVehicleMakeChange"
              />
              <v-autocomplete
                v-model="vehicleForm.model"
                :items="vehicleModelOptions"
                label="Modelo *"
                :loading="vehicleCatalogLoading"
                clearable
                @update:search="searchVehicleModels"
                @update:model-value="onVehicleModelChange"
              />
              <v-text-field v-model.number="vehicleForm.year" label="Año *" type="number" />
              <v-autocomplete
                v-model="vehicleForm.version"
                :items="vehicleVersionOptions"
                label="Version *"
                :loading="vehicleCatalogLoading"
                clearable
                @update:search="searchVehicleVersions"
              />
              <v-text-field v-model="vehicleForm.color" label="Color *" />
              <v-text-field v-model.number="vehicleForm.mileage" label="Kilometraje" type="number" />
              <v-text-field v-model="vehicleForm.serialNumber" label="Numero de serie *" @input="vehicleForm.serialNumber = vehicleForm.serialNumber.toUpperCase()" />
            </div>
            <div class="dialog-actions">
              <v-btn variant="text" @click="vehicleFormDialog = false">Cancelar</v-btn>
              <v-btn color="primary" type="submit" :loading="savingVehicle">Guardar vehiculo</v-btn>
            </div>
          </v-form>
        </v-card-text>
      </v-card>
    </v-dialog>

    <v-dialog v-model="statusesDialog" max-width="820">
      <v-card class="dialog-card">
        <v-card-title>Catalogo de estatus</v-card-title>
        <v-card-text>
          <v-form class="data-form" @submit.prevent="saveStatus">
            <div class="form-grid">
              <v-text-field v-model="statusForm.strValor" label="Valor *" @input="statusForm.strValor = statusForm.strValor.toUpperCase()" />
              <v-text-field v-model="statusForm.strDescripcion" label="Descripcion *" />
            </div>
            <div class="dialog-actions">
              <v-btn variant="text" @click="resetStatusForm">Limpiar</v-btn>
              <v-btn color="primary" type="submit" :loading="savingStatus">{{ editingStatusId ? 'Actualizar' : 'Crear' }}</v-btn>
            </div>
          </v-form>
          <v-data-table :headers="statusHeaders" :items="statuses" class="data-table mt-4" density="compact">
            <template #item.actions="{ item }">
              <div class="inline-actions">
                <v-btn icon="mdi-pencil" size="small" variant="text" @click="editStatus(item)" />
                <v-btn icon="mdi-cancel" size="small" variant="text" color="warning" @click="cancelStatus(item)" />
              </div>
            </template>
          </v-data-table>
        </v-card-text>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import Swal from 'sweetalert2'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { workshopFacade } from '../facades/workshopFacade'

const props = defineProps({
  canSuspend: { type: Boolean, default: false },
})

const nameRegex = /^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ\s]+$/
const curpRegex = /^[A-Z][AEIOUX][A-Z]{2}\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])[HM](AS|BC|BS|CC|CL|CM|CS|CH|DF|DG|GT|GR|HG|JC|MC|MN|MS|NT|NL|OC|PL|QT|QR|SP|SL|SR|TC|TS|TL|VZ|YN|ZS|NE)[B-DF-HJ-NP-TV-Z]{3}[A-Z0-9]\d$/
const rfcRegex = /^([A-ZÑ&]{3,4})\d{6}[A-Z0-9]{3}$/
const vinRegex = /^[A-HJ-NPR-Z0-9]{17}$/
const plateRegex = /^[A-Z0-9-]{5,10}$/
const maxPhotoBytes = 15 * 1024 * 1024

const headers = [
  { title: 'Cliente', key: 'fullName', sortable: true, width: '190px' },
  { title: 'Estatus', key: 'status', sortable: true, width: '80px' },
  { title: 'Taller', key: 'workshops', sortable: false, width: '90px' },
  { title: 'Vehiculos', key: 'vehicleCount', sortable: false, width: '95px' },
  { title: 'Email', key: 'email', sortable: true, width: '145px' },
  { title: 'Telefono', key: 'contactPhone', sortable: true, width: '95px' },
  { title: 'Trabajo', key: 'workPhone', sortable: true, width: '95px' },
  { title: '', key: 'actions', sortable: false, width: '95px', align: 'end' },
]
const vehicleHeaders = [
  { title: 'Placas', key: 'plate' },
  { title: 'VIN', key: 'vin' },
  { title: 'Marca', key: 'make' },
  { title: 'Modelo', key: 'model' },
  { title: 'Año', key: 'year' },
  { title: 'Estatus', key: 'status' },
  { title: '', key: 'actions', sortable: false },
]
const statusHeaders = [
  { title: 'ID', key: 'id' },
  { title: 'Valor', key: 'strValor' },
  { title: 'Descripcion', key: 'strDescripcion' },
  { title: '', key: 'actions', sortable: false },
]

const loading = ref(false)
const saving = ref(false)
const postalLoading = ref(false)
const savingVehicle = ref(false)
const savingStatus = ref(false)
const vehicleCatalogLoading = ref(false)
const customers = ref([])
const workshops = ref([])
const vehicles = ref([])
const statuses = ref([])
const vehicleMakes = ref([])
const vehicleModels = ref([])
const vehicleVersions = ref([])
const states = ref([])
const municipalities = ref([])
const settlements = ref([])
const formDialog = ref(false)
const historyDialog = ref(false)
const vehiclesDialog = ref(false)
const vehicleFormDialog = ref(false)
const statusesDialog = ref(false)
const selectedHistory = ref([])
const selectedCustomer = ref(null)
const editingId = ref(null)
const editingVehicleId = ref(null)
const editingStatusId = ref(null)
const photoInput = ref(null)
const photoFile = ref(null)
const photoPreview = ref('')
const lastPostalLookup = ref('')
const table = reactive({ page: 1, itemsPerPage: 10, sortBy: [], totalItems: 0 })
const filters = reactive({ workshopId: null })
const form = reactive(emptyForm())
const vehicleForm = reactive(emptyVehicleForm())
const statusForm = reactive(emptyStatusForm())

const workshopOptions = computed(() => workshops.value.map((workshop) => ({ title: workshop.name, value: workshop.id })))
const settlementNames = computed(() => [...new Set(settlements.value.map((settlement) => settlement.name))])
const vehicleMakeOptions = computed(() => vehicleMakes.value.map((make) => make.name))
const vehicleModelOptions = computed(() => vehicleModels.value.map((model) => model.name))
const vehicleVersionOptions = computed(() => vehicleVersions.value.map((version) => version.name))

watch(() => filters.workshopId, () => loadCustomers({ page: 1, itemsPerPage: table.itemsPerPage, sortBy: table.sortBy }))
watch(
  () => form.postalCode,
  (value) => {
    const normalized = String(value || '').replace(/\D/g, '').slice(0, 5)
    if (value !== normalized) {
      form.postalCode = normalized
      return
    }
    if (normalized.length < 5) {
      lastPostalLookup.value = ''
      settlements.value = []
      form.state = ''
      form.municipality = ''
      form.neighborhood = ''
      return
    }
    if (normalized !== lastPostalLookup.value) {
      lookupPostalCode({ silent: true })
    }
  },
)

onMounted(async () => {
  await Promise.all([loadWorkshops(), loadStates()])
})

async function loadWorkshops() {
  workshops.value = await workshopFacade.workshops.list()
}

async function loadStates() {
  states.value = await workshopFacade.postalCatalog.states()
}

async function loadCustomers(options = table) {
  table.page = options.page || 1
  table.itemsPerPage = options.itemsPerPage || 10
  table.sortBy = options.sortBy || []
  const sort = table.sortBy[0] || {}
  loading.value = true
  try {
    const response = await workshopFacade.customers.list({
      page: table.page - 1,
      size: table.itemsPerPage,
      workshopId: filters.workshopId,
      sortBy: sort.key || 'id',
      direction: sort.order || 'asc',
    })
    customers.value = response.content || []
    table.totalItems = response.totalElements || 0
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  photoFile.value = null
  photoPreview.value = ''
  lastPostalLookup.value = ''
  Object.assign(form, emptyForm())
  formDialog.value = true
}

function openEdit(customer) {
  editingId.value = customer.id
  photoFile.value = null
  photoPreview.value = ''
  lastPostalLookup.value = ''
  Object.assign(form, {
    ...emptyForm(),
    ...customer,
    currentWorkshopId: customer.workshops?.[0]?.workshopId || null,
    workshopIds: customer.workshops?.map((visit) => visit.workshopId) || [],
  })
  formDialog.value = true
}

function openHistory(customer) {
  selectedHistory.value = customer.workshops || []
  historyDialog.value = true
}

async function openVehicles(customer) {
  selectedCustomer.value = customer
  vehiclesDialog.value = true
  vehicles.value = await workshopFacade.customers.vehicles(customer.id)
  syncSelectedCustomerVehicleCount()
}

async function openVehicleForm(vehicle = null) {
  editingVehicleId.value = vehicle?.id || null
  Object.assign(vehicleForm, vehicle ? {
    vin: vehicle.vin,
    plate: vehicle.plate,
    make: vehicle.make,
    model: vehicle.model,
    year: vehicle.year,
    version: vehicle.version,
    color: vehicle.color,
    mileage: vehicle.mileage,
    serialNumber: vehicle.serialNumber,
  } : emptyVehicleForm())
  vehicleVersions.value = []
  if (vehicle?.make) {
    await searchVehicleMakes(vehicle.make)
    await searchVehicleModels(vehicle.model || '')
  }
  if (vehicle?.make && vehicle?.model) {
    await searchVehicleVersions(vehicle.version || '')
  }
  vehicleFormDialog.value = true
}

async function saveVehicle() {
  try {
    validateVehicleForm()
    savingVehicle.value = true
    const payload = normalizeVehiclePayload()
    if (editingVehicleId.value) {
      await workshopFacade.customers.updateVehicle(selectedCustomer.value.id, editingVehicleId.value, payload)
    } else {
      await workshopFacade.customers.createVehicle(selectedCustomer.value.id, payload)
    }
    await Swal.fire('Vehiculo guardado', 'El vehiculo fue registrado correctamente.', 'success')
    vehicleFormDialog.value = false
    vehicles.value = await workshopFacade.customers.vehicles(selectedCustomer.value.id)
    syncSelectedCustomerVehicleCount()
    await loadCustomers()
  } catch (error) {
    await Swal.fire('Validacion detenida', error.message, 'warning')
  } finally {
    savingVehicle.value = false
  }
}

async function cancelVehicle(vehicle) {
  const result = await Swal.fire({
    title: 'Cancelar vehiculo',
    text: `No se eliminara el registro; solo cambiara su estatus para placas ${vehicle.plate}.`,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: 'Cancelar vehiculo',
    cancelButtonText: 'Regresar',
  })
  if (!result.isConfirmed) return
  await workshopFacade.customers.cancelVehicle(selectedCustomer.value.id, vehicle.id)
  vehicles.value = await workshopFacade.customers.vehicles(selectedCustomer.value.id)
  syncSelectedCustomerVehicleCount()
  await loadCustomers()
}

async function openStatuses() {
  statusesDialog.value = true
  await loadStatuses()
}

async function loadStatuses() {
  statuses.value = await workshopFacade.statuses.list()
}

function editStatus(status) {
  editingStatusId.value = status.id
  Object.assign(statusForm, {
    strValor: status.strValor,
    strDescripcion: status.strDescripcion,
  })
}

async function saveStatus() {
  try {
    if (!statusForm.strValor.trim() || !statusForm.strDescripcion.trim()) {
      throw new Error('Captura valor y descripcion del estatus.')
    }
    savingStatus.value = true
    const payload = {
      strValor: statusForm.strValor.trim().toUpperCase(),
      strDescripcion: statusForm.strDescripcion.trim().toLowerCase(),
    }
    if (editingStatusId.value) {
      await workshopFacade.statuses.update(editingStatusId.value, payload)
    } else {
      await workshopFacade.statuses.create(payload)
    }
    resetStatusForm()
    await loadStatuses()
  } catch (error) {
    await Swal.fire('Estatus no guardado', error.message, 'warning')
  } finally {
    savingStatus.value = false
  }
}

async function cancelStatus(status) {
  const result = await Swal.fire({
    title: 'Cancelar estatus',
    text: 'No se elimina fisicamente; se actualiza a CANCELADO.',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: 'Continuar',
    cancelButtonText: 'Regresar',
  })
  if (!result.isConfirmed) return
  await workshopFacade.statuses.cancel(status.id)
  await loadStatuses()
}

function resetStatusForm() {
  editingStatusId.value = null
  Object.assign(statusForm, emptyStatusForm())
}

function primaryWorkshopName(customer) {
  return customer.workshops?.[0]?.workshopName || 'Sin taller'
}

function extraWorkshopCount(customer) {
  return Math.max((customer.workshops?.length || 0) - 1, 0)
}

async function saveCustomer() {
  try {
    validateForm()
    saving.value = true
    const payload = normalizePayload()
    const saved = editingId.value
      ? await workshopFacade.customers.update(editingId.value, payload, photoFile.value)
      : await workshopFacade.customers.register(payload, photoFile.value)
    await Swal.fire(
      saved.existingCustomer ? 'Cliente existente' : 'Cliente guardado',
      saved.existingCustomer
        ? 'El cliente ya existia; se registro la visita al taller seleccionado.'
        : 'El cliente fue registrado correctamente.',
      'success',
    )
    formDialog.value = false
    await loadCustomers()
  } catch (error) {
    await Swal.fire('Validacion detenida', error.message, 'warning')
  } finally {
    saving.value = false
  }
}

async function suspendCustomer(customer) {
  if (customer.status === 'SUSPENDIDO') {
    await Swal.fire('Cliente ya suspendido', 'El cliente ya se encuentra suspendido.', 'info')
    return
  }
  const result = await Swal.fire({
    title: 'Suspender cliente',
    text: `Se aplicara en todos los talleres para ${customer.fullName}.`,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: 'Suspender',
    cancelButtonText: 'Cancelar',
  })
  if (!result.isConfirmed) return
  await workshopFacade.customers.suspend(customer.id)
  await loadCustomers()
}

async function searchVehicleMakes(query = '') {
  if (String(query || '').trim().length < 1) return
  vehicleCatalogLoading.value = true
  try {
    vehicleMakes.value = await workshopFacade.vehicleCatalog.makes(query)
  } finally {
    vehicleCatalogLoading.value = false
  }
}

async function onVehicleMakeChange() {
  vehicleForm.model = ''
  vehicleForm.version = ''
  vehicleModels.value = []
  vehicleVersions.value = []
  if (vehicleForm.make) {
    await searchVehicleModels('')
  }
}

async function onVehicleModelChange() {
  vehicleForm.version = ''
  vehicleVersions.value = []
  if (vehicleForm.make && vehicleForm.model) {
    await searchVehicleVersions('')
  }
}

async function searchVehicleModels(query = '') {
  if (!vehicleForm.make) return
  vehicleCatalogLoading.value = true
  try {
    vehicleModels.value = await workshopFacade.vehicleCatalog.models(vehicleForm.make, query)
  } finally {
    vehicleCatalogLoading.value = false
  }
}

async function searchVehicleVersions(query = '') {
  if (!vehicleForm.make || !vehicleForm.model) return
  vehicleCatalogLoading.value = true
  try {
    vehicleVersions.value = await workshopFacade.vehicleCatalog.versions(vehicleForm.make, vehicleForm.model, query)
  } finally {
    vehicleCatalogLoading.value = false
  }
}

function syncSelectedCustomerVehicleCount() {
  if (!selectedCustomer.value) return
  selectedCustomer.value.vehicleCount = vehicles.value.length
  const row = customers.value.find((customer) => customer.id === selectedCustomer.value.id)
  if (row) row.vehicleCount = vehicles.value.length
}

async function lookupPostalCode(options = {}) {
  if (!/^[0-9]{5}$/.test(form.postalCode || '')) return
  postalLoading.value = true
  try {
    const response = await workshopFacade.postalCatalog.lookup(form.postalCode)
    lastPostalLookup.value = form.postalCode
    form.state = response.state
    form.municipality = response.municipality
    settlements.value = response.settlements || []
    if (settlements.value.length === 1) form.neighborhood = settlements.value[0].name
    if (settlements.value.length > 1 && !settlements.value.some((settlement) => settlement.name === form.neighborhood)) {
      form.neighborhood = ''
    }
  } catch (error) {
    lastPostalLookup.value = ''
    settlements.value = []
    form.state = ''
    form.municipality = ''
    form.neighborhood = ''
    if (!options.silent) {
      await Swal.fire('Codigo postal no encontrado', error.message, 'warning')
    }
  } finally {
    postalLoading.value = false
  }
}

async function loadMunicipalities() {
  form.municipality = ''
  form.neighborhood = ''
  settlements.value = []
  municipalities.value = form.state ? await workshopFacade.postalCatalog.municipalities(form.state) : []
}

async function loadSettlements() {
  form.neighborhood = ''
  settlements.value = form.state && form.municipality
    ? await workshopFacade.postalCatalog.settlements(form.state, form.municipality)
    : []
}

async function lookupPostalSelection() {
  if (!form.state || !form.municipality || !form.neighborhood) return
  const response = await workshopFacade.postalCatalog.lookupSelection(form.state, form.municipality, form.neighborhood)
  form.postalCode = response.postalCode
}

function validateForm() {
  const requiredNames = [
    [form.firstName, 'Nombre'],
    [form.lastName, 'Apellido paterno'],
    [form.secondLastName, 'Apellido materno'],
    [form.alternateContactName, 'Contacto alternativo'],
  ]
  requiredNames.forEach(([value, label]) => {
    if (!nameRegex.test(String(value || '').trim())) throw new Error(`${label} solo debe contener letras, acentos, ñ y espacios.`)
  })
  if (!curpRegex.test(form.curp)) throw new Error('La CURP no tiene un formato valido.')
  if (!rfcRegex.test(form.rfc)) throw new Error('El RFC no tiene un formato valido.')
  if (!/^[0-9]{10}$/.test(form.contactPhone)) throw new Error('El telefono de contacto debe contener 10 digitos.')
  if (!/^[0-9]{10}$/.test(form.workPhone)) throw new Error('El telefono del trabajo debe contener 10 digitos.')
  if (!/^[0-9]{5}$/.test(form.postalCode)) throw new Error('El codigo postal debe contener 5 digitos.')
  if (!isEmail(form.email)) throw new Error('El email no tiene un formato valido.')
  if (form.workEmail && !isEmail(form.workEmail)) throw new Error('El email del trabajo no tiene un formato valido.')
  if (!form.birthDate || new Date(form.birthDate) > new Date()) throw new Error('La fecha de nacimiento no puede ser futura.')
  if (!editingId.value && !form.currentWorkshopId) throw new Error('Selecciona el taller actual.')
  if (!editingId.value && !photoFile.value) throw new Error('La foto del cliente es obligatoria.')
  if (editingId.value && !form.workshopIds.length) throw new Error('Selecciona al menos un taller asociado.')
}

function validateVehicleForm() {
  const normalizedVin = vehicleForm.vin.trim().toUpperCase()
  const normalizedPlate = vehicleForm.plate.trim().toUpperCase()
  if (!vinRegex.test(normalizedVin)) throw new Error('El VIN debe contener 17 caracteres validos, sin I, O ni Q.')
  if (!plateRegex.test(normalizedPlate)) throw new Error('Las placas deben tener de 5 a 10 caracteres en mayusculas, numeros o guion.')
  if (!vehicleForm.make?.trim()) throw new Error('Selecciona o captura la marca.')
  if (!vehicleForm.model?.trim()) throw new Error('Selecciona o captura el modelo.')
  const currentYear = new Date().getFullYear() + 1
  if (!vehicleForm.year || vehicleForm.year < 1900 || vehicleForm.year > currentYear) throw new Error('El año del vehiculo no es valido.')
  if (!vehicleForm.version?.trim()) throw new Error('Captura la version del vehiculo.')
  if (!vehicleForm.color?.trim()) throw new Error('Captura el color del vehiculo.')
  if (vehicleForm.mileage !== null && vehicleForm.mileage !== '' && vehicleForm.mileage < 0) throw new Error('El kilometraje no puede ser negativo.')
  if (!vehicleForm.serialNumber?.trim()) throw new Error('Captura el numero de serie.')
}

function handlePhoto(event) {
  const file = event.target.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    Swal.fire('Archivo invalido', 'Solo se permiten imagenes o fotografias.', 'error')
    event.target.value = ''
    return
  }
  if (file.size > maxPhotoBytes) {
    Swal.fire('Archivo demasiado grande', 'La foto no debe superar 15 MB.', 'error')
    event.target.value = ''
    return
  }
  photoFile.value = file
  photoPreview.value = URL.createObjectURL(file)
}

function normalizePayload() {
  const payload = {
    firstName: form.firstName.trim(),
    lastName: form.lastName.trim(),
    secondLastName: form.secondLastName.trim(),
    alternateContactName: form.alternateContactName.trim(),
    birthDate: form.birthDate,
    curp: form.curp.trim().toUpperCase(),
    rfc: form.rfc.trim().toUpperCase(),
    contactPhone: digits(form.contactPhone),
    workPhone: digits(form.workPhone),
    email: form.email.trim().toLowerCase(),
    workEmail: form.workEmail ? form.workEmail.trim().toLowerCase() : null,
    street: form.street.trim(),
    neighborhood: form.neighborhood.trim(),
    municipality: form.municipality.trim(),
    state: form.state.trim(),
    postalCode: digits(form.postalCode),
  }
  return editingId.value ? { ...payload, workshopIds: form.workshopIds } : { ...payload, currentWorkshopId: form.currentWorkshopId }
}

function normalizeVehiclePayload() {
  return {
    vin: vehicleForm.vin.trim().toUpperCase(),
    plate: vehicleForm.plate.trim().replace(/\s+/g, '').toUpperCase(),
    make: vehicleForm.make.trim(),
    model: vehicleForm.model.trim(),
    year: Number(vehicleForm.year),
    version: vehicleForm.version.trim(),
    color: vehicleForm.color.trim(),
    mileage: vehicleForm.mileage === '' || vehicleForm.mileage === null ? null : Number(vehicleForm.mileage),
    serialNumber: vehicleForm.serialNumber.trim().toUpperCase(),
  }
}

function emptyForm() {
  return {
    firstName: '',
    lastName: '',
    secondLastName: '',
    alternateContactName: '',
    birthDate: '',
    curp: '',
    rfc: '',
    contactPhone: '',
    workPhone: '',
    email: '',
    workEmail: '',
    street: '',
    neighborhood: '',
    municipality: '',
    state: '',
    postalCode: '',
    currentWorkshopId: null,
    workshopIds: [],
  }
}

function emptyVehicleForm() {
  return {
    vin: '',
    plate: '',
    make: '',
    model: '',
    year: new Date().getFullYear(),
    version: '',
    color: '',
    mileage: '',
    serialNumber: '',
  }
}

function emptyStatusForm() {
  return {
    strValor: '',
    strDescripcion: '',
  }
}

function digits(value) {
  return String(value || '').replace(/\D/g, '')
}

function isEmail(value) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(value || '').trim())
}

function formatDate(value) {
  return value ? new Date(value).toLocaleString('es-MX') : 'Sin dato'
}
</script>
