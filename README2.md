# CAMBIO EN LA PRÁCTICA GRUPAL
## INCORPORACIÓN DE PERSISTENCIA

Correspondiente a lo ya realizado, procedo a incorporar persistencia a mi parte del proyecto.

<details>
<summary><strong>MOVIENDO LÓGICA DE CONTROLLER A SERVICIOS </strong></summary>

Dentro del controlador, toda la lógica será movida a 'UsuarioService', quedando así las tres endpoint en el Controlador:
```java
// ============================
    // SECCIÓN: USUARIOS
    // ============================

    @GetMapping("/pistaPadel/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Collection<Usuario> listarUsuarios(){
        return usuarioService.listarUsuarios();
    }

    @GetMapping("/pistaPadel/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Usuario obtenerUsuario(@PathVariable Long userId) {
        return usuarioService.obtenerUsuario(userId);
    }

    @PatchMapping("/pistaPadel/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Usuario actualizarUsuario(@PathVariable Long userId, @RequestBody Map<String, Object> cambios) {
        return usuarioService.actualizarUsuario(userId, cambios);
    }
```

Por lo tanto, quedará así la lógica en la parte de 'UsuarioService':

```java
public Collection<Usuario> listarUsuarios() {
        logger.info("Devolucion lista de usuarios registrados");
        List<Usuario> usuariosRegistrados = repoUsuario.findAll();
        if (usuariosRegistrados.isEmpty()) {
            logger.info("No hay usuarios registrados");
        }
        return usuariosRegistrados;
    }


    public Usuario obtenerUsuario(Long userId) {
        Optional<Usuario> usuarioBuscado = repoUsuario.findById(userId);

        if (usuarioBuscado.isEmpty()) {
            logger.info("Usuario no encontrado al hacer GET del userId");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return usuarioBuscado.get();
    }


    public Usuario actualizarUsuario(Long userId, Map<String, Object> cambios) {

        logger.info("PATCH /users/{} llamado con cambios: {}", userId, cambios);

        Usuario user = repoUsuario.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String emailViejo = user.getEmail();

        // Valido email único:
        if (cambios.containsKey("email")) {
            String nuevoEmail = (String) cambios.get("email");
            boolean emailExiste = repoUsuario.existsByEmailAndIdNot(nuevoEmail, userId);

            if (emailExiste) { throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya registrado en app");}
            user.setEmail(nuevoEmail);
        }

        // Campos permitidos
        if (cambios.containsKey("nombre"))
            user.setNombre((String) cambios.get("nombre"));
        if (cambios.containsKey("apellidos"))
            user.setApellidos((String) cambios.get("apellidos"));
        if (cambios.containsKey("password"))
            user.setPassword((String) cambios.get("password"));
        if (cambios.containsKey("telefono"))
            user.setTelefono((String) cambios.get("telefono"));
        if (cambios.containsKey("activo"))
            user.setActivo((Boolean) cambios.get("activo"));

        Usuario actualizado = repoUsuario.save(user);
        logger.info("Usuario actualizado correctamente: id={}, email={}",
                actualizado.getId(), actualizado.getEmail());

        return actualizado;
    }
```

Para el desarrollo de la lógica y el trabajo con la entidad usuario, dentro de 'RepoUsuario', añado los siguientes métodos para poder buscar:

```java
Usuario findByEmail(String email);
Usuario findByNombre(String nombre);
// Crud tiene implícito: Iterable<Usuario> findAll();
boolean existsByEmailAndIdNot(String email, Long id);
```

</details>


<details>
<summary><strong>MODIFICACIÓN TEST INTEGRADOS</strong></summary>

Para la parte de test de integración, suele interesar probar más la capa de persistencia (relegando el testing de endpoints y el controlador a E2E). Por ello, procedo a añadir las siguientes modificaciones dentro de test:

```java
@Autowired
    RepoUsuario repoUsuario;

    @Autowired
    private ObjectMapper objectMapper;
    private static final String REGISTER = "/pistaPadel/auth/register";

    @Autowired
    private ControladorREST controladorREST;


    @Test
    void registro_ok_201() throws Exception {

        Usuario user = new Usuario(1L, "Ana", "Ramos", "ana.integration@test.com", "123", "456", NombreRol.USER, null,true);
        repoUsuario.save(user);
        assertNotNull(repoUsuario.findById(user.getId()));
    }


    @Test
    void registro_emailDuplicado_409() throws Exception {
        Usuario user = new Usuario(1L, "Ana", "Ramos", "ana.integration@test.com", "123", "456", NombreRol.USER, null,true);

        // Guardo por primera vez:
        repoUsuario.save(user);
        // Guardo por segunda vez:
        DataIntegrityViolationException error = null;
        try{
            repoUsuario.save(user);
        } catch (DataIntegrityViolationException e) {
            error = e;
        }
        // Nos aseguramos que salte el error:
        assertNotNull(error);
    }
```


</details>

<details>
<summary><strong>MODIFICACIÓN TEST E2E</strong></summary>

Para esta parte, como se encarga de la itneracción con el controlador y los endpoints, no he tenido que realizar ningún cambio. De igual manera, aunque no cambiara nada, adjunto los endpoint relativos a esta parte por si se llega a perder algo con el volcado al main:

```java

    @Test
    void registro_ok_201() {
        String body = """
            {
              "idUsuario": 1,
              "nombre": "Ana",
              "apellidos": "Ramos",
              "email": "ana.e2e@test.com",
              "password": "123",
              "telefono": "666",
              "rol": "USER",
              "fechaRegistro": null,
              "activo": true
            }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                REGISTER, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void registro_emailDuplicado_409() {
        String body = """
            {
              "idUsuario": 1,
              "nombre": "Ana",
              "apellidos": "Ramos",
              "email": "dup.e2e@test.com",
              "password": "123",
              "telefono": "666",
              "rol": "USER",
              "fechaRegistro": null,
              "activo": true
            }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 1ª -> 201
        ResponseEntity<String> r1 = restTemplate.exchange(
                REGISTER, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        Assertions.assertEquals(HttpStatus.CREATED, r1.getStatusCode());

        // 2ª mismo email -> 409
        ResponseEntity<String> r2 = restTemplate.exchange(
                REGISTER, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        Assertions.assertEquals(HttpStatus.CONFLICT, r2.getStatusCode());
    }


    @Test
    public void actualizarUsuario_EmailDuplicado_Test(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Creamos el primer usuario:
        //ResponseEntity<Usuario> response1 =
        restTemplate.exchange(
                "/pistaPadel/auth/register",
                HttpMethod.POST,
                new HttpEntity<>(
                        """
                           {
                            "idUsuario": "1",
                            "nombre": "Juan",
                            "apellidos": "Lovato",
                            "email": "juan@ejemplo.com",
                            "password": "123456",
                            "telefono": "123456789",
                            "rol": "USER",
                            "fechaRegistro": null,
                            "activo": true
                           }
                        """, headers),
                String.class
        );


        // Creamos un segundo usuario:
        //ResponseEntity<Usuario> response2 =
        restTemplate.exchange(
                "/pistaPadel/auth/register",
                HttpMethod.POST,
                new HttpEntity<>(
                        """
                            {
                            "idUsuario": "5",
                            "nombre": "Martina",
                            "apellidos": "Ortiz",
                            "email": "mod@ejemplo.com",
                            "password": "123456",
                            "telefono": "123456789",
                            "rol": "USER",
                            "fechaRegistro": null,
                            "activo": true
                            }
                        """, headers),
                String.class
        );

        // Intentamos poner el email de uno al otro para ver si salta error:
        String cambios = """
                {
                "email": "mod@ejemplo.com"
                }
                """;
        ResponseEntity<String> response = restTemplate.exchange(
                "/pistaPadel/users/1",
                HttpMethod.PATCH,
                new HttpEntity<>(cambios, headers),
                String.class
        );

        Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

    }

```

</details>

<details>
<summary><strong>CREACIÓN TAREAS PROGRAMADAS</strong></summary>

Se nos pide la realización de dos tareas programadas:
1- Todas las noches a las 2 de la mañana deberá mandar un correo a los usuarios que tienen 
pista reservada ese día como recordatorio. 
2- El primer día del mes se mandará un correo a todos los usuarios con las pistas y los horarios disponibles.

Quedando el desarrollo de la tarea programada de tal manera:

```java
@Component
public class TareasProgramadas {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public PistaService servicioPista;
    @Autowired
    public ReservaService servicioReserva;

    @Scheduled(cron = "0 0 2 * * *") // En el segundo y minuto 0, a las 2am de cada día, mes.
    public void remindPista() {
        logger.info("Me ejecuto cada día a las 2 AM");

        /* Mandar correo a usuarios que tienen pista reservada para ese día */

        servicioReserva.enviarRecordatorioDia(); // Esta clase debemos implementarla en Servicio
    }

    @Scheduled(cron = "0 0 0 1 * *") // Se ejecutará justo al empezar el día 1
    public void showDisponibilidad() {
        logger.info("Me ejecuto el día 1 de cada mes");

        /* Mandar correo a todos los usuarios con las pistas y los horarios disponibles */

        servicioPista.enviarDisponibilidadMensual(); // Esta clase debemos implementarla en Servicio
    }
}
```

Las funciones a las que se llama se crearán en 'PistaService' y en 'ReservaService'.
Además, para el envío de un email, se debe crear un Servicio adicional llamado 'EmailService':

```java
@Service
public class EmailService {
    @Autowired
    JavaMailSender mailSender;

    public void enviarEmail(String to, String subject, String text){
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(to);
        mensaje.setSubject(subject);
        mensaje.setText(text);
        mailSender.send(mensaje);
    }
}
```

Y para que funcione el manejo del email, se debe añadir una nueva dependencia en el pom.xml:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```


</details>
