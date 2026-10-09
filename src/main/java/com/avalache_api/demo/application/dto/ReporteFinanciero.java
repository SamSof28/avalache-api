package com.avalache_api.demo.application.dto;

public record ReporteFinanciero(
    String nombreUsuario,
    ResultadoAvalancha resultado,
    String consejo
) {}
