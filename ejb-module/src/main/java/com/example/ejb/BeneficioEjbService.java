package com.example.ejb;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class BeneficioEjbService {

    @PersistenceContext
    private EntityManager em;

    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        if (fromId == null || toId == null || amount == null) {
            throw new IllegalArgumentException("Parâmetros não podem ser nulos.");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero.");
        }
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("Origem e destino não podem ser iguais.");
        }

        // Lock pessimista para evitar lost update em transferências concorrentes
        Beneficio from = em.find(Beneficio.class, fromId, LockModeType.PESSIMISTIC_WRITE);
        Beneficio to   = em.find(Beneficio.class, toId,   LockModeType.PESSIMISTIC_WRITE);

        if (from == null) throw new IllegalArgumentException("Beneficio de origem não encontrado: " + fromId);
        if (to   == null) throw new IllegalArgumentException("Beneficio de destino não encontrado: " + toId);

        if (Boolean.FALSE.equals(from.getAtivo())) throw new IllegalStateException("Beneficio de origem está inativo.");
        if (Boolean.FALSE.equals(to.getAtivo()))   throw new IllegalStateException("Beneficio de destino está inativo.");

        if (from.getValor().compareTo(amount) < 0) {
            throw new IllegalStateException("Saldo insuficiente. Disponível: " + from.getValor());
        }

        from.setValor(from.getValor().subtract(amount));
        to.setValor(to.getValor().add(amount));

        em.merge(from);
        em.merge(to);
    }

    public Beneficio findById(Long id) {
        if (id == null) throw new IllegalArgumentException("id não pode ser nulo.");
        Beneficio b = em.find(Beneficio.class, id);
        if (b == null) throw new IllegalArgumentException("Beneficio não encontrado: " + id);
        return b;
    }

    public Beneficio create(Beneficio beneficio) {
        em.persist(beneficio);
        return beneficio;
    }

    public Beneficio update(Beneficio beneficio) {
        return em.merge(beneficio);
    }

    public void deactivate(Long id) {
        Beneficio b = findById(id);
        b.setAtivo(false);
        em.merge(b);
    }
}
