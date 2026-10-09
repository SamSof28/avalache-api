package com.avalache_api.demo.application;

import org.springframework.stereotype.Service;

import com.avalache_api.demo.application.dto.ReporteFinanciero;
import com.avalache_api.demo.application.dto.ResultadoAvalancha;
import com.avalache_api.demo.domain.Usuario;

@Service
public class ReporteFinancieroService {
    private final AvalanchaService avalanchaService;
    private final ConsejoFinancieroPort consejoFinancieroPort;
    private final PdfReportPort pdfReportPort;

    public ReporteFinancieroService(
        AvalanchaService avalanchaService,
        ConsejoFinancieroPort consejoFinancieroPort,
        PdfReportPort pdfReportPort
    ) {
        this.avalanchaService = avalanchaService;
        this.consejoFinancieroPort = consejoFinancieroPort;
        this.pdfReportPort = pdfReportPort;
    }

    public byte[] generar(Usuario usuario) {
        ResultadoAvalancha resultado = avalanchaService.simular(usuario);
        String consejo = consejoFinancieroPort.generarConsejo(usuario, resultado);
        return pdfReportPort.generar(new ReporteFinanciero(
            usuario.getNombreUsuario(),
            resultado,
            consejo
        ));
    }
}
