package com.avalache_api.demo.infrastructure.pdf;

import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Service;

import com.avalache_api.demo.application.PdfReportPort;
import com.avalache_api.demo.application.dto.DeudaResultado;
import com.avalache_api.demo.application.dto.ReporteFinanciero;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class OpenPdfReportAdapter implements PdfReportPort {
    @Override
    public byte[] generar(ReporteFinanciero reporte) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, output);
            document.open();

            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            document.add(new Paragraph("Reporte financiero - Avalancha", title));
            document.add(new Paragraph("Usuario: " + reporte.nombreUsuario()));

            if (reporte.resultado().alcanzable()) {
                document.add(new Paragraph(
                    "Tiempo estimado para libertad financiera: "
                        + reporte.resultado().mesesEstimados() + " meses"
                ));
            } else {
                document.add(new Paragraph(
                    "La simulación no alcanza el límite máximo de 600 meses."
                ));
            }

            document.add(new Paragraph(
                "Intereses estimados: " + reporte.resultado().interesesEstimados()
            ));
            document.add(new Paragraph("Recomendación financiera"));
            document.add(new Paragraph(reporte.consejo()));
            document.add(new Paragraph("Resumen de deudas"));

            for (DeudaResultado deuda : reporte.resultado().deudas()) {
                document.add(new Paragraph(
                    deuda.nombre() + ": saldo inicial " + deuda.saldoInicial()
                        + ", saldo final " + deuda.saldoFinal()
                ));
            }

            document.add(new Paragraph(
                "Este documento es una simulación educativa y no constituye asesoría "
                    + "financiera, legal ni crediticia."
            ));
            document.close();
            return output.toByteArray();
        } catch (DocumentException exception) {
            throw new IllegalStateException("No fue posible generar el reporte PDF", exception);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("No fue posible escribir el reporte PDF", exception);
        }
    }
}
