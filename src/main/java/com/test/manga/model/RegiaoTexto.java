package com.test.manga.model;

import org.opencv.core.Rect;

public class RegiaoTexto {

    private int id;
    private String tipo; // BALAO | NARRACAO
    private Rect box;

    private String textoOriginal;
    private String traducao;

    public RegiaoTexto(int id, String tipo, Rect box) {
        this.id = id;
        this.tipo = tipo;
        this.box = box;
    }

    public int getId() { return id; }
    public String getTipo() { return tipo; }
    public Rect getBox() { return box; }

    public String getTextoOriginal() { return textoOriginal; }
    public void setTextoOriginal(String textoOriginal) {
        this.textoOriginal = textoOriginal;
    }

    public String getTraducao() { return traducao; }
    public void setTraducao(String traducao) {
        this.traducao = traducao;
    }
}
