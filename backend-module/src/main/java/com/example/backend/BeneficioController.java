package com.example.backend;

import com.example.backend.dto.TransferRequest;
import com.example.backend.model.Beneficio;
import com.example.backend.service.BeneficioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Beneficios", description = "CRUD de benefícios e operação de transferência")
@RestController
@RequestMapping("/api/v1/beneficios")
public class BeneficioController {

    private final BeneficioService service;

    public BeneficioController(BeneficioService service) {
        this.service = service;
    }

    // ── Listagem ─────────────────────────────────────────────────────────────

    @Operation(summary = "Lista todos os benefícios")
    @GetMapping
    public List<Beneficio> list(
            @Parameter(description = "Filtrar apenas ativos") @RequestParam(required = false) Boolean ativo) {
        return Boolean.TRUE.equals(ativo) ? service.findAtivos() : service.findAll();
    }

    // ── Busca por ID ─────────────────────────────────────────────────────────

    @Operation(summary = "Busca um benefício por ID",
               responses = {@ApiResponse(responseCode = "404", description = "Não encontrado")})
    @GetMapping("/{id}")
    public Beneficio getById(@PathVariable Long id) {
        return service.findById(id);
    }

    // ── Criação ───────────────────────────────────────────────────────────────

    @Operation(summary = "Cria um novo benefício")
    @PostMapping
    public ResponseEntity<Beneficio> create(@Valid @RequestBody Beneficio beneficio) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(beneficio));
    }

    // ── Atualização ───────────────────────────────────────────────────────────

    @Operation(summary = "Atualiza um benefício existente")
    @PutMapping("/{id}")
    public Beneficio update(@PathVariable Long id, @Valid @RequestBody Beneficio beneficio) {
        return service.update(id, beneficio);
    }

    // ── Exclusão lógica ───────────────────────────────────────────────────────

    @Operation(summary = "Desativa (exclusão lógica) um benefício")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    // ── Transferência ─────────────────────────────────────────────────────────

    @Operation(summary = "Transfere valor entre dois benefícios",
               description = "Valida saldo, aplica pessimistic locking e executa em transação atômica.",
               responses = {
                   @ApiResponse(responseCode = "204", description = "Transferência realizada"),
                   @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                   @ApiResponse(responseCode = "422", description = "Saldo insuficiente ou benefício inativo")
               })
    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest req) {
        service.transfer(req.getFromId(), req.getToId(), req.getAmount());
        return ResponseEntity.noContent().build();
    }
}
