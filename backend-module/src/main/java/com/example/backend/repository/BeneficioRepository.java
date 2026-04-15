package com.example.backend.repository;

import com.example.backend.model.Beneficio;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficioRepository extends JpaRepository<Beneficio, Long> {

    List<Beneficio> findByAtivo(Boolean ativo);

    /**
     * Busca um benefício com PESSIMISTIC_WRITE lock — mesma estratégia aplicada no EJB corrigido.
     * Garante serialização de transferências concorrentes ao mesmo registro.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Beneficio b WHERE b.id = :id")
    Optional<Beneficio> findByIdWithLock(@Param("id") Long id);
}
