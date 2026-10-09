package com.avalache_api.demo.application;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import org.springframework.stereotype.Service;

import com.avalache_api.demo.application.dto.DeudaResultado;
import com.avalache_api.demo.application.dto.ResultadoAvalancha;
import com.avalache_api.demo.domain.Usuario;
import com.avalache_api.demo.domain.DeudaUsuario;

@Service
public class AvalanchaService {
    private static final int MAX_MESES_SIMULACION = 600;

    public Integer ejecutarAvalancha(Usuario usuario) {
        Simulacion simulacion = ejecutarSimulacion(usuario.getDeudas(), usuario.getMontoExtra(), true);
        return simulacion.resultado().mesesEstimados();
    }

    public ResultadoAvalancha simular(Usuario usuario) {
        Simulacion simulacion = ejecutarSimulacion(usuario.getDeudas(), usuario.getMontoExtra(), false);
        List<DeudaResultado> deudas = usuario.getDeudas().stream()
            .map(deuda -> new DeudaResultado(
                deuda.getNombreDeuda(),
                deuda.getSaldo(),
                simulacion.resultado().alcanzable() ? BigDecimal.ZERO : deuda.getSaldo(),
                deuda.getTasaInteres()))
            .toList();

        return new ResultadoAvalancha(
            simulacion.resultado().mesesEstimados(),
            simulacion.resultado().alcanzable(),
            simulacion.intereses(),
            deudas
        );
    }

    private Simulacion ejecutarSimulacion(
        List<DeudaUsuario> deudasEntrada,
        BigDecimal montoExtra,
        boolean mutarEntrada
    ) {
        PriorityQueue<DeudaUsuario> deudas = new PriorityQueue<>();
        deudasEntrada.forEach(deuda -> deudas.add(mutarEntrada ? deuda : copiar(deuda)));
        int mesesTranscurridos = 0;
        BigDecimal montoExtraGlobal = montoExtra;
        BigDecimal intereses = BigDecimal.ZERO;

        while (!deudas.isEmpty()) {
            mesesTranscurridos++;

            if (mesesTranscurridos > MAX_MESES_SIMULACION) {
                return new Simulacion(
                    new ResultadoAvalancha(-1, false, intereses, List.of()),
                    intereses
                );
            }

            intereses = intereses.add(interesDelMes(deudas));
            montoExtraGlobal = aplicarFaseMantenimiento(deudas, montoExtraGlobal);
            montoExtraGlobal = aplicarFaseVoraz(deudas, montoExtraGlobal);
        }

        return new Simulacion(
            new ResultadoAvalancha(mesesTranscurridos, true, intereses, List.of()),
            intereses
        );
    }

    private static DeudaUsuario copiar(DeudaUsuario deuda) {
        return new DeudaUsuario(
            deuda.getNombreDeuda(),
            deuda.getSaldo(),
            deuda.getPagoMinimo(),
            deuda.getTasaInteres()
        );
    }

    private static BigDecimal interesDelMes(PriorityQueue<DeudaUsuario> deudas) {
        return deudas.stream()
            .map(deuda -> deuda.getSaldo().multiply(deuda.getTasaInteres()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
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
                deuda.setSaldo(BigDecimal.ZERO);
                montoExtraGlobal = montoExtraGlobal.add(deuda.getPagoMinimo());
            } else {
                deuda.setSaldo(saldoConInteres.subtract(deuda.getPagoMinimo()));
                deudasSobrevivientes.add(deuda);
            }
        }

        deudas.addAll(deudasSobrevivientes);
        return montoExtraGlobal;
    }

    private record Simulacion(ResultadoAvalancha resultado, BigDecimal intereses) {}

    private BigDecimal aplicarFaseVoraz(
        PriorityQueue<DeudaUsuario> deudas,
        BigDecimal montoExtraGlobal
    ) {
        BigDecimal dineroParaAtacar = montoExtraGlobal;

        while (dineroParaAtacar.compareTo(BigDecimal.ZERO) > 0 && !deudas.isEmpty()) {
            DeudaUsuario deuda = deudas.poll();

            if (dineroParaAtacar.compareTo(deuda.getSaldo()) >= 0) {
                dineroParaAtacar = dineroParaAtacar.subtract(deuda.getSaldo());
                deuda.setSaldo(BigDecimal.ZERO);
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
