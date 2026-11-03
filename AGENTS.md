# Repository Guidelines

## Estructura del Proyecto y Organización de Módulos

Este repositorio contiene un sistema full stack para taller mecánico. `frontend/` alberga la aplicación Vue 3/Vite/Vuetify: `src/components/` contiene vistas, `src/facades/workshopFacade.js` separa interfaz y negocio, y `src/services/api.js` concentra solicitudes HTTP/JWT. `backend/` es la API Spring Boot con Java 21 bajo `com.taller.security`, organizada en `controller`, `facade`, `service`, `repository`, `model`, `dto` y `security`. El esquema SQL y catálogos locales están en `database/`; la documentación vive en la raíz y en `docs/`.

## Comandos de Compilación, Pruebas y Desarrollo

Inicia MySQL con `docker compose --env-file .env up -d` después de copiar `.env.example` como `.env`. Desde `backend/`, ejecuta `mvn spring-boot:run`; la API queda en `http://localhost:8080`. Desde `frontend/`, ejecuta `yarn install` y después `yarn dev`; Vite queda en `http://localhost:5173`.

Usa `mvn test` para compilar y ejecutar pruebas del backend, `yarn build` para generar producción y `yarn preview` para revisarla localmente.

## Estilo de Código y Convenciones de Nombres

Usa 4 espacios de indentación para Java y 2 para Vue, JavaScript, YAML y SQL. Conserva tipos Java en PascalCase (`CustomerService`), métodos y campos en camelCase, y paquetes en minúsculas. Coloca los contratos de solicitud/respuesta en `dto/*Dtos.java`. Conserva el flujo por capas: controller -> facade -> service -> repository; los controladores no deben llamar repositorios directamente.

Los componentes Vue usan archivos PascalCase, como `CustomersModule.vue`; los módulos de facade y servicios usan camelCase. Centraliza llamadas REST en `frontend/src/services/api.js` y expónlas mediante `workshopFacade.js`. No hay formatter ni linter configurados: respeta el estilo existente y ejecuta los builds antes de enviar cambios.

## Lineamientos de Pruebas

Spring Boot Test y Spring Security Test están disponibles mediante Maven. Agrega pruebas en `backend/src/test/java`, con nombres `*Test.java`; cubre autorización, validaciones, duplicados y cambios de estatus al modificar APIs. El frontend no tiene ejecutor de pruebas automatizadas: como mínimo ejecuta `yarn build` y verifica manualmente los flujos modificados contra la API local.

## Commits y Pull Requests

Sigue la convención breve existente: `feat: agregar endpoint de vehiculos`, `fix: reparar registro de clientes` o `docs: actualizar guia de fase`. Mantén cada commit enfocado. Los pull requests deben explicar el cambio funcional, listar comandos de validación, enlazar el issue relacionado cuando exista e incluir capturas para cambios visibles de Vue/Vuetify.

## Seguridad y Configuración

Nunca subas `.env`, secretos JWT, contraseñas de base de datos ni archivos generados. Parte de `.env.example`, usa credenciales solo locales durante desarrollo y rótalas antes de desplegar.
