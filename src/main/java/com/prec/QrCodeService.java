package com.prec;

import java.util.List;

/**
 * @author shiguangpeng
 * @date 2023/7/4 16:11
 */
public interface QrCodeService {

    List<String> decode(String fileUrl,String suffix);
}
