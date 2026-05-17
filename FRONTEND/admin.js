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

    "reservas-cargar":  fetchTodasReservas,
    "reserva-buscar":   fetchReservaPorId,
    "reserva-eliminar": fetchEliminarReserva,
    "reserva-editar":   fetchEditarReserva
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
            contenido.innerHTML = `<p>Cargando todas las reservas del sistema...</p>`;
            ejecutarAccion();
            break;

        case "reserva-buscar":
            titulo.textContent = "Ver reserva por ID";
            contenido.innerHTML = `
                <div class="admin-search-bar">
                    <input id="inputBuscarReservaId" type="number" min="1" placeholder="Introduce el ID de la reserva" />
                </div>
                <div id="resultadoReserva"></div>
            `;
            break;

        case "reserva-eliminar":
            titulo.textContent = "Borrar reserva por ID";
            contenido.innerHTML = `
                <div class="admin-search-bar">
                    <input id="reservaIdBorrar" type="number" min="1" placeholder="ID de la reserva a borrar" />
                </div>
            `;
            break;

        case "reserva-editar":
            titulo.textContent = "Editar reserva por ID";
            contenido.innerHTML = `
                <div class="admin-search-bar">
                    <input id="reservaIdEditar" type="number" min="1" placeholder="ID de la reserva" />
                    <button class="btn-admin" onclick="cargarReservaParaEditar()">Cargar reserva</button>
                </div>
                <div id="formularioEdicionReserva"></div>
            `;
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
            case "reservas-cargar":
                renderizarReservasAdmin(data, document.getElementById("contenidoAccion"));
                break;
            case "reserva-buscar":
                renderReservaBuscada(data);
                break;
            case "reserva-eliminar":
                // fetchEliminarReserva gestiona confirmación, feedback y volverAtras() internamente
                break;
            case "reserva-editar":
                const reservaId = document.getElementById("reservaIdEditar")?.value.trim();
                if (reservaId) await confirmarEdicionReserva(reservaId);
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
    accionActual = "usuario-editar";

    document.getElementById("panelAcciones").style.display = "none";
    document.getElementById("panelAccion").style.display  = "block";

    document.getElementById("tituloAccion").textContent = "Editar usuario por ID";
    document.getElementById("contenidoAccion").innerHTML = `
        <div class="admin-search-bar">
            <input id="inputEditarUserId" type="number" min="1" placeholder="Introduce el ID del usuario" />
            <button class="btn-admin" onclick="cargarDatosParaEditar()">🔍 Cargar datos</button>
        </div>

        <div id="formEdicionAdmin" style="display:none;">
            <div class="perfil-card" style="margin-top:20px;">
                <h3 style="color:var(--azul-oscuro); margin-bottom:8px;">Modificar datos</h3>
                <p style="font-size:0.85rem; color:var(--azul-medio-oscuro); margin-bottom:18px;">
                    Rellena solo los campos que quieras cambiar.
                </p>
                <div class="admin-edit-form">
                    <label>Nombre</label>
                    <input id="edit-admin-nombre"    type="text"     placeholder="Nombre" />
                    <label>Apellidos</label>
                    <input id="edit-admin-apellidos" type="text"     placeholder="Apellidos" />
                    <label>Email</label>
                    <input id="edit-admin-email"     type="email"    placeholder="Email" />
                    <label>Teléfono</label>
                    <input id="edit-admin-telefono"  type="text"     placeholder="Teléfono" />
                    <label>Nueva contraseña <span style="font-weight:normal; font-size:0.82rem;">(opcional)</span></label>
                    <input id="edit-admin-password"  type="password" placeholder="Nueva contraseña" />
                    <label>Confirmar contraseña</label>
                    <input id="edit-admin-confirmar" type="password" placeholder="Confirmar contraseña" />
                </div>
                <button class="btn-admin" style="margin-top:20px;" onclick="guardarCambiosUsuario()">
                    💾 Guardar cambios
                </button>
            </div>
        </div>
    `;
}

async function cargarDatosParaEditar() {
    const userId = document.getElementById("inputEditarUserId")?.value?.trim();
    if (!userId) {
        mostrarMensaje("Introduce un ID de usuario", "error");
        return;
    }

    const token = localStorage.getItem("token");

    try {
        const res = await fetch(`${baseUrlAdmin}/pistaPadel/users/${userId}`, {
            headers: { "Authorization": "Bearer " + token }
        });

        if (res.status === 404) {
            mostrarMensaje(`No se encontró ningún usuario con ID ${userId}`, "error");
            return;
        }
        if (res.status === 403) {
            mostrarMensaje("No tienes permiso para editar este usuario", "error");
            return;
        }
        if (!res.ok) {
            mostrarMensaje("Error al cargar los datos del usuario", "error");
            return;
        }

        const user = await res.json();

        document.getElementById("edit-admin-nombre").value    = user.nombre    ?? "";
        document.getElementById("edit-admin-apellidos").value = user.apellidos ?? "";
        document.getElementById("edit-admin-email").value     = user.email     ?? "";
        document.getElementById("edit-admin-telefono").value  = user.telefono  ?? "";
        document.getElementById("edit-admin-password").value  = "";
        document.getElementById("edit-admin-confirmar").value = "";

        document.getElementById("formEdicionAdmin").style.display = "block";

    } catch (error) {
        mostrarMensaje("Error al conectar con el servidor", "error");
    }
}

async function guardarCambiosUsuario() {
    const userId = document.getElementById("inputEditarUserId")?.value?.trim();
    if (!userId) {
        mostrarMensaje("Introduce un ID de usuario", "error");
        return;
    }

    const nombre    = document.getElementById("edit-admin-nombre").value.trim();
    const apellidos = document.getElementById("edit-admin-apellidos").value.trim();
    const email     = document.getElementById("edit-admin-email").value.trim();
    const telefono  = document.getElementById("edit-admin-telefono").value.trim();
    const password  = document.getElementById("edit-admin-password").value.trim();
    const confirmar = document.getElementById("edit-admin-confirmar").value.trim();

    if (password && password !== confirmar) {
        mostrarMensaje("Las contraseñas no coinciden", "error");
        return;
    }

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

    const token = localStorage.getItem("token");

    try {
        const res = await fetch(`${baseUrlAdmin}/pistaPadel/users/${userId}`, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token
            },
            body: JSON.stringify(cambios)
        });

        if (res.status === 409) {
            mostrarMensaje("El email ya está en uso por otra cuenta", "error");
            return;
        }
        if (res.status === 404) {
            mostrarMensaje("Usuario no encontrado", "error");
            return;
        }
        if (!res.ok) {
            mostrarMensaje("Error al actualizar el usuario", "error");
            return;
        }

        mostrarMensaje("Usuario actualizado correctamente", "ok");
        document.getElementById("edit-admin-password").value  = "";
        document.getElementById("edit-admin-confirmar").value = "";
        setTimeout(() => cargarDatosParaEditar(), 1500);

    } catch (error) {
        mostrarMensaje("Error al conectar con el servidor", "error");
    }
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

// Alias para los onclick del HTML original
function cargarReservas()  { abrirPanel("reservas-cargar");  }
function cargarReservaId() { abrirPanel("reserva-buscar");   }
function borrarReservaId() { abrirPanel("reserva-eliminar"); }
function editarReservaId() { abrirPanel("reserva-editar");   }

// Funciones de fetch: devuelven datos o gestionan la operación.
// El renderizado lo hace ejecutarAccion() a través del switch.

async function fetchTodasReservas() {
    const token = localStorage.getItem("token");
    const rol   = localStorage.getItem("userRol");

    if (rol !== "ADMIN") {
        mostrarMensaje("Acceso restringido a administradores", "error");
        volverAtras();
        return;
    }

    const response = await fetch(baseUrlAdmin + "/pistaPadel/admin/reservations", {
        method: "GET",
        headers: { "Authorization": "Bearer " + token }
    });

    if (response.status === 401) throw new Error("401");
    if (response.status === 403) throw new Error("403");
    if (!response.ok) throw new Error("Error al cargar las reservas");

    return await response.json();
}

async function fetchReservaPorId() {
    const input     = document.getElementById("inputBuscarReservaId");
    const reservaId = input ? input.value.trim() : "";

    if (!reservaId) {
        mostrarMensaje("Introduce un ID de reserva", "error");
        return;
    }

    const token     = localStorage.getItem("token");
    const resultado = document.getElementById("resultadoReserva");
    resultado.innerHTML = "<p>Cargando...</p>";

    const response = await fetch(`${baseUrlAdmin}/pistaPadel/reservations/${reservaId}`, {
        headers: { "Authorization": "Bearer " + token }
    });

    if (response.status === 404) {
        resultado.innerHTML = `<p class="admin-error">No se encontró ninguna reserva con ID ${reservaId}.</p>`;
        return null;
    }
    if (response.status === 403) {
        resultado.innerHTML = `<p class="admin-error">No tienes permiso para ver esta reserva.</p>`;
        return null;
    }
    if (!response.ok) {
        resultado.innerHTML = `<p class="admin-error">Error al buscar la reserva.</p>`;
        return null;
    }

    return await response.json();
}

async function fetchEliminarReserva() {
    const reservaId = document.getElementById("reservaIdBorrar")?.value.trim();

    if (!reservaId) {
        mostrarMensaje("Introduce un ID de reserva", "error");
        return;
    }

    if (!confirm("¿Estás seguro de que deseas eliminar esta reserva?")) return;

    const token = localStorage.getItem("token");

    const response = await fetch(`${baseUrlAdmin}/pistaPadel/reservations/${reservaId}`, {
        method: "DELETE",
        headers: { "Authorization": "Bearer " + token }
    });

    if (response.status === 403) { mostrarMensaje("No tienes permiso para eliminar esta reserva", "error"); return; }
    if (response.status === 404) { mostrarMensaje("La reserva no existe", "error"); return; }
    if (!response.ok)            { mostrarMensaje("Error al eliminar la reserva", "error"); return; }

    mostrarMensaje("Reserva eliminada correctamente", "ok");
    volverAtras();
}

// No se invoca desde ejecutarAccion(); el flujo de edición va por
// cargarReservaParaEditar() + confirmarEdicionReserva().
// Se mantiene en accionesAdmin por coherencia estructural.
async function fetchEditarReserva() {
    return null;
}


// ========================
// RENDER DE RESERVAS
// ========================

function renderReservaBuscada(reserva) {
    const resultado = document.getElementById("resultadoReserva");
    if (!reserva) return;

    resultado.innerHTML = `
        <div class="perfil-card" style="margin-top:20px;">
            <h3 style="color:var(--azul-oscuro); margin-bottom:15px;">Detalles de la reserva</h3>
            <div class="perfil-info">
                <p><strong>ID Reserva:</strong> <span>${reserva.reservationId}</span></p>
                <p><strong>Usuario:</strong> <span>${reserva.usuario?.nombre ?? "—"} ${reserva.usuario?.apellidos ?? ""}</span></p>
                <p><strong>Pista:</strong> <span>${reserva.pista?.nombre ?? "Pista " + (reserva.pista?.id ?? "—")}</span></p>
                <p><strong>Fecha Inicio:</strong> <span>${formatearFechaAdmin(reserva.inicio)}</span></p>
                <p><strong>Fecha Fin:</strong> <span>${formatearFechaAdmin(reserva.fin)}</span></p>
                <p><strong>Duración:</strong> <span>${reserva.duracionMinutos} minutos</span></p>
                <p><strong>Estado:</strong> <span class="estado-${reserva.estado?.toLowerCase()}">${reserva.estado}</span></p>
                <p><strong>Fecha Creación:</strong> <span>${formatearFechaAdmin(reserva.fechaCreacion)}</span></p>
            </div>
        </div>
    `;
}

function renderizarReservasAdmin(reservas, contenedor) {
    if (!reservas || reservas.length === 0) {
        contenedor.innerHTML = `<p style="font-style:italic; color:var(--azul-medio-oscuro);">No hay reservas registradas.</p>`;
        return;
    }

    const filas = reservas.map(r => `
        <tr class="fila-reserva" data-fecha-inicio="${r.inicio.substring(0, 10)}">
            <td>${r.reservationId}</td>
            <td>${r.usuario?.nombre ?? "—"} ${r.usuario?.apellidos ?? ""}</td>
            <td>${r.pista?.nombre ?? "Pista " + (r.pista?.id ?? "—")}</td>
            <td>${r.inicio?.substring(0, 16).replace("T", " ")}</td>
            <td>${r.fin?.substring(0, 16).replace("T", " ")}</td>
            <td>${r.duracionMinutos} min</td>
            <td><span class="estado-${r.estado?.toLowerCase()}">${r.estado}</span></td>
        </tr>
    `).join("");

    contenedor.innerHTML = `
        <div class="filtro-fechas" style="margin-bottom: 20px;">
            <label for="filtroDesdeAdmin">Desde:</label>
            <input type="date" id="filtroDesdeAdmin" name="filtroDesdeAdmin">
            <label for="filtroHastaAdmin">Hasta:</label>
            <input type="date" id="filtroHastaAdmin" name="filtroHastaAdmin">
            <button class="btn-admin" onclick="aplicarFiltroReservasAdmin()">Filtrar</button>
            <button class="btn-admin" onclick="limpiarFiltroReservasAdmin()">Limpiar filtro</button>
        </div>
        <div class="tabla-reservas">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Usuario</th>
                        <th>Pista</th>
                        <th>Inicio</th>
                        <th>Fin</th>
                        <th>Duración</th>
                        <th>Estado</th>
                    </tr>
                </thead>
                <tbody id="cuerpoTablaReservasAdmin">${filas}</tbody>
            </table>
        </div>
    `;
}

function aplicarFiltroReservasAdmin() {
    const desde = document.getElementById("filtroDesdeAdmin")?.value;
    const hasta = document.getElementById("filtroHastaAdmin")?.value;

    if (!desde || !hasta) {
        mostrarMensaje("Por favor selecciona ambas fechas", "error");
        return;
    }

    const desdeDate = new Date(desde);
    const hastaDate = new Date(hasta);

    if (hastaDate <= desdeDate) {
        mostrarMensaje("La fecha 'hasta' debe ser posterior a 'desde'", "error");
        return;
    }

    const filas = document.querySelectorAll("#cuerpoTablaReservasAdmin .fila-reserva");
    let contadorVisibles = 0;

    filas.forEach(fila => {
        const fechaStr = fila.getAttribute("data-fecha-inicio");
        if (!fechaStr) return;

        const fecha = new Date(fechaStr);

        if (fecha >= desdeDate && fecha <= hastaDate) {
            fila.style.display = "";
            contadorVisibles++;
        } else {
            fila.style.display = "none";
        }
    });

    mostrarMensaje(`Filtro aplicado: ${contadorVisibles} reserva(s) mostrada(s)`, "ok");
}

function limpiarFiltroReservasAdmin() {
    document.getElementById("filtroDesdeAdmin").value = "";
    document.getElementById("filtroHastaAdmin").value = "";

    const filas = document.querySelectorAll("#cuerpoTablaReservasAdmin .fila-reserva");
    filas.forEach(fila => {
        fila.style.display = "";
    });

    mostrarMensaje("Filtro eliminado", "ok");
}


// ========================
// EDITAR RESERVA — flujo de dos pasos
// ========================

async function cargarReservaParaEditar() {
    const reservaId     = document.getElementById("reservaIdEditar")?.value.trim();
    const formularioDiv = document.getElementById("formularioEdicionReserva");

    if (!reservaId) {
        mostrarMensaje("Introduce un ID de reserva", "error");
        return;
    }

    const token = localStorage.getItem("token");
    formularioDiv.innerHTML = "<p>Cargando...</p>";

    try {
        const response = await fetch(`${baseUrlAdmin}/pistaPadel/reservations/${reservaId}`, {
            headers: { "Authorization": "Bearer " + token }
        });

        if (response.status === 404) { formularioDiv.innerHTML = `<p class="admin-error">No se encontró la reserva.</p>`; return; }
        if (!response.ok)            { formularioDiv.innerHTML = `<p class="admin-error">Error al cargar la reserva.</p>`; return; }

        const reserva = await response.json();

        formularioDiv.innerHTML = `
            <div style="margin-top: 15px; border: 1px solid var(--azul-claro); padding: 15px; border-radius: 5px;">
                <h4>Detalles actuales</h4>
                <p><strong>Usuario:</strong> ${reserva.usuario?.nombre ?? "—"}</p>
                <p><strong>Pista:</strong> ${reserva.pista?.nombre ?? "—"}</p>
                <p><strong>Inicio:</strong> ${formatearFechaAdmin(reserva.inicio)}</p>
                <p><strong>Fin:</strong> ${formatearFechaAdmin(reserva.fin)}</p>
                <p><strong>Estado:</strong> <span class="estado-${reserva.estado?.toLowerCase()}">${reserva.estado}</span></p>

                <hr style="margin: 15px 0;">

                <h4>Editar</h4>
                <label for="reservaNuevaFechaInicio">Nueva fecha inicio:</label>
                <input type="datetime-local" id="reservaNuevaFechaInicio" style="margin-bottom: 10px; padding: 8px; border: 1px solid var(--azul-claro); border-radius: 4px; font-size: 14px; width: 100%; box-sizing: border-box;">

                <label for="reservaNuevaFechaFin">Nueva fecha fin:</label>
                <input type="datetime-local" id="reservaNuevaFechaFin" style="margin-bottom: 10px; padding: 8px; border: 1px solid var(--azul-claro); border-radius: 4px; font-size: 14px; width: 100%; box-sizing: border-box;">

                <label for="reservaNuevoPista">Nueva pista ID:</label>
                <input type="number" id="reservaNuevoPista" placeholder="Opcional" style="margin-bottom: 10px; padding: 8px; border: 1px solid var(--azul-claro); border-radius: 4px; font-size: 14px; width: 100%; box-sizing: border-box;">
            </div>
        `;
    } catch (error) {
        formularioDiv.innerHTML = `<p class="admin-error">Error al conectar con el servidor.</p>`;
    }
}

async function confirmarEdicionReserva(reservaId) {
    const fechaInicio = document.getElementById("reservaNuevaFechaInicio")?.value;
    const fechaFin    = document.getElementById("reservaNuevaFechaFin")?.value;
    const pistaId     = document.getElementById("reservaNuevoPista")?.value;

    if (!fechaInicio && !fechaFin && !pistaId) {
        mostrarMensaje("Debes cambiar al menos un campo", "error");
        return;
    }

    const token = localStorage.getItem("token");
    const body  = {};

    if (fechaInicio) body.inicio = fechaInicio;
    if (fechaFin)    body.fin    = fechaFin;
    if (pistaId)     body.pista  = { id: parseInt(pistaId) };

    try {
        const response = await fetch(`${baseUrlAdmin}/pistaPadel/reservations/${reservaId}`, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token
            },
            body: JSON.stringify(body)
        });

        if (response.status === 400) { mostrarMensaje("Datos inválidos", "error"); return; }
        if (response.status === 409) { mostrarMensaje("La pista no está disponible en esas horas", "error"); return; }
        if (response.status === 403) { mostrarMensaje("No tienes permiso para editar esta reserva", "error"); return; }
        if (!response.ok)            { mostrarMensaje("Error al actualizar la reserva", "error"); return; }

        mostrarMensaje("Reserva actualizada correctamente", "ok");
        volverAtras();
    } catch (error) {
        console.error("Error:", error);
        mostrarMensaje("Error al conectar con el servidor", "error");
    }
}


// ========================
// UTILIDADES
// ========================

function formatearFechaAdmin(fechaString) {
    if (!fechaString) return "—";
    const fecha   = new Date(fechaString);
    const dia     = String(fecha.getDate()).padStart(2, "0");
    const mes     = String(fecha.getMonth() + 1).padStart(2, "0");
    const año     = fecha.getFullYear();
    const horas   = String(fecha.getHours()).padStart(2, "0");
    const minutos = String(fecha.getMinutes()).padStart(2, "0");
    return `${dia}/${mes}/${año} ${horas}:${minutos}`;
}


// ========================
// MENSAJES
// ========================

function mostrarMensaje(texto, tipo) {
    const div = document.createElement("div");
    div.textContent = texto;
    div.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        border-radius: 5px;
        z-index: 1000;
        font-weight: bold;
        ${tipo === "error" ? "background-color: #ffcccc; color: #cc0000;" : "background-color: #ccffcc; color: #009900;"}
    `;
    document.body.appendChild(div);

    setTimeout(() => {
        div.remove();
    }, 3000);
}

function cargarReservas() {
    console.log("Cargando reservas...");
}
