package com.thanos.common.security;

import org.bouncycastle.util.encoders.Base64;

/**
 * @author Llx
 * @create 2018/8/21
 **/
public class DefaultSignProvider extends SignProvider {

	@Override
	public byte[] excSign(byte[] inData) throws Exception {
		byte[] signedData;
		
		if(SecurityIDs.SM3_WITH_SM2.equals(super.getSignatureAlgorithm())){
			signedData = SM2Util.sign(inData, Base64.toBase64String(super.getPrivateKeyByte()));
		} else{
		    signedData = RSAUtil.pkcs1Sign(inData, super.getPrivateKeyByte(), super.getSignatureAlgorithm());
		}
		
		return signedData;
	}

}
