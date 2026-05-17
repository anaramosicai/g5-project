# g5-project (+integrantes)
Final Project of PAT by group 5: Felicia Huynh, Antonio Lafont, Yago Méndez, Martina Ortiz y Ana Mei Li Ramos

---
# Datos de usuarios para poder probar la función de usuario y administrador

En la parte de Frontend, para crear un primer registro, los campos a rellenar serían los siguientes:
- Nombre: Anina
- Apellidos: Ortiz Delgado 
- Email: aninasos@gmail.com
- Teléfono: 456789321
- Contraseña: trabajopat
- Confirmar contraseña: trabajopat

**Nota**: El usuario se registra por defecto con el rol USER; para poder modificar su rol, no se hará directamente desde la interfaz del Frontend, sino desde la propia base de datos, donde se editará dicho campo. Una vez modificado el rol en base de datos, ya se podrán acceder a las funcionalidades del ADMIN en Frontend.

La query para hacer el cambio de rol es la siguiente:
```sql
UPDATE USUARIO SET ROL = 'ADMIN' WHERE EMAIL = 'aninasos@gmail.com';
```

En PostMan, dicho primer registro usaría un JSON como este:
```json
{
  "nombre": "Anina",
  "apellidos": "Ortiz Delgado",
  "email": "aninasos@gmail.com",
  "password": "trabajopat",
  "telefono": "456789321"
}
```

**Nota adicional**: Dentro de la base de datos ya creada, existe un usuario ADMIN ('Martina'). Cuya contraseña no cifrada es 'holahola'. Por si se quisiera trabajar con ese usuario ADMIN en lugar de crear uno nuevo. En caso de querer trabajar con un usuario con rol USER, registrar con los datos indicados al principio y hacer el 'login' del mismo.
