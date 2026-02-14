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

#### Record: Usuario

```java
public record Usuario(
        @NonNull
        Long idUsuario,
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,
        String apellidos,
        @Email(message = "El formato del email es incorrecto")
        @NotBlank(message = "El email es obligatorio")
        String email,
        @NotBlank(message = "La contraseña es obligatoria")
        String password,
        String telefono,
        NombreRol rol,
        LocalDateTime fechaRegistro,
        Boolean activo)
{}
```

#### Implementación POST: resgistro

```java
    private Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, Usuario> usuarios = new ConcurrentHashMap<>(); // guardo los usurarios por email
    private final Map<Long, Usuario> usuariosporId = new ConcurrentHashMap<>(); // guardo los usurarios por email
    private final AtomicLong idUsuarioSeq = new AtomicLong(1);

    @PostMapping("/pistaPadel/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario registrarUsuario(@Valid @RequestBody Usuario usuarioNuevo, BindingResult bindingResult) {
        logger.info("Intento de registro para email={}", usuarioNuevo.email());
        logger.debug("Usurario recibido: nombre={}, apellidos={}, telefono={}",
                usuarioNuevo.nombre(), usuarioNuevo.apellidos(), usuarioNuevo.telefono());
        if (bindingResult.hasErrors()) {
            // Error 400 --> datos inválidos
            logger.error("Error inesperado");
            throw new ExcepcionUsuarioIncorrecto(bindingResult);
        }
        if (usuarios.get(usuarioNuevo.email())!= null) {
            // Error 409 --> email ya existe
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email ya existe");
        }

        // Generar id en servidor
        long id = idUsuarioSeq.getAndIncrement();

        Usuario u = new Usuario(
                id,
                usuarioNuevo.nombre(),
                usuarioNuevo.apellidos(),
                usuarioNuevo.email(),
                usuarioNuevo.password(),
                usuarioNuevo.telefono(),
                NombreRol.USER, // rol por defecto
                java.time.LocalDateTime.now(),
                true
        );

        usuariosporId.put(id, u);
        usuarios.put(u.email(), u);

        logger.info("Usuario registrado correctamente id={} email={}", id, usuarioNuevo.email());
        // Devuelve 201 con un DTO de salida SIN password
        return u;
    }
```