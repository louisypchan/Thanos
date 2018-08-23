package com.thanos.common.security;

import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.sec.ECPrivateKey;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.TBSCertificate;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.encoders.Base64;

import java.math.BigInteger;

public class SM2Util {

	public static final byte[] sign(byte[] toSignBytes, String privateKeyBase64) throws Exception {
		
		byte[] signed = null;
		try {
			byte[] sm2PriKeyP8Bytes = Base64.decode(privateKeyBase64);
			PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(sm2PriKeyP8Bytes);
			ECPrivateKey ecPriKey = ECPrivateKey.getInstance(privateKeyInfo.getPrivateKey());
			byte[] rawPri2 = ecPriKey.getKey().toByteArray();
			DERBitString pubBit = ecPriKey.getPublicKey();
			byte[] rawPub = pubBit.getBytes();
			byte[] rawPub2 = new byte[64];
			System.arraycopy(rawPub, rawPub.length - 64, rawPub2, 0, 64);

			SM2Crypto sm2Crypto = new SM2Crypto();
			String signStr = sm2Crypto.Sm2Sign(toSignBytes, rawPub2, rawPri2);
			byte[] signValue = Base64.decode(signStr);
			byte[] r = Arrays.copyOfRange(signValue, 0, 32);
			byte[] s = Arrays.copyOfRange(signValue, 32, 64);
			ASN1EncodableVector v = new ASN1EncodableVector();
			v.add(new ASN1Integer(new BigInteger(1, r)));
			v.add(new ASN1Integer(new BigInteger(1, s)));
			DERSequence signSeq = new DERSequence(v);
			signed = signSeq.getEncoded("DER");
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		return signed;
	}


	public static final boolean verify(byte[] toSignBytes, byte[] signedValue, String signCertBase64) throws Exception {

		boolean verifyResult = false;
		try {
			byte[] signCertBytes = Base64.decode(signCertBase64);
			ASN1Sequence signCertSeq = ASN1Sequence.getInstance(signCertBytes);
			ASN1Sequence tbsCertSeq = (ASN1Sequence) signCertSeq.getObjectAt(0);

			TBSCertificate tbsServerSigCert = TBSCertificate.getInstance(tbsCertSeq);
			SubjectPublicKeyInfo issuerPublicKeyInfo = tbsServerSigCert.getSubjectPublicKeyInfo();
			DERBitString serverPubBit = issuerPublicKeyInfo.getPublicKeyData();
			byte[] rawPub = serverPubBit.getBytes();
			byte[] rawPub2 = new byte[64];
			System.arraycopy(rawPub, rawPub.length - 64, rawPub2, 0, 64);

			byte[] signValue2 = null;
			if (signedValue.length > 64 && signedValue[0] == 0x30) {
				ASN1Sequence signSeq = ASN1Sequence.getInstance(signedValue);
				ASN1Integer r = (ASN1Integer) signSeq.getObjectAt(0);
				ASN1Integer s = (ASN1Integer) signSeq.getObjectAt(1);
				byte[] rBytes = BigIntegers.asUnsignedByteArray(32, r.getPositiveValue());
				byte[] sBytes = BigIntegers.asUnsignedByteArray(32, s.getPositiveValue());
				signValue2 = Arrays.concatenate(rBytes, sBytes);
			} else {
				signValue2 = signedValue;
			}

			SM2Crypto sm2Crypto = new SM2Crypto();
			verifyResult = sm2Crypto.Verify(toSignBytes, signValue2, rawPub2);

		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		return verifyResult;
	}

}
