$(document).ready(function() {
    const currentPath = window.location.pathname;

    // Marcar enlace activo basado en la URL actual
    $('.nav-sidebar a.nav-link').each(function() {
        const href = $(this).attr('href');

        if (href && href !== '#' && currentPath.includes(href)) {
            $(this).addClass('active');

            // Si está dentro de un submenú, abrir el menú padre
            const $parentTreeview = $(this).closest('.nav-treeview');
            if ($parentTreeview.length) {
                $parentTreeview.show();
                const $parentItem = $parentTreeview.closest('.nav-item.has-treeview');
                $parentItem.addClass('menu-open');
            }
        }
    });

    // Manejar clics SOLO en enlaces con href="#" (los menús padre)
    $('.nav-item.has-treeview > a.nav-link[href="#"]').on('click', function(e) {
        e.preventDefault();
        e.stopPropagation();

        const $parentItem = $(this).parent('.nav-item.has-treeview');
        const $submenu = $(this).next('.nav-treeview');

        // Toggle del submenú
        if ($parentItem.hasClass('menu-open')) {
            $submenu.slideUp(300);
            $parentItem.removeClass('menu-open');
        } else {
            $submenu.slideDown(300);
            $parentItem.addClass('menu-open');
        }

        return false;
    });
});