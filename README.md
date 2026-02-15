# g5-project
Final Project of PAT by group 5 

## Cambios realizados en Martina_branch:
En primer lugar, mi parte irá principalmente enfocada al tratado de Usuario, en conjunto con la parte de Ana (autorización + usuario). Me encargaré de la realización de los tres endpoint siguientes:

- **GET** /pistaPadel/users
- **GET** /pistaPadel/users/{userId}
- **PATCH** /pistaPadel/users/{userId}

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


### IMPLEMENTACIÓN GET POR ID DE USUARIO

Creación del segundo GET. (@GetMapping("/pistaPadel/users/{userId}"))



### IMPLEMENTACIÓN PATCH DE USUARIO

Creación del endpoint con PATCH que nos permita actualizar datos dado un Id de usuario.
[REVISAR]
Para este endpoint, se usará la librería Jackson (de gran utilidad para trabajar con JSON) y dentro de la misma, su clase principal que será "ObjectMapper". La gran ventaja de esta clase es que nos permite aplicar cambios parciales de un Map a un objeto Java (más si son records, de tipo inmutable).

[SEGUIR POR AQUÍ Y REVISAR PATCH CON POSTMAN]




-----> Pregunta: ¿No sería mejor aprovechar la clase creada 'Rol' para ConfigSeguridad en lugar de escribir manualmente "ADMIN" o "USER"?


