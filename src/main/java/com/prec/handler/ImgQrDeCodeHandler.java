package com.prec.handler;

import com.alibaba.fastjson.JSON;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图片类型直接进行识别
 * @author shiguangpeng
 * @date 2023/7/4 16:50
 */
@Service
public class ImgQrDeCodeHandler implements QrDecodeHandler {
    private static final Logger logger = LoggerFactory.getLogger(ImgQrDeCodeHandler.class);

    @Override
    public boolean checkType(String decodeType) {
        return QrDecodeHandlerEnum.IMG.getCode().equals(decodeType);
    }

    @Override
    public List<String> decode(String filePath, String suffix) {
        logger.info("ImgQrDeCodeHandler decode begin");
        List<String> list = new ArrayList<>();

        // pdf大小写  具体执行哪些后缀的文件
        if (".pdf".equalsIgnoreCase(suffix)) {
            return list;
        }

        try {
            BufferedImage image;
            image = ImageIO.read(new File(filePath));
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            Binarizer binarizer = new HybridBinarizer(source);
            BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
            Map<DecodeHintType, Object> hints = new HashMap<DecodeHintType, Object>();
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
            Result result = new MultiFormatReader().decode(binaryBitmap, hints);
            logger.info("图片中内容： {}" , result.getText());
            if (!StringUtils.isEmpty(result.getText())) {
                list.add(result.getText());
            }
            logger.info("ImgQrDeCodeHandler decode end result is {}", JSON.toJSONString(list));
            return list;
        } catch (Exception e) {
            logger.info("img qr code decode error", e);
            return list;
        }
    }
}
