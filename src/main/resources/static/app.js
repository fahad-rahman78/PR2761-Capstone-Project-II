/* =====================================================================
   app.js - frontend logic. Plain (vanilla) JavaScript, no framework.
   It talks to the Spring Boot REST API using fetch() and updates the
   page. Read top-to-bottom: state -> helpers -> load functions -> wire-up.
   ===================================================================== */

const API = "/api"; // the backend serves this page, so a relative path works

// --- simple in-memory state ---
const state = {
    resources: [],
    users: [],
    selectedResource: null,
    selectedDate: todayISO(),
    currentUserId: null,
};

// --- small helpers ---------------------------------------------------

function todayISO() {
    return new Date().toISOString().slice(0, 10); // "YYYY-MM-DD"
}

// Show a brief message at the bottom of the screen.
function toast(message, type = "") {
    const el = document.getElementById("toast");
    el.textContent = message;
    el.className = "toast show " + type;
    setTimeout(() => { el.className = "toast " + type; }, 2800);
}

// Turn "2026-06-15T14:00:00" into "14:00".
function timeLabel(iso) {
    return iso.slice(11, 16);
}

// Wrapper around fetch that throws on non-2xx so we can catch conflicts etc.
async function api(path, options = {}) {
    const res = await fetch(API + path, {
        headers: { "Content-Type": "application/json" },
        ...options,
    });
    if (!res.ok) {
        let body = {};
        try { body = await res.json(); } catch (e) { /* no JSON body */ }
        const err = new Error(body.message || ("Request failed: " + res.status));
        err.status = res.status;
        throw err;
    }
    return res.status === 204 ? null : res.json();
}

// --- load functions --------------------------------------------------

async function loadUsers() {
    state.users = await api("/users");
    const select = document.getElementById("userSelect");
    select.innerHTML = "";
    state.users.forEach(u => {
        const opt = document.createElement("option");
        opt.value = u.userId;
        opt.textContent = u.name;
        select.appendChild(opt);
    });
    if (state.users.length) {
        state.currentUserId = state.users[0].userId;
    }
}

async function loadResources() {
    state.resources = await api("/resources");
    const list = document.getElementById("resourceList");
    list.innerHTML = "";

    state.resources.forEach(r => {
        const card = document.createElement("button");
        card.className = "resource-card";
        card.innerHTML = `
            <h3>${r.name}</h3>
            <div class="type">${r.type.replace("_", " ")}</div>
            <span class="cap">capacity ${r.capacity}</span>`;
        card.addEventListener("click", () => selectResource(r, card));
        list.appendChild(card);
    });
}

function selectResource(resource, cardEl) {
    state.selectedResource = resource;
    document.querySelectorAll(".resource-card")
        .forEach(c => c.classList.remove("selected"));
    cardEl.classList.add("selected");
    loadSlots();
}

async function loadSlots() {
    if (!state.selectedResource) return;
    const area = document.getElementById("slotArea");
    area.innerHTML = `<p class="muted">Loading slots&hellip;</p>`;

    const id = state.selectedResource.resourceId;
    const slots = await api(`/resources/${id}/slots?date=${state.selectedDate}`);

    const grid = document.createElement("div");
    grid.className = "slot-grid";

    slots.forEach(s => {
        const btn = document.createElement("button");
        btn.className = "slot " + (s.available ? "available" : "full");
        const stateText = s.available
            ? `available (${s.booked}/${s.capacity})`
            : `full (${s.booked}/${s.capacity})`;
        btn.innerHTML = `
            <div class="time">${timeLabel(s.startTime)} - ${timeLabel(s.endTime)}</div>
            <div class="state">${stateText}</div>`;
        if (s.available) {
            btn.addEventListener("click", () => book(s));
        } else {
            btn.disabled = true;
        }
        grid.appendChild(btn);
    });

    area.innerHTML = "";
    area.appendChild(grid);
}

async function book(slot) {
    if (!state.currentUserId) {
        toast("Pick a user first", "error");
        return;
    }
    try {
        await api("/bookings", {
            method: "POST",
            body: JSON.stringify({
                userId: Number(state.currentUserId),
                resourceId: state.selectedResource.resourceId,
                startTime: slot.startTime,
                endTime: slot.endTime,
            }),
        });
        toast("Booked " + timeLabel(slot.startTime) + " \u2713", "success");
        loadSlots();        // refresh availability
        loadMyBookings();   // refresh the list below
    } catch (err) {
        if (err.status === 409) {
            toast("That slot was just taken - try another", "error");
            loadSlots(); // someone else got it; refresh
        } else {
            toast(err.message, "error");
        }
    }
}

async function loadMyBookings() {
    if (!state.currentUserId) return;
    const list = document.getElementById("bookingList");
    const bookings = await api(`/users/${state.currentUserId}/bookings`);

    if (!bookings.length) {
        list.innerHTML = `<p class="muted">No bookings yet.</p>`;
        return;
    }

    list.innerHTML = "";
    bookings.forEach(b => {
        const resource = state.resources.find(r => r.resourceId === b.resourceId);
        const name = resource ? resource.name : "Resource " + b.resourceId;
        const row = document.createElement("div");
        row.className = "booking-row";
        const date = b.startTime.slice(0, 10);
        const badgeClass = b.status === "CONFIRMED" ? "confirmed" : "cancelled";
        row.innerHTML = `
            <div class="info">
                <strong>${name}</strong>
                <span>${date} &middot; ${timeLabel(b.startTime)} - ${timeLabel(b.endTime)}</span>
            </div>
            <div class="row-actions">
                <span class="badge ${badgeClass}">${b.status}</span>
            </div>`;
        if (b.status === "CONFIRMED") {
            const btn = document.createElement("button");
            btn.className = "cancel-btn";
            btn.textContent = "Cancel";
            btn.addEventListener("click", () => cancelBooking(b.bookingId));
            row.querySelector(".row-actions").appendChild(btn);
        }
        list.appendChild(row);
    });
}

async function cancelBooking(bookingId) {
    try {
        await api("/bookings/" + bookingId, { method: "DELETE" });
        toast("Booking cancelled", "success");
        loadSlots();
        loadMyBookings();
    } catch (err) {
        toast(err.message, "error");
    }
}

// --- wire up the page ------------------------------------------------

document.addEventListener("DOMContentLoaded", async () => {
    const dateInput = document.getElementById("dateInput");
    dateInput.value = state.selectedDate;
    dateInput.addEventListener("change", e => {
        state.selectedDate = e.target.value;
        loadSlots();
    });

    document.getElementById("userSelect").addEventListener("change", e => {
        state.currentUserId = e.target.value;
        loadMyBookings();
    });

    try {
        await loadUsers();
        await loadResources();
        await loadMyBookings();
    } catch (err) {
        toast("Could not reach the server. Is the backend running?", "error");
    }
});
