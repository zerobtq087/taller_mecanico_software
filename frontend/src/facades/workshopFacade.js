import { api } from '../services/api'

export const workshopFacade = {
  auth: {
    login(payload) {
      return api.login(payload)
    },
    forgotPassword(payload) {
      return api.forgotPassword(payload)
    },
    resetPassword(payload) {
      return api.resetPassword(payload)
    },
    changePassword(payload) {
      return api.changePassword(payload)
    },
  },
  users: {
    create(payload) {
      return api.createUser(payload)
    },
    list() {
      return api.listUsers()
    },
    updateRoles(id, roles) {
      return api.updateUserRoles(id, roles)
    },
  },
  customers: {
    register(payload, photo) {
      return api.createCustomer(payload, photo)
    },
    update(id, payload, photo) {
      return api.updateCustomer(id, payload, photo)
    },
    suspend(id) {
      return api.suspendCustomer(id)
    },
    list(params) {
      return api.listCustomers(params)
    },
  },
  workshops: {
    list() {
      return api.listWorkshops()
    },
    create(payload, photo) {
      return api.createWorkshop(payload, photo)
    },
    update(id, payload, photo) {
      return api.updateWorkshop(id, payload, photo)
    },
  },
  postalCatalog: {
    lookup(postalCode) {
      return api.lookupPostalCode(postalCode)
    },
    states() {
      return api.listPostalStates()
    },
    municipalities(state) {
      return api.listPostalMunicipalities(state)
    },
    settlements(state, municipality) {
      return api.listPostalSettlements(state, municipality)
    },
    lookupSelection(state, municipality, settlement) {
      return api.lookupPostalSelection(state, municipality, settlement)
    },
  },
}
