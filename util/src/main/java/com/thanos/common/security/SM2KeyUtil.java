package com.thanos.common.security;

import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.sec.ECPrivateKey;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.encoders.Base64;

import java.math.BigInteger;

public class SM2KeyUtil {
	
	public static AsymmetricCipherKeyPair generateKeyPair() {
		SM2Crypto sm2Crypto = new SM2Crypto();
		return sm2Crypto.ecc_key_pair_generator.generateKeyPair();
	}
	
	public static byte[] getPrivateKeyRaw(AsymmetricCipherKeyPair sm2KeyPair) {
		ECPrivateKeyParameters sm2PrivateKeyarameters = (ECPrivateKeyParameters) sm2KeyPair.getPrivate();
		ECPublicKeyParameters sm2PublicKeyarameters = (ECPublicKeyParameters) sm2KeyPair.getPublic();
		BigInteger privateKey = sm2PrivateKeyarameters.getD();
		ECPoint publicKey = sm2PublicKeyarameters.getQ();

		return BigIntegers.asUnsignedByteArray(32, privateKey);
	}

	public static byte[] getPublicKeyRaw(AsymmetricCipherKeyPair sm2KeyPair) {
		ECPrivateKeyParameters sm2PrivateKeyarameters = (ECPrivateKeyParameters) sm2KeyPair.getPrivate();
		ECPublicKeyParameters sm2PublicKeyarameters = (ECPublicKeyParameters) sm2KeyPair.getPublic();
		BigInteger privateKey = sm2PrivateKeyarameters.getD();
		ECPoint publicKey = sm2PublicKeyarameters.getQ();

		return publicKey.getEncoded(false);
	}

	public static byte[] privateKeyRawToDean(byte[] rawPriKey, byte[] rawPubKey) {
		String deanHeader = "MBMTABMAEwATABMAEwACAgEAAgEH";

		if (rawPubKey==null || rawPubKey.length!=65) {
			return null;
		}

		try {
			ASN1Sequence deanHeaderSeq = ASN1Sequence.getInstance(Base64.decode(deanHeader));
			byte[] pubX = Arrays.copyOfRange(rawPubKey, 1, 33);
			byte[] pubY = Arrays.copyOfRange(rawPubKey, 33, 65);
			byte[] pri = BigIntegers.asUnsignedByteArray(32, new BigInteger(1, rawPriKey));
			
			DERPrintableString pubXString = new DERPrintableString(new String(pubX, "ISO-8859-1"));
			DERPrintableString pubYString = new DERPrintableString(new String(pubY, "ISO-8859-1"));
			DERPrintableString priString = new DERPrintableString(new String(pri, "ISO-8859-1"));

			ASN1EncodableVector v1 = new ASN1EncodableVector();
			v1.add(deanHeaderSeq);
			v1.add(pubXString);
			v1.add(pubYString);
			v1.add(priString);
			DERSequence deanKeySeq = new DERSequence(v1);

			return deanKeySeq.getEncoded("DER");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] privateKeyDeanToPKCS8(byte[] deanKey) {
		try {
			ASN1Sequence deanKeySeq = ASN1Sequence.getInstance(deanKey);
			
			DERPrintableString xPub = (DERPrintableString) deanKeySeq.getObjectAt(1);
			DERPrintableString yPub = (DERPrintableString) deanKeySeq.getObjectAt(2);
			DERPrintableString pri = (DERPrintableString) deanKeySeq.getObjectAt(3);
			
			ASN1ObjectIdentifier objId = new ASN1ObjectIdentifier("1.2.840.10045.2.1");
			ASN1ObjectIdentifier objParam = new ASN1ObjectIdentifier("1.2.156.10197.1.301");
			AlgorithmIdentifier algId = new AlgorithmIdentifier(objId, objParam);
			
			byte[] X = BigIntegers.asUnsignedByteArray(32, new BigInteger(1, xPub.getOctets()));
			byte[] Y = BigIntegers.asUnsignedByteArray(32, new BigInteger(1, yPub.getOctets()));
			byte[] pubBytes = Arrays.concatenate(new byte[]{0x04}, X, Y);
			SubjectPublicKeyInfo subjectPublicKeyInfo = new SubjectPublicKeyInfo(algId, pubBytes);

			BigInteger priKey = new BigInteger(1, pri.getOctets());
            ECPrivateKey ecPriKey = new ECPrivateKey(priKey, new DERBitString(pubBytes), objParam);
			PrivateKeyInfo privateKeyInfo = new PrivateKeyInfo(algId, ecPriKey);

			return privateKeyInfo.getEncoded("DER");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[][] privateKeyDeanToRaw(byte[] deanKey) {
		byte[][] out = new byte[2][];
		try {
			ASN1Sequence deanKeySeq = ASN1Sequence.getInstance(deanKey);
			
			DERPrintableString xPub = (DERPrintableString) deanKeySeq.getObjectAt(1);
			DERPrintableString yPub = (DERPrintableString) deanKeySeq.getObjectAt(2);
			DERPrintableString pri = (DERPrintableString) deanKeySeq.getObjectAt(3);
			
			byte[] X = BigIntegers.asUnsignedByteArray(32, new BigInteger(1, xPub.getOctets()));
			byte[] Y = BigIntegers.asUnsignedByteArray(32, new BigInteger(1, yPub.getOctets()));
			byte[] pubBytes = Arrays.concatenate(new byte[]{0x04}, X, Y);
			byte[] priBytes = BigIntegers.asUnsignedByteArray(32, new BigInteger(1, pri.getOctets()));

			out[0] = pubBytes;
			out[1] = priBytes;
			return out;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] privateKeyPKCS8ToDean(byte[] derP8) {
		String deanHeader = "MBMTABMAEwATABMAEwACAgEAAgEH";

		try {
			PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(derP8);
			ECPrivateKey ecPrivateKey = ECPrivateKey.getInstance(privateKeyInfo.parsePrivateKey()) ;
			BigInteger priKey = ecPrivateKey.getKey();
			DERBitString pubBitStr = ecPrivateKey.getPublicKey();
			byte[] rawPriKey = BigIntegers.asUnsignedByteArray(32, priKey);
            byte[] rawPubKey = pubBitStr.getBytes();
			ASN1Sequence deanHeaderSeq = ASN1Sequence.getInstance(Base64.decode(deanHeader));
			byte[] pubX = Arrays.copyOfRange(rawPubKey, 1, 33);
			byte[] pubY = Arrays.copyOfRange(rawPubKey, 33, 65);
			byte[] pri = BigIntegers.asUnsignedByteArray(32, new BigInteger(1, rawPriKey));

			DERPrintableString pubXString = new DERPrintableString(new String(pubX, "ISO-8859-1"));
			DERPrintableString pubYString = new DERPrintableString(new String(pubY, "ISO-8859-1"));
			DERPrintableString priString = new DERPrintableString(new String(pri, "ISO-8859-1"));

			ASN1EncodableVector v1 = new ASN1EncodableVector();
			v1.add(deanHeaderSeq);
			v1.add(pubXString);
			v1.add(pubYString);
			v1.add(priString);
			DERSequence deanKeySeq = new DERSequence(v1);

			return deanKeySeq.getEncoded("DER");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[][] privateKeyPKCS8ToRaw(byte[] derP8) {
		byte[][] out = new byte[2][];
		try {
			PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(derP8);
			ECPrivateKey ecPrivateKey = ECPrivateKey.getInstance(privateKeyInfo.parsePrivateKey()) ;
			BigInteger priKey = ecPrivateKey.getKey();
			DERBitString pubBitStr = ecPrivateKey.getPublicKey();
			byte[] rawPriKey = BigIntegers.asUnsignedByteArray(32, priKey);
            byte[] rawPubKey = pubBitStr.getBytes();
			byte[] pubX = Arrays.copyOfRange(rawPubKey, 1, 33);
			byte[] pubY = Arrays.copyOfRange(rawPubKey, 33, 65);
			
			byte[] pubBytes = Arrays.concatenate(new byte[]{0x04}, pubX, pubY);
			byte[] priBytes = BigIntegers.asUnsignedByteArray(32, new BigInteger(1, rawPriKey));

			out[0] = pubBytes;
			out[1] = priBytes;
			return out;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] privateKeyRawToPKCS8(byte[] rawPriKey, byte[] rawPubKey) {
		try {
			ASN1ObjectIdentifier objId = new ASN1ObjectIdentifier("1.2.840.10045.2.1");
			ASN1ObjectIdentifier objParam = new ASN1ObjectIdentifier("1.2.156.10197.1.301");
			AlgorithmIdentifier algId = new AlgorithmIdentifier(objId, objParam);
			
			BigInteger priKey = new BigInteger(1, rawPriKey);
            ECPrivateKey ecPriKey = new ECPrivateKey(priKey, new DERBitString(rawPubKey), objParam);
			PrivateKeyInfo privateKeyInfo = new PrivateKeyInfo(algId, ecPriKey);
			
			return privateKeyInfo.getEncoded("DER");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] getPrivateKeyPKCS8(AsymmetricCipherKeyPair sm2KeyPair) {
		byte[] rawPriKey = getPrivateKeyRaw(sm2KeyPair);
		byte[] rawPubKey = getPublicKeyRaw(sm2KeyPair);
		if (rawPriKey==null || rawPubKey==null) {
			return null;
		}
		
		return privateKeyRawToPKCS8(rawPriKey, rawPubKey);
	}

	public static byte[] publicKeyRawToPKCS8(byte[] rawPubKey) {
		try {
			ASN1ObjectIdentifier objId = new ASN1ObjectIdentifier("1.2.840.10045.2.1");
			ASN1ObjectIdentifier objParam = new ASN1ObjectIdentifier("1.2.156.10197.1.301");
			AlgorithmIdentifier algId = new AlgorithmIdentifier(objId, objParam);
			
			SubjectPublicKeyInfo subjectPublicKeyInfo = new SubjectPublicKeyInfo(algId, rawPubKey);

			return subjectPublicKeyInfo.getEncoded("DER");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] getPublicKeyPKCS8(AsymmetricCipherKeyPair sm2KeyPair) {
		byte[] rawPubKey = getPublicKeyRaw(sm2KeyPair);
		if (rawPubKey==null) {
			return null;
		}
		
		return publicKeyRawToPKCS8(rawPubKey);
	}

}
