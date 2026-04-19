/**
 * Injects shared header from /header.html and applies auth visibility for nav links.
 */
(function () {
  async function injectSiteHeader() {
    var mount = document.getElementById("site-header-mount");
    if (!mount) return;
    try {
      var res = await fetch("/header.html");
      if (!res.ok) return;
      var html = await res.text();
      mount.insertAdjacentHTML("beforebegin", html);
      mount.remove();
    } catch (e) {
      return;
    }
    initSiteHeaderAuth();
    document.dispatchEvent(new Event("siteheaderloaded"));
  }

  function initSiteHeaderAuth() {
    var accountLink = document.getElementById("account-link");
    var adminNavLink = document.getElementById("admin-nav-link");
    if (!accountLink) return;

    var userRaw = localStorage.getItem("smartCampusUser");
    if (userRaw) {
      try {
        var user = JSON.parse(userRaw);
        var avatarSrc = user.profileImageData || "https://via.placeholder.com/28?text=P";
        accountLink.innerHTML =
          '<img class="nav-cta-avatar" src="' +
          avatarSrc +
          '" alt="" width="24" height="24" decoding="async">My Profile';
        accountLink.href = "/profile.html";
        if (adminNavLink && user.role !== "ADMIN") {
          adminNavLink.style.display = "none";
        }
      } catch (_) {
        localStorage.removeItem("smartCampusUser");
        if (window.SmartCampusResourceApi && typeof window.SmartCampusResourceApi.clearCredentials === "function") {
          window.SmartCampusResourceApi.clearCredentials();
        }
        if (adminNavLink) adminNavLink.style.display = "none";
      }
    } else {
      if (adminNavLink) adminNavLink.style.display = "none";
    }
  }

  injectSiteHeader();
})();
