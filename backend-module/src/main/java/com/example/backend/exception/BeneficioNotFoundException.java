package com.example.backend.exception;

public class BeneficioNotFoundException extends RuntimeException {
    public BeneficioNotFoundException(Long id) {
        super("Beneficio não encontrado: id=" + id);
    }
}
