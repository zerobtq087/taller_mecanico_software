# Diagrama de componentes - Fase 02

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

Se intento ejecutar el comando solicitado:

```bash
npx skills add tt-a1i/archify -g
```

El equipo no tiene `npx` ni `npm` disponibles en el PATH, por lo que no fue posible instalar la skill `archify` desde esta maquina. El diagrama queda guardado como artefacto versionable en Markdown con Mermaid.
