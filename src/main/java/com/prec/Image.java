package com.prec;

import com.alibaba.fastjson.annotation.JSONField;

import java.awt.image.RenderedImage;

/**
 * @author shiguangpeng
 * @date 2023/7/4 16:50
 */
public class Image {

    private static final long serialVersionUID = 1L;

    /**
     * image path
     */
    private String imagePath;


    /**
     * image name
     */
    private String name;

    /**
     * picture binary object
     */
    @JSONField(serialize = false)
    private RenderedImage sourceImage;


    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RenderedImage getSourceImage() {
        return sourceImage;
    }

    public void setSourceImage(RenderedImage sourceImage) {
        this.sourceImage = sourceImage;
    }

}
