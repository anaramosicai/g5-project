/* ======= JAVASCRIPT DE RESERVAS ======= */

const baseUrl = "http://localhost:8080";

// Referencias a elementos del DOM
const formCrearReserva = document.getElementById("crearReservaForm");
const formReprogramar = document.getElementById("reprogramarReservaForm");
const seccionReprogramar = document.getElementById("seccionReprogramar");
const seccionTodasReservas = document.getElementById("seccionTodasReservas");
const btnCerrarReprogramar = document.getElementById("btnCerrarReprogramar");
const misReservasTabla = document.getElementById("misReservasTabla");
const todasReservasTabla = document.getElementById("todasReservasTabla");

// Estado para guardar la reserva que se está editando
let reservaEnEdicion = null;

// ========================
// CARGAR AL INICIAR
// ========================

document.addEventListener("DOMContentLoaded", function () {
    cargarPistasEnSelect();
    cargarMisReservas();
});

// ========================
// CARGAR PISTAS EN SELECT
// ========================

async function cargarPistasEnSelect() {
    const selectPista = document.getElementById("pista");
    const selectPistaReprogramar = document.getElementById("pistaReprogramar");

    try {
        const response = await fetch(baseUrl + "/pistaPadel/courts", {
            method: "GET"
        });

        if (!response.ok) {
            console.error("Error al obtener pistas");
            return;
        }

        const pistas = await response.json();

        // Limpiar opciones excepto la primera
        while (selectPista.options.length > 1) {
            selectPista.remove(1);
        }
        if (selectPistaReprogramar) {
            while (selectPistaReprogramar.options.length > 1) {
                selectPistaReprogramar.remove(1);
            }
        }

        // Agregar pistas
        pistas.forEach(pista => {
            const option = document.createElement("option");
            option.value = pista.id;
            option.textContent = `Pista ${pista.numero} - ${pista.ubicacion}`;
            selectPista.appendChild(option);

            if (selectPistaReprogramar) {
                const optionReprogramar = document.createElement("option");
                optionReprogramar.value = pista.id;
                optionReprogramar.textContent = `Pista ${pista.numero} - ${pista.ubicacion}`;
                selectPistaReprogramar.appendChild(optionReprogramar);
            }
        });
    } catch (error) {
        console.error("Error:", error);
    }
}

// ========================
// CARGAR PISTAS AL INICIAR
// ========================

document.addEventListener("DOMContentLoaded", function () {
    cargarPistasEnSelect();
    cargarMisReservas();
});

// ========================
// CARGAR PISTAS EN SELECT
// ========================

async function cargarPistasEnSelect() {
    const selectPista = document.getElementById("pista");
    const selectPistaReprogramar = document.getElementById("pistaReprogramar");

    try {
        const response = await fetch(baseUrl + "/pistaPadel/courts", {
            method: "GET"
        });

        if (!response.ok) {
            console.error("Error al obtener pistas");
            return;
        }

        const pistas = await response.json();

        // Limpiar opciones excepto la primera
        while (selectPista.options.length > 1) {
            selectPista.remove(1);
        }
        if (selectPistaReprogramar) {
            while (selectPistaReprogramar.options.length > 1) {
                selectPistaReprogramar.remove(1);
            }
        }

        // Agregar pistas
        pistas.forEach(pista => {
            const option = document.createElement("option");
            option.value = pista.id;
            option.textContent = `Pista ${pista.numero} - ${pista.ubicacion}`;
            selectPista.appendChild(option);

            if (selectPistaReprogramar) {
                const optionReprogramar = document.createElement("option");
                optionReprogramar.value = pista.id;
                optionReprogramar.textContent = `Pista ${pista.numero} - ${pista.ubicacion}`;
                selectPistaReprogramar.appendChild(optionReprogramar);
            }
        });
    } catch (error) {
        console.error("Error:", error);
    }
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

// ========================
// VERIFICAR AUTENTICACIÓN
// ========================

function verificarAutenticacion() {
    const token = localStorage.getItem("token");
    if (!token) {
        window.location.href = "login.html";
        return false;
    }
    return token;
}

// ========================
// CARGAR TODAS MIS RESERVAS
// ========================

async function cargarMisReservas() {
    const token = verificarAutenticacion();
    if (!token) return;

    try {
        const response = await fetch(baseUrl + "/pistaPadel/reservations", {
            method: "GET",
            headers: { "Authorization": "Bearer " + token }
        });

        if (!response.ok) {
            mostrarMensaje("Error al cargar tus reservas", "error");
            return;
        }

        const reservas = await response.json();
        mostrarReservasEnTabla(reservas, misReservasTabla, false);
        configurarFiltrosReservas();
    } catch (error) {
        console.error("Error:", error);
        mostrarMensaje("Error al conectar con el servidor", "error");
    }
}

// ========================
// CARGAR TODAS LAS RESERVAS (ADMIN)
// ========================

async function cargarTodasReservas() {
    const token = verificarAutenticacion();
    if (!token) return;

    const userRol = localStorage.getItem("userRol");
    if (userRol !== "ADMIN") {
        mostrarMensaje("Solo administradores pueden ver todas las reservas", "error");
        return;
    }

    try {
        const response = await fetch(baseUrl + "/pistaPadel/admin/reservations", {
            method: "GET",
            headers: { "Authorization": "Bearer " + token }
        });

        if (!response.ok) {
            mostrarMensaje("Error al cargar las reservas", "error");
            return;
        }

        const reservas = await response.json();
        mostrarReservasEnTabla(reservas, todasReservasTabla, true);
        seccionTodasReservas.style.display = "block";
        configurarFiltrosReservas();
    } catch (error) {
        console.error("Error:", error);
        mostrarMensaje("Error al conectar con el servidor", "error");
    }
}

// ========================
// MOSTRAR RESERVAS EN TABLA
// ========================

function mostrarReservasEnTabla(reservas, tbody, mostrarUsuario = false) {
    tbody.innerHTML = "";

    if (reservas.length === 0) {
        const fila = tbody.insertRow();
        const celda = fila.insertCell();
        celda.colSpan = mostrarUsuario ? 8 : 7;
        celda.textContent = "No hay reservas";
        celda.style.textAlign = "center";
        return;
    }

    reservas.forEach(reserva => {
        console.log("Reserva cargada:", reserva); // DEBUG: Ver qué propiedades tiene el objeto
        
        const fila = tbody.insertRow();

        // ID
        fila.insertCell().textContent = reserva.reservationId;

        // Usuario (solo si es admin)
        if (mostrarUsuario) {
            fila.insertCell().textContent = reserva.usuario ? reserva.usuario.nombre : "Desconocido";
        }

        // Pista
        fila.insertCell().textContent = `${reserva.pista.nombre}`;

        // Inicio
        fila.insertCell().textContent = formatearFecha(reserva.inicio);

        // Fin
        fila.insertCell().textContent = formatearFecha(reserva.fin);

        // Duración
        const duracion = calcularDuracion(reserva.inicio, reserva.fin);
        fila.insertCell().textContent = duracion + " min";

        // Estado
        const celdaEstado = fila.insertCell();
        const spanEstado = document.createElement("span");
        spanEstado.className = reserva.estado === "ACTIVA" ? "estado-activa" : "estado-cancelada";
        spanEstado.textContent = reserva.estado;
        celdaEstado.appendChild(spanEstado);

        // Acciones
        const celdaAcciones = fila.insertCell();
        
        if (reserva.estado === "ACTIVA") {
            const btnEditar = document.createElement("button");
            btnEditar.textContent = "Editar";
            btnEditar.className = "btn btn-editar";
            btnEditar.setAttribute("data-id", reserva.reservationId);
            btnEditar.onclick = () => {
                console.log("Editando reserva con ID:", reserva.reservationId); // DEBUG
                abrirModalEditar(reserva, reserva.reservationId);
            };
            celdaAcciones.appendChild(btnEditar);

            const btnCancelar = document.createElement("button");
            btnCancelar.textContent = "Cancelar";
            btnCancelar.className = "btn btn-cancelar";
            btnCancelar.setAttribute("data-id", reserva.reservationId);
            btnCancelar.onclick = () => {
                console.log("Cancelando reserva con ID:", reserva.reservationId); // DEBUG
                cancelarReserva(reserva.reservationId);
            };
            celdaAcciones.appendChild(btnCancelar);
        }
    });
}

// ========================
// FORMATEAR FECHA
// ========================

function formatearFecha(fechaString) {
    const fecha = new Date(fechaString);
    const dia = String(fecha.getDate()).padStart(2, "0");
    const mes = String(fecha.getMonth() + 1).padStart(2, "0");
    const año = fecha.getFullYear();
    const horas = String(fecha.getHours()).padStart(2, "0");
    const minutos = String(fecha.getMinutes()).padStart(2, "0");
    return `${dia}/${mes}/${año} ${horas}:${minutos}`;
}

// ========================
// CALCULAR DURACIÓN
// ========================

function calcularDuracion(horaInicio, horaFin) {
    const inicio = new Date(horaInicio);
    const fin = new Date(horaFin);
    const duracion = (fin - inicio) / (1000 * 60); // en minutos
    return Math.round(duracion);
}

// ========================
// CREAR NUEVA RESERVA
// ========================

if (formCrearReserva) {
    formCrearReserva.addEventListener("submit", async function (event) {
        event.preventDefault();

        const token = verificarAutenticacion();
        if (!token) return;

        const pistaId = document.getElementById("pista").value;
        const fechaInicio = document.getElementById("fechaInicio").value;
        const fechaFin = document.getElementById("fechaFin").value;

        if (!fechaInicio || !fechaFin) {
            mostrarMensaje("Por favor completa todos los campos de fecha", "error");
            return;
        }

        // Validar que la fecha de fin sea posterior a la de inicio
        const inicio = new Date(fechaInicio);
        const fin = new Date(fechaFin);
        if (fin <= inicio) {
            mostrarMensaje("La fecha de fin debe ser posterior a la de inicio", "error");
            return;
        }

        try {
            const response = await fetch(baseUrl + "/pistaPadel/reservations", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + token
                },

                body: JSON.stringify({
                    pista: pistaId ? { id: parseInt(pistaId) } : null,
                    inicio: fechaInicio,
                    fin: fechaFin
                })
            });

            if (response.status === 400) {
                mostrarMensaje("Datos inválidos", "error");
                return;
            }

            if (response.status === 409) {
                mostrarMensaje("La pista no está disponible en esas horas", "error");
                return;
            }

            if (!response.ok) {
                mostrarMensaje("Error al crear la reserva", "error");
                return;
            }

            mostrarMensaje("Reserva creada correctamente", "ok");
            formCrearReserva.reset();
            cargarMisReservas();
        } catch (error) {
            console.error("Error:", error);
            mostrarMensaje("Error al conectar con el servidor", "error");
        }
    });
}

// ========================
// ABRIR MODAL DE EDICIÓN
// ========================

function abrirModalEditar(reserva, reservaId) {
    // Usar el ID explícitamente pasado si la propiedad reservationId no está disponible
    const idActual = reservaId || reserva.reservationId;
    reservaEnEdicion = reserva;
    
    // También guardar el ID por separado para asegurarnos
    if (!reservaEnEdicion.reservationId && idActual) {
        reservaEnEdicion.reservationId = idActual;
    }

    // Rellenar los campos del formulario
    document.getElementById("reservaIdReprogramar").value = idActual;
    document.getElementById("pistaReprogramar").value = reserva.pista.id;
    document.getElementById("fechaInicioReprogramar").value = convertirADatetimeLocal(reserva.inicio);
    document.getElementById("fechaFinReprogramar").value = convertirADatetimeLocal(reserva.fin);

    // Mostrar la sección de reprogramación
    seccionReprogramar.style.display = "block";

    // Scroll hacia el modal
    seccionReprogramar.scrollIntoView({ behavior: "smooth" });
}

// ========================
// CONVERTIR A DATETIME-LOCAL
// ========================

function convertirADatetimeLocal(fechaString) {
    const fecha = new Date(fechaString);
    const año = fecha.getFullYear();
    const mes = String(fecha.getMonth() + 1).padStart(2, "0");
    const dia = String(fecha.getDate()).padStart(2, "0");
    const horas = String(fecha.getHours()).padStart(2, "0");
    const minutos = String(fecha.getMinutes()).padStart(2, "0");
    return `${año}-${mes}-${dia}T${horas}:${minutos}`;
}

// ========================
// CERRAR MODAL DE EDICIÓN
// ========================

if (btnCerrarReprogramar) {
    btnCerrarReprogramar.addEventListener("click", function () {
        seccionReprogramar.style.display = "none";
        reservaEnEdicion = null;
        formReprogramar.reset();
    });
}

// ========================
// REPROGRAMAR RESERVA
// ========================

if (formReprogramar) {
    formReprogramar.addEventListener("submit", async function (event) {
        event.preventDefault();

        const token = verificarAutenticacion();
        if (!token) return;

        if (!reservaEnEdicion) {
            mostrarMensaje("Error: no se encontró la reserva a editar", "error");
            return;
        }

        const pistaId = document.getElementById("pistaReprogramar").value;
        const fechaInicio = document.getElementById("fechaInicioReprogramar").value;
        const fechaFin = document.getElementById("fechaFinReprogramar").value;

        if (!fechaInicio || !fechaFin) {
            mostrarMensaje("Por favor completa todos los campos", "error");
            return;
        }

        const inicio = new Date(fechaInicio);
        const fin = new Date(fechaFin);
        if (fin <= inicio) {
            mostrarMensaje("La fecha de fin debe ser posterior a la de inicio", "error");
            return;
        }

        // Obtener el ID desde el campo del formulario o desde reservaEnEdicion
        const reservaId = document.getElementById("reservaIdReprogramar").value || reservaEnEdicion.reservationId;
        
        if (!reservaId) {
            mostrarMensaje("Error: no se pudo obtener el ID de la reserva", "error");
            return;
        }

        try {
            const response = await fetch(baseUrl + "/pistaPadel/reservations/" + reservaId, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + token
                },

                body: JSON.stringify({
                    pista: pistaId ? { id: parseInt(pistaId) } : null,
                    inicio: fechaInicio,
                    fin: fechaFin
                })
            });

            if (response.status === 400) {
                mostrarMensaje("Datos inválidos", "error");
                return;
            }

            if (response.status === 409) {
                mostrarMensaje("La pista no está disponible en esas horas", "error");
                return;
            }

            if (response.status === 403) {
                mostrarMensaje("No tienes permiso para editar esta reserva", "error");
                return;
            }

            if (!response.ok) {
                mostrarMensaje("Error al actualizar la reserva", "error");
                return;
            }

            mostrarMensaje("Reserva actualizada correctamente", "ok");
            seccionReprogramar.style.display = "none";
            reservaEnEdicion = null;
            formReprogramar.reset();
            cargarMisReservas();
        } catch (error) {
            console.error("Error:", error);
            mostrarMensaje("Error al conectar con el servidor", "error");
        }
    });
}

// ========================
// CANCELAR RESERVA
// ========================

async function cancelarReserva(reservaId) {
    // Validar que se haya pasado un ID válido
    if (!reservaId || reservaId === undefined) {
        mostrarMensaje("Error: no se pudo obtener el ID de la reserva", "error");
        return;
    }

    if (!confirm("¿Estás seguro de que deseas cancelar esta reserva?")) {
        return;
    }

    const token = verificarAutenticacion();
    if (!token) return;

    try {
        const response = await fetch(baseUrl + "/pistaPadel/reservations/" + reservaId, {
            method: "DELETE",
            headers: { "Authorization": "Bearer " + token }
        });

        if (response.status === 403) {
            mostrarMensaje("No tienes permiso para cancelar esta reserva", "error");
            return;
        }

        if (response.status === 404) {
            mostrarMensaje("La reserva no existe", "error");
            return;
        }

        if (!response.ok) {
            mostrarMensaje("Error al cancelar la reserva", "error");
            return;
        }

        mostrarMensaje("Reserva cancelada correctamente", "ok");
        cargarMisReservas();
    } catch (error) {
        console.error("Error:", error);
        mostrarMensaje("Error al conectar con el servidor", "error");
    }
}

// ========================
// FILTROS (FUNCIONAL)
// ========================

function configurarFiltrosReservas() {
    // Buscar secciones por sus características
    const secciones = document.querySelectorAll(".reserva-section");
    
    secciones.forEach((seccion) => {
        const btnFiltro = seccion.querySelector("button.btn-filtro");
        const filtroDesdeFin = seccion.querySelector("input[id^='filtroDesde']");
        const tbody = seccion.querySelector("tbody");
        
        if (btnFiltro && filtroDesdeFin && tbody) {
            // Determinar qué tabla es y qué IDs de filtro usar
            const filtrosIds = filtroDesdeFin.id; // "filtroDesde" o "filtroDesdeAdmin"
            const tablaIds = tbody.id; // "misReservasTabla" o "todasReservasTabla"
            
            btnFiltro.onclick = function() {
                if (filtrosIds === "filtroDesde") {
                    filtrarReservasPorFecha(tbody, "filtroDesde", "filtroHasta");
                } else if (filtrosIds === "filtroDesdeAdmin") {
                    filtrarReservasPorFecha(tbody, "filtroDesdeAdmin", "filtroHastaAdmin");
                }
            };
        }
    });
}

function filtrarReservasPorFecha(tabla, idDesde, idHasta) {
    const desde = document.getElementById(idDesde)?.value;
    const hasta = document.getElementById(idHasta)?.value;
    
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

    const filas = tabla.querySelectorAll("tr");
    let filasVisibles = 0;

    filas.forEach(fila => {
        if (fila.cells.length < 3) return; // fila vacía o de "no hay reservas"

        // Detectar en qué columna está la fecha de inicio
        let fechaInicioTexto = "";

        if (fila.cells.length >= 8) { 
            // Es tabla de ADMIN (más columnas)
            fechaInicioTexto = fila.cells[3]?.textContent;
        } else {
            // Tabla normal de usuario
            fechaInicioTexto = fila.cells[2]?.textContent;
        }

        if (!fechaInicioTexto) return;

        // Parsear fecha DD/MM/YYYY HH:MM
        const partes = fechaInicioTexto.split(" ");
        const fechaParts = partes[0].split("/");
        const fecha = new Date(fechaParts[2], fechaParts[1] - 1, fechaParts[0]);

        if (fecha >= desdeDate && fecha <= hastaDate) {
            fila.style.display = "";
            filasVisibles++;
        } else {
            fila.style.display = "none";
        }
    });

    if (filasVisibles === 0) {
        mostrarMensaje("No hay reservas en ese rango de fechas", "ok");
    } else {
        mostrarMensaje(`Filtro aplicado: ${filasVisibles} reserva(s) mostrada(s)`, "ok");
    }
}

// ========================
// CARGAR RESERVAS AL INICIAR
// ========================

document.addEventListener("DOMContentLoaded", function () {
    const token = localStorage.getItem("token");
    if (token) {
        cargarMisReservas();
        // Opcional: cargar todas las reservas si es admin
        const userRol = localStorage.getItem("userRol");
        if (userRol === "ADMIN") {
            cargarTodasReservas();
        }
    }
});
