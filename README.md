# g5-project
Final Project of PAT by group 5 

## 1. Cambios realizados en ANA_BRANCH
### 1.1. Descripción de mi parte
Mi parte trataba de la **Autenticación + detalle de usuario (errores tipo 401/403)**

Tenía  los siguientes endpoints a desarrollar:
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

Tabla repaso HTTP STATUS CODES
<table border="1" cellpadding="10" cellspacing="0">
  <thead>
    <tr>
      <th>Código</th>
      <th>Descripción</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>200</td>
      <td> OK </td>
    </tr>
    <tr>
      <td>201</td>
      <td> Created </td>
    </tr>
    <tr>
      <td>204</td>
      <td> No Content </td>
    </tr>
    <tr>
      <td>400</td>
      <td> Bad Request </td>
    </tr>
    <tr>
      <td>401</td>
      <td> Unauthorizded </td>
    </tr>
    <tr>
      <td>403</td>
      <td> Forbidden </td>
    </tr>
    <tr>
      <td>409</td>
      <td> Conflict </td>
    </tr>
  </tbody>
</table>

### 1.2. Desarrollo de mi parte
Tomando como base el código que subio mi compañera Felicia parto creando el **record** **`Usuario`** que tiene los siguientes caracterísitcas y restricciones:

- Caracterísitcas
  - `idUsuario`: Identificador único del usuario.
  - `nombre`: Nombre del usuario.
  - `apellidos`: Apellidos del usuario.
  - `email`: Correo electrónico (único en el sistema).
  - `password`: Contraseña cifrada.
  - `telefono`: Teléfono de contacto.
  - `rol`: Rol del usuario en el sistema. <small>*Valores posibles: USER, ADMIN.*</small>
  - `fechaRegistro`: Fecha y hora de alta en el sistema.
  - `activo`: Indica si el usuario está activo o deshabilitado.

- Restricciones
  - El email debe ser **único**.
  - Un usuario puede tener **0..n** reservas.
  - Solo los usuarios con **rol ADMIN** pueden **gestionar pistas**.

