package com.test.manga.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Map;

@Service
public class EasyOcrService {

    private static final String OCR_URL = "http://localhost:9000/ocr";

    public BufferedImage baixarImagem(String imageUrl) {
        try {
            return ImageIO.read(new URL(imageUrl));
        } catch (Exception e) {
            return null;
        }
    }

    public Map<String, Object> executarOCR(String imageUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String json = "{ \"imageUrl\": \"" + imageUrl + "\" }";

        HttpEntity<String> request = new HttpEntity<>(json, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response =
                restTemplate.postForEntity(OCR_URL, request, Map.class);

        return response.getBody();
    }

    public String executarOCR(BufferedImage imagem) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(imagem, "png", baos);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(baos.toByteArray()) {
                @Override
                public String getFilename() {
                    return "image.png";
                }
            });

            HttpEntity<MultiValueMap<String, Object>> request =
                    new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response =
                    restTemplate.postForEntity(OCR_URL, request, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            StringBuilder textoFinal = new StringBuilder();
            for (JsonNode n : root.get("resultados")) {
                textoFinal.append(n.get("texto").asText()).append("\n");
            }

            return textoFinal.toString().trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}