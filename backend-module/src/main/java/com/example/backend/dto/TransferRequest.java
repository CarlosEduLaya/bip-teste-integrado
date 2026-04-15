package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Dados para transferência de valor entre dois benefícios")
public class TransferRequest {

    @Schema(description = "ID do benefício de origem", example = "1")
    @NotNull(message = "fromId é obrigatório")
    private Long fromId;

    @Schema(description = "ID do benefício de destino", example = "2")
    @NotNull(message = "toId é obrigatório")
    private Long toId;

    @Schema(description = "Valor a transferir (deve ser > 0)", example = "100.00")
    @NotNull(message = "amount é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal amount;

    public Long getFromId() { return fromId; }
    public void setFromId(Long fromId) { this.fromId = fromId; }

    public Long getToId() { return toId; }
    public void setToId(Long toId) { this.toId = toId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
