# Fase 02 - Registro de clientes y alta protegida de usuarios

## Resumen ejecutivo

En esta fase se implemento el modulo de registro de clientes con flujo Vue -> REST -> Facade -> Service -> Repository -> MySQL. Tambien se analizo el login y el registro de usuarios: el login conserva el contrato visual, pero ahora pasa por `AuthFacade`; el registro publico fue retirado y el alta de usuarios queda protegida para usuarios con rol `GERENTE` o `SECRETARIO`.

## Reglas funcionales

| Regla | Implementacion | Estado |
| --- | --- | --- |
| El usuario que da de alta clientes debe estar registrado y autorizado. | Endpoints bajo `/api/secretaria/**`, protegidos por Spring Security para `GERENTE` y `SECRETARIO`. | Terminado |
| No crear doble registro de cliente. | Validacion por email y telefono personal antes de guardar, mas llaves unicas en MySQL. | Terminado |
| Alertar datos duplicados. | Backend devuelve error; frontend muestra SweetAlert2 con mensaje de registro detenido. | Terminado |
| Validar formatos. | Bean Validation en DTO: email, telefono, edad, fecha pasada, codigo postal y tamanos maximos. | Terminado |
| Foto del cliente. | Boton de carga, solo imagenes, maximo 20 MB, vista previa y envio como `photoDataUrl`. | Terminado |
| Asociacion futura a varios talleres. | Campo opcional `workshopId` en cliente e indice SQL para futura relacion formal. | Preparado para fase futura |
| Resultado esperado. | Cliente registrado y respuesta `CustomerResponse`. | Terminado |

## Modulos desarrollados

| Modulo | Archivos principales | Datos que maneja | Estado | Descripcion |
| --- | --- | --- | --- | --- |
| Autenticacion con facade | `AuthFacade.java`, `AuthController.java`, `AuthService.java` | Login, recuperacion, cambio de contrasena, JWT, usuario autenticado | Terminado | Se agrego `AuthFacade` para estandarizar el patron facade entre controlador y servicio sin cambiar el flujo de login. |
| Alta protegida de usuarios | `UserRegistrationController.java`, `UserDtos.java`, `AuthService.java` | Nombre, email, password temporal, roles | Terminado | El registro de usuarios ya no es publico. Solo `GERENTE` o `SECRETARIO` pueden crear usuarios; solo `GERENTE` puede asignar rol `GERENTE`. |
| Administrador inicial | `InitialAdminSeeder.java`, `InitialAdminProperties.java`, `application.yml`, `.env.example` | Correo, nombre y contrasena inicial por variables de entorno | Terminado | El backend crea el primer usuario `GERENTE` si no existe, para permitir login real sin usar modo demo. |
| Clientes backend | `CustomerController.java`, `CustomerFacade.java`, `CustomerService.java`, `CustomerRepository.java`, `Customer.java`, `CustomerDtos.java` | Datos personales, contacto alternativo, telefonos, emails, foto, direccion, `workshopId` | Terminado | Registro y consulta de clientes con facade, validaciones, normalizacion y proteccion contra duplicados. |
| Base de datos | `database/schema.sql` | Tabla `customers`, llaves unicas, relacion con `users` | Terminado | Se agrego tabla de clientes con indices para email, telefono personal, usuario creador y futuro taller. |
| Frontend clientes | `frontend/src/App.vue`, `frontend/src/services/api.js`, `frontend/src/style.css` | Formulario de cliente, foto, vista previa, validacion de duplicados, alertas | Terminado | El panel muestra clientes y usuarios solo a roles autorizados. Usa SweetAlert2 para exito/error. |
| Diagrama de componentes | `docs/diagrama_componentes_fase02.md`, `.archify/architecture-taller-fase02-20260928-182517/taller-fase02.html` | Arquitectura Vue, REST, facade, service, repository, MySQL | Terminado | Se guardo diagrama Mermaid y HTML interactivo generado con Archify. |

## Metodos y funciones creadas o modificadas

| Archivo | Metodo o funcion | Proposito |
| --- | --- | --- |
| `AuthFacade.java` | `login` | Recibe credenciales desde REST y delega el inicio de sesion a `AuthService`. |
| `AuthFacade.java` | `createUser` | Orquesta el alta protegida de usuarios desde gerencia o recepcion. |
| `AuthFacade.java` | `forgotPassword` | Inicia recuperacion de contrasena sin exponer servicio directo al controlador. |
| `AuthFacade.java` | `resetPassword` | Completa cambio de contrasena por token. |
| `AuthFacade.java` | `changePassword` | Cambia contrasena de usuario autenticado. |
| `UserRegistrationController.java` | `createUser` | Expone `POST /api/secretaria/users` para alta de usuarios autorizados. |
| `CustomerController.java` | `registerCustomer` | Expone `POST /api/secretaria/clientes` y registra cliente con el usuario autenticado como creador. |
| `CustomerController.java` | `listCustomers` | Expone `GET /api/secretaria/clientes` para consulta y validacion previa desde la vista. |
| `CustomerFacade.java` | `registerCustomer` | Aplica el patron facade entre controlador y servicio de clientes. |
| `CustomerFacade.java` | `listCustomers` | Entrega clientes normalizados para Vue y futuras integraciones. |
| `CustomerService.java` | `createCustomer` | Valida duplicados, normaliza datos y persiste el cliente. |
| `CustomerService.java` | `listCustomers` | Consulta clientes desde repository. |
| `CustomerService.java` | `toResponse` | Convierte entidad JPA a DTO seguro para la vista. |
| `CustomerService.java` | `normalizeEmail` | Normaliza emails a minusculas. |
| `CustomerService.java` | `normalizeOptionalEmail` | Normaliza email laboral opcional. |
| `CustomerService.java` | `normalizeOptional` | Limpia campos opcionales vacios. |
| `CustomerService.java` | `normalizePhone` | Estandariza espacios en telefonos. |
| `CustomerService.java` | `validatePhotoDataUrl` | Verifica que la foto enviada sea una imagen en formato data URL. |
| `AuthService.java` | `createUser` | Crea usuarios con BCrypt y roles autorizados. |
| `AuthService.java` | `resolveAllowedRoles` | Impide que recepcion cree usuarios con rol `GERENTE`. |
| `InitialAdminSeeder.java` | `seedInitialAdmin` | Crea el primer administrador real al arrancar la API si el correo configurado no existe. |
| `RestExceptionHandler.java` | `duplicate` | Devuelve error controlado cuando MySQL detecta datos duplicados. |
| `frontend/src/services/api.js` | `createUser` | Consume el endpoint protegido de alta de usuarios. |
| `frontend/src/services/api.js` | `listCustomers` | Consulta clientes para detectar duplicados antes del alta. |
| `frontend/src/services/api.js` | `createCustomer` | Consume el endpoint de registro de clientes. |
| `App.vue` | `emptyCustomer` | Genera el estado inicial del formulario de cliente. |
| `App.vue` | `run` | Centraliza estado de carga y manejo de errores visuales. |
| `App.vue` | `hasRole` | Evalua permisos de UI segun roles del usuario autenticado. |
| `App.vue` | `createUser` | Envia el alta de usuario y confirma con SweetAlert2. |
| `App.vue` | `createCustomer` | Valida duplicados, registra cliente y confirma con SweetAlert2. |
| `App.vue` | `validateDuplicateCustomer` | Evita crear doble registro desde la vista cuando ya existe email o telefono. |
| `App.vue` | `normalizeCustomerPayload` | Limpia email y campos opcionales antes de enviar al backend. |
| `App.vue` | `handlePhotoUpload` | Valida imagen, peso maximo de 20 MB, genera vista previa y data URL. |

## Datos capturados del cliente

| Categoria | Campos |
| --- | --- |
| Identidad | `fullName`, `alternateContactName`, `age`, `birthDate` |
| Contacto | `personalPhone`, `workPhone`, `email`, `workEmail` |
| Foto | `photoDataUrl` |
| Direccion | `street`, `neighborhood`, `municipality`, `state`, `postalCode` |
| Escalabilidad | `workshopId` opcional para asociacion futura a varios talleres |
| Auditoria | `createdByUserId`, `createdAt` |

## Endpoints REST

| Metodo | Ruta | Roles | Resultado |
| --- | --- | --- | --- |
| `POST` | `/api/auth/login` | Publico | JWT y usuario autenticado |
| `POST` | `/api/secretaria/users` | `GERENTE`, `SECRETARIO` | Usuario registrado |
| `GET` | `/api/secretaria/clientes` | `GERENTE`, `SECRETARIO` | Lista de clientes |
| `POST` | `/api/secretaria/clientes` | `GERENTE`, `SECRETARIO` | Cliente registrado |

## Pendientes recomendados

| Pendiente | Motivo |
| --- | --- |
| Migraciones con Flyway o Liquibase | Versionar cambios de base de datos para produccion. |
| Almacenamiento externo de fotos | Evitar guardar imagenes grandes como base64 en MySQL cuando haya volumen alto. |
| Pruebas backend | Agregar pruebas de seguridad, duplicados y validacion de DTO. |
| Pruebas frontend | Validar carga de imagen, permisos de tabs y alertas SweetAlert2. |
| Modelo formal de talleres | Crear entidad `Workshop` y relacion cliente-taller cuando inicie la fase multi-sucursal. |

## Verificacion tecnica

| Verificacion | Resultado |
| --- | --- |
| SweetAlert2 | Instalado con Yarn: `sweetalert2@11.26.25`. |
| Archify | HTML generado con la skill local. `npx` no existe en PATH, por eso se uso `node /home/david/.agents/skills/archify/bin/archify.mjs`. |
| Diagrama | Guardado en `docs/diagrama_componentes_fase02.md` y `.archify/architecture-taller-fase02-20260928-182517/taller-fase02.html`. |
