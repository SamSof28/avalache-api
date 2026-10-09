package com.avalache_api.demo.infrastructure;

import lombok.Data;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data 
public class DeudaRequestDTO {
    @NotBlank
    private String  nombreDeuda;
    @NotNull
    @Positive
    private BigDecimal saldo;
    @NotNull
    @Positive
    private Integer numCuotas;
    @NotNull
    @Positive
    private BigDecimal pagoMinimo;
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal tasaInteres;
}
