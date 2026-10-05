# Taller mecanico

Aplicacion fullstack para gestionar la operacion inicial de un taller mecanico con autenticacion, autorizacion por roles, alta protegida de usuarios y registro de clientes.

## Estado actual

| Area | Estado | Descripcion |
| --- | --- | --- |
| Autenticacion | Terminado | Login JWT, recuperacion, reset y cambio de contrasena. |
| Autorizacion | Terminado | Roles `GERENTE`, `SECRETARIO`, `MECANICO` y `AUXILIAR` con rutas protegidas. |
| Alta de usuarios | Terminado | Solo `GERENTE` o `SECRETARIO` pueden crear usuarios; solo `GERENTE` puede asignar rol `GERENTE`. |
| Registro de clientes | Terminado | Formulario Vue, facade frontend, REST, facade backend, service, repository y MySQL. |
| Fotos de clientes | Terminado | Imagenes hasta 20 MB, vista previa y almacenamiento como `photoDataUrl`. |
| Catalogo postal local | Terminado | Consulta de codigos postales SEPOMEX desde MySQL, sin depender de internet en ejecucion. |
| Base de datos | Terminado para fase 03 | MySQL en Docker con `users`, `user_roles`, `customers` y `postal_settlements`. |
| Multi-taller | Preparado | `workshopId` queda reservado para futura asociacion cliente-taller. |

## Stack

- Backend: Spring Boot `3.5.16`, Java `21`, Spring Security, Spring Data JPA, Bean Validation, JJWT `0.12.6`.
- Frontend: Vue `3.5.43`, Vite `8.3.0`, Vuetify `3.13.4`, SweetAlert2, Bootstrap `5.3.8`.
- Base de datos: MySQL Docker `mysql:latest`.
- Paquetes frontend: Yarn Classic `1.22.22`.

## Arquitectura

Flujo principal:

```text
Vue App -> workshopFacade.js -> services/api.js -> REST Controllers
REST Controllers -> AuthFacade/CustomerFacade -> Services -> Repositories -> MySQL
Catalogo postal -> PostalCatalogFacade -> PostalCatalogService -> PostalSettlementRepository
```

Documentacion relacionada:

- [Planeacion fase inicial](./planeacion_fase_inicial.md)
- [Fase 02 - Registro de clientes](./fase02_registro_clientes.md)
- [Fase 03 - Catalogo postal local](./fase03_catalogo_postal_local.md)
- [Diagrama de componentes fase 02](./docs/diagrama_componentes_fase02.md)
- Diagrama HTML Archify actualizado: `.archify/architecture-taller-fase02-postal-editable-20261004-001500/taller-fase02-postal-editable.html`

## Credenciales y variables

Las credenciales locales van en `.env`, que no debe subirse a Git. La plantilla versionada es [.env.example](./.env.example).

| Variable | Uso |
| --- | --- |
| `MYSQL_ROOT_PASSWORD` | Password root del contenedor MySQL. |
| `MYSQL_DATABASE` | Base creada por Docker, por defecto `taller_db`. |
| `MYSQL_USER` / `MYSQL_PASSWORD` | Usuario de aplicacion creado por MySQL. |
| `DB_HOST`, `DB_PORT`, `DB_NAME` | Conexion JDBC del backend. |
| `DB_USERNAME`, `DB_PASSWORD` | Credenciales usadas por Spring Boot. |
| `JWT_SECRET` | Secreto para firmar JWT. |
| `ADMIN_INITIAL_EMAIL`, `ADMIN_INITIAL_PASSWORD` | Usuario administrador inicial. |

Usuario local inicial de desarrollo:

```text
Correo: admin@taller.local
Contrasena: Admin12345!
```

Cambia esa contrasena antes de usar el sistema fuera de desarrollo.

## Base de datos con Docker

Opcion recomendada con Docker Compose:

```bash
cp .env.example .env
docker volume create mysql-data
docker compose --env-file .env up -d
```

El contenedor carga automaticamente:

- `database/schema.sql`: estructura de tablas.
- `database/sepomex_data.sql`: catalogo postal local con 145,420 asentamientos, aproximadamente 15 MB.

Opcion equivalente con `docker run`:

```bash
docker volume create mysql-data
docker run -d --name mysql-server \
  -e MYSQL_ROOT_PASSWORD=TU_PASSWORD_ROOT_SEGURO \
  -e MYSQL_DATABASE=taller_db \
  -e MYSQL_USER=taller_app \
  -e MYSQL_PASSWORD=TU_PASSWORD_APP_SEGURO \
  -v mysql-data:/var/lib/mysql \
  -p 3306:3306 \
  --restart unless-stopped \
  mysql:latest
```

Si el volumen ya existia con una estructura vieja, recrea el volumen solo cuando aceptes perder datos locales de prueba.

## Backend

```bash
cd backend
mvn spring-boot:run
```

La API queda en:

```text
http://localhost:8080
```

Endpoints principales:

| Metodo | Ruta | Acceso |
| --- | --- | --- |
| `POST` | `/api/auth/login` | Publico |
| `POST` | `/api/auth/forgot-password` | Publico |
| `POST` | `/api/auth/reset-password` | Publico con token |
| `POST` | `/api/auth/change-password` | Usuario autenticado |
| `POST` | `/api/secretaria/users` | `GERENTE`, `SECRETARIO` |
| `GET` | `/api/admin/users` | `GERENTE` |
| `PATCH` | `/api/admin/users/{id}/roles` | `GERENTE` |
| `GET` | `/api/secretaria/clientes` | `GERENTE`, `SECRETARIO` |
| `POST` | `/api/secretaria/clientes` | `GERENTE`, `SECRETARIO` |
| `GET` | `/api/catalogos/codigos-postales/{postalCode}` | Usuario autenticado |

## Frontend

```bash
cd frontend
yarn install
yarn dev --host 0.0.0.0
```

La interfaz queda en:

```text
http://localhost:5173
```

Para despliegue remoto define:

```bash
VITE_API_URL=https://tu-api.example/api
```

## Validaciones de cliente

- Telefono personal: 10 digitos.
- Telefono del trabajo: 10 digitos.
- Email: formato valido y unico.
- Foto: archivo de imagen, maximo 20 MB.
- Codigo postal: 5 digitos numericos; colonia, municipio y estado se resuelven desde MySQL local y quedan editables para correccion manual.
- Direccion: calle, colonia, municipio, estado y codigo postal obligatorios.
- Duplicados: backend valida email y telefono personal; MySQL mantiene llaves unicas.

Los errores visibles estan redactados para usuario final, por ejemplo:

```text
El telefono personal debe contener 10 digitos.
```

## Verificacion local

Comandos usados para validar esta fase:

```bash
cd backend
mvn -Dmaven.repo.local=/tmp/taller-m2 test

cd ../frontend
yarn build
```

Tambien se verifico:

- Login admin contra backend.
- Registro REST de cliente contra MySQL.
- Registro desde UI con SweetAlert2.
- Consulta local de codigo postal `01030` contra `postal_settlements`.
- Mensajes de validacion sin expresiones regulares internas.
- Diagrama Archify HTML con `validate`, `deliver` y `check`.

## Despliegue sugerido

Frontend:

- Vercel, Netlify o Cloudflare Pages.
- Directorio: `frontend`.
- Build: `yarn build`.
- Salida: `dist`.
- Variable: `VITE_API_URL`.

Backend:

- Render, Railway, Fly.io, AWS App Runner o VM con Docker.
- Requiere Java 21, MySQL gestionado o MySQL en red privada.
- No expongas MySQL `3306` a internet.

Pendientes antes de produccion:

- Migraciones con Flyway o Liquibase.
- Envio real de correo para recuperacion de contrasena.
- Cookies `HttpOnly` para JWT o estrategia equivalente.
- Pruebas automatizadas de seguridad y duplicados.
- Almacenamiento externo de fotos si el volumen crece.
- Modelo formal `Workshop` para varios talleres.
