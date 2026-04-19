(function () {
  function parseBool(value) {
    if (value === "") return null;
    if (value === "true") return true;
    if (value === "false") return false;
    return null;
  }

  function categoryFromResource(resource) {
    const type = resource && resource.type ? String(resource.type) : "";
    switch (type) {
      case "MEETING_ROOM":
        return "STAFF_LEARNING";
      case "LECTURE_HALL":
      case "LAB":
        return "HALL_LAB";
      case "EQUIPMENT":
        return "PROJECTOR_EQUIPMENT";
      default:
        return "STUDENT_DISCUSSION";
    }
  }

  function categoryLabel(category) {
    switch (category) {
      case "STAFF_LEARNING":
        return "Learning space";
      case "STUDENT_DISCUSSION":
        return "Discussion space";
      case "HALL_LAB":
        return "Halls & labs";
      case "PROJECTOR_EQUIPMENT":
        return "Projectors & equipment";
      default:
        return "General";
    }
  }

  function typeLabel(type) {
    return type ? String(type).replace(/_/g, " ") : "-";
  }

  function yesNo(value) {
    return value ? "Yes" : "No";
  }

  function getFilterValues() {
    return {
      name: document.getElementById("filter-name")?.value.trim() || "",
      category: document.getElementById("filter-category")?.value || "",
      type: document.getElementById("filter-type")?.value || "",
      minCapacity: document.getElementById("filter-min-capacity")?.value || "",
      maxCapacity: document.getElementById("filter-max-capacity")?.value || "",
      hasWifi: parseBool(document.getElementById("filter-wifi")?.value || ""),
      hasAc: parseBool(document.getElementById("filter-ac")?.value || "")
    };
  }

  function applyFilters(resources) {
    const filters = getFilterValues();
    return resources.filter((resource) => {
      if ((resource.status || "ACTIVE") !== "ACTIVE") return false;
      if (filters.name && !(resource.name || "").toLowerCase().includes(filters.name.toLowerCase())) return false;
      if (filters.category && categoryFromResource(resource) !== filters.category) return false;
      if (filters.type && resource.type !== filters.type) return false;
      if (filters.minCapacity && Number(resource.capacity || 0) < Number(filters.minCapacity)) return false;
      if (filters.maxCapacity && Number(resource.capacity || 0) > Number(filters.maxCapacity)) return false;
      if (filters.hasWifi !== null && Boolean(resource.hasWifi) !== filters.hasWifi) return false;
      if (filters.hasAc !== null && Boolean(resource.hasAc) !== filters.hasAc) return false;
      return true;
    });
  }

  function renderRows(resources) {
    const tbody = document.getElementById("resource-table-body");
    const statusText = document.getElementById("status-text");
    if (!tbody) return;

    if (!resources.length) {
      tbody.innerHTML = '<tr><td colspan="9">No matching resources found.</td></tr>';
      if (statusText) statusText.textContent = "No resources matched your current filters.";
      return;
    }

    tbody.innerHTML = resources.map((resource) => {
      const bookHref = "/book.html?resourceId=" + encodeURIComponent(String(resource.id));
      return `<tr>
        <td>${resource.id ?? "-"}</td>
        <td>${resource.name ?? "-"}</td>
        <td>${typeLabel(resource.type)}</td>
        <td>${categoryLabel(categoryFromResource(resource))}</td>
        <td>${resource.location ?? "-"}</td>
        <td>${resource.capacity ?? "-"}</td>
        <td>${yesNo(resource.hasWifi)}</td>
        <td>${yesNo(resource.hasAc)}</td>
        <td><a href="${bookHref}">Book</a></td>
      </tr>`;
    }).join("");

    if (statusText) statusText.textContent = resources.length + " resource(s) available.";
  }

  async function loadResources() {
    const statusText = document.getElementById("status-text");
    try {
      if (statusText) statusText.textContent = "Loading resources...";
      const response = await fetch("/api/resources");
      if (!response.ok) throw new Error("Failed to fetch resources");
      const resources = await response.json();
      window.__smartCampusResources = Array.isArray(resources) ? resources : [];
      renderRows(applyFilters(window.__smartCampusResources));
    } catch (_) {
      if (statusText) statusText.textContent = "Could not load resources right now.";
    }
  }

  function rerender() {
    const resources = Array.isArray(window.__smartCampusResources) ? window.__smartCampusResources : [];
    renderRows(applyFilters(resources));
  }

  document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll(".category-pick").forEach((button) => {
      button.addEventListener("click", function () {
        const categorySelect = document.getElementById("filter-category");
        if (categorySelect) categorySelect.value = button.dataset.category || "";
        rerender();
      });
    });

    const applyButton = document.getElementById("apply-filters");
    if (applyButton) applyButton.addEventListener("click", rerender);

    const clearButton = document.getElementById("clear-filters");
    if (clearButton) {
      clearButton.addEventListener("click", function () {
        const form = document.getElementById("filter-form");
        if (form) form.reset();
        rerender();
      });
    }

    loadResources();
  });
})();
