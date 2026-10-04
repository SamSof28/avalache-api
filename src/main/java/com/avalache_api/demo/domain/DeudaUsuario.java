package com.avalache_api.demo.domain;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

public class DeudaUsuario implements Comparable<DeudaUsuario> {
    @Getter @Setter
    private String nombreDeuda ;

    @Getter @Setter
    private BigDecimal saldo;

    @Getter @Setter
    private BigDecimal pagoMinimo;

    @Getter @Setter
    private BigDecimal tasaInteres;

    public DeudaUsuario(String nombreDeuda, BigDecimal saldo,
         BigDecimal pagoMinimo, BigDecimal tasaInteres){
            this.nombreDeuda = nombreDeuda;
            this.saldo = saldo;
            this.pagoMinimo = pagoMinimo;
            this.tasaInteres = tasaInteres;
         }

    @Override 
    public int compareTo(DeudaUsuario otraDeuda){
        if (this.tasaInteres == null && otraDeuda.tasaInteres == null) {
            return this.saldo.compareTo(otraDeuda.saldo);
        }
        if (this.tasaInteres == null) {
            return 1;
        }
        if (otraDeuda.tasaInteres == null) {
            return -1;
        }

        // Orden descendente: la tasa mas alta tiene prioridad.
        int result = otraDeuda.tasaInteres.compareTo(this.tasaInteres);

        if (result != 0){
            return result;
        }

        return this.saldo.compareTo(otraDeuda.saldo);
    }
    
}
