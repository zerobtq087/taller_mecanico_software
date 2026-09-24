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
          surface: '#11101a',
          primary: '#8b5cf6',
          secondary: '#22d3ee',
          accent: '#c084fc',
          error: '#fb7185',
          warning: '#f59e0b',
          success: '#34d399',
        },
      },
    },
  },
})
