package com.avalache_api.demo.application.dto;

import java.math.BigDecimal;

public record DeudaResultado(
    String nombre,
    BigDecimal saldoInicial,
    BigDecimal saldoFinal,
    BigDecimal tasaInteres
) {}
