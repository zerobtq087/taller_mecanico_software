# Taller Paradox Security

Proyecto fullstack para un taller de reparacion de autos con autenticacion, autorizacion y roles.

## 1. Versiones estables recomendadas

- Spring Boot `3.5.16`, estable y disponible en Maven Central.
- Java `21 LTS`.
- Vue `3.5.43`.
- Vite `8.3.0`.
- Vuetify `3.13.4`.
- Bootstrap `5.3.8`, recomendable para utilidades CSS si no quieres resolver todo con Vuetify.
- MySQL Docker `mysql:latest`, siguiendo tu comando solicitado. Para produccion conviene fijar un tag exacto.
- Yarn classic `1.22.22` o pnpm si quieres instalaciones mas rapidas.

## 2. Base de datos en Docker

Comandos solicitados:

```bash
docker volume create mysql-data
docker run -d --name mysql-server -e MYSQL_ROOT_PASSWORD=Paradox87 -v mysql-data:/var/lib/mysql -p 3306:3306 --restart unless-stopped mysql:latest
```

Alternativa con el `docker-compose.yml` del proyecto:

```bash
docker volume create mysql-data
docker compose up -d
```

Luego importa `database/schema.sql` si usas `docker run` directo.

## 3. Backend REST Spring Boot

```bash
cd backend
mvn spring-boot:run
```

Endpoints principales:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `POST /api/auth/change-password`
- `GET /api/taller/ordenes`
- `GET /api/secretaria/citas`
- `GET /api/admin/users`

## 4. Seguridad

- Passwords con `BCryptPasswordEncoder(12)`, que genera hash y salt unico.
- JWT firmado para sesiones stateless.
- Roles: `GERENTE`, `SECRETARIO`, `AUXILIAR`, `MECANICO`.
- Autorizacion por rutas REST con Spring Security.
- Pool Hikari y Tomcat ajustados para concurrencia alta.

## 5. Frontend

```bash
cd frontend
yarn install
yarn dev --host 0.0.0.0
```

El frontend intenta usar el backend en `http://localhost:8080`, y si no esta disponible entra en modo demo para revisar el diseno.
