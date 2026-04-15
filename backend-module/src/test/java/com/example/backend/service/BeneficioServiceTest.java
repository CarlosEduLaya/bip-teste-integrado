package com.example.backend.service;

import com.example.backend.exception.BeneficioNotFoundException;
import com.example.backend.model.Beneficio;
import com.example.backend.repository.BeneficioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficioServiceTest {

    @Mock
    private BeneficioRepository repository;

    @InjectMocks
    private BeneficioService service;

    private Beneficio origem;
    private Beneficio destino;

    @BeforeEach
    void setUp() {
        origem = new Beneficio("Origem", "desc", new BigDecimal("1000.00"));
        origem.setAtivo(true);

        destino = new Beneficio("Destino", "desc", new BigDecimal("200.00"));
        destino.setAtivo(true);
    }

    // ── findById ─────────────────────────────────────────────────────────────

    @Test
    void findById_deveRetornarBeneficio_quandoExistir() {
        when(repository.findById(1L)).thenReturn(Optional.of(origem));
        assertEquals(origem, service.findById(1L));
    }

    @Test
    void findById_deveLancarExcecao_quandoNaoExistir() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(BeneficioNotFoundException.class, () -> service.findById(99L));
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    void create_devePersistirERetornarBeneficio() {
        when(repository.save(any())).thenReturn(origem);
        Beneficio result = service.create(origem);
        assertNotNull(result);
        verify(repository).save(any());
    }

    // ── transfer ─────────────────────────────────────────────────────────────

    @Test
    void transfer_deveSubtrairDaOrigem_e_SomarAoDestino() {
        when(repository.findByIdWithLock(1L)).thenReturn(Optional.of(origem));
        when(repository.findByIdWithLock(2L)).thenReturn(Optional.of(destino));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.transfer(1L, 2L, new BigDecimal("300.00"));

        assertEquals(new BigDecimal("700.00"), origem.getValor());
        assertEquals(new BigDecimal("500.00"), destino.getValor());
        verify(repository, times(2)).save(any());
    }

    @Test
    void transfer_deveLancarExcecao_quandoSaldoInsuficiente() {
        when(repository.findByIdWithLock(1L)).thenReturn(Optional.of(origem));
        when(repository.findByIdWithLock(2L)).thenReturn(Optional.of(destino));

        assertThrows(IllegalStateException.class,
                () -> service.transfer(1L, 2L, new BigDecimal("9999.00")));
        verify(repository, never()).save(any());
    }

    @Test
    void transfer_deveLancarExcecao_quandoValorNaoPositivo() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(1L, 2L, BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(1L, 2L, new BigDecimal("-1")));
    }

    @Test
    void transfer_deveLancarExcecao_quandoIdsIguais() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(1L, 1L, new BigDecimal("100.00")));
    }

    @Test
    void transfer_deveLancarExcecao_quandoParametroNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(null, 2L, new BigDecimal("100.00")));
    }

    @Test
    void transfer_deveLancarExcecao_quandoOrigemInativa() {
        origem.setAtivo(false);
        when(repository.findByIdWithLock(1L)).thenReturn(Optional.of(origem));
        when(repository.findByIdWithLock(2L)).thenReturn(Optional.of(destino));

        assertThrows(IllegalStateException.class,
                () -> service.transfer(1L, 2L, new BigDecimal("100.00")));
    }

    @Test
    void transfer_deveLancarExcecao_quandoBeneficioNaoExistir() {
        when(repository.findByIdWithLock(99L)).thenReturn(Optional.empty());

        assertThrows(BeneficioNotFoundException.class,
                () -> service.transfer(99L, 2L, new BigDecimal("100.00")));
    }
}
