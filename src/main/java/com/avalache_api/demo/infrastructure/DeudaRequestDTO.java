package com.avalache_api.demo.infrastructure;

import lombok.Data;

import java.math.BigDecimal;

@Data 
public class DeudaRequestDTO {
    private String  nombreDeuda;
    private BigDecimal saldo;
    private Integer numCuotas;
    private BigDecimal pagoMinimo;
    private BigDecimal tasaInteres;
}
