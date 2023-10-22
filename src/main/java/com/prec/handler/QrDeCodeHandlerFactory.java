package com.prec.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author shiguangpeng
 * @date 2023/7/4 20:16
 */
@Component
public class QrDeCodeHandlerFactory {

    @Autowired
    List<QrDecodeHandler> qrDecodeHandlers;

    public QrDecodeHandler getQrDecodeHandler(String checkType){
        for (QrDecodeHandler qrDecodeHandler : qrDecodeHandlers) {
            if(qrDecodeHandler.checkType(checkType)){
                return qrDecodeHandler;
            }
        }
        return null;
    }
}
