# g5-project
Final Project of PAT by group 5 

### Cambios realizados en Martina_branch:
1º- Creación del record Usuario el cual usaré para los tres endpoints que me conciernen:
    - **GET** /pistaPadel/users
    - **GET** /pistaPadel/users/{userId}
    - **PATCH** /pistaPadel/users/{userId}

2º- Creación de UsuarioController para poder Controlar exclusivamente a cada usuario y segmentar código. Ahí empezaré a crear cada endpoint de los nombrados arriba.

3º- Creación del primer GET.
Añado la siguiente dependencia para poder usar la Preautorización de roles y la seguridad:
```java
      <dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-security</artifactId>
		</dependency>```

En este primer GET se pide el Listado de usuarios, el cual, por facilidad, se devolverá ordenado en nombre alfabético (aunque podría haberse devuelto por fecha de registro).
