package com.test.manga.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TradutorServiceImpl implements TradutorService {

    private static final String LIBRE_URL = "http://localhost:5000/translate";


    @Autowired
    private RestTemplate restTemplate;
    
    @Override
    public String traduzir(String texto, String source, String target) {
        System.out.println("TRADUTOR FOI CHAMADO:");
        String url = "http://127.0.0.1:5000/translate";
        System.out.println("Enviando para o Docker: " + texto); 

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, Object> body = new HashMap<>();
        body.put("q", texto);
        body.put("source", source);
        body.put("target", target);
        body.put("format", "text");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        String bodyResponse = response.getBody();

        // DEBUG CRÍTICO
        System.out.println("Resposta bruta da tradução:");
        System.out.println(bodyResponse);

        // Se vier HTML → bloqueio ou URL errada
        if (bodyResponse == null || bodyResponse.trim().startsWith("<")) {
            return "Erro: serviço de tradução retornou HTML (bloqueio ou limite)";
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(bodyResponse);
            return json.get("translatedText").asText();
            
        } catch (JsonProcessingException e) {
            System.err.println("Erro ao ler JSON: " + e.getMessage());
            return "Erro no processamento da tradução";
            
        }
    }

}
