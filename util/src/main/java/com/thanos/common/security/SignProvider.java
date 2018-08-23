package com.thanos.common.security;

/**
 * @author Llx
 * @create 2018/8/21
 **/
public abstract class SignProvider {
	private byte[] privateKeyByte;
	private String signatureAlgorithm;

    public byte[] getPrivateKeyByte() {
        return privateKeyByte;
    }

    public void setPrivateKeyByte(byte[] privateKeyByte) {
        this.privateKeyByte = privateKeyByte;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public abstract byte[] excSign(byte[] inData) throws Exception ;
}
