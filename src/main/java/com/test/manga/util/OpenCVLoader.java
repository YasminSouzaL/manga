package com.test.manga.util;

import nu.pattern.OpenCV;

public class OpenCVLoader {
    static {
        OpenCV.loadLocally();
        System.out.println("[OpenCV] carregado com sucesso");
    }
}
