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



formRegister.addEventListener("submit", async function (event){
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

    let url = baseUrl + "/auth/register";

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


// Al hacer el login, es imprescindible guardar el token en localStorage:
// localStorage.setItem("token", data.token);


/*
CREAR NUEVO HTML "perfil.html", EL CUAL DEBE INCORPORAR TAMBIÉN EL PATCH PARA EDITAR EL PERFIL
*/



// Esta función ponerla en un desplegable del menú "Ver Perfil".
// Para que, una vez registrado, el usuario pueda ver su perfil:
async function cargarPerfil() {
    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId");

    if (!token || !userId) {
        window.location.href = "login.html";
        return;
    }

    try {
        const response = await fetch(`${baseUrl}/pistaPadel/users/${userId}`, {
            headers: {
                "Authorization": "Bearer " + token
            }
        });

        if (!response.ok) {
            throw new Error("No se pudo cargar el perfil");
        }

        const user = await response.json();

        // Mostramos los datos del usuario registrado:
        document.getElementById("bienvenida").textContent =
            `Bienvenido, ${user.nombre} ${user.apellidos}`;

        document.getElementById("email").textContent =
            `Email: ${user.email}`;

    } catch (error) {
        alert("Error cargando perfil");
    }
}
/*
// Cargar al abrir página
window.onload = cargarPerfil;
*/

