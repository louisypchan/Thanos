package com.thanos.common.security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author Llx
 * @create 2018/8/21
 **/
public class RSAUtil {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

	private static final String KEY_ALGORITHM = "RSA";
	private static final String PUBLIC_KEY = "RSAPublicKey";
	private static final String PRIVATE_KEY = "RSAPrivateKey";

    public static byte[] pkcs1Sign(byte[] toSignBytes, byte[] privateKeyBytes, String algorithm){
        byte[] signedData = null;

        try {
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);

            Signature signature = Signature.getInstance(algorithm);
            signature.initSign(privateKey);
            signature.update(toSignBytes);
            signedData = signature.sign();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return signedData;
    }

    public static boolean pkcs1Verify(byte[] toSignBytes, byte[] signValueBytes, byte[] publicKeyBytes, String algorithm){
        boolean verifyResult = false;

        try {
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);

            Signature sig = Signature.getInstance(algorithm);
            sig.initVerify(pubKey);
            sig.update(toSignBytes);

            if(sig.verify(signValueBytes))
                verifyResult = true;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return verifyResult;
    }
}
