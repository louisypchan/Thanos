package com.thanos.test.pdf;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

/**
 * 
 * @Description: TODO
 * @author Lianglx
 * @date 2017年4月12日
 */
public class CertificateUtil {
	
	public static Certificate getCertificateFromJks(byte[] certData, String certPwd) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException{
		return getCertificate(certData, certPwd, "JKS");
	}
	
	public static Certificate getCertificateFromPkcs12(byte[] certData, String certPwd) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException{
		return getCertificate(certData, certPwd, "PKCS12");
	}
	
	private static Certificate getCertificate(byte[] certData, String certPwd, String certType) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException{
		Certificate cert = null;
		
		String priKeyName = null;
        char[] passphrase = null;
        
        if(null != certPwd && !certPwd.isEmpty())
        	passphrase = certPwd.toCharArray();
		
		try {
			KeyStore ks = KeyStore.getInstance(certType);
			
			ks.load(new ByteArrayInputStream(certData), passphrase);

	        if (ks.aliases().hasMoreElements()) {
	            priKeyName = ks.aliases().nextElement();
	        }
	          
	        cert = (Certificate) ks.getCertificate(priKeyName);
	        
		} catch (KeyStoreException e) {
			throw e;
		} catch (NoSuchAlgorithmException e) {
			throw e;
		} catch (CertificateException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
        
        return cert;
	}
	
	public static PrivateKey getPrivateKeyFromPkcs12(byte[] certData, String certPwd) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException{
		return getPrivateKey(certData, certPwd, "PKCS12");
	}
	
	private static PrivateKey getPrivateKey(byte[] certData, String certPwd, String certType) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException{
		PrivateKey prikey = null;
		
		String priKeyName = null;
        char[] passphrase = null;
        
        if(null != certPwd && !certPwd.isEmpty())
        	passphrase = certPwd.toCharArray();
        
		try {
			KeyStore ks = KeyStore.getInstance(certType);
			
			ks.load(new ByteArrayInputStream(certData), passphrase);

	        if (ks.aliases().hasMoreElements()) {
	            priKeyName = ks.aliases().nextElement();
	        }
	        
	        prikey = (PrivateKey) ks.getKey(priKeyName, passphrase);
	        
		} catch (KeyStoreException e) {
			throw e;
		} catch (NoSuchAlgorithmException e) {
			throw e;
		} catch (CertificateException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} catch (UnrecoverableKeyException e) {
			throw e;
		}
        
		return prikey;
	}
	
	public static PublicKey getPublicKeyFromJks(byte[] certData, String certPwd) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException{
		return getPublicKey(certData, certPwd, "JKS");
	}
	
	public static PublicKey getPublicKeyFromPkcs12(byte[] certData, String certPwd) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException{
		return getPublicKey(certData, certPwd, "PKCS12");
	}
	
	private static PublicKey getPublicKey(byte[] certData, String certPwd, String certType) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException{
		Certificate cert = getCertificate(certData, certPwd, certType);
		
		return cert.getPublicKey();
	}
	
	public static X509Certificate getX509Certificate(String certPath) throws FileNotFoundException, CertificateException{
		File file = new File(certPath); 
		
        InputStream inStream = null;
        X509Certificate x509Cert = null;
        
		try {
			inStream = new FileInputStream(file);
			
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			
			x509Cert = (X509Certificate) cf.generateCertificate(inStream);
		} catch (FileNotFoundException e) {
			throw e;
		} catch (CertificateException e) {
			throw e;
		} finally {
			try {
				inStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return x509Cert;
	}
	
	public static X509Certificate getX509Certificate(byte[] certData) throws FileNotFoundException, CertificateException{
		
        InputStream inStream = null;
        X509Certificate x509Cert = null;
        
		try {
			inStream = new ByteArrayInputStream(certData);
			
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			
			x509Cert = (X509Certificate) cf.generateCertificate(inStream);
		} catch (CertificateException e) {
			throw e;
		} finally {
			try {
				inStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return x509Cert;
	}

	public static X509Certificate getX509Certificate2(byte[] certData) throws CertificateException {
		Security.addProvider(new BouncyCastleProvider());

		X509Certificate x509cert = null;

		try{
			X509CertificateHolder x509CertHoler = new X509CertificateHolder(certData);

			x509cert = new JcaX509CertificateConverter().setProvider("BC") .getCertificate(x509CertHoler);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return x509cert;
	}
	
	public static int getKeySize(PublicKey pubkey) throws NoSuchAlgorithmException, InvalidKeySpecException{
		String algorithm = pubkey.getAlgorithm();
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
		
		BigInteger prime = null;
		
		if ("RSA".equals(algorithm)) { // 如果是RSA加密
		    RSAPublicKeySpec keySpec = (RSAPublicKeySpec)keyFactory.getKeySpec(pubkey, RSAPublicKeySpec.class);
		    prime = keySpec.getModulus();
		} else if ("DSA".equals(algorithm)) { // 如果是DSA加密
		    DSAPublicKeySpec keySpec = (DSAPublicKeySpec)keyFactory.getKeySpec(pubkey, DSAPublicKeySpec.class);
		    prime = keySpec.getP();
		}
		
		int keysize = prime.toString(2).length(); // 转换为二进制，获取公钥长度
		
		return keysize;
	}
}
