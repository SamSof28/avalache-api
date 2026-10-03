package com.avalache_api.demo.domain;
import java.util.List;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

import com.avalache_api.demo.domain.DeudaUsuario;

public class Usuario {
    @Getter @Setter
    private String nombreUsuario;

    @Getter @Setter
    private List<DeudaUsuario> deudas;

    @Getter @Setter
    private BigDecimal ingresos;

    @Getter @Setter
    private BigDecimal monto_extra;

    public Usuario(String nombreUsuario, List<DeudaUsuario> deudas,
        BigDecimal ingresos, BigDecimal monto_extra){
            this.nombreUsuario = nombreUsuario;
            this.deudas = deudas;
            this.ingresos = ingresos;
            this.monto_extra = monto_extra;
        }
}  
