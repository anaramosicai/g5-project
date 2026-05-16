const BASE_URL = "http://localhost:8080/pistaPadel/courts";

// --------------------
// AUTH (SAFE VERSION)
// --------------------

const token = localStorage.getItem("token");
const userRol = localStorage.getItem("userRol");

const isAdmin = userRol === "ADMIN";

// DEBUG
console.log("TOKEN:", token);
console.log("ROLE:", userRol);
console.log("IS ADMIN:", isAdmin);
// --------------------
// INIT
// --------------------
window.onload = () => {

  if (!token) {
    alert("Please log in first");
    window.location.href = "login.html";
    return;
  }

  if (userRol !== "ADMIN") {
    alert("Access denied: Admin only");
    window.location.href = "index.html";
    return;
  }

  loadCourts();
  setupEvents();
}; 
// --------------------
// EVENT LISTENERS
// --------------------
function setupEvents() {
  document.getElementById("ID").addEventListener("change", onCourtSelect);
  document.querySelector(".pista-button").addEventListener("click", saveCourt);
}
let selectedCourtId = null;
let courtsData = [];
// --------------------
// LOAD COURTS INTO DROPDOWN
// --------------------
async function loadCourts() {
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
    courtsData = courts;

    const select = document.getElementById("ID");

    select.innerHTML = `<option value="">Selecciona una pista</option>
    <option value="new"> Crear nueva pista</option>`;

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
// --------------------
// WHEN USER SELECTS A COURT
// --------------------
async function onCourtSelect(e) {

  const id = e.target.value;

  console.log("SELECTED ID:", id);

  // --------------------
  // CREATE NEW COURT MODE
  // --------------------
  if (id === "new") {
    selectedCourtId = null;
    clearForm();
    return;
  }

  // --------------------
  // NO SELECTION
  // --------------------
  if (!id) {
    selectedCourtId = null;
    clearForm();
    return;
  }

  try {

    // --------------------
    // FETCH COURT DATA
    // --------------------
    const res = await fetch(`${BASE_URL}/${id}`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${token}`
      }
    });

    if (!res.ok) {
      throw new Error(`HTTP ERROR: ${res.status}`);
    }

    const court = await res.json();

    console.log("COURT DATA:", court);

    // --------------------
    // SAVE SELECTED ID
    // --------------------
    selectedCourtId = id;

    // --------------------
    // FILL FORM
    // --------------------
    document.getElementById("nombre").value =
      court.nombre || "";

    document.getElementById("ubicacion").value =
      court.ubicacion || "";

    document.getElementById("precio").value =
      court.precioHora || "";

    document.getElementById("activa").value =
      court.activa ? "true" : "false";

    document.getElementById("date").value =
      court.fechaAlta
        ? court.fechaAlta.split("T")[0]
        : "";

  } catch (err) {

    console.error("Error loading court:", err);

    alert("Could not load court data");
  }
}
// --------------------
// CREATE OR UPDATE COURT
// --------------------
async function saveCourt() {

  const nombre = document.getElementById("nombre").value.trim();

  const data = {
    nombre,
    ubicacion: document.getElementById("ubicacion").value,
    precioHora: parseFloat(document.getElementById("precio").value),
    activa: document.getElementById("activa").value === "true",
    fechaAlta: document.getElementById("date").value
  };

  if (!selectedCourtId) {

  const alreadyExists = courtsData.some(court =>
    court.nombre &&
    court.nombre.toLowerCase() === nombre.toLowerCase()
  );

  if (alreadyExists) {
    alert("Already a court with this name. Choose another name for the court :)");
    return;
  }
}

  try {

    if (!selectedCourtId) {
      // CREATE
      await fetch(BASE_URL, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify(data)
      });

      alert("Court created!");

    } else {
      // UPDATE
      await fetch(`${BASE_URL}/${selectedCourtId}`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify(data)
      });

      alert("Court updated!");
    }

    clearForm();
    loadCourts();

  } catch (err) {
    console.error("Save error:", err);
  }
}

// --------------------
// CLEAR FORM
// --------------------
function clearForm() {
  document.getElementById("ubicacion").value = "";
  document.getElementById("precio").value = "";
  document.getElementById("activa").value = "";
  document.getElementById("date").value = "";
  document.getElementById("ID").value = "";
}

// --------------------
// OPTIONAL: DELETE COURT
// --------------------
window.deleteCourt = async function () {

  if (!selectedCourtId) {
    alert("Select a court first");
    return;
  }

  if (!confirm("Delete this court?")) return;

  try {
    await fetch(`${BASE_URL}/${selectedCourtId}`, {
      method: "DELETE",
      headers: {
        "Authorization": `Bearer ${token}`
      }
    });

    alert("Court deleted!");

    clearForm();
    loadCourts();

  } catch (err) {
    console.error("Delete error:", err);
  }
};

document.addEventListener("DOMContentLoaded", () => {

    const role = localStorage.getItem("userRol");
    const adminBtn = document.getElementById("adminBtn");

    if (role === "ADMIN" && adminBtn) {
        adminBtn.style.display = "inline-block";
    }
});

