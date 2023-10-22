package com.prec.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shiguangpeng
 * @date 2023/7/4 16:44
 */
@Component
public class QrDecodeHandlerChain {

    @Autowired
    private QrDeCodeHandlerFactory qrDeCodeHandlerFactory;

    /**
     * 执行所有策略识别二维码
     * @param filePath
     * @param suffix
     * @return
     */
    public List<String> decode(String filePath, String suffix) {
        try {
            List<QrDecodeHandlerEnum> qrDecodeHandlerEnums = QrDecodeHandlerEnum.needDecodeHandlerEnum();
            for (QrDecodeHandlerEnum qrDecodeHandlerEnum : qrDecodeHandlerEnums) {
                QrDecodeHandler qrDecodeHandler = qrDeCodeHandlerFactory.getQrDecodeHandler(qrDecodeHandlerEnum.getCode());
                if (qrDecodeHandler != null) {
                    List<String> list = qrDecodeHandler.decode(filePath, suffix);
                    if (CollectionUtils.isEmpty(list)) {
                        continue;
                    }
                    return list;
                }
            }

            return new ArrayList<>();
        } finally {
            File file = new File(filePath);
            file.deleteOnExit();
        }
    }
}
