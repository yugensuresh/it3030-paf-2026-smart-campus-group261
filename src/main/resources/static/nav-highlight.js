(function () {
    function normalizePath(pathname) {
        if (!pathname) {
            return "/";
        }
        let p = pathname.split("?")[0];
        if (p === "/index.html" || p.endsWith("/index.html")) {
            return "/";
        }
        p = p.replace(/\/$/, "");
        return p === "" ? "/" : p;
    }

    function pathsMatch(currentPath, href) {
        if (!href || href.startsWith("#")) {
            return false;
        }
        try {
            const u = new URL(href, window.location.origin);
            return normalizePath(u.pathname) === normalizePath(currentPath);
        } catch (_) {
            return false;
        }
    }

    function highlightTopNav() {
        const path = window.location.pathname;
        document.querySelectorAll("header.top-nav nav a[href]").forEach(function (a) {
            a.classList.remove("nav-is-active");
            const href = a.getAttribute("href");
            if (pathsMatch(path, href)) {
                a.classList.add("nav-is-active");
            }
        });
    }

    function highlightAdminSidebar() {
        const path = window.location.pathname;
        document.querySelectorAll(".admin-sidebar-nav a[href]").forEach(function (a) {
            a.classList.remove("is-active");
            const href = a.getAttribute("href");
            if (pathsMatch(path, href)) {
                a.classList.add("is-active");
                const details = a.closest(".admin-sidebar-details");
                if (details) {
                    details.open = true;
                    details.classList.add("has-active-child");
                }
            }
        });
    }

    function highlightAdminTopBar() {
        const path = window.location.pathname;
        document.querySelectorAll(".admin-top-bar a[href]").forEach(function (a) {
            a.classList.remove("nav-is-active");
            const href = a.getAttribute("href");
            if (pathsMatch(path, href)) {
                a.classList.add("nav-is-active");
            }
        });
    }

    function run() {
        highlightTopNav();
        highlightAdminSidebar();
        highlightAdminTopBar();
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", run);
    } else {
        run();
    }

    document.addEventListener("siteheaderloaded", run);

    window.addEventListener("load", function () {
        highlightTopNav();
        highlightAdminTopBar();
    });
})();
