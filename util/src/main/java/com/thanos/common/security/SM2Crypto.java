package com.thanos.common.security;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECFieldElement.Fp;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.encoders.Base64;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

public class SM2Crypto {

	public String[] ecc_param = sm2_param;

	public static BigInteger ecc_p;
	public BigInteger ecc_a;
	public BigInteger ecc_b;
	public BigInteger ecc_n;
	public BigInteger ecc_gx;
	public BigInteger ecc_gy;

	public static ECCurve ecc_curve;
	public ECPoint ecc_point_g;

	public ECDomainParameters ecc_bc_spec;

	public ECKeyPairGenerator ecc_key_pair_generator;
	public ECFieldElement ecc_gx_field_element;
	public ECFieldElement ecc_gy_field_element;

	public SM2Crypto() {
		ecc_p = new BigInteger(ecc_param[0], 16);
		ecc_a = new BigInteger(ecc_param[1], 16);
		ecc_b = new BigInteger(ecc_param[2], 16);
		ecc_n = new BigInteger(ecc_param[3], 16);
		ecc_gx = new BigInteger(ecc_param[4], 16);
		ecc_gy = new BigInteger(ecc_param[5], 16);

		ecc_gx_field_element = new Fp(ecc_p, ecc_gx);
		ecc_gy_field_element = new Fp(ecc_p, ecc_gy);

		ecc_curve = new ECCurve.Fp(ecc_p, ecc_a, ecc_b, null, null);
		ecc_point_g = new ECPoint.Fp(ecc_curve, ecc_gx_field_element, ecc_gy_field_element, false);

		ecc_bc_spec = new ECDomainParameters(ecc_curve, ecc_point_g, ecc_n);

		ECKeyGenerationParameters ecc_ec_gen_param;
		ecc_ec_gen_param = new ECKeyGenerationParameters(ecc_bc_spec, new SecureRandom());

		ecc_key_pair_generator = new ECKeyPairGenerator();
		ecc_key_pair_generator.init(ecc_ec_gen_param);
	}

	public static String[] sm2_param = { "FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFF", // p,0
			"FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFC", // a,1
			"28E9FA9E9D9F5E344D5A9E4BCF6509A7F39789F515AB8F92DDBCBD414D940E93", // b,2
			"FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFF7203DF6B21C6052B53BBF40939D54123", // n,3
			"32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7", // gx,4
			"BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0" // gy,5
	};

	public boolean Verify(byte[] msg, byte[] signedData, byte[] certPK) {
		byte[] pkX = SubByte(certPK, 0, 32);
		byte[] pkY = SubByte(certPK, 32, 32);
		BigInteger biX = new BigInteger(1, pkX);
		BigInteger biY = new BigInteger(1, pkY);
		ECFieldElement x = new Fp(ecc_p, biX);
		ECFieldElement y = new Fp(ecc_p, biY);
		ECPoint userKey = new ECPoint.Fp(ecc_curve, x, y, false);

		SM3Digest sm3 = new SM3Digest();
		byte[] p = msg;
		sm3.update(p, 0, p.length);
		byte[] md = new byte[32];
		sm3.doFinal(md, 0);
		byte[] btRS = signedData;
		byte[] btR = SubByte(btRS, 0, btRS.length / 2);
		byte[] btS = SubByte(btRS, btR.length, btRS.length - btR.length);

		BigInteger r = new BigInteger(1, btR);
		BigInteger s = new BigInteger(1, btS);

		BigInteger e = new BigInteger(1, md);

		BigInteger t = r.add(s).mod(ecc_n);

		if (t.equals(BigInteger.ZERO))
			return false;

		ECPoint x1y1 = ecc_point_g.multiply(s);
		x1y1 = x1y1.add(userKey.multiply(t));

		BigInteger R = e.add(x1y1.normalize().getXCoord().toBigInteger()).mod(ecc_n);

		return r.equals(R);

	}

	public static byte[] SubByte(byte[] input, int startIndex, int length) {
		byte[] bt = new byte[length];
		for (int i = 0; i < length; i++) {
			bt[i] = input[i + startIndex];
		}
		return bt;
	}

	public String Sm2Sign(byte[] md, byte[] pk, byte[] privatekey) {
		SM3Digest sm3 = new SM3Digest();

		byte[] p = md;
		sm3.update(p, 0, p.length);

		byte[] hashData = new byte[32];
		sm3.doFinal(hashData, 0);

		BigInteger e = new BigInteger(1, hashData);
		BigInteger k = null;
		BigInteger r = null;
		BigInteger s = null;
		BigInteger pri = new BigInteger(1, privatekey);
		do {
			do {
				k = BigInteger.probablePrime(256, new Random());
				ECPoint kG = ecc_point_g.multiply(k);
				BigInteger x1 = kG.normalize().getXCoord().toBigInteger();

				r = e.add(x1);
				r = r.mod(ecc_n);
			} while (r.equals(BigInteger.ZERO) || r.add(k).equals(ecc_n));

			BigInteger da_1 = pri.add(BigInteger.ONE);
			da_1 = da_1.modInverse(ecc_n);
			s = r.multiply(pri);
			s = k.subtract(s);
			s = da_1.multiply(s).mod(ecc_n);
		} while (s.equals(BigInteger.ZERO));

		byte[] btRS = new byte[64];
		byte[] btR = BigIntegers.asUnsignedByteArray(32, r);
		byte[] btS = BigIntegers.asUnsignedByteArray(32, s);
		System.arraycopy(btR, btR.length - 32, btRS, 0, 32);
		System.arraycopy(btS, btS.length - 32, btRS, 32, 32);
		byte[] encode = Base64.encode(btRS);
		return new String(encode);
	}

}