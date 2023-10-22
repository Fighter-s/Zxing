package com.prec;

import com.prec.handler.QrDecodeHandlerChain;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author shiguangpeng
 * @date 2023/7/4 16:13
 */
@Service
public class QrCodeServiceImpl implements QrCodeService {
    private static final Logger logger = LoggerFactory.getLogger(QrCodeServiceImpl.class);

    @Autowired
    private QrDecodeHandlerChain qrDecodeHandlerChain;

    private static final String BASE_PATH = System.getProperty("catalina.home") + "/";

    @Override
    public List<String> decode(String fileUrl, String suffix) {
        logger.info("QrCodeServiceImpl decode fileId is {}", fileUrl);
        // 下载文件
        String filePath = BASE_PATH + UUID.randomUUID().toString() + suffix;
        boolean flag = downLoad(fileUrl, filePath);

        if (!flag) {
            return new ArrayList<>();
        }

        // 二维码识别
        return qrDecodeHandlerChain.decode(filePath, suffix);
    }

    /**
     * 文件下载
     *
     * @param url
     * @param localFileName
     * @return
     */
    private boolean downLoad(String url, String localFileName) {
        try {
            CloseableHttpClient httpClient = HttpClients.custom().build();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            File file = new File(localFileName);
            if (!file.exists()) {
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                file.createNewFile();
            }
            InputStream in = entity.getContent();
            OutputStream out = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int readLength = 0;
            while ((readLength = in.read(buffer)) > 0) {
                byte[] bytes = new byte[readLength];
                System.arraycopy(buffer, 0, bytes, 0, readLength);
                out.write(bytes);
            }
            out.flush();
            return true;
        } catch (IOException e) {
            logger.error("下载文件失败", e);
            return false;
        }
    }
}
