package com.avalache_api.demo.domain;
import java.util.List;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;


public class Usuario {
    @Getter @Setter
    private String nombreUsuario;

    @Getter @Setter
    private List<DeudaUsuario> deudas;

    @Getter @Setter
    private BigDecimal montoExtra;

    public Usuario(String nombreUsuario, List<DeudaUsuario> deudas,
         BigDecimal montoExtra){
            this.nombreUsuario = nombreUsuario;
            this.deudas = deudas;
            this.montoExtra = montoExtra;
        }
}  
