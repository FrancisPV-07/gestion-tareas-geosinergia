document.addEventListener("DOMContentLoaded", () => {
    /* =====================================================
       CORREO CORPORATIVO
       ===================================================== */
    const correoCompleto =
        document.getElementById("correo");

    const correoLocal =
        document.getElementById("correoLocal");

    const dominio =
        "@geosinergia.com.pe";


    if (correoCompleto && correoLocal) {

        if (correoCompleto.value) {

            correoLocal.value =
                correoCompleto.value.endsWith(dominio)
                    ? correoCompleto.value.slice(
                        0,
                        -dominio.length
                    )
                    : correoCompleto.value;

        }


        const actualizarCorreo = () => {

            const usuario =
                correoLocal.value.trim();


            correoCompleto.value =
                usuario
                    ? usuario + dominio
                    : "";

        };


        correoLocal.addEventListener(
            "input",
            actualizarCorreo
        );


        const formulario =
            correoLocal.closest("form");


        if (formulario) {

            formulario.addEventListener(
                "submit",
                actualizarCorreo
            );

        }

    }
    
    /* =====================================================
       MOSTRAR / OCULTAR CONTRASEÑAS
       ===================================================== */
    const botonesPassword =
        document.querySelectorAll(
            "[data-password-toggle]"
        );

    botonesPassword.forEach((boton) => {

        boton.addEventListener("click", () => {

            const inputId =
                boton.dataset.passwordToggle;

            const input =
                document.getElementById(inputId);

            if (!input) {
                return;
            }

            const estaOculta =
                input.type === "password";

            input.type =
                estaOculta
                    ? "text"
                    : "password";

            const eyeOpen =
                boton.querySelector(".eye-open");

            const eyeClosed =
                boton.querySelector(".eye-closed");

            if (eyeOpen && eyeClosed) {

                eyeOpen.classList.toggle(
                    "hidden",
                    estaOculta
                );

                eyeClosed.classList.toggle(
                    "hidden",
                    !estaOculta
                );

            }

            boton.setAttribute(
                "aria-label",
                estaOculta
                    ? "Ocultar contraseña"
                    : "Mostrar contraseña"
            );

        });

    });

});