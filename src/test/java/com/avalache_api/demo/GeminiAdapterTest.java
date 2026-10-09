package com.avalache_api.demo;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.avalache_api.demo.application.AvalanchaService;
import com.avalache_api.demo.application.dto.ResultadoAvalancha;
import com.avalache_api.demo.domain.DeudaUsuario;
import com.avalache_api.demo.domain.Usuario;
import com.avalache_api.demo.infrastructure.gemini.GeminiAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;

class GeminiAdapterTest {
    @Test
    void usaRecomendacionLocalCuandoNoHayClaveConfigurada() {
        Usuario usuario = new Usuario(
            "cliente",
            List.of(new DeudaUsuario(
                "Tarjeta",
                new BigDecimal("50000"),
                new BigDecimal("100000"),
                BigDecimal.ZERO
            )),
            BigDecimal.ZERO
        );
        ResultadoAvalancha resultado = new AvalanchaService().simular(usuario);
        GeminiAdapter adapter = new GeminiAdapter(
            new ObjectMapper(),
            "",
            "http://localhost:1",
            "modelo"
        );

        String consejo = adapter.generarConsejo(usuario, resultado);

        assertTrue(consejo.contains("deuda"));
    }
}
