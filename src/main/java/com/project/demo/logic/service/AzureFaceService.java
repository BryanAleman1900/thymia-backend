package com.project.demo.logic.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AzureFaceService {

    @Value("${azure.face.endpoint}")
    private String faceEndpoint;

    @Value("${azure.face.subscription-key}")
    private String subscriptionKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 🔹 Paso 1: Detectar rostro y obtener faceId
    public String detectFace(String base64Image) throws Exception {
        String url = faceEndpoint + "/face/v1.0/detect?returnFaceId=true";

        // 🔍 Log de URL y longitud de la imagen
        System.out.println("🔸 DetectFace - URL: " + url);
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        System.out.println("🔸 DetectFace - Bytes: " + imageBytes.length);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set("Ocp-Apim-Subscription-Key", subscriptionKey);

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(imageBytes, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

        // 🔍 Log de status y body de respuesta
        System.out.println("🔸 DetectFace - Status: " + response.getStatusCode());
        System.out.println("🔸 DetectFace - Body: " + response.getBody());

        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.isArray() && root.size() > 0) {
                return root.get(0).get("faceId").asText();
            } else {
                throw new RuntimeException("No face detected in image.");
            }
        } else {
            throw new RuntimeException("Failed to detect face: " + response.getBody());
        }
    }


    // 🔹 Paso 2: Verificar dos faceId
    public double verifyFaces(String faceId1, String faceId2) throws Exception {
        String url = faceEndpoint + "/face/v1.0/verify";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Ocp-Apim-Subscription-Key", subscriptionKey);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("faceId1", faceId1);
        requestBody.put("faceId2", faceId2);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.get("confidence").asDouble(); // ✅ porcentaje de coincidencia
        } else {
            throw new RuntimeException("Failed to verify faces: " + response.getBody());
        }
    }
}
