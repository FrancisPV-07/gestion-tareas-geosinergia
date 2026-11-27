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
class TareaDesasignacionSeguridadTest {

    @Autowired
    private MockMvc mockMvc;

    // ==========================================
    // HU-06: USUARIO ANONIMO
    // ==========================================

    @Test
    void usuarioAnonimoNoPuedeDesasignar() throws Exception {

        mockMvc.perform(
                post("/tareas/999999/desasignar")
                        .with(csrf())
                        .param("motivo", "Prueba"))
                .andExpect(status().is3xxRedirection());
    }

    // ==========================================
    // HU-06: COLABORADOR NO AUTORIZADO
    // ==========================================

    @Test
    @WithMockUser(roles = "COLABORADOR")
    void colaboradorNoPuedeDesasignarPorHttp()
            throws Exception {

        mockMvc.perform(
                post("/tareas/999999/desasignar")
                        .with(csrf())
                        .param("motivo", "Prueba"))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // HU-06: COORDINADOR SIN TOKEN CSRF
    // ==========================================

    @Test
    @WithMockUser(roles = "COORDINADOR")
    void coordinadorSinCsrfNoPuedeDesasignar()
            throws Exception {

        mockMvc.perform(
                post("/tareas/999999/desasignar")
                        .param("motivo", "Prueba"))
                .andExpect(status().isForbidden());
    }
}
