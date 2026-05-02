const baseUrl = "http://localhost:8080";

document.addEventListener("DOMContentLoaded", function () {

    const pistaId = document.getElementById("ID");

    pistaId.addEventListener("change", function () {
        cargarPista(pistaId.value);
    });
});

function cargarPista(courtId) {

    fetch(`${baseUrl}/pistaPadel/courts/${courtId}`, {
        method: "GET"
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Error al obtener pista");
        }
        return response.json();
    })
    .then(data => {
        console.log(data);

        document.getElementById("ubicación").value = data.ubicacion;
        document.getElementById("precio").value = data.precioPorHora;
        document.getElementById("activa").value = data.activa;
        document.getElementById("date").value = data.fechaAlta;
    })
    .catch(error => {
        console.error(error);
    });
}