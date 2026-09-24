# Planeacion de la fase inicial

## Resumen

Esta primera fase establece la base tecnica de una aplicacion para un taller de reparacion de autos: API REST Spring Boot, interfaz Vue/Vite/Vuetify, MySQL en Docker y seguridad basada en JWT y roles.

Autenticacion, autorizacion, usuarios y la interfaz de acceso estan terminados en el codigo. Ordenes, citas y reportes son endpoints demostrativos con datos fijos; no tienen persistencia ni operaciones de negocio completas, por lo que permanecen iniciados.

## Estado por fase

| Fase | Estado | Entregables | Datos |
| --- | --- | --- | --- |
| Base tecnica | Terminada | Directorios `backend`, `frontend`, `database`, Docker Compose y dependencias. | Conexion, pool Hikari y limites Tomcat. |
| Identidad y acceso | Terminada con pendientes de produccion | Registro, login, JWT, BCrypt, recuperacion, cambio de contrasena y roles. | Usuario, correo, hash, roles, token JWT y token de recuperacion. |
| Administracion de usuarios | Terminada | Consulta de usuarios y actualizacion de roles por gerente. | Id, nombre, correo y roles. |
| Operacion del taller | Iniciada | Rutas de ordenes, citas y reportes con respuestas de ejemplo. | Folio, vehiculo, estado, cliente, hora, servicio e indicadores; no persistidos. |
| Interfaz web | Terminada para acceso y demostracion | Formularios de acceso, panel visual por rol y diseno responsive. | Formularios, usuario y token en `localStorage`. |
| Calidad y despliegue | Parcial | Frontend compilado correctamente y guia de ejecucion. | Sin pruebas automatizadas de API ni prueba integrada con MySQL. |

## Modulos y codigo

### Autenticacion y autorizacion - terminado

Responsabilidad: identificar usuarios y delimitar acciones por rol.

| Archivo | Funcion |
| --- | --- |
| `backend/src/main/java/com/taller/security/service/AuthService.java` | Registro como `AUXILIAR`, login, JWT, recuperacion, reset y cambio de contrasena. |
| `backend/src/main/java/com/taller/security/config/SecurityConfig.java` | Seguridad stateless, CORS, BCrypt con factor 12, reglas por ruta y filtro JWT. |
| `backend/src/main/java/com/taller/security/security/JwtService.java` | Crea, firma y valida JWT con roles. |
| `backend/src/main/java/com/taller/security/security/JwtAuthenticationFilter.java` | Extrae `Bearer` y establece la autenticacion de Spring Security. |
| `backend/src/main/java/com/taller/security/security/TallerUserDetailsService.java` | Convierte el usuario almacenado a `UserDetails`. |
| `backend/src/main/java/com/taller/security/controller/AuthController.java` | Expone las operaciones REST de acceso. |
| `backend/src/main/java/com/taller/security/dto/AuthDtos.java` | Contratos de entrada/salida y validaciones. |

Roles declarados en `backend/src/main/java/com/taller/security/model/Role.java`:

| Rol | Acceso implementado |
| --- | --- |
| `GERENTE` | Administracion, secretaria y taller; administra roles. |
| `SECRETARIO` | Rutas de secretaria. |
| `MECANICO` | Rutas de taller. |
| `AUXILIAR` | Rutas de taller; rol inicial del registro publico. |

| Ruta | Autorizacion | Resultado |
| --- | --- | --- |
| `POST /api/auth/register` | Publica | Crea auxiliar y devuelve JWT. |
| `POST /api/auth/login` | Publica | Valida credenciales y devuelve JWT. |
| `POST /api/auth/forgot-password` | Publica | Genera token con vigencia de 30 minutos. |
| `POST /api/auth/reset-password` | Publica con token | Actualiza el hash y anula el token. |
| `POST /api/auth/change-password` | Autenticada | Comprueba la contrasena actual y actualiza la nueva. |
| `GET /api/me` | Autenticada | Devuelve el correo del usuario. |
| `GET /api/admin/users` | `GERENTE` | Lista usuarios. |
| `PATCH /api/admin/users/{id}/roles` | `GERENTE` | Sustituye los roles del usuario. |

Pendientes necesarios para produccion: el token de recuperacion se devuelve en la respuesta solo para demostracion y debe enviarse por correo; el JWT esta en `localStorage` y debe migrarse a cookie `HttpOnly`, `Secure` y `SameSite`; faltan limite de intentos, auditoria, verificacion de correo, rotacion de secretos y pruebas de seguridad.

### Usuarios - terminado

| Archivo | Funcion |
| --- | --- |
| `backend/src/main/java/com/taller/security/model/User.java` | Entidad JPA `users`: id, nombre, correo unico, hash, habilitacion, roles, token de recuperacion, expiracion y creacion. |
| `backend/src/main/java/com/taller/security/repository/UserRepository.java` | Consultas por correo, existencia y token de recuperacion. |
| `backend/src/main/java/com/taller/security/controller/UserController.java` | API administrativa de usuarios y roles. |
| `backend/src/main/java/com/taller/security/dto/UserDtos.java` | Solicitud validada para actualizar roles. |
| `database/schema.sql` | Tablas `users` y `user_roles`, indice de token y correo unico. |

### Operacion del taller - iniciada

`backend/src/main/java/com/taller/security/controller/WorkshopController.java` entrega `GET /api/taller/ordenes`, `GET /api/secretaria/citas` y `GET /api/admin/reportes`. Devuelve listas y mapas creados en codigo; no consulta MySQL. Faltan entidades, tablas, repositorios, servicios, CRUD, reglas de negocio, trazabilidad e interfaz para declararlo terminado.

### Frontend - terminado para acceso y demostracion

| Archivo | Funcion |
| --- | --- |
| `frontend/src/App.vue` | Login, registro, recuperacion, reset, cambio de contrasena, cierre y panel visual por roles; incluye modo demo sin API. |
| `frontend/src/services/api.js` | Cliente `fetch`, JWT desde `localStorage` y `VITE_API_URL`. |
| `frontend/src/plugins/vuetify.js` | Inicializacion de Vuetify, componentes, directivas y tema oscuro morado. |
| `frontend/src/style.css` | Estilos negro/morado y rejillas adaptables a movil, tableta y escritorio. |
| `frontend/src/main.js` | Arranque de Vue, Vuetify y Bootstrap. |
| `frontend/vite.config.js` | Plugin Vue, acceso por red y puerto 5173. |
| `frontend/package.json` | Scripts y dependencias. |

## Infraestructura y capacidad

`docker-compose.yml` inicia MySQL en `mysql-server`, publica el puerto 3306, usa el volumen externo `mysql-data` e importa `database/schema.sql` cuando la base es nueva. La imagen es `mysql:latest`; en produccion debe fijarse un tag exacto.

`backend/src/main/resources/application.yml` configura Tomcat con 400 hilos maximos, 40 de reserva, cola de 1000 y 10000 conexiones maximas. HikariCP usa hasta 80 conexiones y 10 inactivas. Son valores iniciales, no una garantia de usuarios masivos: deben validarse con pruebas de carga segun CPU, memoria, consultas e infraestructura.

## Credenciales y secretos

Las credenciales locales estan en `.env`, que se crea desde `.env.example` y no se versiona. Contiene `MYSQL_ROOT_PASSWORD`, `MYSQL_PASSWORD`, `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET`.

El backend las lee en `backend/src/main/resources/application.yml` mediante `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET`. Docker Compose lee las variables `MYSQL_*` desde `.env`. `.gitignore` protege ese archivo de GitHub.

Si el repositorio ya se publico con una contrasena real en un commit anterior, se debe rotar en MySQL y guardar la nueva solamente en el gestor de secretos o el archivo local.

## Ejecucion local

### Software requerido

- Frontend: Node.js 22 LTS o posterior compatible, Yarn Classic 1.22.22, navegador moderno y Visual Studio Code.
- Backend: JDK 21, Apache Maven 3.9 o posterior, Docker Engine o Docker Desktop con Compose v2.
- Base de datos: MySQL en Docker; no es necesario instalarlo en el sistema anfitrion.

### Base de datos

1. Copia `.env.example` a `.env` y sustituye todos los valores de ejemplo.
2. Ejecuta `docker volume create mysql-data`.
3. En la raiz ejecuta `docker compose --env-file .env up -d`.
4. Comprueba con `docker compose ps`.

### Backend

1. Carga las variables de `.env` en la terminal que ejecutara Spring Boot.
2. Entra a `backend` y ejecuta `mvn spring-boot:run`.
3. La API queda en `http://localhost:8080`; las rutas protegidas requieren `Authorization: Bearer <token>`.

### Frontend

1. Entra a `frontend` y ejecuta `yarn install`.
2. Ejecuta `yarn dev --host 0.0.0.0`.
3. Abre `http://localhost:5173`.
4. Para API remota, define `VITE_API_URL=https://tu-api.example/api` antes de `yarn build`.

La compilacion `yarn build` fue exitosa en esta fase. Maven no estaba instalado en el entorno de construccion, por lo que falta validar la API y MySQL de forma integrada.

## Despliegue recomendado

Para frontend: Vercel, Netlify o Cloudflare Pages. Configuracion: directorio raiz `frontend`, build `yarn build`, salida `dist` y variable `VITE_API_URL` con la API HTTPS.

Para backend: Render, Railway, Fly.io, AWS App Runner o una VM con Docker. Configura Java 21 y las variables de `.env`; usa MySQL gestionado o una red privada. No expongas MySQL por el puerto 3306 a internet. Antes de produccion faltan Dockerfile de API, migraciones Flyway, health checks, observabilidad, respaldos, CI/CD y pruebas de carga.

## Dependencias y siguiente fase

| Componente | Version declarada | Uso |
| --- | --- | --- |
| Spring Boot | 3.5.16 | REST, seguridad, JPA y validacion. |
| Java | 21 | Runtime backend. |
| JJWT | 0.12.6 | JWT. |
| Vue | 3.5.43 | Interfaz reactiva. |
| Vite | 8.3.0 | Desarrollo y build. |
| Vuetify | 3.13.4 | Componentes UI. |
| Bootstrap | 5.3.8 | Utilidades CSS disponibles. |
| Yarn | 1.22.22 | Gestor de paquetes. |

Usar Vuetify como sistema principal y Bootstrap solo para utilidades evita duplicar estilos. En la siguiente fase se recomienda agregar Pinia, Vue Router, VueUse, Vitest/Playwright, entidades de clientes, vehiculos, ordenes, citas, inventario, pagos y migraciones Flyway.
