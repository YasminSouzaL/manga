package com.test.manga.model;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

public class BalaoDetectado {
    public Rect rect;
    public Mat imagem;
    public String tipo;

    public BalaoDetectado(Rect rect, Mat imagem, String tipo) {
        this.rect = rect;
        this.imagem = imagem;
        this.tipo = tipo;
    }

    public Mat getImagem() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getImagem'");
    }

    public Object getRect() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRect'");
    }
}
