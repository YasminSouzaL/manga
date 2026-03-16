package com.test.manga.util;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class ConverterImagemUtil {

    public static Mat bufferedImageToMat(BufferedImage pagina) {
        if (pagina == null) return new Mat();

        // Garantir formato BGR
        BufferedImage converted = new BufferedImage(
                pagina.getWidth(),
                pagina.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR
        );

        converted.getGraphics().drawImage(pagina, 0, 0, null);

        byte[] pixels = ((DataBufferByte) converted.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(converted.getHeight(), converted.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, pixels);
        return mat;
    }


    public static BufferedImage matToBufferedImage(Mat mat) {
        if (mat == null || mat.empty()) return null;

        int type = BufferedImage.TYPE_3BYTE_BGR;

        BufferedImage image = new BufferedImage(mat.width(), mat.height(), type);
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        mat.get(0, 0, data);

        return image;
    }


    public static BufferedImage downloadImage(String url) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'downloadImage'");
    }
}
