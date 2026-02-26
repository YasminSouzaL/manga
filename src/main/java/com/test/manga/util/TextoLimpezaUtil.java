package com.test.manga.util;

public class TextoLimpezaUtil {

    /* ===============================
       UTILITÁRIO DE LIMPEZA
    =============================== */
    public static String limparTexto(String texto) {
        if (texto == null) return "";
        return texto
                .replaceAll("[^a-zA-Z0-9\\s.,!?'\\náéíóúâêôãõçÁÉÍÓÚÂÊÔÃÕÇ]", "")
                .replaceAll(" +", " ")
                .trim();
    }

    /* ===============================
       FILTRO ANTI-FANTASMA LINHA POR LINHA
    =============================== */
    public static String aplicarFiltroRigoroso(String textoBruto) {
        String textoLimpo = limparTexto(textoBruto);
        if (textoLimpo.isEmpty()) return "";

        StringBuilder textoFinal = new StringBuilder();
        String[] linhas = textoLimpo.split("\\n");

        for (String linha : linhas) {
            String linhaTrim = linha.trim();

            if (linhaTrim.isEmpty()) continue;
            if (linhaTrim.length() < 3) continue;
            if (!linhaTrim.matches(".*[a-zA-Z]{3,}.*")) continue;
            if (linhaTrim.matches(".*(.)\\1{3,}.*")) continue;

            textoFinal.append(linhaTrim).append("\n");
        }

        String resultadoFinal = textoFinal.toString().trim();
        if (resultadoFinal.length() < 3) return "";

        long countLetras = resultadoFinal.chars().filter(Character::isLetter).count();
        long countVogais = resultadoFinal.chars().filter(ch -> "AEIOUYaeiouyÁÉÍÓÚÂÊÔÃÕÇáéíóúâêôãõç".indexOf(ch) != -1).count();

        if (countLetras > 4 && (double) countVogais / countLetras < 0.15) {
            return "";
        }

        return resultadoFinal;
    }
}