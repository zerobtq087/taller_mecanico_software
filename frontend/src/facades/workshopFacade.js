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
  },
  customers: {
    register(payload) {
      return api.createCustomer(payload)
    },
    list() {
      return api.listCustomers()
    },
  },
  postalCatalog: {
    lookup(postalCode) {
      return api.lookupPostalCode(postalCode)
    },
  },
}
