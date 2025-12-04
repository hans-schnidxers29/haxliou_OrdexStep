/**
 * ============================================
 * JAVASCRIPT DEL SISTEMA - SIDEBAR
 * Archivo: sidebar-script.js
 * ============================================
 */

/**
 * Toggle del Sidebar (Abrir/Cerrar)
 */
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const mainContent = document.getElementById('mainContent');

    if (!sidebar || !mainContent) {
        console.error('No se encontraron los elementos sidebar o mainContent');
        return;
    }

    sidebar.classList.toggle('closed');
    mainContent.classList.toggle('expanded');

    // Guardar estado en localStorage
    const isClosed = sidebar.classList.contains('closed');
    localStorage.setItem('sidebarClosed', isClosed);
}

/**
 * Restaurar estado del sidebar al cargar la página
 */
function restoreSidebarState() {
    const sidebar = document.getElementById('sidebar');
    const mainContent = document.getElementById('mainContent');

    if (!sidebar || !mainContent) return;

    const sidebarClosed = localStorage.getItem('sidebarClosed') === 'true';

    if (sidebarClosed) {
        sidebar.classList.add('closed');
        mainContent.classList.add('expanded');
    }
}

/**
 * Resaltar el enlace activo según la ruta actual
 */
function highlightActiveLink() {
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.nav-link');

    navLinks.forEach(link => {
        link.classList.remove('active');

        const linkPath = link.getAttribute('href');

        // Si la ruta coincide exactamente o si estamos en una subruta
        if (linkPath === currentPath ||
            (linkPath !== '/' && currentPath.startsWith(linkPath))) {
            link.classList.add('active');
        }
    });
}

/**
 * Confirmación antes de eliminar
 * @param {string} mensaje - Mensaje personalizado
 * @returns {boolean}
 */
function confirmarEliminacion(mensaje = '¿Estás seguro de que deseas eliminar este elemento?') {
    return confirm(mensaje);
}

/**
 * Mostrar mensaje de carga
 */
function mostrarCargando() {
    const loader = document.createElement('div');
    loader.id = 'loader';
    loader.innerHTML = `
        <div style="position: fixed; top: 0; left: 0; width: 100%; height: 100%; 
                    background: rgba(0,0,0,0.5); display: flex; align-items: center; 
                    justify-content: center; z-index: 9999;">
            <div style="background: white; padding: 20px; border-radius: 10px; 
                        text-align: center;">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Cargando...</span>
                </div>
                <p class="mt-2 mb-0">Cargando...</p>
            </div>
        </div>
    `;
    document.body.appendChild(loader);
}

/**
 * Ocultar mensaje de carga
 */
function ocultarCargando() {
    const loader = document.getElementById('loader');
    if (loader) {
        loader.remove();
    }
}

/**
 * Auto-cerrar alertas después de un tiempo
 * @param {number} tiempo - Tiempo en milisegundos (por defecto 5000ms)
 */
function autoCerrarAlertas(tiempo = 5000) {
    const alertas = document.querySelectorAll('.alert:not(.alert-permanent)');

    alertas.forEach(alerta => {
        setTimeout(() => {
            const bsAlert = new bootstrap.Alert(alerta);
            bsAlert.close();
        }, tiempo);
    });
}

/**
 * Validar formularios antes de enviar
 */
function validarFormulario(formId) {
    const form = document.getElementById(formId);

    if (!form) {
        console.error(`No se encontró el formulario con id: ${formId}`);
        return false;
    }

    // Bootstrap validation
    if (!form.checkValidity()) {
        form.classList.add('was-validated');
        return false;
    }

    return true;
}

/**
 * Formatear números como moneda
 * @param {number} valor - Valor numérico
 * @param {string} moneda - Código de moneda (por defecto 'COP')
 * @returns {string}
 */
function formatearMoneda(valor, moneda = 'COP') {
    return new Intl.NumberFormat('es-CO', {
        style: 'currency',
        currency: moneda,
        minimumFractionDigits: 0
    }).format(valor);
}

/**
 * Formatear fecha
 * @param {string} fecha - Fecha en formato ISO
 * @returns {string}
 */
function formatearFecha(fecha) {
    const opciones = {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    };
    return new Date(fecha).toLocaleDateString('es-CO', opciones);
}

/**
 * Agregar animación de fade-in a elementos
 */
function animarElementos() {
    const elementos = document.querySelectorAll('.card, .table, .alert');

    elementos.forEach((elemento, index) => {
        setTimeout(() => {
            elemento.classList.add('fade-in');
        }, index * 100);
    });
}

/**
 * Buscar en tabla (filtro simple)
 * @param {string} inputId - ID del input de búsqueda
 * @param {string} tableId - ID de la tabla
 */
function buscarEnTabla(inputId, tableId) {
    const input = document.getElementById(inputId);
    const table = document.getElementById(tableId);

    if (!input || !table) return;

    input.addEventListener('keyup', function() {
        const filter = this.value.toLowerCase();
        const rows = table.getElementsByTagName('tr');

        for (let i = 1; i < rows.length; i++) {
            const row = rows[i];
            const cells = row.getElementsByTagName('td');
            let found = false;

            for (let j = 0; j < cells.length; j++) {
                const cell = cells[j];
                if (cell.textContent.toLowerCase().indexOf(filter) > -1) {
                    found = true;
                    break;
                }
            }

            row.style.display = found ? '' : 'none';
        }
    });
}

/**
 * Inicializar tooltips de Bootstrap
 */
function inicializarTooltips() {
    const tooltipTriggerList = [].slice.call(
        document.querySelectorAll('[data-bs-toggle="tooltip"]')
    );
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

/**
 * Inicializar todo al cargar el DOM
 */
document.addEventListener('DOMContentLoaded', function() {
    // Restaurar estado del sidebar
    restoreSidebarState();

    // Resaltar enlace activo
    highlightActiveLink();

    // Auto-cerrar alertas
    autoCerrarAlertas();

    // Animar elementos
    animarElementos();

    // Inicializar tooltips
    if (typeof bootstrap !== 'undefined') {
        inicializarTooltips();
    }

    console.log('Sistema inicializado correctamente');
});

/**
 * Exportar funciones para uso global
 */
window.sistemaGestion = {
    toggleSidebar,
    confirmarEliminacion,
    mostrarCargando,
    ocultarCargando,
    validarFormulario,
    formatearMoneda,
    formatearFecha,
    buscarEnTabla
};