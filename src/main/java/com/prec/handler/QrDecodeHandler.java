package com.prec.handler;

import java.util.List;

/**
 * @author shiguangpeng
 * @date 2023/7/4 16:45
 */
public interface QrDecodeHandler {
    boolean checkType(String decodeType);

    List<String> decode(String filePath,String suffix);
}
