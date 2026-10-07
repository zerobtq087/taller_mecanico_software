import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import { aliases, mdi } from 'vuetify/iconsets/mdi'

export default createVuetify({
  components,
  directives,
  icons: {
    defaultSet: 'mdi',
    aliases,
    sets: { mdi },
  },
  theme: {
    defaultTheme: 'tallerDark',
    themes: {
      tallerDark: {
        dark: true,
        colors: {
          background: '#07060b',
          surface: '#11131d',
          primary: '#7c3aed',
          secondary: '#2dd4bf',
          accent: '#22c55e',
          error: '#fb7185',
          warning: '#facc15',
          success: '#22c55e',
        },
      },
    },
  },
})
