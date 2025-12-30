$(document).ready(function () {
    // 1. Obtener la ruta actual (ej: /listarclientes)
    var url = window.location.pathname;

    // 2. Limpiar cualquier clase 'active' o 'menu-open' previa
    $('.nav-sidebar .nav-link').removeClass('active');
    $('.nav-sidebar .nav-item').removeClass('menu-open');

    // 3. Buscar el enlace que coincida con la URL actual
    $('.nav-sidebar a').filter(function () {
        // Retorna true si el href coincide exactamente con la URL
        // o si la URL empieza con el href (para resaltar /clientes/editar cuando el href es /clientes)
        return this.getAttribute("href") === url || (url.startsWith(this.getAttribute("href")) && this.getAttribute("href") !== "/");
    }).addClass('active')
        .parentsUntil(".nav-sidebar", ".nav-item")
        .addClass('menu-open')
        .find('> .nav-link')
        .addClass('active');

    // 4. Caso especial para el Home (evitar que siempre esté activo)
    if (url === '/Home' || url === '/') {
        $('.nav-sidebar a[href*="Home"]').addClass('active');
    }
});