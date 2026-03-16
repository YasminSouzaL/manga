package com.test.manga.service;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import com.test.manga.util.ConverterImagemUtil;
import java.awt.image.BufferedImage;


public class TextoDetectorService {
    public List<Rect> detectarRegioesTexto(BufferedImage img) {

    Mat mat = ConverterImagemUtil.bufferedImageToMat(img);

    Mat gray = new Mat();
    Mat bin = new Mat();

    Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);

    Imgproc.adaptiveThreshold(
            gray,
            bin,
            255,
            Imgproc.ADAPTIVE_THRESH_MEAN_C,
            Imgproc.THRESH_BINARY_INV,
            15,
            3
    );

    List<MatOfPoint> contornos = new ArrayList<>();

    Imgproc.findContours(
            bin,
            contornos,
            new Mat(),
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_SIMPLE
    );

    List<Rect> caixas = new ArrayList<>();

    for (MatOfPoint c : contornos) {

        Rect r = Imgproc.boundingRect(c);

        if (r.width < 40 || r.height < 20) continue;

        caixas.add(r);
    }

    return caixas;
}
}
