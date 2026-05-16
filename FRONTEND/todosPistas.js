const BASE_URL = "http://localhost:8080/pistaPadel/courts";

const token = localStorage.getItem("token");
const userRol = localStorage.getItem("userRol");

async function seeAllCourts() {
     document.getElementById("courtDetails").innerHTML = "";
    try {
        const response = await fetch(BASE_URL, {
            method: "GET",
            headers: {
                "Accept": "application/json",
                "Authorization": `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error("Failed to fetch courts");
        }

        const courts = await response.json();

        displayCourts(courts);

    } catch (error) {
        console.error("Error fetching courts:", error);
    }
}

function displayCourts(courts) {
    const container = document.getElementById("courtsContainer");

    if (!container) {
        console.error("Missing #courtsContainer in HTML");
        return;
    }

    container.innerHTML = "";

    courts.forEach(court => {
        const card = document.createElement("div");
        card.className = "court-card";

        card.innerHTML = `
            <h3>${court.nombre}</h3>
            <p> ${court.ubicacion}</p>
            <p> ${court.precioHora} €/h</p>
            <p> ${court.activa ? "Active" : "Inactive"}</p>
        `;

        container.appendChild(card);
    });
}

async function seeDetails() {
    const id = document.getElementById("allPistasDropdown").value;

    if (!id || id === "new") {
        alert("Please select a court first");
        return;
    }

    document.getElementById("courtsContainer").innerHTML = "";
    try {
        const response = await fetch(`${BASE_URL}/${id}`, {
            method: "GET",
            headers: {
                "Accept": "application/json",
                "Authorization": `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error("Failed to fetch court details");
        }

        const court = await response.json();

        displayCourtDetails(court);

    } catch (error) {
        console.error("Error fetching details:", error);
        alert("Could not load court details");
    }
}

function displayCourtDetails(court) {
    const container = document.getElementById("courtDetails");

    container.innerHTML = `
        <h2>Detalles de la pista</h2>

        <p><strong>ID:</strong> ${court.id}</p>
        <p><strong>Nombre:</strong> ${court.nombre}</p>
        <p><strong>Ubicación:</strong> ${court.ubicacion}</p>
        <p><strong>Precio/hora:</strong> ${court.precioHora} €</p>
        <p><strong>Activa:</strong> ${court.activa ? "Sí" : "No"}</p>
        <p><strong>Fecha alta:</strong> ${court.fechaAlta ? court.fechaAlta.split("T")[0] : ""}</p>
    `;
}

window.addEventListener("DOMContentLoaded", () => {
    loadCourtsForView("allPistasDropdown");
});


async function loadCourtsForView(selectId) {
  try {
    const res = await fetch(BASE_URL, {
      headers: {
        "Authorization": `Bearer ${token}`
      }
    });

    if (!res.ok) {
      throw new Error(res.status);
    }

    const courts = await res.json();

    const select = document.getElementById(selectId);

    if (!select) {
      console.error("Select not found:", selectId);
      return;
    }

    select.innerHTML = `<option value="">Selecciona una pista</option>`;

    courts.forEach(court => {
      const option = document.createElement("option");
      option.value = court.id;
      option.textContent = `${court.nombre} - ${court.ubicacion}`;
      select.appendChild(option);
    });

  } catch (err) {
    console.error("Error loading courts:", err);
  }
}
