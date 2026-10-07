# Fase 02 - Registro y administracion de clientes

## 1. Resumen ejecutivo

En esta fase se consolido el modulo de clientes para operar con autenticacion JWT, roles, patron facade y persistencia en MySQL. El flujo principal queda separado por capas: Vue 3 + Vuetify -> `workshopFacade.js` -> cliente REST -> controladores Spring Boot -> facade backend -> servicios -> repositories -> MySQL.

El registro de clientes ya no depende de una sola sucursal. La informacion maestra del cliente se guarda una vez en `customers` y su relacion con talleres se guarda en `customer_workshop`, lo que permite asociar un mismo cliente a varias sucursales sin duplicar datos.

## 2. Alcance funcional terminado

| Modulo | Estado | Descripcion |
| --- | --- | --- |
| Login y autorizacion | Terminado | Login JWT con roles `GERENTE`, `SECRETARIO`, `AUXILIAR` y otros perfiles operativos. Las rutas administrativas se protegen con Spring Security. |
| Registro de clientes | Terminado | Alta de clientes desde usuario autenticado, con datos personales, contacto, direccion, foto y taller actual obligatorio. |
| Administracion de clientes | Terminado | Tabla con paginacion de servidor, filtro por taller, ordenamiento, edicion, historial de talleres y suspension logica. |
| Relacion cliente-taller | Terminado | Tabla `customer_workshop` para relacionar clientes con varias sucursales y registrar primera visita, ultima visita y usuario que registro. |
| Talleres | Terminado | Entidad `Workshop`, alta/edicion por `GERENTE`, validacion de RFC unico, foto opcional y datos semilla para Tula, Tepeji y Queretaro. |
| Catalogo SEPOMEX local | Terminado | Tabla `postal_settlements` en MySQL y endpoints para CP, estados, municipios, colonias y busqueda inversa. No depende de internet en ejecucion. |
| Fotos | Terminado | Carga multipart con limite de 15 MB, solo imagenes, guardadas en disco o volumen mediante `UPLOAD_DIR`; no se guardan en base64. |
| Frontend modular | Terminado | Clientes, talleres y usuarios estan separados en componentes propios fuera de `App.vue`. |
| Patron facade | Terminado | La vista Vue consume `workshopFacade.js`; el backend usa facades para clientes, talleres, catalogo postal y autenticacion. |

## 3. Reglas de negocio implementadas

| Regla | Implementacion | Estado |
| --- | --- | --- |
| Solo usuarios autorizados pueden registrar clientes. | Endpoint bajo `/api/secretaria/clientes`; requiere usuario autenticado con rol permitido por configuracion de seguridad. | Terminado |
| El cliente no debe duplicarse. | Busqueda por CURP, RFC o email antes de crear. Si existe, se reutiliza y solo se registra la visita al taller. | Terminado |
| CURP, RFC y email son identidad del cliente. | Indices unicos en `customers` para `curp`, `rfc` y `email`. | Terminado |
| El cliente puede asistir a varios talleres. | `customer_workshop` mantiene el historial por sucursal. | Terminado |
| Taller actual obligatorio al crear cliente. | `currentWorkshopId` obligatorio en `CustomerRequest`. | Terminado |
| Edad calculada, no capturada. | Se calcula desde `birthDate` en `CustomerService.toResponse`. | Terminado |
| Suspension global del cliente. | `PATCH /api/secretaria/clientes/{id}/suspender` cambia `status` a `SUSPENDIDO`; solo `GERENTE`. | Terminado |
| Datos normalizados. | Textos a minusculas; CURP y RFC a mayusculas; telefonos solo digitos; emails a minusculas. | Terminado |
| Foto obligatoria al crear cliente. | Frontend y backend validan que exista foto al crear; al editar es opcional. | Terminado |
| CP local y editable. | Al capturar 5 digitos se autocompletan estado, municipio y colonias; los campos se pueden cambiar manualmente. | Terminado |

## 4. Datos capturados del cliente

| Categoria | Campos |
| --- | --- |
| Identidad | `firstName`, `lastName`, `secondLastName`, `curp`, `rfc`, `birthDate`, edad calculada |
| Contacto | `alternateContactName`, `contactPhone`, `workPhone`, `email`, `workEmail` |
| Direccion | `postalCode`, `state`, `municipality`, `neighborhood`, `street` |
| Foto | `photoPath`, archivo multipart guardado en almacenamiento local/volumen |
| Operacion | `status`, `createdByUserId`, `createdAt`, `existingCustomer` |
| Talleres | `currentWorkshopId` en alta; `workshopIds` en edicion; historial en `customer_workshop` |

## 5. Base de datos

| Tabla | Proposito | Datos principales | Estado |
| --- | --- | --- | --- |
| `users` | Usuarios del sistema | Nombre, email, hash BCrypt, estatus, token de recuperacion | Terminado |
| `user_roles` | Roles por usuario | `GERENTE`, `SECRETARIO`, `AUXILIAR`, etc. | Terminado |
| `workshops` | Sucursales o talleres | Nombre, razon social, RFC, telefono, email, direccion, foto | Terminado |
| `customers` | Datos maestros del cliente | Nombre separado, CURP, RFC, telefonos, emails, direccion, foto, estatus | Terminado |
| `customer_workshop` | Historial cliente-taller | Cliente, taller, primera visita, ultima visita, usuario que registro | Terminado |
| `postal_settlements` | Catalogo SEPOMEX local | CP, colonia, tipo, municipio, estado, ciudad | Terminado |

El archivo principal del esquema es `database/schema.sql`. Incluye creacion de tablas, semillas de talleres y un procedimiento de migracion para instalaciones que aun tengan columnas antiguas como `full_name`, `personal_phone`, `photo_data_url` o `workshop_id`.

## 6. Validaciones

| Campo | Regla | Capa |
| --- | --- | --- |
| Nombre y apellidos | Solo letras, acentos, `ñ` y espacios | Frontend y backend |
| CURP | Regex oficial en mayusculas | Frontend y backend |
| RFC | Regex fiscal en mayusculas | Frontend y backend |
| Telefonos | Exactamente 10 digitos | Frontend y backend |
| Codigo postal | Exactamente 5 digitos | Frontend y backend |
| Email | Formato valido y unico | Frontend, backend y MySQL |
| Fecha de nacimiento | No futura | Frontend y backend |
| Foto cliente | Obligatoria al crear, imagen valida, maximo 15 MB | Frontend y backend |
| Foto taller | Opcional, imagen valida, maximo 15 MB | Backend |

## 7. Endpoints REST

| Metodo | Ruta | Rol requerido | Descripcion |
| --- | --- | --- | --- |
| `POST` | `/api/auth/login` | Publico | Inicia sesion y devuelve JWT. |
| `POST` | `/api/secretaria/users` | `GERENTE`, `SECRETARIO` | Alta protegida de usuarios. |
| `GET` | `/api/secretaria/users` | `GERENTE`, `SECRETARIO` | Lista usuarios administrativos. |
| `PUT` | `/api/secretaria/users/{id}/roles` | `GERENTE` | Actualiza roles. |
| `GET` | `/api/talleres` | Autenticado | Lista talleres. |
| `POST` | `/api/talleres` | `GERENTE` | Crea taller con multipart opcional para foto. |
| `PUT` | `/api/talleres/{id}` | `GERENTE` | Edita taller y puede reemplazar foto. |
| `GET` | `/api/secretaria/clientes` | `GERENTE`, `SECRETARIO` | Lista clientes con paginacion, filtro y ordenamiento. |
| `POST` | `/api/secretaria/clientes` | `GERENTE`, `SECRETARIO` | Registra cliente con multipart obligatorio para foto. |
| `PUT` | `/api/secretaria/clientes/{id}` | `GERENTE`, `SECRETARIO` | Edita cliente y talleres asociados; foto opcional. |
| `PATCH` | `/api/secretaria/clientes/{id}/suspender` | `GERENTE` | Suspension logica global del cliente. |
| `GET` | `/api/catalogos/codigos-postales/{postalCode}` | Autenticado | Busca estado, municipio y colonias por CP. |
| `GET` | `/api/catalogos/codigos-postales/estados` | Autenticado | Lista estados del catalogo local. |
| `GET` | `/api/catalogos/codigos-postales/municipios?state=` | Autenticado | Lista municipios por estado. |
| `GET` | `/api/catalogos/codigos-postales/colonias?state=&municipality=` | Autenticado | Lista colonias por estado y municipio. |
| `GET` | `/api/catalogos/codigos-postales/buscar?state=&municipality=&settlement=` | Autenticado | Obtiene CP desde estado, municipio y colonia. |

## 8. Archivos principales

| Archivo | Descripcion |
| --- | --- |
| `backend/src/main/java/com/taller/security/controller/CustomerController.java` | Endpoints REST de clientes, multipart, paginacion, edicion y suspension. |
| `backend/src/main/java/com/taller/security/service/CustomerService.java` | Reglas de negocio: duplicados, normalizacion, edad, historial y suspension. |
| `backend/src/main/java/com/taller/security/dto/CustomerDtos.java` | DTOs y validaciones de cliente. |
| `backend/src/main/java/com/taller/security/repository/CustomerRepository.java` | Consultas por identidad, paginacion y filtro por taller. |
| `backend/src/main/java/com/taller/security/model/Customer.java` | Entidad JPA de cliente. |
| `backend/src/main/java/com/taller/security/model/CustomerWorkshop.java` | Relacion historica cliente-taller. |
| `backend/src/main/java/com/taller/security/controller/WorkshopController.java` | Endpoints REST de talleres. |
| `backend/src/main/java/com/taller/security/service/WorkshopService.java` | Reglas de alta/edicion de talleres y duplicado por RFC. |
| `backend/src/main/java/com/taller/security/service/FileStorageService.java` | Validacion y persistencia de fotos en disco/volumen. |
| `backend/src/main/java/com/taller/security/controller/PostalCatalogController.java` | Endpoints del catalogo SEPOMEX local. |
| `frontend/src/components/CustomersModule.vue` | Tabla, formulario, foto, historial, filtros y validaciones de clientes. |
| `frontend/src/components/WorkshopsModule.vue` | Administracion de talleres. |
| `frontend/src/components/UsersModule.vue` | Vista administrativa de usuarios. |
| `frontend/src/facades/workshopFacade.js` | Facade frontend para auth, usuarios, clientes, talleres y catalogo postal. |
| `frontend/src/services/api.js` | Cliente REST con JWT, JSON, multipart y manejo de errores. |
| `database/schema.sql` | Esquema MySQL, semillas y migracion de columnas antiguas. |
| `database/sepomex_data.sql` | Carga local del catalogo postal. |

## 9. Metodos y funciones relevantes

| Archivo | Metodo o funcion | Proposito |
| --- | --- | --- |
| `CustomerController.java` | `registerCustomer` | Recibe `request` y `photo` multipart para crear cliente. |
| `CustomerController.java` | `listCustomers` | Entrega pagina de clientes con filtro por taller y ordenamiento. |
| `CustomerController.java` | `updateCustomer` | Edita datos maestros y talleres asociados. |
| `CustomerController.java` | `suspendCustomer` | Suspende globalmente al cliente; solo `GERENTE`. |
| `CustomerService.java` | `createCustomer` | Crea o reutiliza cliente existente y registra visita al taller. |
| `CustomerService.java` | `listCustomers` | Consulta clientes para administracion. |
| `CustomerService.java` | `updateCustomer` | Actualiza datos y sincroniza talleres. |
| `CustomerService.java` | `registerVisit` | Crea o actualiza relacion en `customer_workshop`. |
| `CustomerService.java` | `syncWorkshops` | Mantiene lista de talleres asociados en edicion. |
| `CustomerService.java` | `normalize` | Normaliza textos, CURP, RFC, telefonos, emails y CP. |
| `CustomerService.java` | `calculateAge` | Calcula edad desde fecha de nacimiento. |
| `FileStorageService.java` | `saveRequiredCustomerPhoto` | Exige foto al crear cliente. |
| `FileStorageService.java` | `saveOptionalCustomerPhoto` | Guarda foto nueva cuando se edita. |
| `FileStorageService.java` | `validateImage` | Bloquea archivos que no sean imagen o superen 15 MB. |
| `WorkshopController.java` | `createWorkshop` | Crea taller; solo `GERENTE`. |
| `PostalCatalogController.java` | `lookupPostalCode` | Resuelve CP desde MySQL local. |
| `CustomersModule.vue` | `loadCustomers` | Carga tabla paginada desde backend. |
| `CustomersModule.vue` | `saveCustomer` | Valida, arma payload y registra/edita cliente. |
| `CustomersModule.vue` | `lookupPostalCode` | Autocompleta direccion por CP. |
| `CustomersModule.vue` | `lookupPostalSelection` | Obtiene CP desde estado, municipio y colonia. |
| `CustomersModule.vue` | `handlePhoto` | Valida tipo y peso de foto, y genera vista previa. |
| `workshopFacade.js` | `customers.register`, `customers.update`, `customers.suspend`, `customers.list` | Fachada frontend para operaciones de clientes. |

## 10. Pendientes recomendados

| Pendiente | Motivo |
| --- | --- |
| Flyway o Liquibase | Versionar migraciones de base de datos en ambientes productivos. |
| Pruebas automatizadas backend | Cubrir permisos, duplicados, multipart, paginacion y suspension. |
| Pruebas automatizadas frontend | Cubrir validaciones, carga de foto, CP local y acciones de tabla. |
| Control de archivos huerfanos | Eliminar fotos reemplazadas si ya no estan referenciadas. |
| Auditoria extendida | Guardar usuario y fecha de cada edicion, no solo alta/visita. |
| Optimizacion de bundle frontend | Separar modulos con carga dinamica si el bundle crece en produccion. |

## 11. Verificacion tecnica

| Verificacion | Resultado |
| --- | --- |
| Frontend | `yarn build` compila correctamente. |
| Backend | Spring Boot levanta contra MySQL Docker cuando `taller_db` esta disponible. |
| MySQL | `database/schema.sql` crea tablas, semillas y migracion de fase anterior. |
| SEPOMEX local | El catalogo se consulta desde MySQL sin depender de internet. |
| Diagrama Archify | Archivo HTML actualizado en `.archify/architecture-taller-fase02-postal-editable-20261004-001500/taller-fase02-postal-editable.html`. |
