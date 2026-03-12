package com.test.manga.controller;

import com.test.manga.service.EasyOcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/manhwa")
@CrossOrigin(origins = "*")
public class ManhwaTranslatorController {

    private final EasyOcrService ocrService;

    @Autowired
    public ManhwaTranslatorController(EasyOcrService ocrService) {
        this.ocrService = ocrService;
    }

    @PostMapping("/traduzir")
    public ResponseEntity<?> traduzir(@RequestBody Map<String, String> body) {

        System.out.println(" Cheguei no manhawaTranslatorController ");

        String imageUrl = body.get("imageUrl");
        if (imageUrl == null || imageUrl.isBlank()) {
            System.out.println("imageUrl ausente");
            return ResponseEntity.badRequest().body("imageUrl ausente");
        }

        
        Map<String, Object> resultado = ocrService.executarOCR(imageUrl);
        return ResponseEntity.ok(resultado);

    }
}

/*
 * CODIGO ANTIGO
 * private final OcrService ocrService;
 * private final TradutorService tradutorService;
 * 
 * @Autowired
 * public ManhwaTranslatorController(
 * OcrService ocrService,
 * TradutorService tradutorService
 * ) {
 * this.ocrService = ocrService;
 * this.tradutorService = tradutorService;
 * }
 * 
 * @PostMapping("/traduzir")
 * public ResponseEntity<TraducaoResponseDTO> traduzir(
 * 
 * @RequestBody Map<String, String> body
 * ) {
 * 
 * String imageUrl = body.get("imageUrl");
 * 
 * if (imageUrl == null || imageUrl.isBlank()) {
 * return ResponseEntity.badRequest().build();
 * }
 * 
 * // 1. Baixa imagem
 * BufferedImage imagem = ocrService.baixarImagem(imageUrl);
 * if (imagem == null) {
 * return ResponseEntity.badRequest().build();
 * }
 * 
 * // 2. OCR + detecção + tradução
 * List<BalaoDTO> baloes =
 * ocrService.extrairBaloesTraduzidos(imagem, tradutorService);
 * 
 * // 3. Monta resposta
 * TraducaoResponseDTO resp = new TraducaoResponseDTO();
 * resp.imageUrl = imageUrl;
 * resp.baloes = baloes;
 * 
 * return ResponseEntity.ok(resp);
 * }
 * 
 */
