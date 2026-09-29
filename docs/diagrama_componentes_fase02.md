# Diagrama de componentes - Fase 02

## Version interactiva Archify

El diagrama HTML interactivo generado con Archify queda en:

`/.archify/architecture-taller-fase02-20260928-182517/taller-fase02.html`

Archify valido el artefacto con `validate`, `deliver` y `check`. El `browser-check` quedo omitido porque el equipo no tiene Chrome/Chromium disponible para ese gate automatizado.

```mermaid
flowchart LR
    Usuario[Gerente o recepcionista autenticado]
    Vue[Vue 3 + Vite + Vuetify]
    Api[services/api.js]
    AuthController[AuthController REST]
    UserRegistrationController[UserRegistrationController REST]
    CustomerController[CustomerController REST]
    AuthFacade[AuthFacade]
    CustomerFacade[CustomerFacade]
    AuthService[AuthService]
    CustomerService[CustomerService]
    UserRepository[UserRepository]
    CustomerRepository[CustomerRepository]
    MySQL[(MySQL Docker)]
    Security[Spring Security + JWT + Roles]

    Usuario --> Vue
    Vue --> Api
    Api --> AuthController
    Api --> UserRegistrationController
    Api --> CustomerController

    AuthController --> AuthFacade
    UserRegistrationController --> AuthFacade
    CustomerController --> CustomerFacade

    AuthFacade --> AuthService
    CustomerFacade --> CustomerService

    AuthService --> UserRepository
    CustomerService --> CustomerRepository
    CustomerService --> UserRepository

    UserRepository --> MySQL
    CustomerRepository --> MySQL

    Security -. protege .-> UserRegistrationController
    Security -. protege .-> CustomerController
    Security -. valida JWT .-> AuthController
```

## Nota sobre archify

La skill `archify` quedo disponible localmente y se ejecuto directamente con:

```bash
node /home/david/.agents/skills/archify/bin/archify.mjs finalize architecture .archify/architecture-taller-fase02-20260928-182517/candidate.json .archify/architecture-taller-fase02-20260928-182517/taller-fase02.html --repo-root /home/david/Documentos/ChatGPT/taller --quality showcase --json
```

El comando solicitado originalmente con `npx` no se pudo ejecutar porque el equipo no tiene `npx` ni `npm` en el PATH, pero el HTML de Archify si fue generado desde la instalacion local de la skill.
