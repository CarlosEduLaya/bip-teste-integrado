package com.example.backend.service;

import com.example.backend.exception.BeneficioNotFoundException;
import com.example.backend.model.Beneficio;
import com.example.backend.repository.BeneficioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BeneficioService {

    private final BeneficioRepository repository;

    public BeneficioService(BeneficioRepository repository) {
        this.repository = repository;
    }

    public List<Beneficio> findAll() {
        return repository.findAll();
    }

    public List<Beneficio> findAtivos() {
        return repository.findByAtivo(true);
    }

    public Beneficio findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BeneficioNotFoundException(id));
    }

    @Transactional
    public Beneficio create(Beneficio beneficio) {
        beneficio.setId(null);
        return repository.save(beneficio);
    }

    @Transactional
    public Beneficio update(Long id, Beneficio dados) {
        Beneficio existente = findById(id);
        existente.setNome(dados.getNome());
        existente.setDescricao(dados.getDescricao());
        existente.setValor(dados.getValor());
        existente.setAtivo(dados.getAtivo());
        return repository.save(existente);
    }

    @Transactional
    public void deactivate(Long id) {
        Beneficio b = findById(id);
        b.setAtivo(false);
        repository.save(b);
    }

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        if (fromId == null || toId == null || amount == null) {
            throw new IllegalArgumentException("fromId, toId e amount são obrigatórios.");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero.");
        }
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("Origem e destino não podem ser iguais.");
        }

        // Lock pessimista para evitar concorrência na transferência
        Beneficio from = repository.findByIdWithLock(fromId)
                .orElseThrow(() -> new BeneficioNotFoundException(fromId));
        Beneficio to = repository.findByIdWithLock(toId)
                .orElseThrow(() -> new BeneficioNotFoundException(toId));

        if (Boolean.FALSE.equals(from.getAtivo())) throw new IllegalStateException("Beneficio de origem está inativo.");
        if (Boolean.FALSE.equals(to.getAtivo()))   throw new IllegalStateException("Beneficio de destino está inativo.");

        if (from.getValor().compareTo(amount) < 0) {
            throw new IllegalStateException("Saldo insuficiente. Disponível: " + from.getValor());
        }

        from.setValor(from.getValor().subtract(amount));
        to.setValor(to.getValor().add(amount));

        repository.save(from);
        repository.save(to);
    }
}
