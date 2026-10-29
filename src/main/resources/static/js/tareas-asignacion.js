
document.addEventListener("DOMContentLoaded", () => {

    // Elementos del formulario
    const selectorTarea = document.getElementById("idTarea");

    const detalleSinSeleccion =
        document.getElementById("detalleSinSeleccion");

    const detalleDinamico =
        document.getElementById("detalleTareaDinamico");

    // Verificar que estamos en el modo de selección manual
    if (!selectorTarea || !detalleSinSeleccion || !detalleDinamico) {
        return;
    }

    // Campos de la tarjeta informativa
    const descripcion = document.getElementById("detalleDescripcion");
    const estado = document.getElementById("detalleEstado");
    const creador = document.getElementById("detalleCreador");

    function actualizarDetalleTarea() {

        const opcion = selectorTarea.selectedOptions[0];

        // Si no hay una tarea seleccionada
        if (!opcion || !opcion.value) {

            detalleSinSeleccion.hidden = false;
            detalleDinamico.hidden = true;

            return;
        }

        // Mostrar la información seleccionada
        descripcion.textContent =
            opcion.dataset.descripcion || "Sin descripción disponible.";

        estado.textContent =
            opcion.dataset.estado || "Pendiente";

        creador.textContent =
            opcion.dataset.creador || "No disponible";

        detalleSinSeleccion.hidden = true;
        detalleDinamico.hidden = false;
    }

    // Actualizar al cambiar la selección
    selectorTarea.addEventListener(
        "change",
        actualizarDetalleTarea
    );

    // Restaurar el detalle si existe una selección previa
    actualizarDetalleTarea();

});
