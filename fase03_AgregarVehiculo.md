# Fase 03 - Catalogo de estatus, administracion de clientes y vehiculos

## 1. Resumen ejecutivo

Esta fase consolida el modulo de administracion de clientes y agrega el flujo completo para registrar, consultar, editar y cancelar vehiculos asociados a clientes activos. El flujo mantiene el patron Facade ya usado en el login y en clientes:

`Vue 3 + Vuetify -> workshopFacade.js -> api.js -> REST Spring Boot -> Service -> Repository -> MySQL`.

Tambien se agrega un catalogo local de marcas, modelos y versiones de vehiculos. La aplicacion no depende de internet en tiempo de ejecucion: los datos se cargan en MySQL desde archivos CSV locales.

Fuente del catalogo vehicular: [gor3a/vehicle-makes-models](https://github.com/gor3a/vehicle-makes-models). Se usan `makes-models.csv` para marcas/modelos y `engines.csv` para sugerir versiones combinando generacion, carroceria y motor.

## 2. Modulos desarrollados

| Modulo | Estado | Descripcion |
| --- | --- | --- |
| Catalogo de estatus | Terminado | CRUD REST para `status_catalog` con alta, edicion, listado y cancelacion logica. Solo `GERENTE`. |
| Cliente con estatus por FK | Terminado | `customers.status_id` apunta a `status_catalog.id`; suspender actualiza estatus sin eliminar el registro. |
| Administracion de clientes | Terminado | Tabla con paginacion de servidor, filtro por taller, ordenamiento, acciones, edicion modal e historial. |
| Vista UI de clientes | Terminado | Tabla compacta, sin recorte horizontal en escritorio, con columnas visibles: cliente, estatus, taller, vehiculos, email, telefono, trabajo y acciones. |
| Contador de vehiculos | Terminado | `CustomerService` devuelve `vehicleCount`; el frontend sincroniza el contador al abrir/agregar/cancelar vehiculos. |
| Vehiculos por cliente | Terminado | Alta, edicion, listado y cancelacion logica de vehiculos asociados a clientes activos. |
| Catalogo vehicular local | Terminado | Tablas `vehicle_makes`, `vehicle_models` y `vehicle_versions` cargadas desde `database/vehicle_catalog/vehicle_catalog_seed.sql`. |
| Autocompletado vehicular | Terminado | Endpoints para marcas, modelos y versiones; el modal de vehiculo consume los datos mediante Facade. |
| Validaciones de vehiculo | Terminado | VIN, placas, año, marca, modelo, version, color, kilometraje y numero de serie se validan en frontend y backend. |
| Eliminacion logica | Terminado | Clientes se suspenden; vehiculos y estatus se cancelan logicamente. No se borran fisicamente. |

## 3. Flujo de datos

| Paso | Capa | Archivo principal | Resultado |
| --- | --- | --- | --- |
| 1 | Vista Vue | `frontend/src/components/CustomersModule.vue` | Muestra clientes, contador de vehiculos, modales de cliente, estatus y vehiculos. |
| 2 | Facade | `frontend/src/facades/workshopFacade.js` | Centraliza operaciones de clientes, estatus, vehiculos y catalogos. |
| 3 | Cliente REST | `frontend/src/services/api.js` | Agrega JWT y llama endpoints REST de Spring Boot. |
| 4 | Controller | `VehicleController`, `VehicleCatalogController`, `StatusCatalogController` | Expone endpoints REST. |
| 5 | Service | `VehicleService`, `VehicleCatalogService`, `StatusCatalogService`, `CustomerService` | Aplica reglas de negocio, validaciones y normalizacion. |
| 6 | Repository | Repositorios JPA | Consulta y persiste entidades. |
| 7 | Base de datos | `database/schema.sql` | Guarda clientes, talleres, estatus, vehiculos y catalogos locales. |

## 4. Tablas agregadas o actualizadas

| Tabla | Campos principales | Uso |
| --- | --- | --- |
| `status_catalog` | `id`, `str_valor`, `str_descripcion` | Catalogo general: `ACTIVO`, `SUSPENDIDO`, `CANCELADO`, `ACTUALIZADO`. |
| `customers` | `status_id` | Relaciona clientes con `status_catalog`; incluye foto, CURP, RFC y datos de contacto. |
| `customer_workshop` | `customer_id`, `workshop_id`, `first_visit_at`, `last_visit_at`, `registered_by_user_id` | Relaciona cliente con uno o varios talleres. |
| `vehicle_makes` | `id`, `make_group`, `name` | Catalogo local de marcas. |
| `vehicle_models` | `id`, `make_id`, `name`, `year_start`, `year_end` | Catalogo local de modelos por marca. |
| `vehicle_versions` | `id`, `model_id`, `name` | Versiones sugeridas por modelo desde generacion, carroceria y motor. |
| `vehicles` | `customer_id`, `vin`, `plate`, `make`, `model`, `model_year`, `version`, `color`, `mileage`, `serial_number`, `status_id` | Vehiculos asociados a clientes. |

## 5. Reglas de negocio

| Regla | Implementacion |
| --- | --- |
| Registrar cliente | Permitido para `GERENTE` y `SECRETARIO`. |
| Actualizar cliente | Permitido para `GERENTE` y `SECRETARIO`. |
| Suspender cliente | Solo `GERENTE`; valida que el cliente no este suspendido. |
| Registrar vehiculo | Permitido para `GERENTE` y `SECRETARIO`; el cliente debe estar `ACTIVO`. |
| Cliente suspendido | No puede recibir vehiculos nuevos. |
| Cliente con varios vehiculos | Relacion uno a muchos mediante `vehicles.customer_id`. |
| Vehiculo nuevo | Se guarda con estatus `ACTIVO`. |
| Cancelar vehiculo | No se elimina; cambia a estatus `CANCELADO`. |
| Duplicados de vehiculo | No permite duplicar VIN ni placas. |
| VIN | Debe cumplir `^[A-HJ-NPR-Z0-9]{17}$`. |
| Placas | Se normalizan a mayusculas y cumplen `^[A-Z0-9-]{5,10}$`. |
| Version | Se captura con autocompletado desde `vehicle_versions`, pero puede ajustarse manualmente. |

## 6. Endpoints REST

| Metodo | Ruta | Rol requerido | Descripcion |
| --- | --- | --- | --- |
| `GET` | `/api/admin/estatus` | `GERENTE` | Lista catalogo de estatus. |
| `POST` | `/api/admin/estatus` | `GERENTE` | Crea un estatus. |
| `PUT` | `/api/admin/estatus/{id}` | `GERENTE` | Modifica un estatus. |
| `DELETE` | `/api/admin/estatus/{id}` | `GERENTE` | Cancela logicamente un estatus. |
| `GET` | `/api/secretaria/clientes` | `GERENTE`, `SECRETARIO` | Lista clientes con paginacion backend, filtro y ordenamiento. |
| `PUT` | `/api/secretaria/clientes/{id}` | `GERENTE`, `SECRETARIO` | Actualiza datos principales del cliente. |
| `PATCH` | `/api/secretaria/clientes/{id}/suspender` | `GERENTE` | Suspende cliente si esta activo. |
| `GET` | `/api/secretaria/clientes/{customerId}/vehiculos` | `GERENTE`, `SECRETARIO` | Lista vehiculos de un cliente. |
| `POST` | `/api/secretaria/clientes/{customerId}/vehiculos` | `GERENTE`, `SECRETARIO` | Registra vehiculo a cliente activo. |
| `PUT` | `/api/secretaria/clientes/{customerId}/vehiculos/{vehicleId}` | `GERENTE`, `SECRETARIO` | Actualiza datos del vehiculo. |
| `DELETE` | `/api/secretaria/clientes/{customerId}/vehiculos/{vehicleId}` | `GERENTE`, `SECRETARIO` | Cancela logicamente el vehiculo. |
| `GET` | `/api/catalogos/vehiculos/marcas?q=` | Usuario autenticado | Autocompleta marcas. |
| `GET` | `/api/catalogos/vehiculos/modelos?make=&q=` | Usuario autenticado | Autocompleta modelos por marca. |
| `GET` | `/api/catalogos/vehiculos/versiones?make=&model=&q=` | Usuario autenticado | Autocompleta versiones por marca y modelo. |

## 7. Archivos creados o modificados

| Archivo | Descripcion |
| --- | --- |
| `database/schema.sql` | Agrega y migra `status_catalog`, `vehicles`, `vehicle_makes`, `vehicle_models`, `vehicle_versions` y `customers.status_id`. |
| `database/vehicle_catalog/makes-models.csv` | CSV fuente para marcas y modelos. |
| `database/vehicle_catalog/engines.csv` | CSV fuente para sugerencias de version. |
| `database/vehicle_catalog/vehicle_catalog_seed.sql` | SQL generado para poblar marcas, modelos y versiones. |
| `StatusCatalog.java` | Entidad JPA del catalogo de estatus. |
| `Vehicle.java` | Entidad JPA de vehiculos asociados a cliente. |
| `VehicleMake.java` | Entidad JPA de marcas. |
| `VehicleModel.java` | Entidad JPA de modelos. |
| `VehicleVersion.java` | Entidad JPA de versiones por modelo. |
| `StatusCatalogController.java` | CRUD REST del catalogo de estatus. |
| `VehicleController.java` | REST para vehiculos por cliente. |
| `VehicleCatalogController.java` | REST para autocompletado de marcas, modelos y versiones. |
| `StatusCatalogService.java` | Reglas del catalogo y consulta de estatus requeridos. |
| `VehicleService.java` | Alta, edicion, normalizacion, validacion y cancelacion logica de vehiculos. |
| `VehicleCatalogService.java` | Busqueda eficiente de marcas, modelos y versiones. |
| `CustomerService.java` | Integra `vehicleCount` en la respuesta de clientes y mantiene historial por taller. |
| `CustomersModule.vue` | Tabla de clientes, contador de vehiculos, modales de vehiculos/estatus y autocompletados. |
| `frontend/src/services/api.js` | Metodos REST para estatus, vehiculos y catalogo vehicular. |
| `frontend/src/facades/workshopFacade.js` | Facade methods para clientes, vehiculos, estatus y catalogos. |
| `frontend/src/style.css` | Ajustes UI/UX de tabla de clientes, barra lateral y layout responsivo. |

## 8. Carga del catalogo vehicular local

Despues de aplicar `database/schema.sql`, carga el catalogo local:

```bash
docker exec -i mysql-server mysql -uroot -pParadox87 taller_db < database/vehicle_catalog/vehicle_catalog_seed.sql
```

Validacion de carga recomendada:

```bash
docker exec mysql-server mysql -uroot -pParadox87 -e "USE taller_db; SELECT COUNT(*) FROM vehicle_makes; SELECT COUNT(*) FROM vehicle_models; SELECT COUNT(*) FROM vehicle_versions;"
```

## 9. Verificacion ejecutada

| Verificacion | Resultado |
| --- | --- |
| `mvn test` en backend | Correcto; build Spring Boot exitoso. |
| `yarn build` en frontend | Correcto; build Vite exitoso. |
| Consulta API de clientes | Correcto; `vehicleCount` se devuelve por cliente. |
| UI de clientes | Correcto; tabla sin recorte horizontal en escritorio y contador visible. |
| MySQL local | Correcto; tablas de catalogo vehicular cargadas en Docker. |

## 10. Pendientes recomendados

| Pendiente | Motivo |
| --- | --- |
| Migraciones Flyway/Liquibase | Formalizar cambios incrementales de esquema para ambientes productivos. |
| Auditoria de cambios | Registrar usuario, fecha y valor anterior/nuevo al editar cliente, vehiculo o estatus. |
| Permisos mas granulares | Separar administracion de catalogos de permisos generales de gerencia. |
| Pruebas automatizadas | Cubrir validaciones de VIN, placas, duplicados, cliente suspendido y cancelacion logica. |
| Historial vehicular | Agregar servicios, ordenes de trabajo y bitacora de reparaciones por vehiculo. |
