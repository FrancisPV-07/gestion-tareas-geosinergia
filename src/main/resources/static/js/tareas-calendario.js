
document.addEventListener("DOMContentLoaded", () => {

    const campo = document.getElementById("fechaLimite");
    const boton = document.getElementById("abrirCalendario");
    const aviso = document.getElementById("fechaLimiteErrorCliente");

    if (!campo) return;

    // Mantener el calendario nativo si Flatpickr no carga.
    if (typeof flatpickr !== "function" ||
        !flatpickr.l10ns.es) {

        boton?.addEventListener("click", () => {
            if (typeof campo.showPicker === "function") {
                campo.showPicker();
            } else {
                campo.focus();
            }
        });

        return;
    }

    const calendario = flatpickr(campo, {

        locale: flatpickr.l10ns.es,

        // Valor enviado a Spring Boot
        dateFormat: "Y-m-d",

        // Fecha visible para el usuario
        altInput: true,
        altFormat: "d/m/Y",

        allowInput: false,
        disableMobile: true,

        // No ofrecer fechas anteriores
        minDate: "today",

        // Conservar fechas inválidas devueltas por el servidor
        allowInvalidPreload: true,

        monthSelectorType: "static",

        onChange: function () {

            if (aviso) aviso.hidden = true;

            this.altInput?.classList.remove("input-invalid");
        }
    });

    /* ==========================================
       SELECTOR PERSONALIZADO DE AÑOS
       ========================================== */

    const encabezado = calendario.calendarContainer
        .querySelector(".flatpickr-current-month");

    if (encabezado) {

        // Crear botón para desplegar los años
        const botonAnio = document.createElement("button");

        botonAnio.type = "button";
        botonAnio.className = "geo-year-trigger";
        botonAnio.setAttribute("aria-label", "Seleccionar año");
        botonAnio.setAttribute("aria-haspopup", "listbox");
        botonAnio.setAttribute("aria-expanded", "false");

        encabezado.appendChild(botonAnio);

        // Crear el menú de años
        const menuAnios = document.createElement("div");

        menuAnios.className = "geo-year-menu";
        menuAnios.setAttribute("role", "listbox");
        menuAnios.setAttribute("aria-label", "Años disponibles");
        menuAnios.hidden = true;

        calendario.calendarContainer.appendChild(menuAnios);
        calendario.calendarContainer.classList.add("geo-year-calendar");

        function cerrarMenuAnios() {
            menuAnios.hidden = true;
            botonAnio.setAttribute("aria-expanded", "false");
        }

        function actualizarAnio() {
            botonAnio.textContent = String(calendario.currentYear);
        }

        function crearOpcionesAnios() {

            menuAnios.replaceChildren();

            const anioActual = calendario.currentYear;

            const anioMinimo = calendario.config.minDate
                ? calendario.config.minDate.getFullYear()
                : new Date().getFullYear();

            // Mostrar años próximos al que estamos consultando
            const inicio = Math.max(anioMinimo, anioActual - 2);
            const fin = anioActual + 10;

            for (let anio = inicio; anio <= fin; anio++) {

                const opcion = document.createElement("button");

                opcion.type = "button";
                opcion.className = "geo-year-option";
                opcion.textContent = String(anio);
                opcion.setAttribute("role", "option");

                const seleccionado = anio === anioActual;

                opcion.setAttribute(
                    "aria-selected",
                    String(seleccionado)
                );

                if (seleccionado) {
                    opcion.classList.add("is-selected");
                }

                opcion.addEventListener("click", () => {

                    calendario.changeYear(anio);

                    actualizarAnio();
                    cerrarMenuAnios();
                    botonAnio.focus();
                });

                menuAnios.appendChild(opcion);
            }
        }

        // Abrir o cerrar el desplegable
        botonAnio.addEventListener("click", (evento) => {

            evento.stopPropagation();
            cerrarMenuMeses();

            if (!menuAnios.hidden) {
                cerrarMenuAnios();
                return;
            }

            crearOpcionesAnios();

            menuAnios.hidden = false;
            botonAnio.setAttribute("aria-expanded", "true");

            const seleccionada =
                menuAnios.querySelector(".is-selected");

            seleccionada?.focus();
        });

        // Cerrar mediante Escape
        menuAnios.addEventListener("keydown", (evento) => {

            if (evento.key === "Escape") {
                cerrarMenuAnios();
                botonAnio.focus();
            }
        });

        // Cerrar al hacer clic fuera
        document.addEventListener("click", (evento) => {

            if (!botonAnio.contains(evento.target) &&
                !menuAnios.contains(evento.target)) {

                cerrarMenuAnios();
            }
        });

        // Actualizar el año cuando se navegue con las flechas
        calendario.config.onYearChange.push(actualizarAnio);
        calendario.config.onMonthChange.push(actualizarAnio);
        calendario.config.onClose.push(cerrarMenuAnios);

        /* ==========================================
           SELECTOR PERSONALIZADO DE MESES
           ========================================== */

        // Botón que mostrará el mes actual
        const botonMes = document.createElement("button");

        botonMes.type = "button";
        botonMes.className = "geo-month-trigger";
        botonMes.setAttribute("aria-label", "Seleccionar mes");
        botonMes.setAttribute("aria-haspopup", "listbox");
        botonMes.setAttribute("aria-expanded", "false");

        // Colocar el mes antes del botón del año
        encabezado.insertBefore(botonMes, botonAnio);

        // Crear lista personalizada de meses
        const menuMeses = document.createElement("div");

        menuMeses.className = "geo-month-menu";
        menuMeses.setAttribute("role", "listbox");
        menuMeses.setAttribute("aria-label", "Meses disponibles");
        menuMeses.hidden = true;

        calendario.calendarContainer.appendChild(menuMeses);
        calendario.calendarContainer.classList.add("geo-month-calendar");

        // Nombres de los meses según el idioma configurado
        const nombresMeses = calendario.l10n.months.longhand;

        function cerrarMenuMeses() {
            menuMeses.hidden = true;
            botonMes.setAttribute("aria-expanded", "false");
        }

        function actualizarMes() {
            botonMes.textContent =
                nombresMeses[calendario.currentMonth];
        }

        function crearOpcionesMeses() {

            menuMeses.replaceChildren();

            const mesActual = calendario.currentMonth;
            const anioActual = calendario.currentYear;
            const fechaMinima = calendario.config.minDate;

            nombresMeses.forEach((nombre, indice) => {

                const opcion = document.createElement("button");

                opcion.type = "button";
                opcion.className = "geo-month-option";
                opcion.textContent = nombre;
                opcion.setAttribute("role", "option");

                const seleccionado = indice === mesActual;

                opcion.setAttribute(
                    "aria-selected",
                    String(seleccionado)
                );

                // Impedir navegar a meses anteriores
                // al mínimo permitido
                if (fechaMinima &&
                    anioActual === fechaMinima.getFullYear() &&
                    indice < fechaMinima.getMonth()) {

                    opcion.disabled = true;
                }

                if (seleccionado) {
                    opcion.classList.add("is-selected");
                }

                opcion.addEventListener("click", () => {

                    // Ir directamente al mes elegido
                    calendario.changeMonth(indice, false);

                    actualizarMes();
                    cerrarMenuMeses();
                    botonMes.focus();
                });

                menuMeses.appendChild(opcion);
            });
        }

        // Abrir o cerrar el menú
        botonMes.addEventListener("click", (evento) => {

            evento.stopPropagation();

            cerrarMenuAnios();

            if (!menuMeses.hidden) {
                cerrarMenuMeses();
                return;
            }

            crearOpcionesMeses();

            menuMeses.hidden = false;
            botonMes.setAttribute("aria-expanded", "true");

            const opcionActiva =
                menuMeses.querySelector(".is-selected");

            opcionActiva?.focus();
        });

        // Cerrar con Escape
        menuMeses.addEventListener("keydown", (evento) => {

            if (evento.key === "Escape") {
                cerrarMenuMeses();
                botonMes.focus();
            }
        });

        // Cerrar al hacer clic fuera
        document.addEventListener("click", (evento) => {

            if (!botonMes.contains(evento.target) &&
                !menuMeses.contains(evento.target)) {

                cerrarMenuMeses();
            }
        });

        // Mantener el mes actualizado
        calendario.config.onMonthChange.push(actualizarMes);
        calendario.config.onYearChange.push(actualizarMes);
        calendario.config.onClose.push(cerrarMenuMeses);

        // Mostrar el mes inicial
        actualizarMes();

        actualizarAnio();
    }

    // Mantener el identificador original para Spring Boot.
    // El campo visible es generado por Flatpickr.
    const campoVisible = calendario.altInput;

    if (campoVisible) {

        campoVisible.id = "fechaLimiteVisible";
        campoVisible.placeholder = "dd/mm/aaaa";

        const etiqueta =
            document.querySelector('label[for="fechaLimite"]');

        if (etiqueta) {
            etiqueta.htmlFor = "fechaLimiteVisible";
        }
    }

    // El input original se vuelve oculto.
    // Validaremos el obligatorio antes del envío.
    campo.removeAttribute("required");

    boton?.addEventListener("click", () => {
        calendario.open();
    });

    const formulario = campo.closest("form");

    formulario?.addEventListener("submit", (evento) => {

        if (!campo.value) {

            evento.preventDefault();

            if (aviso) aviso.hidden = false;

            campoVisible?.classList.add("input-invalid");

            calendario.open();
        }
    });

});
