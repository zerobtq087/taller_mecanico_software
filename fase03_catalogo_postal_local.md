# Fase 03 - Catalogo postal local SEPOMEX

## Objetivo

Integrar un catalogo postal local para que el registro de clientes no dependa de internet. El sistema usa datos SEPOMEX cargados en MySQL y expone una API REST autenticada para completar estado, municipio y colonias a partir del codigo postal.

## Elementos desarrollados

| Modulo | Archivo | Estado | Descripcion |
| --- | --- | --- | --- |
| Tabla postal | `database/schema.sql` | Terminado | Agrega `postal_settlements` con indices por codigo postal, estado y municipio. |
| Datos locales | `database/sepomex_data.sql` | Terminado | SQL versionable con 145,420 asentamientos; pesa aproximadamente 15 MB. |
| Importacion Docker | `docker-compose.yml` | Terminado | Monta `schema.sql` y `sepomex_data.sql` en `/docker-entrypoint-initdb.d` para carga inicial. |
| Convertidor | `database/tools/convert_sepomex_postgres_to_mysql.py` | Terminado | Convierte el dump PostgreSQL de `sepomex-db-postgresql` al formato MySQL local del proyecto. |
| Entidad JPA | `PostalSettlement.java` | Terminado | Representa un asentamiento postal dentro de MySQL. |
| Repository | `PostalSettlementRepository.java` | Terminado | Consulta asentamientos por codigo postal ordenados por colonia. |
| Service | `PostalCatalogService.java` | Terminado | Valida formato de 5 digitos, evita respuestas vacias y arma el DTO de salida. |
| Facade backend | `PostalCatalogFacade.java` | Terminado | Aplica el patron facade entre controlador y service. |
| Controller REST | `PostalCatalogController.java` | Terminado | Expone `GET /api/catalogos/codigos-postales/{postalCode}`. |
| Seguridad | `SecurityConfig.java` | Terminado | Requiere usuario autenticado para `/api/catalogos/**`. |
| Facade frontend | `frontend/src/facades/workshopFacade.js` | Terminado | Agrega `postalCatalog.lookup` para mantener Vue desacoplado del cliente REST. |
| Cliente REST frontend | `frontend/src/services/api.js` | Terminado | Agrega `lookupPostalCode`. |
| Vista Vue | `frontend/src/App.vue` | Terminado | Consulta CP al completar 5 digitos, muestra carga, llena colonia/municipio/estado y conserva esos campos editables. |
| Diagrama Archify | `.archify/architecture-taller-fase02-postal-editable-20261004-001500/taller-fase02-postal-editable.html` | Terminado con observacion | HTML generado; `validate`, `deliver` y `check` pasaron. `browser-check` se omitio por falta de Chrome/Chromium local. |

## Flujo de datos

| Paso | Origen | Destino | Datos |
| --- | --- | --- | --- |
| 1 | Usuario autenticado | Vista Vue | Captura codigo postal de 5 digitos. |
| 2 | `App.vue` | `workshopFacade.js` | Solicita busqueda local del CP. |
| 3 | `workshopFacade.js` | `services/api.js` | Ejecuta `lookupPostalCode`. |
| 4 | `services/api.js` | Spring Boot | Envia `GET /api/catalogos/codigos-postales/{postalCode}` con JWT. |
| 5 | `PostalCatalogController` | `PostalCatalogFacade` | Delega la solicitud REST. |
| 6 | `PostalCatalogFacade` | `PostalCatalogService` | Orquesta la busqueda. |
| 7 | `PostalCatalogService` | `PostalSettlementRepository` | Consulta `postal_settlements` por CP. |
| 8 | MySQL | Backend | Devuelve estado, municipio, ciudad y colonias. |
| 9 | Backend | Vue | Retorna `PostalCodeLookupResponse`. |
| 10 | Vue | Formulario cliente | Llena colonia, municipio y estado al completar 5 digitos; los campos quedan editables para correccion manual. |

## Endpoint REST

| Metodo | Ruta | Acceso | Respuesta |
| --- | --- | --- | --- |
| `GET` | `/api/catalogos/codigos-postales/{postalCode}` | Usuario autenticado | `postalCode`, `state`, `municipality`, `city`, `settlements[]` |

Ejemplo:

```bash
curl -H "Authorization: Bearer TU_TOKEN" \
  http://localhost:8080/api/catalogos/codigos-postales/01030
```

## Reglas y validaciones

| Regla | Implementacion |
| --- | --- |
| No depender de internet en ejecucion. | El frontend solo consulta al backend y el backend solo consulta MySQL local. |
| Codigo postal valido. | El backend acepta solo 5 digitos numericos. |
| Usuario autorizado. | La consulta requiere JWT valido. |
| Direccion consistente. | Colonia, estado y municipio se llenan desde el catalogo, pero quedan editables para corregir datos cuando el catalogo no coincida con la realidad del cliente. |
| Acentos correctos. | MySQL se configura con `utf8mb4` y el SQL debe importarse con `--default-character-set=utf8mb4`. |
| Multiples colonias. | Vue muestra opciones cuando un CP tiene mas de una colonia y permite capturar una correccion manual. |
| Catalogo mantenible. | El script de conversion permite regenerar `sepomex_data.sql` cuando se actualice la fuente. |

## Orden del formulario

Despues de `email` y `email del trabajo opcional`, el formulario solicita `codigo postal`, autocompleta `colonia`, `municipio` y `estado`, y despues pide `calle`, que se captura manualmente.

## Tamano y almacenamiento

| Archivo | Peso aproximado | Registros |
| --- | --- | --- |
| `database/sepomex_data.sql` | 15 MB | 145,420 asentamientos |

El peso es razonable para versionarlo en el repositorio y permite operar sin internet. En produccion, la carga inicial puede hacerse con Docker Compose, migraciones o una tarea de importacion controlada.

## Pendientes recomendados

| Pendiente | Motivo |
| --- | --- |
| Versionar con Flyway o Liquibase | Evitar cargas manuales y controlar cambios de esquema/datos. |
| Refresco periodico del catalogo | Definir frecuencia y responsable para actualizar SEPOMEX. |
| Prueba automatizada del endpoint | Cubrir CP existente, CP inexistente y CP con formato invalido. |
| Relacion formal con talleres | Asociar direcciones y clientes a un futuro modelo `Workshop`. |
