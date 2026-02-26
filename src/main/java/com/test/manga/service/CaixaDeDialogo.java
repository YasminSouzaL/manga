package com.test.manga.service;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;


public class CaixaDeDialogo {

    private CaixaDeDialogo() {
        // evita instanciação

    }

    
    public static void desenhar(Mat src, Rect r) {
        Imgproc.rectangle(
                src,
                new Point(r.x, r.y),
                new Point(r.x + r.width, r.y + r.height),
                new Scalar(0, 255, 0),
                3
        );
    }
}

