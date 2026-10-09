package com.avalache_api.demo.infrastructure;

import lombok.Data;

import java.util.List;

import java.math.BigDecimal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Data
public class UsuarioRequestDTO { 
    @NotBlank
    private String nombreUsuario;
    @NotEmpty
    @Valid
    private List<@Valid DeudaRequestDTO> deudas;
    @NotNull
    @PositiveOrZero
    private BigDecimal montoExtra;
}
