package com.avalache_api.demo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.avalache_api.demo.application.AvalanchaService;
import com.avalache_api.demo.application.ReporteFinancieroService;
import com.avalache_api.demo.infrastructure.AvalanchaController;
import com.avalache_api.demo.infrastructure.UsuarioMapper;

@ExtendWith(MockitoExtension.class)
class AvalanchaControllerTest {
    private MockMvc mockMvc;

    @Mock
    private AvalanchaService avalanchaService;

    @Mock
    private ReporteFinancieroService reporteFinancieroService;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new AvalanchaController(
            avalanchaService,
            new UsuarioMapper(),
            reporteFinancieroService
        )).setValidator(validator).build();
    }

    @Test
    void descargaUnPdfConLosHeadersCorrectos() throws Exception {
        byte[] pdf = "%PDF-1.7\ncontenido de prueba".getBytes(StandardCharsets.US_ASCII);
        when(reporteFinancieroService.generar(any())).thenReturn(pdf);

        mockMvc.perform(post("/api/v1/avalancha/reporte")
                .contentType("application/json")
                .content("""
                    {
                      "nombreUsuario": "cliente-demo",
                      "montoExtra": 500000,
                      "deudas": [{
                        "nombreDeuda": "Tarjeta",
                        "saldo": 2500000,
                        "numCuotas": 24,
                        "pagoMinimo": 150000,
                        "tasaInteres": 0.025
                      }]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/pdf"))
            .andExpect(header().string(
                "Content-Disposition",
                "attachment; filename=\"reporte-financiero.pdf\""
            ))
            .andExpect(content().bytes(pdf));
    }

    @Test
    void rechazaDatosInvalidosAntesDeGenerarElPdf() throws Exception {
        mockMvc.perform(post("/api/v1/avalancha/reporte")
                .contentType("application/json")
                .content("""
                    {
                      "nombreUsuario": "",
                      "montoExtra": -1,
                      "deudas": []
                    }
                    """))
            .andExpect(status().isBadRequest());
    }
}
