package com.test.manga.service;

import com.test.manga.model.BalaoDetectado;
import com.test.manga.util.ConverterImagemUtil;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@Service
public class BalaoDetectorService {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public List<BalaoDetectado> detectarBaloes(BufferedImage pagina) {

        Mat src = ConverterImagemUtil.bufferedImageToMat(pagina);
        List<BalaoDetectado> resultado = new ArrayList<>();

        if (src == null || src.empty()) return resultado;

        Mat gray = new Mat();
        Mat blur = new Mat();
        Mat thresh = new Mat();

        // 1️⃣ CINZA
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        // 2️⃣ BLUR MAIS FORTE
        Imgproc.GaussianBlur(gray, blur, new Size(7, 7), 0);

        // 3️⃣ THRESHOLD CERTO (sem INV)
        Imgproc.adaptiveThreshold(
                blur,
                thresh,
                255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY,
                31,
                2
        );

        // 4️⃣ CONTORNOS EXTERNOS
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(
                thresh,
                contours,
                new Mat(),
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE
        );

        for (MatOfPoint contour : contours) {

            Rect r = Imgproc.boundingRect(contour);
            double area = Imgproc.contourArea(contour);

            // área mínima
            if (area < 4000) continue;

            // tamanho mínimo
            if (r.width < 80 || r.height < 60) continue;

            // proporção típica de balão
            double ratio = (double) r.width / r.height;
            if (ratio < 0.4 || ratio > 2.5) continue;

            // ignorar rodapé
            if (r.y > src.height() * 0.92) continue;

            // 5️⃣ BALÃO PRECISA SER CLARO
            Mat roiGray = new Mat(gray, r);
            Scalar mean = Core.mean(roiGray);
            if (mean.val[0] < 180) continue;

            // tipo
            String tipo = area > 12000 ? "BALAO" : "NARRACAO";

            Mat recorte = new Mat(src, r);
            resultado.add(new BalaoDetectado(r, recorte, tipo));
        }

        System.out.println("Balões detectados (reais): " + resultado.size());
        return resultado;
    }

    public Mat imgToMat(BufferedImage imagemOriginal) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'imgToMat'");
    }

    public List<Rect> detectarCoordenadas(Mat frame) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'detectarCoordenadas'");
    }
}
