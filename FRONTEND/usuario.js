/* ======= JAVASCRIPT DE REGISTRO Y USUARIOS ======= */

const baseUrl = "http://localhost:8080";
const formRegister = document.getElementById("formRegister");


function mostrarMensaje(texto, tipo) {
    const div = document.getElementById("mensaje");

    div.textContent = texto;
    div.style.display = "block";

    if (tipo === "error") {
        div.style.color = "red";
    } else {
        div.style.color = "green";
    }

    // desaparecer después de 3 segundos
    setTimeout(() => {
        div.style.display = "none";
    }, 3000);
}

// ========================
// REGISTER
// ========================

// Para el registro de usuarios:
formRegister?.addEventListener("submit", async function (event){
    event.preventDefault();

    // Guardo los datos del formulario en variables:
    const nombre = document.getElementById("name").value;
    const apellidos = document.getElementById("apellidos").value;
    const email = document.getElementById("email").value;
    const telefono = document.getElementById("telefono").value;
    const password = document.getElementById("password").value;
    const confirmar = document.getElementById("confirmar").value;

    // Validación en el frontend de las contraseñas coinciden
    if (password !== confirmar) {
        mostrarMensaje("Las contraseñas no coinciden", "error");
        return;
    }

    let url = baseUrl + "/pistaPadel/auth/register";

    try {
        const response = await fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                nombre,
                apellidos,
                email,
                telefono,
                password
            })
        });

        // Si la respuesta de haber efectuado el POST es 409...
        if (response.status === 409) {
            mostrarMensaje("Usuario ya existente", "error");
            return;
        }

        if (!response.ok) {
            mostrarMensaje("Datos inválidos", "error");
            return;
        }

        // Si no ha saltado ninguno de los errores anteriores, todo ha ido bien:
        mostrarMensaje("Usuario creado correctamente", "ok");

        // Una vez registrado el nuevo usuario, pasados 1.5s se redirige al login
        setTimeout(() => {
            window.location.href = "login.html";
        }, 1500);

    } catch (error) {
        mostrarMensaje("Error al conectar con el servidor", "error");
    }
});


// ========================
// LOGIN
// ========================

// Una vez registrado el usuario, inicia sesión:

const formLogin = document.getElementById("formLogin");
if (formLogin) {
    formLogin.addEventListener("submit", async function (event) {
        event.preventDefault();

        const email    = document.getElementById("email").value;
        const password = document.getElementById("password").value;

        try {
            const response = await fetch(baseUrl + "/pistaPadel/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password })
            });

            if (response.status === 400) {
                mostrarMensaje("Datos inválidos", "error");
                return;
            }
            if (response.status === 401) {
                mostrarMensaje("Email o contraseña incorrectos", "error");
                return;
            }
            if (!response.ok) {
                mostrarMensaje("Error al iniciar sesión", "error");
                return;
            }

            const data = await response.json();
            // Al hacer el login, es imprescindible guardar el token en localStorage:
            localStorage.setItem("token", data.token);

            // Llamamos a /auth/me para obtener el id y rol del usuario autenticado
            const meRes = await fetch(baseUrl + "/pistaPadel/auth/me", {
                headers: { "Authorization": "Bearer " + data.token }
            });
            if (meRes.ok) {
                const me = await meRes.json();
                localStorage.setItem("userId", me.id);
                localStorage.setItem("userRol", me.rol);

                mostrarMensaje("¡Bienvenido!", "ok");
                setTimeout(() => {
                    // Redireccionamos según el rol:
                    if (me.rol === "ADMIN") {
                        window.location.href = "admin.html";
                    } else {
                        window.location.href = "index.html";
                    }
                }, 1000);
            }

            // mostrarMensaje("¡Bienvenido!", "ok");
            // setTimeout(() => { window.location.href = "index.html"; }, 1000);
            
        } catch (error) {
            mostrarMensaje("Error al conectar con el servidor", "error");
        }
    });
}


// Esta función utilizada en el desplegable del menú "Mi Perfil".
/* 
Para que, una vez registrado, el usuario pueda ver su perfil. De no 
haber iniciado sesión, se redirigirá a login.html
*/
async function cargarPerfil() {
    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId");
    const userRol = localStorage.getItem("userRol");

    if (!token || !userId) {
        window.location.href = "login.html";
        return;
    }

    const paginaActual = window.location.pathname;

    if (userRol === "ADMIN" && !paginaActual.endsWith("admin.html")) {
        window.location.href = "admin.html";
        return;
    }

    try {
        const response = await fetch(`${baseUrl}/pistaPadel/users/${userId}`, {
            headers: {
                "Authorization": "Bearer " + token
            }
        });

        if (response.status === 401) {
            localStorage.clear();
            window.location.href = "login.html";
            return;
        }
        if (!response.ok) {
            throw new Error("No se pudo cargar el perfil");
        }

        const user = await response.json();

        // Mostramos los datos del usuario registrado:
        document.getElementById("bienvenida").textContent = `Bienvenido, ${user.nombre} ${user.apellidos}`;

        document.getElementById("email").textContent = `${user.email}`;

        document.getElementById("perfil-telefono").textContent = user.telefono;
        document.getElementById("perfil-rol").textContent = user.rol;
        document.getElementById("perfil-fecha").textContent = user.fechaRegistro ? user.fechaRegistro.substring(0, 10) : "—";

        // // Pre-rellenar el formulario de edición con los datos actuales
        // document.getElementById("edit-nombre").value    = user.nombre;
        // document.getElementById("edit-apellidos").value = user.apellidos || "";
        // document.getElementById("edit-email").value     = user.email;
        // document.getElementById("edit-telefono").value  = user.telefono;

        // Solo si existe el formulario de edición
        const editNombre = document.getElementById("edit-nombre");

        if (editNombre) {
            document.getElementById("edit-nombre").value    = user.nombre;
            document.getElementById("edit-apellidos").value = user.apellidos || "";
            document.getElementById("edit-email").value     = user.email;
            document.getElementById("edit-telefono").value  = user.telefono;
        }

    } catch (error) {
        alert("Error cargando perfil");
    }
}

// ========================
// PERFIL — EDITAR (PATCH)
// ========================

const formEditar = document.getElementById("formEditar");
if (formEditar) {
    formEditar.addEventListener("submit", async function (event) {
        event.preventDefault();

        const token  = localStorage.getItem("token");
        const userId = localStorage.getItem("userId");

        if (!token || !userId) {
            window.location.href = "login.html";
            return;
        }

        const nombre    = document.getElementById("edit-nombre").value.trim();
        const apellidos = document.getElementById("edit-apellidos").value.trim();
        const email     = document.getElementById("edit-email").value.trim();
        const telefono  = document.getElementById("edit-telefono").value.trim();
        const password  = document.getElementById("edit-password").value.trim();
        const confirmar = document.getElementById("edit-confirmar").value.trim();

        if (password && password !== confirmar) {
            mostrarMensaje("Las contraseñas no coinciden", "error");
            return;
        }

        // Solo se envían los campos que el usuario quiere cambiar
        const cambios = {};
        if (nombre)    cambios.nombre    = nombre;
        if (apellidos) cambios.apellidos = apellidos;
        if (email)     cambios.email     = email;
        if (telefono)  cambios.telefono  = telefono;
        if (password)  cambios.password  = password;

        if (Object.keys(cambios).length === 0) {
            mostrarMensaje("No hay cambios que guardar", "error");
            return;
        }

        try {
            const response = await fetch(`${baseUrl}/pistaPadel/users/${userId}`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + token
                },
                body: JSON.stringify(cambios)
            });

            if (response.status === 409) {
                mostrarMensaje("El email ya está en uso por otra cuenta", "error");
                return;
            }
            if (response.status === 401) {
                localStorage.clear();
                window.location.href = "login.html";
                return;
            }
            if (response.status === 403) {
                mostrarMensaje("No tienes permiso para realizar esta acción", "error");
                return;
            }
            if (!response.ok) {
                mostrarMensaje("Error al actualizar el perfil", "error");
                return;
            }

            document.getElementById("edit-password").value  = "";
            document.getElementById("edit-confirmar").value = "";

            mostrarMensaje("Perfil actualizado correctamente", "ok");
            setTimeout(() => cargarPerfil(), 1500);
        } catch (error) {
            mostrarMensaje("Error al conectar con el servidor", "error");
        }
    });
}

// ========================
// LOGOUT
// ========================

async function cerrarSesion() {
    const token = localStorage.getItem("token");

    try {
        await fetch(baseUrl + "/pistaPadel/auth/logout", {
            method: "POST",
            headers: token ? { "Authorization": "Bearer " + token } : {}
        });
    } catch (_) {
        // ignorar errores de red
    } finally {
        localStorage.removeItem("token");
        localStorage.removeItem("userId");
        localStorage.removeItem("userRol");
        window.location.href = "login.html";
    }
}



// Cargar al abrir perfil.html
if (document.getElementById("bienvenida")) {
    window.addEventListener("DOMContentLoaded", cargarPerfil);
}


