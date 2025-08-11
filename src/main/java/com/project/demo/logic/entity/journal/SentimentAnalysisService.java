package com.project.demo.logic.entity.journal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class SentimentAnalysisService {

    @Value("${huggingface.api.key}")
    private String apiKey;

    @Value("${huggingface.model.sentiment}")
    private String modelId;

    @Value("${huggingface.inference.base-url:https://api-inference.huggingface.co}")
    private String baseUrl;

    @Value("${huggingface.inference.timeout-ms:6000}")
    private int timeoutMs;

    private final ObjectMapper mapper = new ObjectMapper();

    /** Alias usado por JournalEntryService */
    public SentimentResult analyze(String text) {
        return analyzeSentiment(text);
    }

    public SentimentResult analyzeSentiment(String text) {
        if (text == null || text.isBlank()) {
            return new SentimentResult("UNKNOWN", 0.0);
        }

        String apiUrl = String.format("%s/models/%s", baseUrl, modelId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, Object> body = Map.of("inputs", text);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // RestTemplate con timeouts usando SimpleClientHttpRequestFactory (opción C)
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);

        RestTemplate restTemplate = new RestTemplate(factory);
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("HuggingFace inference error: " + response.getStatusCode());
        }

        return parseResponse(response.getBody());
    }

    private SentimentResult parseResponse(String json) {
        try {
            JsonNode root = mapper.readTree(json);

            // HF puede devolver:
            // a) [ {label, score}, ... ]
            // b) [ [ {label, score}, ... ] ]
            JsonNode first = root;
            if (root.isArray()) {
                first = root.get(0);
                if (first != null && first.isArray() && first.size() > 0) {
                    first = first.get(0);
                }
            }

            if (first == null || !first.has("label") || !first.has("score")) {
                throw new IllegalStateException("Unexpected HF payload: " + json);
            }

            String rawLabel = first.get("label").asText();
            double score = first.get("score").asDouble();

            return new SentimentResult(normalize(rawLabel), score);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse HuggingFace response", e);
        }
    }

    /** Normaliza etiquetas: LABEL_0/1/2 y negative/neutral/positive */
    private String normalize(String raw) {
        if (raw == null) return "UNKNOWN";
        String up = raw.toUpperCase(Locale.ROOT).trim();
        switch (up) {
            case "LABEL_0":
            case "NEGATIVE":
                return "NEGATIVE";
            case "LABEL_1":
            case "NEUTRAL":
                return "NEUTRAL";
            case "LABEL_2":
            case "POSITIVE":
                return "POSITIVE";
            default:
                return up;
        }
    }

    public record SentimentResult(String label, double score) {}
}





