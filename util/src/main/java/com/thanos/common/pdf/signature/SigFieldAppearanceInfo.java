package com.thanos.common.pdf.signature;

import com.alibaba.fastjson.JSON;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Image;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.util.StringUtils;

/**
 * @author Llx
 * @create 2018/8/21
 **/
public class SigFieldAppearanceInfo {
    private String signatureName;
    private Integer pageNum;
    private Float x;
    private Float y;
    private Float w;
    private Float h;
    private int xPadding;
    private int yPadding;
    private String imgBase64;

    private Float pdfWidth;
    private Float pdfHeight;
    private Float imageWidth;
    private Float imageHeight;

    private boolean isInit;

    /**
     * use before calcSignaturePosition
     * @param pageCount
     * @param pdfWidth
     * @param pdfHeight
     * @param imageWidth
     * @param imageHeight
     * @return
     * @throws Exception
     */
    public SigFieldAppearanceInfo init(String signatureName, int pageCount, Float pdfWidth, Float pdfHeight, Float imageWidth, Float imageHeight) throws Exception {
        this.signatureName = signatureName;

        if(1 > pageCount)
            throw hasError("page count must greater than 0");

        if(1 > pageNum)
            throw hasError("page num must greater than 0");

        if(null != pdfWidth && pdfWidth <= 0)
            throw hasError("pdf width must greater than 0");
        if(null != pdfHeight && pdfHeight <= 0)
            throw hasError("pdf height must greater than 0");

        if(!StringUtils.isEmpty(imgBase64)) {
            try {
                ImageData imageData = ImageDataFactory.create(Base64.decode(imgBase64));
                Image image = new Image(imageData);
                imageWidth = image.getImageWidth();
                imageHeight = image.getImageHeight();
            } catch (Exception e) {
                throw hasError("image data invalid");
            }
        }

        if(null != imageWidth && imageWidth <= 0)
            throw hasError("image width must greater than 0");
        if(null != imageHeight && imageHeight <= 0)
            throw hasError("image height must greater than 0");

        this.pdfWidth = pdfWidth;
        this.pdfHeight = pdfHeight;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;

        this.isInit = true;

        return this;
    }

    /**
     * use after init
     * @return
     * @throws Exception
     */
    public SigFieldAppearanceInfo calcSignaturePosition() throws Exception {
        if(!isInit)
            throw new Exception("method init() not executed");

        if(null == this.pageNum)
            throw hasError("appearance info incomplete");

        this.x = calcX();
        this.y = calcY();
        this.w = calcWidth();
        this.h = calcHeight();

        return this;
    }

    private Float calcX() throws Exception {
        if(null == this.x || this.x < 0)
            throw hasError("lack of x info");

        if(0 == this.xPadding){
            if(this.x > 1)
                return this.x;

            if(null == this.pdfWidth)
                throw hasError("lack of pdf width");

            return this.x * this.pdfWidth;

        } else {
            if(null == this.pdfWidth)
                throw hasError("lack of pdf width");

            if(this.w > 1){
                if(this.x > 1)
                    return this.pdfWidth - this.w - this.x;
                else
                    return this.pdfWidth - this.w - this.x * pdfWidth;
            } else if(this.w >= 0){
                if(this.x > 0)
                    return this.pdfWidth - (this.w + this.x) * this.pdfWidth;
            }

            throw hasError("x info invalid");
        }
    }

    private Float calcY() throws Exception {
        if(null == this.y || this.y < 0)
            throw hasError("lack of y");

        if(0 == this.yPadding){
            if(this.y > 1)
                return this.y;

            if(null == this.pdfHeight)
                throw hasError("lack of pdf height");

            return this.y * this.pdfHeight;

        } else {
            if (null == this.pdfHeight)
                throw hasError("lack of pdf height");

            if (this.h > 1) {
                if (this.y > 1)
                    return this.pdfHeight - this.h - this.y;
                else
                    return this.pdfHeight - this.h - this.y * pdfHeight;
            } else if (this.h >= 0) {
                if (this.y > 0)
                    return this.pdfHeight - (this.h + this.y) * this.pdfHeight;
            }

            throw hasError("y info invalid");
        }
    }

    private Float calcWidth() throws Exception {
        if(null != w){
            if(this.w > 1)
                return this.w;

            if(null == this.pdfWidth)
                throw hasError("lack of pdf width");

            if(this.w > 0)
                return this.w * this.pdfWidth;

            throw hasError("w width invalid");
        } else {
            if(null == this.imageWidth)
                throw hasError("lack of image width");

            return this.imageWidth;
        }
    }

    private Float calcHeight() throws Exception {
        if(null != h){
            if(this.h > 1)
                return this.h;

            if(null == this.pdfHeight)
                throw hasError("lack of pdf height");

            if(this.h > 0)
                return this.h * this.pdfHeight;

            throw hasError("h info invalid");
        } else {
            if(null == this.imageHeight)
                throw hasError("lack of image height");

            return this.imageHeight;
        }
    }

    public String getSignatureName() {
        return signatureName;
    }

    public void setSignatureName(String signatureName) {
        this.signatureName = signatureName;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Float getX() {
        return x;
    }

    public void setX(Float x) {
        this.x = x;
    }

    public Float getY() {
        return y;
    }

    public void setY(Float y) {
        this.y = y;
    }

    public Float getW() {
        return w;
    }

    public void setW(Float w) {
        this.w = w;
    }

    public Float getH() {
        return h;
    }

    public void setH(Float h) {
        this.h = h;
    }

    public int getxPadding() {
        return xPadding;
    }

    public void setxPadding(int xPadding) {
        this.xPadding = xPadding;
    }

    public int getyPadding() {
        return yPadding;
    }

    public void setyPadding(int yPadding) {
        this.yPadding = yPadding;
    }

    public String getImgBase64() {
        return imgBase64;
    }

    public void setImgBase64(String imgBase64) {
        this.imgBase64 = imgBase64;
    }

    public String toSignatureFieldString(){

        StringBuilder builder = new StringBuilder();
        builder.append(!StringUtils.isEmpty(signatureName) ? signatureName : "");
        builder.append("|");
        builder.append(String.valueOf(this.pageNum));
        builder.append("|");
        builder.append(String.valueOf(this.x));
        builder.append("|");
        builder.append(String.valueOf(this.y));
        builder.append("|");
        builder.append(String.valueOf(this.w));
        builder.append("|");
        builder.append(String.valueOf(this.h));

        if(!StringUtils.isEmpty(imgBase64)) {
            builder.append("|");
            builder.append(imgBase64);
        }

        return builder.toString();
    }

    private Exception hasError(String message) {
        return new Exception(String.format("%s: %s", message, JSON.toJSONString(this)));
    }
}
