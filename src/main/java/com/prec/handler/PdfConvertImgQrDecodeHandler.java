package com.prec.handler;

import com.alibaba.fastjson.JSON;
import com.beust.jcommander.internal.Lists;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * pdf转换图片识别
 * @author shiguangpeng
 * @date 2023/7/4 16:52
 */
@Service
public class PdfConvertImgQrDecodeHandler implements QrDecodeHandler {
    private static final Logger logger = LoggerFactory.getLogger(PdfConvertImgQrDecodeHandler.class);

    @Autowired
    private ImgQrDeCodeHandler imgQrDeCodeHandler;

    @Override
    public boolean checkType(String decodeType) {
        return QrDecodeHandlerEnum.PDF_CONVERT_IMG.getCode().equals(decodeType);
    }

    @Override
    public List<String> decode(String filePath, String suffix) {
        logger.info("PdfConvertImgQrDecodeHandler decode begin");
        List<String> list = new ArrayList<>();
        if (!".pdf".equalsIgnoreCase(suffix)) {
            return list;
        }

        // pdf转图片
        List<File> files = convertPdfToPng(filePath);

        // 图片识别
        if (!CollectionUtils.isEmpty(files)) {
            for (File file : files) {
                String path = file.getPath();
                try {
                    List<String> strs = imgQrDeCodeHandler.decode(path, ".png");
                    if (!CollectionUtils.isEmpty(strs)) {
                        list.addAll(strs);
                    }
                } finally {
                    File deleteFile = new File(path);
                    deleteFile.deleteOnExit();
                }
            }
        }
        logger.info("PdfConvertImgQrDecodeHandler decode end result is {}", JSON.toJSONString(list));

        return list;
    }

    public static List<File> convertPdfToPng(String filePath) {
        List<File> fileList = Lists.newArrayList();
        File targetFile = new File(filePath);
        String imageType = "png";
        try (PDDocument document = PDDocument.load(targetFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pageSize = document.getNumberOfPages();
            for (int i = 0; i < pageSize; i++) {
                BufferedImage image = renderer.renderImage(i, 3f);
                String fullName = targetFile.getName();
                String preName = new StringBuilder(fullName).substring(0, fullName.length() - 4);
                String parentPath = targetFile.getParent();
                File outputPath = new File(parentPath + File.separator + preName + i + 1 + "." + imageType);
                ImageIO.write(image, imageType, outputPath);
                fileList.add(outputPath);
            }
        } catch (Exception e) {
            logger.error("pdf转png失败", e);
        }
        return fileList;
    }
}
