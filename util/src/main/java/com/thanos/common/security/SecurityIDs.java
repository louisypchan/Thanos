package com.thanos.common.security;

import java.util.HashMap;

/**
 * @author Llx
 * @create 2018/8/21
 **/
public class SecurityIDs {
	private static final HashMap<String, String> algorithmNames = new HashMap<String, String>();
	private static final HashMap<String, String> algorithmOids = new HashMap<String, String>();

    public static final String ID_SM2_PKCS7_DATA = "1.2.156.10197.6.1.4.2.1";
    public static final String ID_SM2_PKCS7_SIGNED_DATA = "1.2.156.10197.6.1.4.2.2";

	public final static String SM2 = "SM2";
	public final static String SM3_WITH_SM2 = "SM3withSM2";
	public final static String RSA = "RSA";
	public final static String SHA1_WITH_RSA = "SHA1withRSA";
	public final static String SHA256_WITH_RSA = "SHA256withRSA";
	public final static String ECC = "ECC";
	public final static String SHA1_WITH_ECDSA = "SHA1withECDSA";
    public final static String SHA256_WITH_ECDSA = "SHA256withECDSA";

    public final static String SM2_OID = "1.2.156.10197.1.301";
    public final static String SM3_OID = "1.2.156.10197.1.401";
    public final static String SM3_WITH_SM2_OID = "1.2.156.10197.1.501";
    public final static String RSA_OID = "1.2.840.113549.1.1.1";
    public final static String SHA1_WITH_RSA_OID = "1.2.840.113549.1.1.5";
    public final static String SHA256_WITH_RSA_OID = "1.2.840.113549.1.1.11";
    public final static String ECC_OID = "1.2.840.10045.2.1";
    public final static String SHA1_WITH_ECDSA_OID = "1.2.840.10045.4.1";
    public final static String SHA256_WITH_ECDSA_OID = "1.2.840.10045.4.3.2";

    public final static String SM3 = "SM3";
    public final static String SHA1 = "SHA-1";
    public final static String SHA256 = "SHA-256";
	
	static{
		algorithmNames.put("1.2.156.10197.1.301", SM2);
		algorithmNames.put("1.2.156.10197.1.401", SM3);
		algorithmNames.put("1.2.156.10197.1.501", SM3_WITH_SM2);
		algorithmNames.put("1.2.840.113549.1.1.1", RSA);
		algorithmNames.put("1.2.840.113549.1.1.5", SHA1_WITH_RSA);
		algorithmNames.put("1.2.840.113549.1.1.11", SHA256_WITH_RSA);
        algorithmNames.put("1.2.840.10045.2.1", ECC);
		algorithmNames.put("1.2.840.10045.4.1", SHA1_WITH_ECDSA);
		algorithmNames.put("1.2.840.10045.4.3.2", SHA256_WITH_ECDSA);

        algorithmOids.put(SM2, "1.2.156.10197.1.301");
        algorithmOids.put(SM3, "1.2.156.10197.1.401");
        algorithmOids.put(SM3_WITH_SM2, "1.2.156.10197.1.501");
        algorithmOids.put(RSA, "1.2.840.113549.1.1.1");
        algorithmOids.put(SHA1_WITH_RSA, "1.2.840.113549.1.1.5");
        algorithmOids.put(SHA256_WITH_RSA, "1.2.840.113549.1.1.11");
        algorithmOids.put(ECC, "1.2.840.10045.2.1");
        algorithmOids.put(SHA1_WITH_ECDSA, "1.2.840.10045.4.1");
        algorithmOids.put(SHA256_WITH_ECDSA, "1.2.840.10045.4.3.2");
	}
	
	public static String getAlgorithmName(String oid) {
	    return algorithmNames.get(oid);
	}

	public static String getAlgorithmOid(String name) {
        return algorithmOids.get(name);
    }
}
