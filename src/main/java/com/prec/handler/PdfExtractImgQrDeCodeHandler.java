package com.prec.handler;

import com.alibaba.fastjson.JSON;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
 * pdf提取图片识别
 *
 * @author shiguangpeng
 * @date 2023/7/4 16:53
 */
@Service
public class PdfExtractImgQrDeCodeHandler implements QrDecodeHandler {
    private static final Logger logger = LoggerFactory.getLogger(PdfExtractImgQrDeCodeHandler.class);

    @Autowired
    private ImgQrDeCodeHandler imgQrDeCodeHandler;

    private static final String BASE_PATH = System.getProperty("catalina.home") + "/";

    @Override
    public boolean checkType(String decodeType) {
        return QrDecodeHandlerEnum.PDF_EXTRACT_IMG.getCode().equals(decodeType);
    }

    @Override
    public List<String> decode(String filePath, String suffix) {
        logger.info("PdfExtractImgQrDeCodeHandler decode begin");
        List<String> list = new ArrayList<>();
        // pdf大小写
        if (!".pdf".equalsIgnoreCase(suffix)) {
            return list;
        }

        // pdf 提取图片
        String savePath = BASE_PATH + "extractImgs";

        // 提取图片
        File pdfFile = new File(filePath);
        try (PDDocument document = PDDocument.load(pdfFile)) {

            // 创建文件夹
            if (!savePath.endsWith("/")) {
                savePath = savePath + "/";
            }
            mkdir(savePath);

            // 遍历每页pdf提取图片
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                PDPage page = document.getPage(i);

                List<RenderedImage> images = getImagesFromResources(page.getResources());
                // continue if there's no image in current page
                if (images.size() == 0) {
                    continue;
                }

                // 获取到每页的图片，然后保存到dto中
                for (int j = 0; j < images.size(); j++) {
                    RenderedImage renderedImage = images.get(j);
                    Image imageDto = new Image();
                    imageDto.setSourceImage(renderedImage);

                    int seq = j + 1;
                    String imagePath = savePath + seq + ".jpg";
                    File out = new File(imagePath);
                    imageDto.setImagePath(imagePath);
                    RenderedImage image = imageDto.getSourceImage();
                    // 写到本地
                    ImageIO.write(convertRenderedImage(image), "jpg", out);

                    try {
                        // 根据本地路径进行解码
                        List<String> strs = imgQrDeCodeHandler.decode(imageDto.getImagePath(), ".jpg");
                        if (CollectionUtils.isEmpty(strs)) {
                            continue;
                        }
                        list.addAll(strs);
                    } finally {
                        File deleteFile = new File(imageDto.getImagePath());
                        deleteFile.deleteOnExit();
                    }
                }
            }
        } catch (Exception e) {
            logger.info("提取图片异常 {}", e);
        }

        // 删除文件夹
        File file = new File(savePath);
        file.delete();

        logger.info("PdfExtractImgQrDeCodeHandler decode end result is {}", JSON.toJSONString(list));

        return list;
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
