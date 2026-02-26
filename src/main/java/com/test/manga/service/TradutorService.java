package com.test.manga.service;

public interface TradutorService {
    String traduzir(String texto, String origem, String destino);

    static final String LIBRE_URL = "https://libretranslate.com/translate";

    //Mensagem de log para indicar que a tradução foi iniciada
    static final String TRADUCAO_INICIADA = "Iniciando tradução do texto...";
    
}

