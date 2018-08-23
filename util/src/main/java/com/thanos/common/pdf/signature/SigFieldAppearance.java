package com.thanos.common.pdf.signature;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Llx
 * @create 2018/8/21
 **/
public class SigFieldAppearance {
    private String signatureName;
	private int pageNum;
	private float x;
	private float y;
	private float fitWidth;
	private float fitHeight;
    private String imgBase64;

	public static SigFieldAppearance getSignatureField(String positionParam) throws Exception{
		SigFieldAppearance sigField = null;

		String fieldName = null;
		int atPage = 0;
		float x = 0;
		float y = 0;
		float fitWidth = 0;
		float fitHeight = 0;
        String imgBase64 = null;
		
		try{
            String[] params = positionParam.split("\\|");

            int paramsSize = params.length;
            if(6 != paramsSize && 7 != paramsSize)
                throw new Exception("");

			fieldName = params[0];
			atPage = Integer.parseInt(params[1]);
			x = Float.parseFloat(params[2]);
			y = Float.parseFloat(params[3]);
			fitWidth = Float.parseFloat(params[4]);
			fitHeight = Float.parseFloat(params[5]);

			if(7 == paramsSize)
                imgBase64 = params[6];
			
			sigField = new SigFieldAppearance();
			sigField.setSignatureName(fieldName + "-" + UUID.randomUUID().toString());
			sigField.setPageNum(atPage);
			sigField.setX(x);
			sigField.setY(y);
			sigField.setFitWidth(fitWidth);
			sigField.setFitHeight(fitHeight);
            sigField.setImgBase64(imgBase64);
			
		}catch(Exception e){
			throw new Exception(String.format("signature field param invalid, %s", positionParam));
		}
		
		return sigField;
	}
	
	public static List<SigFieldAppearance> getSignatureFieldList(List<String> positionParamList) throws Exception{
		List<SigFieldAppearance> sigFieldList = new ArrayList<SigFieldAppearance>();
		SigFieldAppearance sigFiled = null;
		
		for(String param : positionParamList){
			sigFiled = getSignatureField(param);
			
			sigFieldList.add(sigFiled);
		}
		
		return sigFieldList;
	}

    public String getSignatureName() {
        return signatureName;
    }

    public void setSignatureName(String signatureName) {
        this.signatureName = signatureName;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getFitWidth() {
        return fitWidth;
    }

    public void setFitWidth(float fitWidth) {
        this.fitWidth = fitWidth;
    }

    public float getFitHeight() {
        return fitHeight;
    }

    public void setFitHeight(float fitHeight) {
        this.fitHeight = fitHeight;
    }

    public String getImgBase64() {
        return imgBase64;
    }

    public void setImgBase64(String imgBase64) {
        this.imgBase64 = imgBase64;
    }
}
