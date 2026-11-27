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
class TareaReasignacionSeguridadTest {

    @Autowired
    private MockMvc mockMvc;

    // ==========================================
    // HU-06: SEGURIDAD HTTP DE REASIGNACION
    // ==========================================

    @Test
    void usuarioAnonimoNoPuedeReasignar() throws Exception {

        mockMvc.perform(
                post("/tareas/999999/reasignar")
                        .with(csrf())
                        .param("idNuevoResponsable", "3")
                        .param("motivo", "Prueba de seguridad"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "COLABORADOR")
    void colaboradorNoPuedeReasignarPorHttp()
            throws Exception {

        mockMvc.perform(
                post("/tareas/999999/reasignar")
                        .with(csrf())
                        .param("idNuevoResponsable", "3")
                        .param("motivo", "Prueba de seguridad"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "COORDINADOR")
    void coordinadorSinCsrfNoPuedeReasignar()
            throws Exception {

        mockMvc.perform(
                post("/tareas/999999/reasignar")
                        .param("idNuevoResponsable", "3")
                        .param("motivo", "Prueba de seguridad"))
                .andExpect(status().isForbidden());
    }
}
