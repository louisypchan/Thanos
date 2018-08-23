package com.thanos.common.pdf.signature;

/**
 * @author Llx
 * @create 2018/8/21
 **/
public class SignHash {
    private byte[] hashBytes;
    private byte[] signHashBytes;

    public byte[] getHashBytes() {
        return hashBytes;
    }

    public void setHashBytes(byte[] hashBytes) {
        this.hashBytes = hashBytes;
    }

    public byte[] getSignHashBytes() {
        return signHashBytes;
    }

    public void setSignHashBytes(byte[] signHashBytes) {
        this.signHashBytes = signHashBytes;
    }
}
