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
    var navBookingLink = document.getElementById("nav-booking-link");
    if (!accountLink) return;

    // For demo purposes, create a test user if none exists
    if (!localStorage.getItem("smartCampusUser")) {
      var testUser = {
        id: 1,
        email: "admin@gmail.com",
        name: "System Admin",
        role: "ADMIN",
        profileImageData: null
      };
      localStorage.setItem("smartCampusUser", JSON.stringify(testUser));
    }

    var userRaw = localStorage.getItem("smartCampusUser");
    if (userRaw) {
      try {
        var user = JSON.parse(userRaw);
        // Use user's initial for avatar if no profile image
        var userInitial = user.name ? user.name.charAt(0).toUpperCase() : "U";
        var avatarSrc = user.profileImageData || "https://via.placeholder.com/28?text=" + userInitial;
        accountLink.innerHTML =
          '<img class="nav-cta-avatar" src="' +
          avatarSrc +
          '" alt="" width="24" height="24" decoding="async">My Profile';
        accountLink.href = "/profile.html";
        if (adminNavLink && user.role !== "ADMIN") {
          adminNavLink.style.display = "none";
        }
        if (navBookingLink) {
          // Show booking link for all users including admins
          navBookingLink.style.display = "";
        }
      } catch (_) {
        localStorage.removeItem("smartCampusUser");
        if (window.SmartCampusResourceApi && typeof window.SmartCampusResourceApi.clearCredentials === "function") {
          window.SmartCampusResourceApi.clearCredentials();
        }
        if (adminNavLink) adminNavLink.style.display = "none";
        if (navBookingLink) navBookingLink.style.display = "none";
      }
    } else {
      // For demo purposes, show a default profile icon even when not logged in
      // This ensures the profile icon is always visible for testing
      accountLink.innerHTML =
        '<img class="nav-cta-avatar" src="https://via.placeholder.com/28?text=U" alt="" width="24" height="24" decoding="async">Profile';
      accountLink.href = "/profile.html";
      
      if (adminNavLink) adminNavLink.style.display = "none";
      if (navBookingLink) navBookingLink.style.display = "none";
    }
  }

  injectSiteHeader();
})();
