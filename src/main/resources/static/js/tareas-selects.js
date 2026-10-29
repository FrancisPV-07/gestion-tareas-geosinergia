
document.addEventListener("DOMContentLoaded", () => {

    // Mantener los select nativos si la librería no carga.
    if (typeof TomSelect === "undefined") {
        return;
    }

    // Los tres desplegables de HU-05
    const campos = [
        "idTarea",
        "idResponsable",
        "idPrioridad"
    ];

    campos.forEach((id) => {

        const select = document.getElementById(id);

        // Personalizar solamente los desplegables reales.
        // No modificar los campos ocultos.
        if (!select || select.tagName !== "SELECT" || select.tomselect) {
            return;
        }

        const opcionInicial =
            select.querySelector('option[value=""]');

        const textoInicial = opcionInicial
            ? opcionInicial.textContent.trim()
            : "Seleccionar opción";

        const tomSelect = new TomSelect(select, {

            create: false,
            maxItems: 1,
            allowEmptyOption: false,

            placeholder: textoInicial,

            // Desactivar el campo de escritura y búsqueda
            controlInput: null,
            searchField: [],

            // Cerrar el menú después de seleccionar
            closeAfterSelect: true,

            onInitialize: function () {
                this.control.dataset.placeholder = textoInicial;
            },

            // Conservar el orden original
            sortField: [
                { field: "$order" }
            ],

            render: {
                no_results: function () {
                    return '<div class="no-results">Sin coincidencias</div>';
                }
            }
        });

        // ==========================================
        // INDICADOR VISUAL DE PRIORIDAD
        // ==========================================

        if (id === "idPrioridad") {

            function actualizarIndicadorPrioridad() {

                const valor = String(tomSelect.getValue());

                // Obtener el nombre de la prioridad seleccionada
                const opcion = Array.from(select.options)
                    .find(item => item.value === valor);

                const prioridad = opcion
                    ? opcion.textContent.trim().toLowerCase()
                    : "";

                // Mostrar un color según la prioridad
                if (["alta", "media", "baja"].includes(prioridad)) {

                    tomSelect.wrapper.dataset.prioridad = prioridad;

                } else {

                    // Sin selección: no mostrar ningún círculo
                    delete tomSelect.wrapper.dataset.prioridad;
                }
            }

            // Actualizar al cambiar la selección
            tomSelect.on("change", actualizarIndicadorPrioridad);

            // Recuperar el indicador al cargar el formulario
            actualizarIndicadorPrioridad();
        }

    });

});
