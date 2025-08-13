package com.project.demo.logic.entity.journal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Service
@Slf4j
public class SentimentAnalysisService {

    @Value("${journal.sentiment.enabled:true}")
    private boolean enabled;

    @Value("${huggingface.api.key:}")
    private String apiKey;

    @Value("${huggingface.model.sentiment}")
    private String modelId;

    @Value("${huggingface.inference.base-url:https://api-inference.huggingface.co}")
    private String baseUrl;

    @Value("${huggingface.inference.timeout-ms:6000}")
    private int timeoutMs;

    @Value("${huggingface.inference.wait-for-model:true}")
    private boolean waitForModel;

    private final ObjectMapper mapper = new ObjectMapper();


    public SentimentResult analyze(String text) {
        return analyzeSentiment(text);
    }

    public SentimentResult analyzeSentiment(String text) {
        if (!enabled) {
            log.debug("Sentiment deshabilitado por configuración; devolviendo vacío.");
            return new SentimentResult(null, 0d);
        }
        if (text == null || text.isBlank()) {
            return new SentimentResult(null, 0d);
        }

        final String token = apiKey == null ? null : apiKey.trim();
        if (token == null || token.isEmpty()) {
            log.warn("HuggingFace API key no configurada; guardo sin label/score.");
            return new SentimentResult(null, 0d);
        }


        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .pathSegment("models")
                .pathSegment(modelId.split("/"))
                .queryParam("wait_for_model", String.valueOf(waitForModel))
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token); // Authorization: Bearer hf_...
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        // Body recomendado para text-classification
        Map<String, Object> body = Map.of(
                "inputs", List.of(text),
                "parameters", Map.of("return_all_scores", true),
                "options", Map.of("wait_for_model", waitForModel)
        );

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);

        RestTemplate rest = new RestTemplate(factory);
        ResponseEntity<String> resp;

        try {
            resp = rest.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        } catch (HttpClientErrorException.BadRequest e) {
            // Algunos pipelines devuelven 400 con el body extendido; probamos el mínimo
            log.warn("HF 400 con body extendido: {}", safeBody(e));
            Map<String, Object> fallback = Map.of("inputs", List.of(text));
            resp = rest.postForEntity(url, new HttpEntity<>(fallback, headers), String.class);
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("401 en Hugging Face Inference. Revisa huggingface.api.key / permisos del token.");
            return new SentimentResult(null, 0d);
        } catch (Exception e) {
            log.warn("Fallo al analizar sentimiento; guardo sin label/score. Causa: {}", e.getMessage());
            return new SentimentResult(null, 0d);
        }

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            log.warn("HuggingFace inference no-2xx: {}", resp.getStatusCode());
            return new SentimentResult(null, 0d);
        }

        return parseResponse(resp.getBody());
    }

    private String safeBody(HttpClientErrorException e) {
        try { return e.getResponseBodyAsString(); }
        catch (Exception ignore) { return "<sin cuerpo>"; }
    }

    private SentimentResult parseResponse(String json) {
        try {
            JsonNode root = mapper.readTree(json);

            // Respuestas típicas:
            // A) [ {label, score}, ... ]
            // B) [ [ {label, score}, ... ] ]
            JsonNode arr = root;
            if (root.isArray() && root.size() > 0 && root.get(0).isArray()) {
                arr = root.get(0);
            }

            String bestLabel = null;
            double bestScore = -1;

            if (arr.isArray()) {
                for (JsonNode n : arr) {
                    double s = n.path("score").asDouble(Double.NaN);
                    String l = n.path("label").asText(null);
                    if (!Double.isNaN(s) && l != null && s > bestScore) {
                        bestScore = s;
                        bestLabel = l;
                    }
                }
            }

            if (bestLabel == null) return new SentimentResult(null, 0d);
            return new SentimentResult(normalize(bestLabel), bestScore < 0 ? 0d : bestScore);

        } catch (Exception e) {
            log.warn("No pude parsear respuesta de Hugging Face: {}", e.getMessage());
            return new SentimentResult(null, 0d);
        }
    }


    private String normalize(String raw) {
        if (raw == null) return null;
        String up = raw.toUpperCase(Locale.ROOT).trim();
        switch (up) {
            case "LABEL_0":
            case "NEGATIVE": return "NEGATIVE";
            case "LABEL_1":
            case "NEUTRAL":  return "NEUTRAL";
            case "LABEL_2":
            case "POSITIVE": return "POSITIVE";
            default:         return up;
        }
    }

    public record SentimentResult(String label, double score) {}
}