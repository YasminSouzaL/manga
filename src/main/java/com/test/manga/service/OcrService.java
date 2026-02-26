package com.test.manga.service;

import com.test.manga.model.BalaoDetectado;
import com.test.manga.service.DTO.BalaoDTO;
import com.test.manga.util.ConverterImagemUtil;
import com.test.manga.util.TextoLimpezaUtil;
import net.sourceforge.tess4j.Tesseract;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;

import javax.imageio.ImageIO;

@Service
public class OcrService {

    @Autowired
    private BalaoDetectorService balaoDetectorService;

    public BufferedImage baixarImagem(String imageUrl) {
        try {
            return ImageIO.read(new URL(imageUrl));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final String TESSDATA_PATH =
            "C:/Program Files/Tesseract-OCR/tessdata";

    /* ===============================
       PIPELINE PRINCIPAL
    =============================== */
    public List<BalaoDTO> extrairBaloesTraduzidos(
            BufferedImage pagina,
            TradutorService tradutorService
    ) {

        List<BalaoDTO> resultado = new ArrayList<>();
        if (pagina == null) return resultado;

        List<BalaoDetectado> baloes =
                balaoDetectorService.detectarBaloes(pagina);

        if (baloes.isEmpty()) return resultado;

        Tesseract tesseract = criarTesseract();
        int id = 1;

        for (BalaoDetectado balao : baloes) {

            String textoOriginal = ocrPorLinhas(tesseract, balao.imagem);
            if (textoOriginal.isBlank()) continue;

            String traducao =
                    tradutorService.traduzir(textoOriginal, "en", "pt");

            BalaoDTO dto = new BalaoDTO();
            dto.id = id++;
            dto.x = balao.rect.x;
            dto.y = balao.rect.y;
            dto.w = balao.rect.width;
            dto.h = balao.rect.height;
            dto.tipo = balao.tipo;
            dto.textoOriginal = textoOriginal;
            dto.traducao = traducao;

            resultado.add(dto);
        }

        return resultado;
    }

    /* ===============================
       TESSERACT CONFIG
    =============================== */
    private Tesseract criarTesseract() {
        Tesseract t = new Tesseract();
        t.setDatapath(TESSDATA_PATH);
        t.setLanguage("eng");
        t.setOcrEngineMode(1); // LSTM
        t.setPageSegMode(7);   
        return t;
    }

    /* ===============================
       OCR POR LINHAS (CHAVE DA SOLUÇÃO)
    =============================== */
    private String ocrPorLinhas(Tesseract tesseract, Mat balao) {

        Mat preprocessado = preprocessar(balao);
        List<Rect> linhas = detectarLinhas(preprocessado);

        StringBuilder textoFinal = new StringBuilder();

        for (Rect linha : linhas) {
            Mat recorte = new Mat(preprocessado, linha);
            BufferedImage img =
                    ConverterImagemUtil.matToBufferedImage(recorte);

            try {
                String txt = tesseract.doOCR(img);
                txt = TextoLimpezaUtil.limparTexto(txt);

                if (!txt.isBlank()) {
                    textoFinal.append(txt).append("\n");
                }
            } catch (Exception ignored) {}
        }

        return textoFinal.toString().trim();
    }

    /* ===============================
       PRÉ-PROCESSAMENTO FORTE
    =============================== */
    private Mat preprocessar(Mat src) {

        Mat gray = new Mat();
        Mat blur = new Mat();
        Mat thresh = new Mat();

        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray, blur, new Size(3, 3), 0);

        Imgproc.adaptiveThreshold(
                blur,
                thresh,
                255,
                Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY_INV,
                15,
                3
        );

        return thresh;
    }

    /* ===============================
       DETECTA LINHAS DE TEXTO
    =============================== */
    private List<Rect> detectarLinhas(Mat binaria) {

        List<MatOfPoint> contornos = new ArrayList<>();
        Imgproc.findContours(
                binaria,
                contornos,
                new Mat(),
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE
        );

        List<Rect> linhas = new ArrayList<>();

        for (MatOfPoint c : contornos) {
            Rect r = Imgproc.boundingRect(c);

            // filtros de linha
            if (r.height < 15 || r.width < 40) continue;

            linhas.add(r);
        }

        
        linhas.sort((a, b) -> Integer.compare(a.y, b.y));

        return linhas;
    }

    public String executarOCR(BufferedImage imagem) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'executarOCR'");
    }
}