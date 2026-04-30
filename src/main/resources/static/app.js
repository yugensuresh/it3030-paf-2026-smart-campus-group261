const API_BASE = "/api/resources";
const API_PAGED = `${API_BASE}/paged`;
const LIST_PAGE_SIZE = 500;
const BROWSE_ONLY = window.RESOURCE_PAGE_MODE === "browse";
const ADMIN_ADD = window.RESOURCE_ADMIN_PAGE === "add";
const ADMIN_LIST = window.RESOURCE_ADMIN_PAGE === "list";

let form, cancelEditBtn, tbody, statusText, formStatus, applyFiltersBtn, clearFiltersBtn;

function initializeElements() {
    form = document.getElementById("resource-form");
    cancelEditBtn = document.getElementById("cancel-edit");
    tbody = document.getElementById("resource-table-body");
    statusText = document.getElementById("status-text");
    formStatus = document.getElementById("form-status");
    applyFiltersBtn = document.getElementById("apply-filters");
    clearFiltersBtn = document.getElementById("clear-filters");
}

function setListStatus(msg) {
    if (statusText) {
        statusText.textContent = msg;
    }
}

function setFormStatus(msg) {
    if (formStatus) {
        formStatus.textContent = msg;
    }
}

function validatePageElements() {
    if (BROWSE_ONLY || ADMIN_LIST) {
        if (!tbody || !statusText || !applyFiltersBtn || !clearFiltersBtn) {
            console.error("Resource list page elements are missing.", {
                tbody: !!tbody,
                statusText: !!statusText,
                applyFiltersBtn: !!applyFiltersBtn,
                clearFiltersBtn: !!clearFiltersBtn
            });
            return false;
        }
    }
    if (ADMIN_ADD) {
        if (!form || !cancelEditBtn || !formStatus) {
            console.error("Admin add-resource form elements are missing.", {
                form: !!form,
                cancelEditBtn: !!cancelEditBtn,
                formStatus: !!formStatus
            });
            return false;
        }
    }
    return true;
}

const CATEGORY_LABELS = {
    STAFF_LEARNING: "Learning space (staff)",
    STUDENT_DISCUSSION: "Discussion space (students)",
    HALL_LAB: "Halls & labs",
    PROJECTOR_EQUIPMENT: "Projectors & equipment"
};

const TYPE_LABELS = {
    LECTURE_HALL: "Lecture hall",
    LAB: "Lab",
    MEETING_ROOM: "Meeting room",
    EQUIPMENT: "Equipment"
};

function formatTypeLabel(value) {
    if (!value) {
        return "—";
    }
    return TYPE_LABELS[value] || value;
}

function formatCategoryLabel(value) {
    if (!value) {
        return "—";
    }
    return CATEGORY_LABELS[value] || value;
}

function isResourceAdmin() {
    try {
        const raw = localStorage.getItem("smartCampusUser");
        if (!raw) {
            return false;
        }
        return JSON.parse(raw).role === "ADMIN";
    } catch {
        return false;
    }
}

function mergeMutationHeaders(base) {
    const api = window.SmartCampusResourceApi;
    if (api && typeof api.mutationHeaders === "function") {
        return { ...base, ...api.mutationHeaders() };
    }
    return { ...base };
}

async function fetchResources() {
    const params = buildFilterParams();
    params.set("page", "0");
    params.set("size", String(LIST_PAGE_SIZE));
    const url = `${API_PAGED}?${params.toString()}`;

    const response = await fetch(url);
    if (!response.ok) {
        throw new Error("Failed to fetch resources");
    }
    const data = await response.json();
    const resources = Array.isArray(data.content) ? data.content : [];
    const total = typeof data.totalItems === "number" ? data.totalItems : resources.length;
    renderResources(resources);
    if (resources.length < total) {
        setListStatus(`Showing ${resources.length} of ${total} resource(s) (increase page size if needed)`);
    } else {
        setListStatus(`Loaded ${resources.length} resource(s)`);
    }
}

function buildFilterParams() {
    const params = new URLSearchParams();
    if (BROWSE_ONLY) {
        params.append("status", "ACTIVE");
    }
    addParamIfPresent(params, "name", document.getElementById("filter-name").value);
    addParamIfPresent(params, "category", document.getElementById("filter-category").value);
    addParamIfPresent(params, "type", document.getElementById("filter-type").value);
    addParamIfPresent(params, "minCapacity", document.getElementById("filter-min-capacity").value);
    addParamIfPresent(params, "maxCapacity", document.getElementById("filter-max-capacity").value);
    addParamIfPresent(params, "hasWifi", document.getElementById("filter-wifi").value);
    addParamIfPresent(params, "hasAc", document.getElementById("filter-ac").value);
    return params;
}

function addParamIfPresent(params, key, value) {
    if (value !== null && value !== undefined && value !== "") {
        params.append(key, value);
    }
}

function escapeHtml(value) {
    return String(value == null ? "" : value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function renderResources(resources) {
    tbody.innerHTML = "";

    const showActions = !BROWSE_ONLY && isResourceAdmin();
    const actionsTh = document.getElementById("resource-actions-th");
    if (actionsTh) {
        actionsTh.style.display = showActions ? "" : "none";
    }

    resources.forEach((resource) => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${resource.id}</td>
            <td>${escapeHtml(resource.name)}</td>
            <td>${escapeHtml(formatTypeLabel(resource.type))}</td>
            <td>${escapeHtml(formatCategoryLabel(resource.category))}</td>
            <td>${escapeHtml(resource.location)}</td>
            <td>${resource.capacity}</td>
            <td>${(resource.hasWifi ?? resource.wifiAvailable) ? "Yes" : "No"}</td>
            <td>${(resource.hasAc ?? resource.acAvailable) ? "Yes" : "No"}</td>
            ${BROWSE_ONLY ? `<td class="resource-book-cell"><a class="resource-book-btn" href="/book.html?resourceId=${encodeURIComponent(resource.id)}">Book</a></td>` : ""}
        `;
        if (showActions) {
            row.insertAdjacentHTML("beforeend", `
                <td>
                    <button type="button" data-id="${resource.id}" class="edit-btn">Edit</button>
                    <button type="button" data-id="${resource.id}" class="delete-btn secondary">Delete</button>
                </td>
            `);
        }
        tbody.appendChild(row);
    });

    if (showActions) {
        wireRowButtons(resources);
    }
}

function wireRowButtons(resources) {
    document.querySelectorAll(".edit-btn").forEach((button) => {
        button.addEventListener("click", () => {
            const id = Number(button.dataset.id);
            if (ADMIN_LIST) {
                window.location.href = `/admin-add-resource.html?edit=${encodeURIComponent(id)}`;
                return;
            }
            const resource = resources.find((r) => r.id === id);
            if (resource) {
                fillFormForEdit(resource);
            }
        });
    });

    document.querySelectorAll(".delete-btn").forEach((button) => {
        button.addEventListener("click", async () => {
            const id = Number(button.dataset.id);
            if (!confirm("Delete this resource? Related bookings and maintenance tickets for this resource will also be removed.")) {
                return;
            }
            try {
                await deleteResource(id);
            } catch (error) {
                const msg = error && error.message ? error.message : "Delete failed";
                setListStatus(msg);
            }
        });
    });
}

function fillFormForEdit(resource) {
    if (!form) {
        return;
    }
    document.getElementById("resource-id").value = resource.id;
    document.getElementById("name").value = resource.name;
    document.getElementById("type").value = resource.type;
    const catSelect = document.getElementById("resource-category");
    if (catSelect) {
        catSelect.value = resource.category || "HALL_LAB";
    }
    document.getElementById("location").value = resource.location;
    document.getElementById("capacity").value = resource.capacity;
    const wifiEl = document.getElementById("hasWifi") || document.getElementById("wifiAvailable");
    const acEl = document.getElementById("hasAc") || document.getElementById("acAvailable");
    if (wifiEl) {
        wifiEl.value = String(resource.hasWifi ?? resource.wifiAvailable ?? false);
    }
    if (acEl) {
        acEl.value = String(resource.hasAc ?? resource.acAvailable ?? false);
    }
    const projectorEl = document.getElementById("hasProjector");
    if (projectorEl) {
        projectorEl.value = String(resource.hasProjector ?? false);
    }
    const statusEl = document.getElementById("status");
    if (statusEl) {
        statusEl.value = resource.status && ["ACTIVE", "OUT_OF_SERVICE", "MAINTENANCE"].includes(resource.status)
            ? resource.status
            : "ACTIVE";
    }
    const descEl = document.getElementById("description");
    if (descEl) {
        descEl.value = resource.description ?? "";
    }
    setFormStatus(`Editing resource #${resource.id}`);
}

function clearForm(options) {
    if (!form) {
        return;
    }
    const clearStatus = !options || options.clearStatus !== false;
    form.reset();
    const rid = document.getElementById("resource-id");
    if (rid) {
        rid.value = "";
    }
    if (clearStatus) {
        setFormStatus("");
    }
    if (window.history && window.history.replaceState && ADMIN_ADD) {
        window.history.replaceState(null, "", "/admin-add-resource.html");
    }
}

function getPayload() {
    const categoryEl = document.getElementById("resource-category");
    const wifiEl = document.getElementById("hasWifi") || document.getElementById("wifiAvailable");
    const acEl = document.getElementById("hasAc") || document.getElementById("acAvailable");
    const projectorEl = document.getElementById("hasProjector");
    const statusEl = document.getElementById("status");
    const descEl = document.getElementById("description");
    const descRaw = descEl ? descEl.value.trim() : "";
    return {
        name: document.getElementById("name").value.trim(),
        type: document.getElementById("type").value,
        category: categoryEl ? categoryEl.value : null,
        location: document.getElementById("location").value.trim(),
        capacity: Number(document.getElementById("capacity").value),
        description: descRaw === "" ? null : descRaw,
        hasWifi: wifiEl ? wifiEl.value === "true" : false,
        hasAc: acEl ? acEl.value === "true" : false,
        hasProjector: projectorEl ? projectorEl.value === "true" : false,
        status: statusEl ? statusEl.value : "ACTIVE"
    };
}

async function saveResource(event) {
    event.preventDefault();
    const id = document.getElementById("resource-id").value;
    const payload = getPayload();

    const isEdit = id !== "";
    const url = isEdit ? `${API_BASE}/${id}` : API_BASE;
    const method = isEdit ? "PUT" : "POST";

    const response = await fetch(url, {
        method,
        headers: mergeMutationHeaders({ "Content-Type": "application/json" }),
        body: JSON.stringify(payload)
    });

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Failed to save resource: ${errorText}`);
    }

    clearForm({ clearStatus: false });
    if (ADMIN_LIST || BROWSE_ONLY) {
        await fetchResources();
    } else {
        setFormStatus("Saved successfully. Open Show resources to view the list.");
    }
}

async function deleteResource(id) {
    const response = await fetch(`${API_BASE}/${id}`, {
        method: "DELETE",
        headers: mergeMutationHeaders({})
    });
    if (!response.ok) {
        let msg = "Failed to delete resource";
        const raw = await response.text();
        if (raw) {
            try {
                const err = JSON.parse(raw);
                if (err.message) {
                    msg = err.message;
                }
            } catch {
                msg = raw;
            }
        }
        throw new Error(msg);
    }
    await fetchResources();
}

async function tryLoadEditFromQuery() {
    if (!ADMIN_ADD || !form) {
        return;
    }
    const params = new URLSearchParams(window.location.search);
    const editId = params.get("edit");
    if (!editId) {
        return;
    }
    const response = await fetch(`${API_BASE}/${encodeURIComponent(editId)}`);
    if (!response.ok) {
        setFormStatus("Could not load resource to edit.");
        return;
    }
    const resource = await response.json();
    fillFormForEdit(resource);
}

function setupEventListeners() {
    if (form) {
        form.addEventListener("submit", async (event) => {
            if (!isResourceAdmin()) {
                event.preventDefault();
                setFormStatus("Only administrators can add or change resources.");
                return;
            }
            try {
                await saveResource(event);
            } catch (error) {
                setFormStatus(error.message);
            }
        });
    }

    if (cancelEditBtn) {
        cancelEditBtn.addEventListener("click", () => {
            clearForm();
            setFormStatus("Edit cancelled.");
        });
    }

    if (applyFiltersBtn) {
        applyFiltersBtn.addEventListener("click", async () => {
            try {
                await fetchResources();
            } catch (error) {
                setListStatus(error.message);
            }
        });
    }

    if (clearFiltersBtn) {
        clearFiltersBtn.addEventListener("click", async () => {
            clearFilters();
            document.querySelectorAll(".category-pick").forEach((b) => b.classList.remove("is-active"));
            try {
                await fetchResources();
            } catch (error) {
                setListStatus(error.message);
            }
        });
    }

    document.querySelectorAll(".category-pick").forEach((btn) => {
        btn.addEventListener("click", async () => {
            const filterCat = document.getElementById("filter-category");
            if (filterCat) {
                filterCat.value = btn.dataset.category ?? "";
            }
            document.querySelectorAll(".category-pick").forEach((b) => b.classList.remove("is-active"));
            btn.classList.add("is-active");
            try {
                await fetchResources();
            } catch (error) {
                setListStatus(error.message);
            }
        });
    });

    const filterCategoryEl = document.getElementById("filter-category");
    if (filterCategoryEl) {
        filterCategoryEl.addEventListener("change", async () => {
            const val = filterCategoryEl.value;
            document.querySelectorAll(".category-pick").forEach((b) => {
                b.classList.toggle("is-active", (b.dataset.category || "") === val);
            });
            try {
                await fetchResources();
            } catch (error) {
                setListStatus(error.message);
            }
        });
    }
}

window.addEventListener("load", async () => {
    try {
        initializeElements();
        if (!validatePageElements()) {
            return;
        }
        
        setupEventListeners();
        
        if (ADMIN_LIST || BROWSE_ONLY) {
            await fetchResources();
        }
        if (ADMIN_ADD) {
            await tryLoadEditFromQuery();
        }
    } catch (error) {
        if (statusText) {
            statusText.textContent = error.message;
        }
        if (formStatus) {
            formStatus.textContent = error.message;
        }
    }
});

function clearFilters() {
    const ff = document.getElementById("filter-form");
    if (ff) {
        ff.reset();
    }
}
