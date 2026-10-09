package com.avalache_api.demo.infrastructure.gemini;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.avalache_api.demo.application.ConsejoFinancieroPort;
import com.avalache_api.demo.application.dto.ResultadoAvalancha;
import com.avalache_api.demo.domain.DeudaUsuario;
import com.avalache_api.demo.domain.Usuario;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeminiAdapter implements ConsejoFinancieroPort {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeminiAdapter.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String apiUrl;
    private final String model;

    public GeminiAdapter(
        ObjectMapper objectMapper,
        @Value("${gemini.api.key:}") String apiKey,
        @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta}") String apiUrl,
        @Value("${gemini.api.model:gemini-2.5-flash}") String model
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10_000);
        requestFactory.setReadTimeout(15_000);
        this.restClient = RestClient.builder()
            .requestFactory(requestFactory)
            .build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.model = model;
    }

    @Override
    public String generarConsejo(Usuario usuario, ResultadoAvalancha resultado) {
        if (apiKey.isBlank()) {
            LOGGER.info("Gemini no está configurado; se usará una recomendación local");
            return recomendacionLocal(resultado);
        }

        try {
            String body = objectMapper.writeValueAsString(new GeminiRequest(
                List.of(new Content(List.of(new Part(construirPrompt(usuario, resultado))))),
                new GenerationConfig(0.3, 300)
            ));

            String response = restClient.post()
                .uri(apiUrl + "/models/" + model + ":generateContent")
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

            String consejo = extraerTexto(response);
            LOGGER.info("Gemini generó correctamente el consejo financiero");
            return consejo;
        } catch (HttpStatusCodeException exception) {
            LOGGER.warn(
                "Gemini rechazó la solicitud con HTTP {}: {}",
                exception.getStatusCode().value(),
                resumirError(exception.getResponseBodyAsString())
            );
            return recomendacionLocal(resultado);
        } catch (RestClientException | java.io.IOException exception) {
            LOGGER.warn(
                "No fue posible conectar con Gemini ({}); se usará una recomendación local",
                exception.getClass().getSimpleName()
            );
            return recomendacionLocal(resultado);
        }
    }

    private static String resumirError(String responseBody) {
        String normalized = responseBody.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 300 ? normalized : normalized.substring(0, 300) + "...";
    }

    private String extraerTexto(String response) throws java.io.IOException {
        JsonNode root = objectMapper.readTree(response);
        JsonNode text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (!text.isTextual() || text.asText().isBlank()) {
            throw new IllegalStateException("Gemini devolvió una respuesta sin texto");
        }
        return text.asText().trim();
    }

    private static String construirPrompt(Usuario usuario, ResultadoAvalancha resultado) {
        StringBuilder prompt = new StringBuilder("""
            Actúa como orientador de educación financiera en Colombia.
            Entrega una recomendación breve, clara y responsable en español.
            No prometas resultados garantizados, no recomiendes nuevos créditos
            y aclara que la simulación no reemplaza asesoría profesional.
            El usuario puede quedar libre de deudas en %d meses.
            Intereses estimados: %s.
            Deudas:
            """.formatted(
                resultado.mesesEstimados(),
                resultado.interesesEstimados()
            ));

        for (DeudaUsuario deuda : usuario.getDeudas()) {
            prompt.append("- ")
                .append(deuda.getNombreDeuda())
                .append(": saldo ")
                .append(deuda.getSaldo())
                .append(", tasa mensual ")
                .append(deuda.getTasaInteres())
                .append(", pago mínimo ")
                .append(deuda.getPagoMinimo())
                .append('\n');
        }
        return prompt.toString();
    }

    private static String recomendacionLocal(ResultadoAvalancha resultado) {
        if (!resultado.alcanzable()) {
            return "La simulación no alcanza a liquidar las deudas en el plazo máximo "
                + "evaluado. Revisa tus pagos mínimos, reduce gastos no esenciales y "
                + "busca orientación financiera profesional.";
        }
        return "Prioriza siempre la deuda con mayor tasa de interés y conserva los pagos "
            + "mínimos de las demás. Cuando una deuda termine, dirige ese pago liberado "
            + "a la siguiente obligación. Mantén un presupuesto y un fondo para imprevistos.";
    }

    private record GeminiRequest(List<Content> contents, GenerationConfig generationConfig) {}
    private record Content(List<Part> parts) {}
    private record Part(String text) {}
    private record GenerationConfig(double temperature, int maxOutputTokens) {}
}
