(function () {
  window.SmartCampusApp = {
    apiBaseUrl: "",
    authStorageKey: "smartCampusUser"
  };

  window.SmartCampusResourceApi = {
    clearCredentials: function () {
      try {
        localStorage.removeItem("smartCampusUser");
      } catch (_) {}
    }
  };
})();
