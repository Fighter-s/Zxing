package com.prec;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import com.prec.Image;

/**
 * @author shiguangpeng
 * @date 2023/7/4 19:57
 */
public class ExtractImages {
    private static final Logger logger = LoggerFactory.getLogger(ExtractImages.class);
    public static List<Image> extractImages(String pdfFilePath, String imageSavePath) {
        List<Image> imageResult = new ArrayList<>();
        File pdfFile = new File(pdfFilePath);
        try (PDDocument document = PDDocument.load(pdfFile)) {

            if (!imageSavePath.endsWith("/")) {
                imageSavePath = imageSavePath + "/";
            }
            mkdir(imageSavePath);

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                PDPage page = document.getPage(i);

                List<RenderedImage> images = getImagesFromResources(page.getResources());
                // continue if there's no image in current page
                if (images.size() == 0) {
                    continue;
                }

                for (int j = 0; j < images.size(); j++) {
                    RenderedImage renderedImage = images.get(j);
                    Image imageDto = new Image();
                    imageDto.setSourceImage(renderedImage);
                    imageResult.add(imageDto);
                }


                for (int k = 0; k < imageResult.size(); k++) {
                    Image dto = imageResult.get(k);
                    int seq = k + 1;
                    String imagePath = imageSavePath + seq + ".jpg";
                    File out = new File(imagePath);
                    dto.setImagePath(imagePath);
                    RenderedImage image = dto.getSourceImage();

                    ImageIO.write(convertRenderedImage(image), "jpg", out);
                }


            }
        } catch (Exception e) {
            logger.info("提取图片异常 {}", e);
        }
        return imageResult;
    }

    private static void mkdir(String dir) {
        File folder = new File(dir);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        if (!folder.canWrite()) {
            folder.setWritable(true);
        }
    }

    private static List<RenderedImage> getImagesFromResources(PDResources resources) throws IOException {
        List<RenderedImage> images = new ArrayList<>();
        for (COSName xObjectName : resources.getXObjectNames()) {
            PDXObject xObject = resources.getXObject(xObjectName);
            // handle FormXObject
            if (xObject instanceof PDFormXObject) {
                images.addAll(getImagesFromResources(((PDFormXObject) xObject).getResources()));
                // handle ImageXObject
            } else if (xObject instanceof PDImageXObject) {
                images.add(((PDImageXObject) xObject).getImage());
            }
        }
        return images;
    }

    /**
     * RenderedImage -> BufferedImage
     *
     * @param img
     * @return
     */
    private static BufferedImage convertRenderedImage(RenderedImage img) {
        if (img instanceof BufferedImage) {
            BufferedImage newBufferedImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
            newBufferedImage.createGraphics().drawImage((BufferedImage) img, 0, 0, Color.WHITE, null);
            return newBufferedImage;
        }
        ColorModel cm = img.getColorModel();
        int width = img.getWidth();
        int height = img.getHeight();
        WritableRaster raster = cm.createCompatibleWritableRaster(width, height);
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        Hashtable properties = new Hashtable();
        String[] keys = img.getPropertyNames();
        if (keys != null) {
            for (int i = 0; i < keys.length; i++) {
                properties.put(keys[i], img.getProperty(keys[i]));
            }
        }
        BufferedImage result = new BufferedImage(cm, raster, isAlphaPremultiplied, properties);
        img.copyData(raster);
        BufferedImage newBufferedImage = new BufferedImage(result.getWidth(), result.getHeight(), BufferedImage.TYPE_INT_RGB);
        newBufferedImage.createGraphics().drawImage(result, 0, 0, Color.WHITE, null);
        return newBufferedImage;
    }
}
