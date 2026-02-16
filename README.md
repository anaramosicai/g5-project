# g5-project
Final Project of PAT by group 5 

## Cambios realizados en Martina_branch:
En primer lugar, mi parte irá principalmente enfocada al tratado de Usuario, en conjunto con la parte de Ana (autorización + usuario). Me encargaré de la realización de los cuatro endpoint siguientes:

- **GET** /pistaPadel/users
- **GET** /pistaPadel/users/{userId}
- **PATCH** /pistaPadel/users/{userId}
- **GET** /pistaPadel/health

### TRATADO Y CREACIÓN DE CLASES

En conjunto con Ana, se crea el record Usuario y dentro de este se añaden una serie de validaciones. Por ejemplo, con '@Email' logramos validar que el formato de los correos introducidos son correctos.

Por otro lado, tomando como base la clase 'ControladorREST' de 'felicia_branch', comenzaré a añadir cada endpoint de los nombrados arriba.
Copio también las clases Rol y NombreRol, las cuales necesitaré cuando trabaje con mis otras clases. Además de 'ConfigSeguridad', clase que me permitirá habilitar los roles (principalmente con el que trabajo, que es ADMIN) y controlar el acceso a los endpoint mediante '@PreAuthorize'.

**IMPORTANTE:** De cara a aquel que se encargue de hacer el POST de registro de Usuario, en el body, el usuario se registrará pero no puede él determinar su ID ni su rol (el cual será siempre USER en su caso), de eso se encargará el servidor.

### TRATADO DE DEPENDENCIAS

* Añado la siguiente dependencia para poder usar la Preautorización de roles y la seguridad:
```java
      <dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-security</artifactId>
		</dependency>
```

* Además de la dependencia para realizar validaciones:
```java
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```
Con ello aseguro poder cumplir en el futuro con las condiciones que me impongan y las validaciones que se usaron en el record de Usuario.

### IMPLEMENTACIÓN GET DE USUARIOS

En este primer GET se pide el Listado de usuarios, el cual, por facilidad, se devolverá ordenado en nombre alfabético de apellido (aunque podría haberse devuelto por fecha de registro).

--> Lo de por páginas era opcional, si diera tiempo, añadirlo.

En cuanto a las respuestas esperadas, este método puede devolvernos un 200 (indicador de que se efectuó bien), un 401 (indicador de que no se está autorizado) o un 403 (indicador de que no tenemos permisos y por tanto se nos prohibe acceder al método).
Si el GET devuelve un objeto, tendremos como respuesta el 200.
Si se intenta acceder sin autenticar, Spring Security se encargará de devolver el 401. 
Si intentamos acceder desde otro rol que no sea ADMIN, con '@PreAuthorize("hasRole('ADMIN')")' logramos que Spring Security devuelva el aviso 403. 
Spring Security se ejecuta antes de la llamada del endpoint, cuando llega la petición API, se ejecutan filtros de seguridad antes de acceder como tal al endpoint.

De cara al manejo de errores 401 y 403, podemos modificar su mensaje en lugar de trabajar con lo predeterminado de Spring, añadiendo a nuestra clase 'ConfigSeguridad' el siguiente código:

```java
// === MANEJO DE ERRORES 401 Y 403 ===
                // ¿Qué sucede cuando...? (Aplicado a todos los endpoint protegidos)
                .exceptionHandling(ex -> ex
                        // Usuario NO autenticado → 401
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "401 - No autenticado") )
                        // Usuario autenticado pero sin permisos → 403
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                        "403 - Acceso denegado")
                        )
                )
```

* Foto demostración de funcionamiento:

<img width="919" height="866" alt="image" src="https://github.com/user-attachments/assets/ca52493b-ba4c-4916-aab7-96f6e2ca088d" />


### IMPLEMENTACIÓN GET POR ID DE USUARIO

Creación del segundo GET. (@GetMapping("/pistaPadel/users/{userId}"))


* Fotos demostración de funcionamiento:

<img width="865" height="638" alt="image" src="https://github.com/user-attachments/assets/b0c60a44-5b8d-48a2-9690-d98ff828b40a" />

<img width="862" height="481" alt="image" src="https://github.com/user-attachments/assets/bca8bc6e-a659-4de2-b7f8-9b1f522b15c8" />



### IMPLEMENTACIÓN PATCH DE USUARIO

Creación del endpoint con PATCH que nos permita actualizar datos dado un Id de usuario.

Para el PATCH, en primer lugar, se ha de comprobar que el usuario que se nos pide actualizar existe y que el email que nos dan no está repetido en los registros.
Bloqueo aquellos campos que quiero que permanezcan inalterables (idUsuario, rol y fechaRegistro) y procedo a crear el nuevo usuario actualizado:
```java
Usuario actualizado;
        try{
            actualizado = new Usuario(
                    user.idUsuario(),
                    cambios.containsKey("nombre") ? (String) cambios.get("nombre") : user.nombre(),
                    cambios.containsKey("apellidos") ? (String) cambios.get("apellidos") : user.apellidos(),
                    cambios.containsKey("email") ? (String) cambios.get("email") : user.email(),
                    cambios.containsKey("password") ? (String) cambios.get("password") : user.password(),
                    cambios.containsKey("telefono") ? (String) cambios.get("telefono") : user.telefono(),
                    user.rol(),
                    user.fechaRegistro(),
                    cambios.containsKey("activo") ? (Boolean) cambios.get("activo") : user.activo()
            );

        } catch (IllegalArgumentException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Campo inválido");
        }
```

Además, como previamente se estaba trabajando con dos HashMap (uno por email y otro por id), actualizo ambos con los nuevos datos para que haya una sincronización completa.

Una vez hecho esto, procedemos a comprobar el funcionamiento del PATCH

* Foto demostración del funcionamiento:

<img width="861" height="822" alt="image" src="https://github.com/user-attachments/assets/9312d0f9-7276-472b-8280-7724818e82c1" />

<img width="865" height="707" alt="image" src="https://github.com/user-attachments/assets/9d1b0aa8-a455-43d8-8f14-b51335b54591" />


### IMPLEMENTACIÓN GET PARA HEALTHCHECK

Este endpoint es realmente sencillo y se usa principalmente por otros sistemas para ver si nuestra aplicación está viva y responde funcionando correctamente.

Su implementación es así de sencilla:

```java
@GetMapping("/pistaPadel/health")
    public Map<String, String> health(){
        return Map.of("status", "ok");
    }
```

* Foto demostración del funcionamiento:

<img width="874" height="426" alt="image" src="https://github.com/user-attachments/assets/9ac027fb-6a69-4714-b57d-6f3521e60303" />



-----> Pregunta: ¿No sería mejor aprovechar la clase creada 'Rol' para ConfigSeguridad en lugar de escribir manualmente "ADMIN" o "USER"?


