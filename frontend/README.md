# GEEX · frontend

Angular 22 + Angular Material. Todo el texto de la interfaz está en español.

```bash
npm ci
npm start          # http://localhost:4200 (reenvía /api al backend en :8080)
npm test           # Vitest
npm run lint       # ESLint (angular-eslint)
npm run build      # compilación de producción
```

El cliente de la API (`src/app/api/generated`, no se versiona) se genera antes de cada `start`,
`build` y `test` a partir de `openapi/geex-api.json`, que a su vez genera el backend
(`ContratoOpenApiTest`). Para regenerarlo a mano: `npm run api`.

Estructura:

| Carpeta | Contenido |
|---|---|
| `src/app/core` | Sesión (token en memoria, interceptor, guards), módulos de la pantalla de inicio, errores, servicios de UI |
| `src/app/layout` | Barra superior con el apartado actual y menú de usuario |
| `src/app/features` | Pantallas: acceso, inicio, configuración, mi cuenta |
| `src/app/shared` | Componentes reutilizables (avisos, campo de contraseña) |
