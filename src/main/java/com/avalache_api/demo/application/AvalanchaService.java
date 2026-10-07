package com.avalache_api.demo.application;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import org.springframework.stereotype.Service;

import com.avalache_api.demo.domain.Usuario;
import com.avalache_api.demo.domain.DeudaUsuario;

@Service
public class AvalanchaService {
    private static final int MAX_MESES_SIMULACION = 600;

    public Integer ejecutarAvalancha(Usuario usuario) {
        PriorityQueue<DeudaUsuario> deudas = new PriorityQueue<>(usuario.getDeudas());
        int mesesTranscurridos = 0;
        BigDecimal montoExtraGlobal = usuario.getMontoExtra();

        while (!deudas.isEmpty()) {
            mesesTranscurridos++;

            if (mesesTranscurridos > MAX_MESES_SIMULACION) {
                return -1;
            }

            montoExtraGlobal = aplicarFaseMantenimiento(deudas, montoExtraGlobal);
            montoExtraGlobal = aplicarFaseVoraz(deudas, montoExtraGlobal);
        }

        return mesesTranscurridos;
    }

    private BigDecimal aplicarFaseMantenimiento(
        PriorityQueue<DeudaUsuario> deudas,
        BigDecimal montoExtraGlobal
    ) {
        List<DeudaUsuario> deudasSobrevivientes = new ArrayList<>();

        while (!deudas.isEmpty()) {
            DeudaUsuario deuda = deudas.poll();
            BigDecimal saldoConInteres = deuda.getSaldo().add(
                deuda.getSaldo().multiply(deuda.getTasaInteres())
            );
            deuda.setSaldo(saldoConInteres);

            if (saldoConInteres.compareTo(deuda.getPagoMinimo()) <= 0) {
                montoExtraGlobal = montoExtraGlobal.add(deuda.getPagoMinimo());
            } else {
                deuda.setSaldo(saldoConInteres.subtract(deuda.getPagoMinimo()));
                deudasSobrevivientes.add(deuda);
            }
        }

        deudas.addAll(deudasSobrevivientes);
        return montoExtraGlobal;
    }

    private BigDecimal aplicarFaseVoraz(
        PriorityQueue<DeudaUsuario> deudas,
        BigDecimal montoExtraGlobal
    ) {
        BigDecimal dineroParaAtacar = montoExtraGlobal;

        while (dineroParaAtacar.compareTo(BigDecimal.ZERO) > 0 && !deudas.isEmpty()) {
            DeudaUsuario deuda = deudas.poll();

            if (dineroParaAtacar.compareTo(deuda.getSaldo()) >= 0) {
                dineroParaAtacar = dineroParaAtacar.subtract(deuda.getSaldo());
                montoExtraGlobal = montoExtraGlobal.add(deuda.getPagoMinimo());
            } else {
                deuda.setSaldo(deuda.getSaldo().subtract(dineroParaAtacar));
                dineroParaAtacar = BigDecimal.ZERO;
                deudas.add(deuda);
            }
        }

        return montoExtraGlobal;
    }
}
