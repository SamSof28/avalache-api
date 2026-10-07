package com.avalache_api.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.avalache_api.demo.application.AvalanchaService;
import com.avalache_api.demo.domain.DeudaUsuario;
import com.avalache_api.demo.domain.Usuario;

class AvalanchaServiceTest {
    private final AvalanchaService service = new AvalanchaService();

    @Test
    void liberaElPagoMinimoCuandoUnaDeudaTerminaEnMantenimiento() {
        DeudaUsuario creditoRotativo = deuda("Rotativo", "50000", "100000", "0");
        DeudaUsuario deudaGrande = deuda("Grande", "300000", "100000", "0");
        Usuario usuario = usuario(List.of(creditoRotativo, deudaGrande), "0");

        assertEquals(2, service.ejecutarAvalancha(usuario));
        assertEquals(new BigDecimal("0"), creditoRotativo.getSaldo());
        assertEquals(new BigDecimal("0"), deudaGrande.getSaldo());
    }

    @Test
    void usaElPagoMinimoLiberadoParaAtacarOtraDeudaEnElSiguienteMes() {
        DeudaUsuario deudaCorta = deuda("Corta", "100000", "100000", "0");
        DeudaUsuario deudaLarga = deuda("Larga", "250000", "100000", "0");
        Usuario usuario = usuario(List.of(deudaCorta, deudaLarga), "0");

        assertEquals(2, service.ejecutarAvalancha(usuario));
        assertEquals(new BigDecimal("0"), deudaCorta.getSaldo());
        assertEquals(new BigDecimal("0"), deudaLarga.getSaldo());
    }

    @Test
    void noPagaMasQueElSaldoRestante() {
        DeudaUsuario deuda = deuda("Deuda", "50000", "100000", "0");
        Usuario usuario = usuario(List.of(deuda), "0");

        assertEquals(1, service.ejecutarAvalancha(usuario));
        assertEquals(new BigDecimal("0"), deuda.getSaldo());
    }

    @Test
    void detieneLaSimulacionCuandoLaDeudaNoSeAmortiza() {
        DeudaUsuario deuda = deuda("Deuda", "100000", "0", "0.01");
        Usuario usuario = usuario(List.of(deuda), "0");

        assertEquals(-1, service.ejecutarAvalancha(usuario));
    }

    private static Usuario usuario(List<DeudaUsuario> deudas, String montoExtra) {
        return new Usuario("cliente", deudas, new BigDecimal(montoExtra));
    }

    private static DeudaUsuario deuda(
        String nombre,
        String saldo,
        String pagoMinimo,
        String tasaInteres
    ) {
        return new DeudaUsuario(
            nombre,
            new BigDecimal(saldo),
            new BigDecimal(pagoMinimo),
            new BigDecimal(tasaInteres)
        );
    }
}
