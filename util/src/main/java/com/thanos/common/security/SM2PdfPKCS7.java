package com.thanos.common.security;

import com.itextpdf.kernel.PdfException;
import com.itextpdf.signatures.ITSAClient;
import com.itextpdf.signatures.PdfSigner;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @ClassName SM2PdfPKCS7
 * @Description TODO
 * @author Llx
 *
 */
public class SM2PdfPKCS7 {
    private final int VERSION = 1;

	private byte[] externalDigest;
	private byte[] certBytes;

    private Set<String> digestAlgorithms;
    private String digestEncryptionAlgorithmOid;
    private String digestAlgorithmOid;
    private X509CertificateHolder signCert;
    
    public SM2PdfPKCS7(byte[] certBytes)
			throws IOException {

    	this.certBytes = certBytes;
    	this.signCert = new X509CertificateHolder(certBytes);

    	this.digestEncryptionAlgorithmOid = SecurityIDs.SM2_OID;
    	this.digestAlgorithmOid = SecurityIDs.SM3_WITH_SM2_OID;

		digestAlgorithms = new HashSet<>();
		digestAlgorithms.add(digestAlgorithmOid);

	}
	
	public byte[] getAuthenticatedAttributeBytes(byte secondDigest[], byte[] ocsp, Collection<byte[]> crlBytes) {
        try {
            return getAuthenticatedAttributeSet(secondDigest, ocsp, crlBytes).getEncoded(ASN1Encoding.DER);
        }
        catch (Exception e) {
            throw new PdfException(e);
        }
    }
	
	public void setExternalDigest(byte digest[], String digestEncryptionAlgorithm) {
        externalDigest = digest;
    }
	
	public byte[] getEncodedPKCS7(byte[] secondDigest, ITSAClient tsaClient, byte[] ocsp, Collection<byte[]> crlBytes, PdfSigner.CryptoStandard sigtype) {
        try {
            ASN1EncodableVector v;
            //Create SignedData
            ASN1EncodableVector signedData = new ASN1EncodableVector();
            //Add Version
            signedData.add(new ASN1Integer(VERSION));

            //Add DigestAlgorithms
            v = new ASN1EncodableVector();
            v.add(new ASN1ObjectIdentifier(digestAlgorithmOid));
            v.add(DERNull.INSTANCE);
            DERSet s = new DERSet(new DERSequence(v));
            signedData.add(s);

            //Add ContendInfo
            v = new ASN1EncodableVector();
            v.add(new ASN1ObjectIdentifier(SecurityIDs.ID_SM2_PKCS7_DATA));
            signedData.add(new DERSequence(v));

            //Add Certificates
            ASN1InputStream tempstream = new ASN1InputStream(certBytes);
            v = new ASN1EncodableVector();
            v.add(tempstream.readObject());
            DERSet dercertificates = new DERSet(v);
            signedData.add(new DERTaggedObject(false, 0, dercertificates));

            //Create SignInfos
            ASN1EncodableVector signerInfo = new ASN1EncodableVector();
            //Add Version
            signerInfo.add(new ASN1Integer(1));

            //Add IssuerAndSerialNumber
            v = new ASN1EncodableVector();
            X509CertificateHolder x509CertHoler = new X509CertificateHolder(certBytes);
            v.add(x509CertHoler.getIssuer().toASN1Primitive());
            v.add(new ASN1Integer(x509CertHoler.getSerialNumber()));
            signerInfo.add(new DERSequence(v));

            //Add DigestAlgorithm
            v = new ASN1EncodableVector();
            v.add(new ASN1ObjectIdentifier(digestAlgorithmOid));
            v.add(DERNull.INSTANCE);
            signerInfo.add(new DERSequence(v));

            //Add AuthenticatedAttribute
            DERSet authenticatedAttribute = getAuthenticatedAttributeSet(secondDigest, ocsp, crlBytes);
            signerInfo.add(new DERTaggedObject(false, 0, authenticatedAttribute));

            //Add DigestEncryptionAlgorithm
            v = new ASN1EncodableVector();
            v.add(new ASN1ObjectIdentifier(digestEncryptionAlgorithmOid));
            v.add(DERNull.INSTANCE);
            signerInfo.add(new DERSequence(v));

            //Add EncryptedDigest
            signerInfo.add(new DEROctetString(externalDigest));

            //Add TSA
            if (tsaClient != null) {
                byte[] tsImprint = tsaClient.getMessageDigest().digest(externalDigest);
                byte[] tsToken = tsaClient.getTimeStampToken(tsImprint);
                if (tsToken != null) {
                    ASN1EncodableVector unauthAttributes = buildUnauthenticatedAttributes(tsToken);
                    if (unauthAttributes != null) {
                        signerInfo.add(new DERTaggedObject(false, 1, new DERSet(unauthAttributes)));
                    }
                }
            }

            //Add SignedData
            DERSet dersignerInfos = new DERSet(new DERSequence(signerInfo));
            signedData.add(dersignerInfos);

            //Encodeing whole
            ASN1EncodableVector whole = new ASN1EncodableVector();
            whole.add(new ASN1ObjectIdentifier(SecurityIDs.ID_SM2_PKCS7_SIGNED_DATA));
            whole.add(new DERTaggedObject(0, new DERSequence(signedData)));

            ByteArrayOutputStream bOut = new ByteArrayOutputStream();

            ASN1OutputStream dout = new ASN1OutputStream(bOut);
            dout.writeObject(new DERSequence(whole));
            dout.close();

            return bOut.toByteArray();

        }
        catch (Exception e) {
            throw new PdfException(e);
        }
    }

	private DERSet getAuthenticatedAttributeSet(byte secondDigest[], byte[] ocsp, Collection<byte[]> crlBytes) {
        try {
            ASN1EncodableVector attribute = new ASN1EncodableVector();
            ASN1EncodableVector v = new ASN1EncodableVector();
            v.add(new ASN1ObjectIdentifier(com.itextpdf.signatures.SecurityIDs.ID_CONTENT_TYPE));
            v.add(new DERSet(new ASN1ObjectIdentifier(SecurityIDs.ID_SM2_PKCS7_DATA)));
            attribute.add(new DERSequence(v));
            v = new ASN1EncodableVector();
            v.add(new ASN1ObjectIdentifier(com.itextpdf.signatures.SecurityIDs.ID_MESSAGE_DIGEST));
            v.add(new DERSet(new DEROctetString(secondDigest)));
            attribute.add(new DERSequence(v));
            boolean haveCrl = false;
            if (crlBytes != null) {
                for (byte[] bCrl : crlBytes) {
                    if (bCrl != null) {
                        haveCrl = true;
                        break;
                    }
                }
            }
            if (ocsp != null || haveCrl) {
                v = new ASN1EncodableVector();
                v.add(new ASN1ObjectIdentifier(com.itextpdf.signatures.SecurityIDs.ID_ADBE_REVOCATION));

                ASN1EncodableVector revocationV = new ASN1EncodableVector();

                if (haveCrl) {
                    ASN1EncodableVector v2 = new ASN1EncodableVector();
                    for (byte[] bCrl : crlBytes) {
                        if (bCrl == null)
                            continue;
                        ASN1InputStream t = new ASN1InputStream(new ByteArrayInputStream(bCrl));
                        v2.add(t.readObject());
                    }
                    revocationV.add(new DERTaggedObject(true, 0, new DERSequence(v2)));
                }

                if (ocsp != null) {
	                DEROctetString doctet = new DEROctetString(ocsp);
	                ASN1EncodableVector vo1 = new ASN1EncodableVector();
	                ASN1EncodableVector v2 = new ASN1EncodableVector();
	                v2.add(OCSPObjectIdentifiers.id_pkix_ocsp_basic);
	                v2.add(doctet);
	                ASN1Enumerated den = new ASN1Enumerated(0);
	                ASN1EncodableVector v3 = new ASN1EncodableVector();
	                v3.add(den);
	                v3.add(new DERTaggedObject(true, 0, new DERSequence(v2)));
	                vo1.add(new DERSequence(v3));
	                revocationV.add(new DERTaggedObject(true, 1, new DERSequence(vo1)));
                }

                v.add(new DERSet(new DERSequence(revocationV)));
                attribute.add(new DERSequence(v));
            }

            return new DERSet(attribute);
        }
        catch (Exception e) {
            throw new PdfException(e);
        }
    }
	
	private ASN1EncodableVector buildUnauthenticatedAttributes(byte[] timeStampToken)  throws IOException {
        if (timeStampToken == null)
            return null;

        // @todo: move this together with the rest of the defintions
        String ID_TIME_STAMP_TOKEN = "1.2.840.113549.1.9.16.2.14"; // RFC 3161 id-aa-timeStampToken

        ASN1InputStream tempstream = new ASN1InputStream(new ByteArrayInputStream(timeStampToken));
        ASN1EncodableVector unauthAttributes = new ASN1EncodableVector();

        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new ASN1ObjectIdentifier(ID_TIME_STAMP_TOKEN)); // id-aa-timeStampToken
        ASN1Sequence seq = (ASN1Sequence) tempstream.readObject();
        v.add(new DERSet(seq));

        unauthAttributes.add(new DERSequence(v));
        return unauthAttributes;
     }
}
