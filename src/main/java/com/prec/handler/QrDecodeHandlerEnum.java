package com.prec.handler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shiguangpeng
 * @date 2023/7/4 20:31
 */
public enum QrDecodeHandlerEnum {
    IMG("1","图片识别策略"),
    PDF_CONVERT_IMG("2","pdf转图片识别策略"),
    PDF_EXTRACT_IMG("3","pdf提取图片识别策略");

    private String code;
    private String name;

    QrDecodeHandlerEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static List<QrDecodeHandlerEnum> needDecodeHandlerEnum() {
        List<QrDecodeHandlerEnum> list = new ArrayList<>();
        list.add(IMG);
        list.add(PDF_EXTRACT_IMG);
        list.add(PDF_CONVERT_IMG);
        return list;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
