package com.example.ejb;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BeneficioEjbServiceTest {

    @Mock
    private EntityManager em;

    @InjectMocks
    private BeneficioEjbService service;

    private Beneficio origem;
    private Beneficio destino;

    @BeforeEach
    void setUp() {
        origem = new Beneficio("Origem", "desc", new BigDecimal("1000.00"));
        origem.setId(1L);
        origem.setAtivo(true);

        destino = new Beneficio("Destino", "desc", new BigDecimal("200.00"));
        destino.setId(2L);
        destino.setAtivo(true);

        when(em.find(Beneficio.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(origem);
        when(em.find(Beneficio.class, 2L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(destino);
    }

    @Test
    void transfere_valores_corretamente() {
        service.transfer(1L, 2L, new BigDecimal("300.00"));

        assertEquals(new BigDecimal("700.00"), origem.getValor());
        assertEquals(new BigDecimal("500.00"), destino.getValor());
        verify(em).merge(origem);
        verify(em).merge(destino);
    }

    @Test
    void lanca_excecao_quando_saldo_insuficiente() {
        assertThrows(IllegalStateException.class,
                () -> service.transfer(1L, 2L, new BigDecimal("9999.00")));
    }

    @Test
    void lanca_excecao_quando_valor_zero() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(1L, 2L, BigDecimal.ZERO));
    }

    @Test
    void lanca_excecao_quando_valor_negativo() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(1L, 2L, new BigDecimal("-1.00")));
    }

    @Test
    void lanca_excecao_quando_ids_iguais() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(1L, 1L, new BigDecimal("100.00")));
    }

    @Test
    void lanca_excecao_quando_parametro_nulo() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(null, 2L, new BigDecimal("100.00")));
    }

    @Test
    void lanca_excecao_quando_origem_inativa() {
        origem.setAtivo(false);
        assertThrows(IllegalStateException.class,
                () -> service.transfer(1L, 2L, new BigDecimal("100.00")));
    }

    @Test
    void lanca_excecao_quando_destino_inativo() {
        destino.setAtivo(false);
        assertThrows(IllegalStateException.class,
                () -> service.transfer(1L, 2L, new BigDecimal("100.00")));
    }

    @Test
    void lanca_excecao_quando_origem_nao_encontrada() {
        when(em.find(Beneficio.class, 99L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(99L, 2L, new BigDecimal("100.00")));
    }
}
