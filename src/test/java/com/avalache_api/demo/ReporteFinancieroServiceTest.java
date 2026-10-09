package com.avalache_api.demo;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.avalache_api.demo.application.AvalanchaService;
import com.avalache_api.demo.application.ConsejoFinancieroPort;
import com.avalache_api.demo.application.ReporteFinancieroService;
import com.avalache_api.demo.application.dto.ResultadoAvalancha;
import com.avalache_api.demo.domain.DeudaUsuario;
import com.avalache_api.demo.domain.Usuario;
import com.avalache_api.demo.infrastructure.pdf.OpenPdfReportAdapter;

class ReporteFinancieroServiceTest {
    @Test
    void generaUnPdfConElResultadoYLaRecomendacion() {
        Usuario usuario = new Usuario(
            "cliente",
            List.of(new DeudaUsuario(
                "Tarjeta",
                new BigDecimal("500000"),
                new BigDecimal("100000"),
                new BigDecimal("0.02")
            )),
            new BigDecimal("100000")
        );
        ConsejoFinancieroPort consejo = (u, r) -> "Prioriza la deuda con mayor tasa.";
        ReporteFinancieroService service = new ReporteFinancieroService(
            new AvalanchaService(),
            consejo,
            new OpenPdfReportAdapter()
        );

        byte[] pdf = service.generar(usuario);

        assertTrue(pdf.length > 500);
        assertTrue(new String(pdf, 0, 4).equals("%PDF"));
    }

    @Test
    void laSimulacionParaReporteNoMutaLasDeudasDeEntrada() {
        DeudaUsuario deuda = new DeudaUsuario(
            "Tarjeta",
            new BigDecimal("50000"),
            new BigDecimal("100000"),
            BigDecimal.ZERO
        );
        Usuario usuario = new Usuario("cliente", List.of(deuda), BigDecimal.ZERO);

        ResultadoAvalancha resultado = new AvalanchaService().simular(usuario);

        assertTrue(resultado.alcanzable());
        assertTrue(deuda.getSaldo().compareTo(new BigDecimal("50000")) == 0);
    }
}
