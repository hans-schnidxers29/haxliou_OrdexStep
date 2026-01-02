/**
 * Sistema de Notificaciones con SweetAlert2
 */

const Toast = Swal.mixin({
    toast: true,
    position: 'top-end',
    showConfirmButton: false,
    timer: 3000,
    timerProgressBar: true,
    didOpen: (toast) => {
        toast.addEventListener('mouseenter', Swal.stopTimer);
        toast.addEventListener('mouseleave', Swal.resumeTimer);
    }
});

/**
 * Muestra Toast basado en los mensajes pasados desde el HTML
 */
function checkNotifications(successMsg, errorMsg, infoMsg, warningMsg) {
    // Validamos que el mensaje exista, no sea null string y no esté vacío
    if (successMsg && successMsg !== 'null' && successMsg !== '') {
        Toast.fire({ icon: 'success', title: successMsg });
    }

    if (errorMsg && errorMsg !== 'null' && errorMsg !== '') {
        Toast.fire({ icon: 'error', title: errorMsg });
    }

    if (infoMsg && infoMsg !== 'null' && infoMsg !== '') {
        Toast.fire({ icon: 'info', title: infoMsg });
    }

    if (warningMsg && warningMsg !== 'null' && warningMsg !== '') {
        Toast.fire({ icon: 'warning', title: warningMsg });
    }
}

// ... (Resto de tus funciones confirmarEliminar, etc., se mantienen igual)
function confirmarEliminar(id, formIdPrefix = 'deleteForm') {
    Swal.fire({
        title: '¿Estás seguro?',
        text: "¡Esta acción no se puede deshacer!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: '<i class="fas fa-trash"></i> Sí, eliminar',
        cancelButtonText: 'Cancelar',
        reverseButtons: true
    }).then((result) => {
        if (result.isConfirmed) {
            const form = document.getElementById(formIdPrefix + id);
            if (form) form.submit();
        }
    });
}