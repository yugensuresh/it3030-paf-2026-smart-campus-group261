/**
 * Notification bell + SSE for ticket activity (shared by index.html, ticket.html, etc.)
 */
(function () {
  function getUser() {
    try {
      const raw = localStorage.getItem("smartCampusUser");
      return raw ? JSON.parse(raw) : null;
    } catch (_) {
      return null;
    }
  }

  function authHeaders() {
    const u = getUser();
    return {
      "Content-Type": "application/json",
      "X-User-Email": u && u.email ? u.email : "system@campus.com",
      "X-User-Role": u && u.role ? u.role : "USER"
    };
  }

  function notifyHeaders() {
    const h = authHeaders();
    const u = getUser();
    if (u && u.userId) h["X-User-Id"] = String(u.userId);
    return h;
  }

  function escapeHtml(s) {
    if (s == null) return "";
    return String(s)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  function fmtDateTime(val) {
    if (val == null) return "—";
    if (typeof val === "string") {
      const d = new Date(val);
      return Number.isNaN(d.getTime()) ? val : d.toLocaleString();
    }
    if (Array.isArray(val) && val.length >= 5) {
      const y = val[0];
      const mo = val[1] - 1;
      const d = val[2];
      const h = val[3] || 0;
      const mi = val[4] || 0;
      const s = val[5] || 0;
      return new Date(y, mo, d, h, mi, s).toLocaleString();
    }
    return String(val);
  }

  let ticketEventSource = null;
  const liveTicketStreamEnabled = false;

  function setNotifyBadge(el, count) {
    if (!el) return;
    const n = Math.max(0, Number(count) || 0);
    if (n > 0) {
      el.textContent = n > 99 ? "99+" : String(n);
      el.hidden = false;
    } else {
      el.hidden = true;
    }
  }

  function renderActivityCard(d) {
    const id = d.ticketId != null ? d.ticketId : "—";
    const action = d.action || "Update";
    const note = d.note && String(d.note).trim() ? escapeHtml(d.note) : "—";
    return `<div class="notify-item" data-live="1">
            <div class="notify-item-title">Ticket #${escapeHtml(String(id))} · ${escapeHtml(action)}</div>
            <div class="notify-item-meta">Created: ${escapeHtml(fmtDateTime(d.ticketCreatedAt))}<br>Update: ${escapeHtml(fmtDateTime(d.eventAt))}</div>
            <div class="notify-item-note"><strong>Note:</strong> ${note}</div>
        </div>`;
  }

  function prependActivityFromStream(notifyList, d) {
    if (!notifyList) return;
    const u = getUser();
    if (u && u.role === "USER" && d.ticketOwnerUserId != null && Number(d.ticketOwnerUserId) !== Number(u.userId)) {
      return;
    }
    notifyList.insertAdjacentHTML("afterbegin", renderActivityCard(d));
    notifyList.querySelectorAll(".notify-item").forEach((el, i) => {
      if (i > 50) el.remove();
    });
  }

  async function loadNotificationBadge(notifyBadge) {
    const u = getUser();
    if (!u || !u.userId || !notifyBadge) return;
    try {
      const res = await fetch("/api/notifications/unread/count", { headers: notifyHeaders() });
      if (!res.ok) return;
      const data = await res.json();
      setNotifyBadge(notifyBadge, data.count != null ? data.count : 0);
    } catch (_) {}
  }

  async function loadNotificationListFromApi(notifyList) {
    const u = getUser();
    if (!notifyList || !u || !u.userId) return;
    try {
      const res = await fetch("/api/notifications?page=0&size=25", { headers: notifyHeaders() });
      if (!res.ok) return;
      const body = await res.json();
      const rows = Array.isArray(body.content) ? body.content : [];
      const html = rows
        .map((n) => {
          const title = escapeHtml(n.typeTitle || "Notification");
          const msg = escapeHtml(n.message || "");
          const when = fmtDateTime(n.createdAt);
          return `<div class="notify-item">
                    <div class="notify-item-title">${title}</div>
                    <div class="notify-item-meta">${when}</div>
                    <div class="notify-item-note">${msg}</div>
                </div>`;
        })
        .join("");
      notifyList.innerHTML =
        html ||
        '<div class="notify-empty" role="status">No notifications yet.</div>';
    } catch (_) {}
  }

  function renderNotifyListEmpty(notifyList) {
    if (!notifyList) return;
    notifyList.innerHTML =
      '<div class="notify-empty" role="status">No notifications yet.</div>';
  }

  async function clearAllNotifications(notifyList, notifyBadge) {
    const u = getUser();
    if (!u || !u.userId) return;
    if (!window.confirm("Delete all notifications? This cannot be undone.")) {
      return;
    }
    try {
      const res = await fetch("/api/notifications", {
        method: "DELETE",
        headers: notifyHeaders()
      });
      if (res.ok) {
        renderNotifyListEmpty(notifyList);
        setNotifyBadge(notifyBadge, 0);
      }
    } catch (_) {}
  }

  async function markAllNotificationsRead(notifyBadge) {
    const u = getUser();
    if (!u || !u.userId) return;
    try {
      const res = await fetch("/api/notifications/read-all", {
        method: "PATCH",
        headers: notifyHeaders()
      });
      if (res.ok) await loadNotificationBadge(notifyBadge);
    } catch (_) {}
  }

  function connectTicketStream(notifyList, notifyBadge) {
    if (!liveTicketStreamEnabled) return;
    const u = getUser();
    if (ticketEventSource) {
      ticketEventSource.close();
      ticketEventSource = null;
    }
    if (!u || !u.userId) return;
    let url =
      u.role === "ADMIN"
        ? "/api/tickets/stream?watchAll=true&userRole=ADMIN"
        : "/api/tickets/stream?forUserId=" + encodeURIComponent(u.userId);
    try {
      ticketEventSource = new EventSource(url);
    } catch (_) {
      return;
    }
    ticketEventSource.addEventListener("ticket-activity", (ev) => {
      try {
        const d = JSON.parse(ev.data);
        prependActivityFromStream(notifyList, d);
      } catch (_) {}
      setTimeout(() => loadNotificationBadge(notifyBadge), 250);
    });
    ticketEventSource.addEventListener("ticket-update", () => {
      setTimeout(() => loadNotificationBadge(notifyBadge), 250);
    });
  }

  let bellSetupDone = false;

  function setupNotifyBell() {
    if (bellSetupDone) return;
    const navNotifyWrap = document.getElementById("nav-notify-wrap");
    const notifyBell = document.getElementById("notify-bell");
    const notifyPanel = document.getElementById("notify-panel");
    const notifyList = document.getElementById("notify-list");
    const notifyBadge = document.getElementById("notify-badge");
    const notifyMarkRead = document.getElementById("notify-mark-read");
    const notifyClearAll = document.getElementById("notify-clear-all");

    if (!navNotifyWrap || !notifyBell || !notifyPanel) return;

    const u = getUser();
    if (!u || !u.userId) {
      navNotifyWrap.style.display = "none";
      return;
    }
    navNotifyWrap.style.display = "";

    notifyBell.addEventListener("click", (ev) => {
      ev.stopPropagation();
      const open = notifyPanel.hidden;
      notifyPanel.hidden = !open;
      notifyBell.setAttribute("aria-expanded", open ? "true" : "false");
      if (open) loadNotificationListFromApi(notifyList);
    });

    if (notifyMarkRead) {
      notifyMarkRead.addEventListener("click", (ev) => {
        ev.stopPropagation();
        markAllNotificationsRead(notifyBadge);
      });
    }

    if (notifyClearAll) {
      notifyClearAll.addEventListener("click", (ev) => {
        ev.stopPropagation();
        clearAllNotifications(notifyList, notifyBadge);
      });
    }

    document.addEventListener("click", () => {
      if (notifyPanel && !notifyPanel.hidden) {
        notifyPanel.hidden = true;
        notifyBell.setAttribute("aria-expanded", "false");
      }
    });
    notifyPanel.addEventListener("click", (ev) => ev.stopPropagation());

    loadNotificationBadge(notifyBadge);
    connectTicketStream(notifyList, notifyBadge);
    bellSetupDone = true;
  }

  function tryInitBell() {
    if (!document.getElementById("nav-notify-wrap")) return;
    setupNotifyBell();
  }

  document.addEventListener("siteheaderloaded", tryInitBell);
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", tryInitBell);
  } else {
    tryInitBell();
  }
})();
