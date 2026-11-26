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
class TareaCancelacionSeguridadTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void usuarioAnonimoNoPuedeCancelar() throws Exception {

        mockMvc.perform(
                post("/tareas/999999/cancelar")
                        .with(csrf())
                        .param("motivo", "Prueba"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "COLABORADOR")
    void colaboradorNoPuedeCancelarPorHttp() throws Exception {

        mockMvc.perform(
                post("/tareas/999999/cancelar")
                        .with(csrf())
                        .param("motivo", "Prueba"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "COORDINADOR")
    void coordinadorSinCsrfNoPuedeCancelar() throws Exception {

        mockMvc.perform(
                post("/tareas/999999/cancelar")
                        .param("motivo", "Prueba"))
                .andExpect(status().isForbidden());
    }
}
