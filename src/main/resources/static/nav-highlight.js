(function () {
  var path = window.location.pathname;
  document.querySelectorAll("[data-nav-key='resources']").forEach(function (link) {
    if (path === "/resource.html" || path === "/book.html") {
      link.classList.add("is-active");
    }
  });
})();
