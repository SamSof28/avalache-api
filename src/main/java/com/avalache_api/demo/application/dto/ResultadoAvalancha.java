package com.avalache_api.demo.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record ResultadoAvalancha(
    int mesesEstimados,
    boolean alcanzable,
    BigDecimal interesesEstimados,
    List<DeudaResultado> deudas
) {}
