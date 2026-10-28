const sidebarToggle =
    document.getElementById("sidebarToggle");

const appLayout =
    document.querySelector(".app-layout");


if (sidebarToggle && appLayout) {

    sidebarToggle.addEventListener("click", () => {

        const estaContraido =
            appLayout.classList.toggle(
                "sidebar-collapsed"
            );

        sidebarToggle.setAttribute(
            "aria-expanded",
            !estaContraido
        );

        sidebarToggle.setAttribute(
            "aria-label",
            estaContraido
                ? "Expandir menú lateral"
                : "Contraer menú lateral"
        );

    });

}