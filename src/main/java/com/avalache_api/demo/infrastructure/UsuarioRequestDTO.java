package com.avalache_api.demo.infrastructure;

import lombok.Data;

import java.util.List;

import java.math.BigDecimal;

@Data
public class UsuarioRequestDTO { 
    private String nombreUsuario;
    private List<DeudaRequestDTO> deudas;
    private BigDecimal montoExtra;
}
