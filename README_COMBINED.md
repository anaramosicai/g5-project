# g5-project
Final Project of PAT by group 5

---

## Base (Felicia)

Endpoints created

POST /pistaPadel/courts
GET /pistaPadel/courts
GET /pistaPadel/courts/{courtId}
PATCH /pistaPadel/courts/{courtId}
DELETE /pistaPadel/courts/{courtId}

I created a record named Pista and added endpoints to the REST controller. In the class `ConfigSeguridad` I created two possible user authentications: USER and ADMIN which have different authorities to change details in the different courts.

---

## Contribuciones combinadas

### Ana (integrada sobre Felicia)

## 1. Cambios realizados en ANA_BRANCH

<details>
<summary><strong>📌 1.1. Descripción de mi parte</strong></summary>

Mi parte trataba de la **Autenticación + detalle de usuario (errores tipo 401/403)**
Tenía los siguientes endpoints a desarrollar:

<table border="1" cellpadding="10" cellspacing="0">
  <thead>
    <tr>
      <th>MÉTODO</th>
      <th>RUTA</th>
      <th>DESCRIPCIÓN</th>
      <th>RESPUESTAS (mínimas)</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>POST</strong></td>
      <td><code>/pistaPadel/auth/register</code></td>
      <td>Registrar usuario (rol USER por defecto)</td>
      <td>201 creado, 400 datos inválidos, 409 email ya existe</td>
    </tr>
    <tr>
      <td><strong>POST</strong></td>
      <td><code>/pistaPadel/auth/login</code></td>
      <td>Login y obtención de token (sesión)</td>
      <td>200 ok, 400 request inválida, 401 credenciales incorrectas</td>
    </tr>
    <tr>
      <td><strong>POST</strong></td>
      <td><code>/pistaPadel/auth/logout</code></td>
      <td>Logout (invalidar sesión/tokens si aplica)</td>
      <td>204 ok, 401 no autenticado</td>
    </tr>
    <tr>
      <td><strong>GET</strong></td>
      <td><code>/pistaPadel/auth/me</code></td>
      <td>Devuelve el usuario autenticado</td>
      <td>200 ok, 401 no autenticado</td>
    </tr>
    <tr>
      <td><strong>GET</strong></td>
      <td><code>/pistaPadel/users/{userId}</code></td>
      <td>(ADMIN o dueño) Obtener un usuario por id</td>
      <td>200, 401, 403, 404 no existe</td>
    </tr>
  </tbody>
</table>

<details>
<summary><strong>📊 Tabla referencia: HTTP STATUS CODES</strong></summary>

| Código | Descripción |
|--------|-------------|
| 200 | OK |
| 201 | Created |
| 204 | No Content |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 409 | Conflict |

</details>

</details>

## 1.2. Desarrollo de mi parte

Tomando como base el código que subió mi compañera Felicia, partí creando el **record** `Usuario`.

<details>
<summary><strong>🔹 Record: Usuario (Características y Restricciones)</strong></summary>
**Características:**
- `idUsuario`: Identificador único del usuario.
- `nombre`: Nombre del usuario.
- `apellidos`: Apellidos del usuario.
- `email`: Correo electrónico (único en el sistema).
- `password`: Contraseña cifrada.
- `telefono`: Teléfono de contacto.
- `rol`: Rol del usuario en el sistema. *Valores posibles: USER, ADMIN.*
- `fechaRegistro`: Fecha y hora de alta en el sistema.
- `activo`: Indica si el usuario está activo o deshabilitado.

**Restricciones:**
- El email debe ser **único**.
- Un usuario puede tener **0..n** reservas.
- Solo los usuarios con **rol ADMIN** pueden **gestionar pistas**.

</details>

... (contenido detallado de Ana: endpoints, DTOs, ejemplos, cambios en `ConfigSeguridad`, login/logout/me) ...

### Martina (integrada sobre la combinación actual)

## Cambios realizados en Martina_branch:

En primer lugar, mi parte irá principalmente enfocada al tratado de Usuario, en conjunto con la parte de Ana (autorización + usuario). Me encargaré de la realización de los cuatro endpoint siguientes:

- **GET** /pistaPadel/users
- **GET** /pistaPadel/users/{userId}
- **PATCH** /pistaPadel/users/{userId}
- **GET** /pistaPadel/health

<details>
<summary><strong>TRATADO Y CREACIÓN DE CLASES</strong></summary>

En conjunto con Ana, se crea el record Usuario y dentro de este se añaden una serie de validaciones. Copio también las clases `Rol` y `NombreRol`, las cuales necesitaré cuando trabaje con mis otras clases. Además de `ConfigSeguridad`, clase que me permitirá habilitar los roles (principalmente con el que trabajo, que es ADMIN) y controlar el acceso a los endpoint mediante `@PreAuthorize`.

**IMPORTANTE:** De cara al POST de registro de Usuario, en el body, el usuario se registrará pero no puede él determinar su ID ni su rol (el cual será siempre USER en su caso), de eso se encargará el servidor.

</details>

... (contenido adicional de Martina: dependencias, implementación de endpoints, tests) ...

### Antonio

Antonio aporta cambios y endpoints relacionados con reservas (implementación CRUD reservas).

### Yago

Yago aporta parte `feature/nuevos-endpoint` (detalles incluidos en su rama).

---

## Notas sobre combinación

- He usado la versión de Felicia como punto de partida y luego añadí los contenidos de Ana y Martina respetando sus apartados técnicos.
- Antonio y Yago se incluyen al final con sus secciones; revisarlas y mover o consolidar partes si queréis una estructura más integrada (por ejemplo, un único apartado de Endpoints agrupados por área) sería recomendable.

Si quieres, puedo:

- Crear una versión final en la rama `combine-readmes` y abrir un PR.
- Refinar el merging para eliminar duplicaciones y reestructurar secciones.
