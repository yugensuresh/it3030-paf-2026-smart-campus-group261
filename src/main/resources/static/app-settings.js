(function () {
    const SETTINGS_KEY = "smartCampusSettings";
    const defaults = {
        theme: "light",
        fontSize: "medium",
        fontStyle: "inter"
    };

    const themeTokens = {
        dark: {
            "--blue": "#3b82f6",
            "--blue2": "#2563eb",
            "--blue3": "#1e40af",
            "--blue4": "#1e3a8a",
            "--light-blue": "#dbeafe",
            "--white": "#ffffff",
            "--gray": "#6b7280",
            "--light-gray": "#f3f4f6",
            "--border": "rgba(59, 130, 246, 0.2)",
            "--glow": "rgba(59, 130, 246, 0.1)"
        },
        light: {
            "--blue": "#1e40af",
            "--blue2": "#2563eb",
            "--blue3": "#3b82f6",
            "--blue4": "#60a5fa",
            "--light-blue": "#eff6ff",
            "--white": "#ffffff",
            "--gray": "#4b5563",
            "--light-gray": "#f9fafb",
            "--border": "rgba(59, 130, 246, 0.15)",
            "--glow": "rgba(59, 130, 246, 0.08)"
        }
    };

    const fontSizeMap = {
        small: "14px",
        medium: "16px",
        large: "18px"
    };

    const fontStyleMap = {
        dmSans:
            "\"DM Sans\", ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, \"Segoe UI\", \"Helvetica Neue\", Arial, sans-serif",
        inter:
            "\"Inter\", ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, \"Segoe UI\", \"Helvetica Neue\", Arial, sans-serif",
        serif: "Georgia, \"Times New Roman\", serif",
        system:
            "ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, \"Segoe UI\", \"Helvetica Neue\", Arial, sans-serif"
    };

    function detectApplePlatform() {
        if (typeof navigator === "undefined") {
            return false;
        }
        const platform = navigator.userAgentData && navigator.userAgentData.platform
            ? navigator.userAgentData.platform
            : navigator.platform || "";
        const ua = navigator.userAgent || "";
        return /Mac|iPhone|iPad|iPod/i.test(platform) || /Mac OS X/.test(ua);
    }

    window.SmartCampusPlatform = {
        detectApplePlatform,
        get isApplePlatform() {
            return detectApplePlatform();
        },
        /** Modifier label for shortcuts: "Cmd" on Apple platforms, "Ctrl" elsewhere. */
        modKeyLabel() {
            return detectApplePlatform() ? "Cmd" : "Ctrl";
        },
        /** Symbol-style modifier for compact hints (⌘ vs Ctrl). */
        modSymbol() {
            return detectApplePlatform() ? "⌘" : "Ctrl";
        },
        /**
         * @param {string[]} keys Key names as shown to users, e.g. ["S"] or ["Shift", "P"]
         * @returns {string} e.g. "Ctrl+S" or "⌘+S"
         */
        formatShortcut(keys) {
            const mod = detectApplePlatform() ? "⌘" : "Ctrl";
            const parts = Array.isArray(keys) ? keys.filter(Boolean) : [];
            return [mod, ...parts].join("+");
        }
    };

    function loadSettings() {
        try {
            const raw = localStorage.getItem(SETTINGS_KEY);
            if (!raw) return { ...defaults };
            return { ...defaults, ...JSON.parse(raw) };
        } catch (_) {
            return { ...defaults };
        }
    }

    function saveSettings(nextSettings) {
        const normalized = { ...defaults, ...nextSettings };
        localStorage.setItem(SETTINGS_KEY, JSON.stringify(normalized));
        applySettings(normalized);
        return normalized;
    }

    function applySettings(settings) {
        const root = document.documentElement;
        const safeSettings = { ...defaults, ...settings };
        const theme = themeTokens[safeSettings.theme] ? safeSettings.theme : defaults.theme;
        const themeVars = themeTokens[theme];

        Object.keys(themeVars).forEach((token) => {
            root.style.setProperty(token, themeVars[token]);
        });

        root.style.setProperty("--ff-body", fontStyleMap[safeSettings.fontStyle] || fontStyleMap[defaults.fontStyle]);
        root.style.setProperty("--app-font-size", fontSizeMap[safeSettings.fontSize] || fontSizeMap[defaults.fontSize]);
        root.setAttribute("data-theme", theme);
    }

    const initialSettings = loadSettings();
    applySettings(initialSettings);

    window.SmartCampusSettings = {
        defaults,
        load: loadSettings,
        save: saveSettings,
        apply: applySettings,
        key: SETTINGS_KEY
    };

    const RESOURCE_ADMIN_BASIC_USER = "admin";
    const RESOURCE_ADMIN_BASIC_PASSWORD = "123456";
    const PORTAL_ADMIN_EMAIL = "admin@gmail.com";
    const PORTAL_ADMIN_PASSWORD = "12345";
    const PORTAL_ADMIN_FULL_NAME = "System Admin";

    window.SmartCampusResourceApi = {
        adminUsername: RESOURCE_ADMIN_BASIC_USER,
        adminPassword: RESOURCE_ADMIN_BASIC_PASSWORD,
        portalAdminEmail: PORTAL_ADMIN_EMAIL,
        portalAdminPassword: PORTAL_ADMIN_PASSWORD,
        portalAdminFullName: PORTAL_ADMIN_FULL_NAME,
        rememberAdminCredentials() {
            localStorage.setItem(
                "smartCampusResourceApiAuth",
                btoa(`${RESOURCE_ADMIN_BASIC_USER}:${RESOURCE_ADMIN_BASIC_PASSWORD}`)
            );
        },
        clearCredentials() {
            localStorage.removeItem("smartCampusResourceApiAuth");
        },
        mutationHeaders() {
            const headers = {};
            const token = localStorage.getItem("smartCampusResourceApiAuth");
            if (token) {
                headers.Authorization = `Basic ${token}`;
            }
            return headers;
        }
    };

    (function redirectAdminToDashboardOnly() {
        try {
            const raw = localStorage.getItem("smartCampusUser");
            if (!raw) {
                return;
            }
            const user = JSON.parse(raw);
            if (user.role !== "ADMIN") {
                return;
            }
            const path = (window.location.pathname || "").split("?")[0];
            const base = path.replace(/\/$/, "") || "/";
            const onUserOnlyPage =
                base === "/" ||
                base.endsWith("/index.html") ||
                base.endsWith("/login.html");
            if (onUserOnlyPage) {
                window.location.replace("/admin.html");
            }
        } catch (_) {
        }
    })();
})();
