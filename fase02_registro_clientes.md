# Fase 02 - Registro de clientes y alta protegida de usuarios

## Resumen ejecutivo

En esta fase se implemento el modulo de registro de clientes con flujo Vue -> Frontend Facade -> REST -> Backend Facade -> Service -> Repository -> MySQL. Tambien se analizo el login y el registro de usuarios: el login conserva el contrato visual, pasa por facade en frontend y backend, y el alta de usuarios queda protegida para usuarios con rol `GERENTE` o `SECRETARIO`.

## Reglas funcionales

| Regla | Implementacion | Estado |
| --- | --- | --- |
| El usuario que da de alta clientes debe estar registrado y autorizado. | Endpoints bajo `/api/secretaria/**`, protegidos por Spring Security para `GERENTE` y `SECRETARIO`. | Terminado |
| No crear doble registro de cliente. | Validacion por email y telefono personal antes de guardar, mas llaves unicas en MySQL. | Terminado |
| Alertar datos duplicados. | Backend devuelve error; frontend muestra SweetAlert2 con mensaje de registro detenido. | Terminado |
| Validar formatos. | Bean Validation en DTO: email, telefonos de 10 digitos, edad obligatoria, fecha pasada, codigo postal y tamanos maximos. | Terminado |
| Mensajes legibles. | Los errores de telefono muestran “debe contener 10 digitos”, no expresiones regulares internas ni nombres tecnicos como `personalPhone`. | Terminado |
| Foto del cliente. | Boton de carga, solo imagenes, maximo 20 MB, vista previa y envio como `photoDataUrl`; Jackson y MySQL soportan el payload. | Terminado |
| Codigo postal local. | Al capturar 5 digitos, el formulario consulta `/api/catalogos/codigos-postales/{cp}` y llena colonia, municipio y estado desde MySQL local. Los campos quedan editables para correccion manual. | Terminado |
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
| Frontend facade | `frontend/src/facades/workshopFacade.js`, `frontend/src/services/api.js` | Login, recuperacion, usuarios, clientes, errores de conexion | Terminado | La vista Vue consume un facade local y no queda acoplada directamente al cliente REST. |
| Frontend clientes | `frontend/src/App.vue`, `frontend/src/services/api.js`, `frontend/src/style.css` | Formulario de cliente, foto, vista previa, validaciones, alertas | Terminado | El panel muestra clientes y usuarios solo a roles autorizados. Usa SweetAlert2 para exito/error. |
| Catalogo postal local | `PostalCatalogController.java`, `PostalCatalogFacade.java`, `PostalCatalogService.java`, `PostalSettlementRepository.java`, `database/sepomex_data.sql` | Codigo postal, colonia, tipo de asentamiento, municipio, estado y ciudad | Terminado | Permite resolver direccion sin depender de internet durante el registro de clientes. |
| Diagrama de componentes | `docs/diagrama_componentes_fase02.md`, `.archify/architecture-taller-fase02-postal-editable-20261004-001500/taller-fase02-postal-editable.html` | Arquitectura Vue, frontend facade, REST, backend facade, service, repository, MySQL, catalogo postal y direccion editable | Actualizado | Se guardo diagrama Mermaid y HTML interactivo generado con Archify. |

## Metodos y funciones creadas o modificadas

| Archivo | Metodo o funcion | Proposito |
| --- | --- | --- |
| `AuthFacade.java` | `login` | Recibe credenciales desde REST y delega el inicio de sesion a `AuthService`. |
| `AuthFacade.java` | `createUser` | Orquesta el alta protegida de usuarios desde gerencia o recepcion. |
| `AuthFacade.java` | `forgotPassword` | Inicia recuperacion de contrasena sin exponer servicio directo al controlador. |
| `AuthFacade.java` | `resetPassword` | Completa cambio de contrasena por token. |
| `AuthFacade.java` | `changePassword` | Cambia contrasena de usuario autenticado. |
| `AuthFacade.java` | `listUsers` | Lista usuarios administrativos sin exponer `UserRepository` al controlador. |
| `AuthFacade.java` | `updateRoles` | Actualiza roles desde facade y mantiene el patron controller -> facade -> service -> repository. |
| `AuthService.java` | `listUsers` | Consulta usuarios y los convierte a `UserResponse`. |
| `AuthService.java` | `updateRoles` | Actualiza roles con valor por defecto `AUXILIAR` cuando no se recibe una lista valida. |
| `UserController.java` | `listUsers` | Delega en `AuthFacade` la consulta de usuarios. |
| `UserController.java` | `updateRoles` | Delega en `AuthFacade` el cambio de roles. |
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
| `PostalCatalogController.java` | `lookupByPostalCode` | Expone la consulta REST autenticada de datos SEPOMEX por codigo postal. |
| `PostalCatalogFacade.java` | `lookupByPostalCode` | Mantiene el patron facade entre controlador y servicio del catalogo postal. |
| `PostalCatalogService.java` | `lookupByPostalCode` | Valida que el codigo postal tenga 5 digitos y construye la respuesta con estado, municipio, ciudad y colonias. |
| `PostalSettlementRepository.java` | `findByPostalCodeOrderBySettlementNameAsc` | Consulta asentamientos locales desde MySQL por codigo postal. |
| `AuthService.java` | `createUser` | Crea usuarios con BCrypt y roles autorizados. |
| `AuthService.java` | `resolveAllowedRoles` | Impide que recepcion cree usuarios con rol `GERENTE`. |
| `InitialAdminSeeder.java` | `seedInitialAdmin` | Crea el primer administrador real al arrancar la API si el correo configurado no existe. |
| `RestExceptionHandler.java` | `integrity` | Devuelve error controlado cuando MySQL detecta datos duplicados o restricciones de datos. |
| `RestExceptionHandler.java` | `methodNotSupported` | Devuelve `405` con mensaje claro cuando se usa un metodo HTTP incorrecto. |
| `JacksonConfig.java` | `jacksonReadConstraints` | Amplia el limite de lectura JSON para permitir imagenes de hasta 20 MB en `photoDataUrl`. |
| `workshopFacade.js` | `auth.login` | Expone login a la vista sin acoplarla al cliente REST. |
| `workshopFacade.js` | `users.create` | Orquesta alta de usuarios desde el frontend. |
| `workshopFacade.js` | `customers.register` | Orquesta registro de clientes desde el frontend. |
| `frontend/src/services/api.js` | `createUser` | Consume el endpoint protegido de alta de usuarios. |
| `frontend/src/services/api.js` | `listCustomers` | Consulta clientes autorizados sin incluir fotos en listados pesados. |
| `frontend/src/services/api.js` | `createCustomer` | Consume el endpoint de registro de clientes. |
| `frontend/src/services/api.js` | `lookupPostalCode` | Consulta el catalogo postal local mediante el backend autenticado. |
| `frontend/src/services/api.js` | `request` | Muestra mensaje claro cuando Spring Boot no esta activo y ocurre `failed to fetch`. |
| `App.vue` | `emptyCustomer` | Genera el estado inicial del formulario de cliente. |
| `App.vue` | `watch(customerForm.postalCode)` | Detecta automaticamente cuando el codigo postal tiene 5 digitos y dispara la busqueda local. |
| `App.vue` | `run` | Centraliza estado de carga y manejo de errores visuales. |
| `App.vue` | `hasRole` | Evalua permisos de UI segun roles del usuario autenticado. |
| `App.vue` | `createUser` | Envia el alta de usuario y confirma con SweetAlert2. |
| `App.vue` | `createCustomer` | Valida formato local, registra cliente y confirma con SweetAlert2. |
| `App.vue` | `validateCustomerForm` | Verifica campos requeridos, edad, correo y telefonos de 10 digitos. |
| `App.vue` | `isTenDigitPhone` | Valida telefonos con un mensaje comprensible para el usuario. |
| `App.vue` | `normalizeCustomerPayload` | Limpia email y campos opcionales antes de enviar al backend. |
| `App.vue` | `handlePhotoUpload` | Valida imagen, peso maximo de 20 MB, genera vista previa y data URL. |
| `App.vue` | `lookupPostalCode` | Busca el codigo postal en MySQL local, llena colonia, municipio y estado, y conserva esos campos editables. |

## Orden operativo del formulario de direccion

| Paso | Campo | Comportamiento |
| --- | --- | --- |
| 1 | `email` | Captura y valida email principal del cliente. |
| 2 | `workEmail` | Captura email laboral opcional. |
| 3 | `postalCode` | Al tener 5 digitos consulta MySQL local por medio del backend autenticado. |
| 4 | `neighborhood` | Se llena con la primera colonia encontrada y permite seleccionar o escribir una correccion. |
| 5 | `municipality` | Se autocompleta y queda editable. |
| 6 | `state` | Se autocompleta y queda editable. |
| 7 | `street` | Se captura manualmente porque no viene en el catalogo postal. |

## Datos capturados del cliente

| Categoria | Campos |
| --- | --- |
| Identidad | `fullName`, `alternateContactName`, `age`, `birthDate` |
| Contacto | `personalPhone`, `workPhone`, `email`, `workEmail` |
| Foto | `photoDataUrl` |
| Direccion | `street`, `neighborhood`, `municipality`, `state`, `postalCode` |
| Catalogo postal | `postal_code`, `settlement_name`, `settlement_type`, `municipality_name`, `state_name`, `city_name` |
| Escalabilidad | `workshopId` opcional para asociacion futura a varios talleres |
| Auditoria | `createdByUserId`, `createdAt` |

## Endpoints REST

| Metodo | Ruta | Roles | Resultado |
| --- | --- | --- | --- |
| `POST` | `/api/auth/login` | Publico | JWT y usuario autenticado |
| `POST` | `/api/secretaria/users` | `GERENTE`, `SECRETARIO` | Usuario registrado |
| `GET` | `/api/secretaria/clientes` | `GERENTE`, `SECRETARIO` | Lista de clientes |
| `POST` | `/api/secretaria/clientes` | `GERENTE`, `SECRETARIO` | Cliente registrado |
| `GET` | `/api/catalogos/codigos-postales/{postalCode}` | Usuario autenticado | Estado, municipio, ciudad y colonias del CP |

## Validaciones principales

| Campo | Regla | Mensaje esperado |
| --- | --- | --- |
| `personalPhone` | 10 digitos numericos. | `El telefono personal debe contener 10 digitos.` |
| `workPhone` | 10 digitos numericos. | `El telefono del trabajo debe contener 10 digitos.` |
| `email` | Formato email valido y unico. | `El email del cliente no tiene un formato valido.` o duplicado controlado por backend. |
| `photoDataUrl` | Imagen permitida, maximo 20 MB en la vista y payload soportado por backend. | `La foto no debe superar 20 MB.` |
| `postalCode` | 5 digitos numericos, busqueda local en MySQL y campos de direccion editables. | `El codigo postal debe contener 5 digitos.` |

## Pendientes recomendados

| Pendiente | Motivo |
| --- | --- |
| Migraciones con Flyway o Liquibase | Versionar cambios de base de datos para produccion. |
| Actualizacion periodica SEPOMEX | Definir proceso controlado para refrescar `database/sepomex_data.sql` cuando Correos de Mexico publique cambios. |
| Almacenamiento externo de fotos | Evitar guardar imagenes grandes como base64 en MySQL cuando haya volumen alto. |
| Pruebas backend | Agregar pruebas de seguridad, duplicados y validacion de DTO. |
| Pruebas frontend | Validar carga de imagen, permisos de tabs y alertas SweetAlert2. |
| Modelo formal de talleres | Crear entidad `Workshop` y relacion cliente-taller cuando inicie la fase multi-sucursal. |

## Verificacion tecnica

| Verificacion | Resultado |
| --- | --- |
| SweetAlert2 | Instalado con Yarn: `sweetalert2@11.26.25`. |
| Archify | HTML generado con la skill local mediante `node /home/david/.agents/skills/archify/bin/archify.mjs`. |
| Diagrama | Guardado en `docs/diagrama_componentes_fase02.md` y `.archify/architecture-taller-fase02-postal-editable-20261004-001500/taller-fase02-postal-editable.html`. |
| Archify gates | `validate`, `deliver` y `check` pasaron. `browser-check` quedo omitido porque no hay Chrome/Chromium disponible. |
| Base de datos | Login, registro REST y registro desde UI fueron verificados contra MySQL Docker. Los clientes de prueba fueron eliminados. |
| Catalogo postal local | `database/sepomex_data.sql` contiene 145,420 asentamientos, pesa aproximadamente 15 MB y se consulta sin internet. |
