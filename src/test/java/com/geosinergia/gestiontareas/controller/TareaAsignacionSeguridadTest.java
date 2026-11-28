package com.geosinergia.gestiontareas.controller;

import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TareaAsignacionSeguridadTest {

    @Autowired
    private MockMvc mockMvc;

    // ==========================================
    // HU-05: SEGURIDAD HTTP DE ASIGNACION
    // ==========================================

    @Test
    void usuarioAnonimoNoPuedeAsignarTarea()
            throws Exception {

        mockMvc.perform(
                post("/tareas/asignar")
                        .with(csrf())
                        .param("idTarea", "999999")
                        .param("idResponsable", "3")
                        .param("idPrioridad", "1")
                        .param("fechaLimite", "2026-12-15"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "COLABORADOR")
    void colaboradorNoPuedeAsignarTarea()
            throws Exception {

        mockMvc.perform(
                post("/tareas/asignar")
                        .with(csrf())
                        .param("idTarea", "999999")
                        .param("idResponsable", "3")
                        .param("idPrioridad", "1")
                        .param("fechaLimite", "2026-12-15"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "COORDINADOR")
    void coordinadorSinCsrfNoPuedeAsignarTarea()
            throws Exception {

        mockMvc.perform(
                post("/tareas/asignar")
                        .param("idTarea", "999999")
                        .param("idResponsable", "3")
                        .param("idPrioridad", "1")
                        .param("fechaLimite", "2026-12-15"))
                .andExpect(status().isForbidden());
    }
}
