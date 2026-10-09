package com.avalache_api.demo.application;

import com.avalache_api.demo.application.dto.ReporteFinanciero;

public interface PdfReportPort {
    byte[] generar(ReporteFinanciero reporte);
}
