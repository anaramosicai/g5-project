const BASE_URL = "http://localhost:8080/pistaPadel";

const token = localStorage.getItem("token");

function formatTime(t) {
    return t ? t.substring(0, 5) : '--:--';
}

async function mostrarResultados() {
    const fecha = document.getElementById('fechaConsulta').value;
    const pistaId = document.getElementById('pistaConsulta').value;

    if (!fecha) {
        alert('Por favor selecciona una fecha');
        return;
    }

    let url = `${BASE_URL}/availability?date=${fecha}`;
    if (pistaId) url += `&courtId=${pistaId}`;

    try {
        const res = await fetch(url, {
            headers: token ? { "Authorization": "Bearer " + token } : {}
        });
        if (!res.ok) throw new Error(res.status);
        const disponibilidades = await res.json();

        renderGrid(disponibilidades);
        document.getElementById('seccionTodasPistas').style.display = 'block';
        document.getElementById('seccionDetalle').style.display = 'none';
        document.getElementById('seccionTodasPistas').scrollIntoView({ behavior: 'smooth', block: 'start' });
    } catch (err) {
        console.error('Error al consultar disponibilidad:', err);
        alert('Error al consultar la disponibilidad. ¿Está arrancado el servidor?');
    }
}

function renderGrid(disponibilidades) {
    const grid = document.querySelector('#seccionTodasPistas .disponibilidad-grid');

    if (disponibilidades.length === 0) {
        grid.innerHTML = '<p style="text-align:center; color: var(--naranja); grid-column: 1/-1;">No hay datos de disponibilidad para esta fecha.</p>';
        return;
    }

    grid.innerHTML = disponibilidades.map(d => {
        const tieneFranjas = d.franjasLibres && d.franjasLibres.length > 0;
        const franjasHtml = tieneFranjas
            ? d.franjasLibres.map(f => `<span class="franja-tag">${formatTime(f.inicio)} – ${formatTime(f.fin)}</span>`).join('')
            : '<span class="sin-franjas">Sin franjas libres</span>';

        return `
            <div class="disponibilidad-card">
                <div class="disp-card-header">
                    <h3>${d.pista.nombre}</h3>
                    <span class="disp-estado ${tieneFranjas ? 'disp-disponible' : 'disp-ocupada'}">
                        ${tieneFranjas ? 'Disponible' : 'Ocupada'}
                    </span>
                </div>
                <div class="disp-horario">
                    <span>Apertura: <strong>${formatTime(d.apertura)}</strong></span>
                    <span>Cierre: <strong>${formatTime(d.cierre)}</strong></span>
                </div>
                <div class="disp-franjas">
                    <p class="franjas-titulo">Franjas libres:</p>
                    ${franjasHtml}
                </div>
                <button class="btn btn-filtro disp-btn-detalle" onclick="mostrarDetalle(${d.pista.id})">Ver detalle</button>
            </div>
        `;
    }).join('');
}

async function mostrarDetalle(pistaId) {
    const fecha = document.getElementById('fechaConsulta').value;
    if (!fecha) {
        alert('Por favor selecciona una fecha primero');
        return;
    }

    try {
        const res = await fetch(`${BASE_URL}/courts/${pistaId}/availability?date=${fecha}`, {
            headers: token ? { "Authorization": "Bearer " + token } : {}
        });
        if (!res.ok) throw new Error(res.status);
        const d = await res.json();

        const tieneFranjas = d.franjasLibres && d.franjasLibres.length > 0;

        document.getElementById('tituloDetalle').textContent = 'Detalle · ' + d.pista.nombre;
        document.getElementById('metaDetalle').innerHTML = `
            <span>Apertura: <strong>${formatTime(d.apertura)}</strong></span>
            <span>Cierre: <strong>${formatTime(d.cierre)}</strong></span>
            <span class="disp-estado ${tieneFranjas ? 'disp-disponible' : 'disp-ocupada'}">
                ${tieneFranjas ? 'Disponible' : 'Sin franjas libres'}
            </span>
        `;

        const tbody = document.getElementById('tablaDetalle');
        if (tieneFranjas) {
            tbody.innerHTML = d.franjasLibres.map(f => `
                <tr>
                    <td>${formatTime(f.inicio)}</td>
                    <td>${formatTime(f.fin)}</td>
                    <td><span class="estado-activa">Libre</span></td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = '<tr><td colspan="3" style="text-align:center; font-style:italic; color: var(--naranja);">No hay franjas libres para este día</td></tr>';
        }

        document.getElementById('seccionDetalle').style.display = 'block';
        document.getElementById('seccionDetalle').scrollIntoView({ behavior: 'smooth', block: 'start' });
    } catch (err) {
        console.error('Error al obtener detalle:', err);
        alert('No se pudo obtener el detalle de la pista.');
    }
}

function cerrarDetalle() {
    document.getElementById('seccionDetalle').style.display = 'none';
}
