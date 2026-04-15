package com.example.backend;

import com.example.backend.exception.BeneficioNotFoundException;
import com.example.backend.model.Beneficio;
import com.example.backend.service.BeneficioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BeneficioController.class)
class BeneficioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BeneficioService service;

    private Beneficio fakeBeneficio() {
        Beneficio b = new Beneficio("Beneficio A", "Desc A", new BigDecimal("1000.00"));
        b.setAtivo(true);
        return b;
    }

    @Test
    void list_deveRetornar200_comListaDeBeneficios() throws Exception {
        when(service.findAll()).thenReturn(List.of(fakeBeneficio()));

        mockMvc.perform(get("/api/v1/beneficios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Beneficio A"));
    }

    @Test
    void getById_deveRetornar200_quandoExistir() throws Exception {
        when(service.findById(1L)).thenReturn(fakeBeneficio());

        mockMvc.perform(get("/api/v1/beneficios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Beneficio A"));
    }

    @Test
    void getById_deveRetornar404_quandoNaoExistir() throws Exception {
        when(service.findById(99L)).thenThrow(new BeneficioNotFoundException(99L));

        mockMvc.perform(get("/api/v1/beneficios/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_deveRetornar201_comBeneficioCriado() throws Exception {
        when(service.create(any())).thenReturn(fakeBeneficio());

        mockMvc.perform(post("/api/v1/beneficios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fakeBeneficio())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Beneficio A"));
    }

    @Test
    void create_deveRetornar400_quandoNomeFaltando() throws Exception {
        Beneficio semNome = new Beneficio(null, "desc", new BigDecimal("100.00"));

        mockMvc.perform(post("/api/v1/beneficios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(semNome)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deactivate_deveRetornar204() throws Exception {
        doNothing().when(service).deactivate(1L);

        mockMvc.perform(delete("/api/v1/beneficios/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void transfer_deveRetornar204_quandoValido() throws Exception {
        doNothing().when(service).transfer(eq(1L), eq(2L), any());

        String body = """
                {"fromId": 1, "toId": 2, "amount": 100.00}
                """;

        mockMvc.perform(post("/api/v1/beneficios/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());
    }

    @Test
    void transfer_deveRetornar422_quandoSaldoInsuficiente() throws Exception {
        doThrow(new IllegalStateException("Saldo insuficiente"))
                .when(service).transfer(any(), any(), any());

        String body = """
                {"fromId": 1, "toId": 2, "amount": 9999.00}
                """;

        mockMvc.perform(post("/api/v1/beneficios/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void transfer_deveRetornar400_quandoAmountNulo() throws Exception {
        String body = """
                {"fromId": 1, "toId": 2}
                """;

        mockMvc.perform(post("/api/v1/beneficios/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
