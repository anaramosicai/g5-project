/* ======= JAVASCRIPT DE LOS ADMIN ======= */

let accionActual = null;
const baseUrlAdmin = "http://localhost:8080";
const formRegisterAdmin = document.getElementById("formRegister");

const accionesAdmin = {
    "usuarios-cargar": cargarUsuarios,
    "usuario-buscar": ejecutarBusquedaUsuario,
    "usuario-editar": editarUsuarioAdmin,

    "pista-crear": crearPista,
    "pista-editar": editarPista,
    "pista-eliminar": eliminarPista,

    "reservas-cargar": cargarReservas
};


function mostrarPanel(tipo) {

    document.getElementById("panelAcciones").style.display = "none";
    document.getElementById("panelListados").style.display = "none";
    document.getElementById("panelFormularios").style.display = "none";
    document.getElementById("panelConfirmacion").style.display = "none";

    document.getElementById("panel" + tipo.charAt(0).toUpperCase() + tipo.slice(1)).style.display = "block";
}


// ========================
// ADMIN
// ========================


function abrirPanel(tipo) {
    accionActual = tipo;

    document.getElementById("panelAcciones").style.display = "none";
    document.getElementById("panelAccion").style.display = "block";

    const titulo = document.getElementById("tituloAccion");
    const contenido = document.getElementById("contenidoAccion");

    contenido.innerHTML = "";

    switch (tipo) {

        case "usuarios-cargar":
            titulo.textContent = "Cargar usuarios";
            contenido.innerHTML = `<p>Cargando usuarios...</p>`;
            ejecutarAccion();
            break;

        case "usuario-buscar":
            titulo.textContent = "Buscar usuario por ID";
            contenido.innerHTML = `<input id="userId" placeholder="ID usuario">`;
            break;

        case "usuario-editar":
            titulo.textContent = "Editar usuario";
            contenido.innerHTML = `
                <input id="userId" placeholder="ID usuario">
                <input id="email" placeholder="Nuevo email">
            `;
            break;

        case "pista-crear":
            titulo.textContent = "Crear pista";
            contenido.innerHTML = `
                <input id="nombre" placeholder="Nombre pista">
                <input id="ubicacion" placeholder="Ubicación">
            `;
            break;

        case "pista-eliminar":
            titulo.textContent = "Eliminar pista";
            contenido.innerHTML = `<input id="pistaId" placeholder="ID pista">`;
            break;

        case "reservas-cargar":
            titulo.textContent = "Cargar reservas";
            contenido.innerHTML = `<p>Se mostrarán todas las reservas.</p>`;
            break;
        
        
    }
}


function volverAtras() {
    document.getElementById("panelAccion").style.display = "none";
    document.getElementById("panelAcciones").style.display = "block";
}


// En función de qué acción ha seleccionado el Admin, se llamará a una de las funciones definidas abajo:
async function ejecutarAccion() {
    try {
        const fn = accionesAdmin[accionActual];

        if (!fn) {
            console.log("Acción no encontrada");
            return;
        }

        const data = await fn(); // Se ejecuta una de las funciones de abajo

        switch (accionActual) {
            case "usuarios-cargar":
                renderUsuarios(data);
                break;
        }

        console.log("Acción ejecutada correctamente");

    } catch (err) {
        console.error(err);
        console.log("Error ejecutando acción");
    }
}


// ============== ACCIONES RELATIVAS A USUARIOS ==============

async function cargarUsuarios() {
    const rol   = localStorage.getItem("userRol");
    const token = localStorage.getItem("token");

    if (rol !== "ADMIN") {
        mostrarMensaje("Acceso restringido a administradores", "error");
        volverAtras();
        return;
    }

    const response = await fetch(baseUrlAdmin + "/pistaPadel/users", {
        method: "GET",
        headers: { "Authorization": "Bearer " + token }
    });

    if (response.status === 401) throw new Error("401");
    if (response.status === 403) throw new Error("403");
    if (!response.ok) throw new Error("Error al cargar usuarios");

    return await response.json();
}


function renderUsuarios(usuarios) {
    const contenedor = document.getElementById("contenidoAccion");

    if (!usuarios || usuarios.length === 0) {
        contenedor.innerHTML = `<p style="font-style:italic; color:var(--azul-medio-oscuro);">No hay usuarios registrados.</p>`;
        return;
    }

    const filas = usuarios.map(u => `
        <tr>
            <td>${u.id}</td>
            <td>${u.nombre} ${u.apellidos ?? ""}</td>
            <td>${u.email}</td>
            <td>${u.rol}</td>
            <td>${u.activo ? "Sí" : "No"}</td>
            <td>
                <button class="btn-admin" style="width:auto; padding:6px 12px;"
                        onclick="verUsuario(${u.id})">Ver</button>
            </td>
        </tr>
    `).join("");

    contenedor.innerHTML = `
        <div class="tabla-reservas" style="margin-top:10px;">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nombre</th>
                        <th>Email</th>
                        <th>Rol</th>
                        <th>Activo</th>
                        <th>Acción</th>
                    </tr>
                </thead>
                <tbody>${filas}</tbody>
            </table>
        </div>
    `;
}


function verUsuario(id) {
    buscarUsuarioPorId();
    // Pre-rellenar el input y lanzar la búsqueda automáticamente
    setTimeout(() => {
        const input = document.getElementById("inputBuscarUserId");
        if (input) {
            input.value = id;
            ejecutarBusquedaUsuario();
        }
    }, 50);
}


function buscarUsuarioPorId() {
    accionActual = "usuario-buscar";

    document.getElementById("panelAcciones").style.display = "none";
    document.getElementById("panelAccion").style.display  = "block";

    document.getElementById("tituloAccion").textContent = "Buscar usuario por ID";
    document.getElementById("contenidoAccion").innerHTML = `
        <div class="admin-search-bar">
            <input id="inputBuscarUserId" type="number" min="1" placeholder="Introduce el ID del usuario" />
            <button class="btn-admin" onclick="ejecutarBusquedaUsuario()">🔍 Buscar</button>
        </div>
        <div id="resultadoUsuario"></div>
    `;
}       

async function ejecutarBusquedaUsuario() {
    const input  = document.getElementById("inputBuscarUserId");
    const userId = input ? input.value.trim() : "";

    if (!userId) {
        mostrarMensaje("Introduce un ID de usuario", "error");
        return;
    }

    const token     = localStorage.getItem("token");
    const resultado = document.getElementById("resultadoUsuario");
    resultado.innerHTML = "<p>Cargando...</p>";

    try {
        // 1. Obtener datos del usuario
        const userRes = await fetch(`${baseUrlAdmin}/pistaPadel/users/${userId}`, {
            headers: { "Authorization": "Bearer " + token }
        });

        if (userRes.status === 404) {
            resultado.innerHTML = `<p class="admin-error">No se encontró ningún usuario con ID ${userId}.</p>`;
            return;
        }
        if (userRes.status === 403) {
            resultado.innerHTML = `<p class="admin-error">No tienes permiso para ver este usuario.</p>`;
            return;
        }
        if (!userRes.ok) {
            resultado.innerHTML = `<p class="admin-error">Error al buscar el usuario.</p>`;
            return;
        }

        const user = await userRes.json();

        // 2. Obtener todas las reservas y filtrar las de este usuario
        let reservasHtml = "";
        try {
            const resRes = await fetch(`${baseUrlAdmin}/pistaPadel/admin/reservations`, {
                headers: { "Authorization": "Bearer " + token }
            });
            if (resRes.ok) {
                const todasReservas = await resRes.json();
                const reservasUsuario = todasReservas.filter(r => r.usuario?.id === parseInt(userId));

                if (reservasUsuario.length === 0) {
                    reservasHtml = `<p style="font-style:italic; color:var(--azul-medio-oscuro);">Sin reservas registradas.</p>`;
                } else {
                    const filas = reservasUsuario.map(r => `
                        <tr>
                            <td>${r.reservationId}</td>
                            <td>${r.pista?.nombre ?? "Pista " + (r.pista?.id ?? "—")}</td>
                            <td>${r.inicio?.substring(0, 16).replace("T", " ")}</td>
                            <td>${r.fin?.substring(0, 16).replace("T", " ")}</td>
                            <td>${r.duracionMinutos} min</td>
                            <td><span class="estado-${r.estado?.toLowerCase()}">${r.estado}</span></td>
                        </tr>
                    `).join("");

                    reservasHtml = `
                        <div class="tabla-reservas" style="margin-top:10px;">
                            <table>
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Pista</th>
                                        <th>Inicio</th>
                                        <th>Fin</th>
                                        <th>Duración</th>
                                        <th>Estado</th>
                                    </tr>
                                </thead>
                                <tbody>${filas}</tbody>
                            </table>
                        </div>
                    `;
                }
            }
        } catch (_) {
            reservasHtml = `<p style="font-style:italic;">No se pudieron cargar las reservas.</p>`;
        }

        // 3. Renderizar el resultado
        resultado.innerHTML = `
            <div class="perfil-card" style="margin-top:20px;">
                <h3 style="color:var(--azul-oscuro); margin-bottom:15px;">Datos del usuario</h3>
                <div class="perfil-info">
                    <p><strong>ID:</strong> <span>${user.id}</span></p>
                    <p><strong>Nombre:</strong> <span>${user.nombre} ${user.apellidos ?? ""}</span></p>
                    <p><strong>Email:</strong> <span>${user.email}</span></p>
                    <p><strong>Teléfono:</strong> <span>${user.telefono ?? "—"}</span></p>
                    <p><strong>Rol:</strong> <span>${user.rol}</span></p>
                    <p><strong>Miembro desde:</strong> <span>${user.fechaRegistro?.substring(0, 10) ?? "—"}</span></p>
                    <p><strong>Activo:</strong> <span>${user.activo ? "Sí" : "No"}</span></p>
                </div>
            </div>
            <h3 style="margin-top:25px; color:var(--azul-oscuro);">Reservas del usuario</h3>
            ${reservasHtml}
        `;

    } catch (error) {
        resultado.innerHTML = `<p class="admin-error">Error al conectar con el servidor.</p>`;
    }
}

function editarUsuarioAdmin() {
    // For u Ana
    console.log("Editando usuario...");
}


// ============== ACCIONES RELATIVAS A PISTAS ==============

function crearPista() {
    window.location.href = "pista.html";
    console.log("Creando pista...");

}

function editarPista() {
    console.log("Editando pista...");
}

function eliminarPista() {
    console.log("Eliminando pista...");
}

// ============== ACCIONES RELATIVAS A RESERVAS ==============

function cargarReservas() {
    console.log("Cargando reservas...");
}