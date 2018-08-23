package com.thanos.common.pdf.signature;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

import com.itextpdf.kernel.PdfException;
import com.itextpdf.signatures.SecurityIDs;
import org.bouncycastle.asn1.*;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Pkcs7Parser {
	private int version;
	private int signerversion;
	private Set<String> digestalgos;
	private byte[] digest;
	private byte[] RSAdata;
	private Collection<Certificate> certs;
	private X509Certificate signCert;
	private String digestAlgorithmOid;
	private String digestEncryptionAlgorithmOid;
	private byte[] sigAttr;
	private byte[] sigAttrDer;
	private byte[] digestAttr;
	
	public Pkcs7Parser(byte[] pkcs7Data) {
        try {
            ASN1InputStream din = new ASN1InputStream(new ByteArrayInputStream(pkcs7Data));

            //
            // Basic checks to make sure it's a PKCS#7 SignedData Object
            //
            ASN1Primitive pkcs;

            try {
                pkcs = din.readObject();
            }
            catch (IOException e) {
                throw new IllegalArgumentException(new PdfException("can.t.decode.pkcs7signeddata.object"));
            }
            if (!(pkcs instanceof ASN1Sequence)) {
                throw new IllegalArgumentException(new PdfException("not.a.valid.pkcs.7.object.not.a.sequence"));
            }
            ASN1Sequence signedData = (ASN1Sequence)pkcs;
            ASN1ObjectIdentifier objId = (ASN1ObjectIdentifier)signedData.getObjectAt(0);
            if (!objId.getId().equals(SecurityIDs.ID_PKCS7_SIGNED_DATA) && !objId.getId().equals(com.thanos.common.security.SecurityIDs.ID_SM2_PKCS7_SIGNED_DATA))
                throw new IllegalArgumentException(new PdfException("not.a.valid.pkcs.7.object.not.signed.data"));
            ASN1Sequence content = (ASN1Sequence)((ASN1TaggedObject)signedData.getObjectAt(1)).getObject();
            // the positions that we care are:
            //     0 - version
            //     1 - digestAlgorithms
            //     2 - possible ID_SM2_PKCS7_DATA
            //     (the certificates and crls are taken out by other means)
            //     last - signerInfos

            // the version
            version = ((ASN1Integer)content.getObjectAt(0)).getValue().intValue();

            // the digestAlgorithms
            digestalgos = new HashSet<String>();
			Enumeration<ASN1Sequence> e = ((ASN1Set)content.getObjectAt(1)).getObjects();
            while (e.hasMoreElements()) {
                ASN1Sequence s = e.nextElement();
                ASN1ObjectIdentifier o = (ASN1ObjectIdentifier)s.getObjectAt(0);
                digestalgos.add(o.getId());
            }

            // the possible ID_SM2_PKCS7_DATA
            ASN1Sequence rsaData = (ASN1Sequence)content.getObjectAt(2);
            if (rsaData.size() > 1) {
                ASN1OctetString rsaDataContent = (ASN1OctetString)((ASN1TaggedObject)rsaData.getObjectAt(1)).getObject();
                RSAdata = rsaDataContent.getOctets();
            }

            int next = 3;
            while (!(content.getObjectAt(next) instanceof ASN1TaggedObject))
                ++next;

            // the certificates
/*
			This should work, but that's not always the case because of a bug in BouncyCastle:
*/	
//            X509CertParser cr = new X509CertParser();
//            cr.engineInit(new ByteArrayInputStream(pkcs7Data));
//            certs = cr.engineReadAll();

//            The following workaround was provided by Alfonso Massa, but it doesn't always work either.

            ASN1Set certSet = null;
            ASN1Set crlSet = null;
            while (content.getObjectAt(next) instanceof ASN1TaggedObject) {
                ASN1TaggedObject tagged = (ASN1TaggedObject)content.getObjectAt(next);

                switch (tagged.getTagNo()) {
                case 0:
                    certSet = ASN1Set.getInstance(tagged, false);
                    break;
                case 1:
                    crlSet = ASN1Set.getInstance(tagged, false);
                    break;
                default:
                    throw new IllegalArgumentException("unknown tag value " + tagged.getTagNo());
                }
                ++next;
            }
            certs = new ArrayList<Certificate>(certSet.size());

            CertificateFactory certFact = CertificateFactory.getInstance("X.509", new BouncyCastleProvider());
            for (Enumeration en = certSet.getObjects(); en.hasMoreElements();) {
                ASN1Primitive obj = ((ASN1Encodable)en.nextElement()).toASN1Primitive();
                if (obj instanceof ASN1Sequence) {
    	            ByteArrayInputStream stream = new ByteArrayInputStream(obj.getEncoded());
    	            X509Certificate x509Certificate = (X509Certificate)certFact.generateCertificate(stream);
    	            stream.close();
    				certs.add(x509Certificate);
                }
            }

            // the signerInfos
            ASN1Set signerInfos = (ASN1Set)content.getObjectAt(next);
            if (signerInfos.size() != 1)
                throw new IllegalArgumentException(new PdfException("this.pkcs.7.object.has.multiple.signerinfos.only.one.is.supported.at.this.time"));
            ASN1Sequence signerInfo = (ASN1Sequence)signerInfos.getObjectAt(0);
            // the positions that we care are
            //     0 - version
            //     1 - the signing certificate issuer and serial number
            //     2 - the digest algorithm
            //     3 or 4 - digestEncryptionAlgorithm
            //     4 or 5 - encryptedDigest
            signerversion = ((ASN1Integer)signerInfo.getObjectAt(0)).getValue().intValue();
            // Get the signing certificate
            ASN1Sequence issuerAndSerialNumber = (ASN1Sequence)signerInfo.getObjectAt(1);
            X509Principal issuer = new X509Principal(issuerAndSerialNumber.getObjectAt(0).toASN1Primitive().getEncoded());
            BigInteger serialNumber = ((ASN1Integer)issuerAndSerialNumber.getObjectAt(1)).getValue();
            for (Object element : certs) {
                X509Certificate cert = (X509Certificate)element;
                if (cert.getIssuerDN().equals(issuer) && serialNumber.equals(cert.getSerialNumber())) {
                    signCert = cert;
                    break;
                }
            }
            if (signCert == null) {
                throw new IllegalArgumentException(new PdfException("can.t.find.signing.certificate.with.serial.1",
                    issuer.getName() + " / " + serialNumber.toString(16)));
            }
//            signCertificateChain();
            digestAlgorithmOid = ((ASN1ObjectIdentifier)((ASN1Sequence)signerInfo.getObjectAt(2)).getObjectAt(0)).getId();
            next = 3;
            
            if (signerInfo.getObjectAt(next) instanceof ASN1TaggedObject) {
                ASN1TaggedObject tagsig = (ASN1TaggedObject)signerInfo.getObjectAt(next);
                ASN1Set sseq = ASN1Set.getInstance(tagsig, false);

                for (int k = 0; k < sseq.size(); ++k) {
                    ASN1Sequence seq2 = (ASN1Sequence) sseq.getObjectAt(k);
                    String idSeq2 = ((ASN1ObjectIdentifier) seq2.getObjectAt(0)).getId();
                    if (idSeq2.equals(SecurityIDs.ID_MESSAGE_DIGEST)) {
                        ASN1Set set = (ASN1Set) seq2.getObjectAt(1);
                        digestAttr = ((ASN1OctetString) set.getObjectAt(0)).getOctets();
                    }
                }

                sigAttr = sseq.getEncoded();
                // maybe not necessary, but we use the following line as fallback:
                sigAttrDer = sseq.getEncoded(ASN1Encoding.DER);

                ++next;
            }
            
            digestEncryptionAlgorithmOid = ((ASN1ObjectIdentifier)((ASN1Sequence)signerInfo.getObjectAt(next++)).getObjectAt(0)).getId();
            digest = ((ASN1OctetString)signerInfo.getObjectAt(next++)).getOctets();
            
        }
        catch (Exception e) {
            throw new PdfException(e);
        }
    }

	public byte[] getDigest() {
		return digest;
	}

	public X509Certificate getSignCert() {
		return signCert;
	}

	public String getDigestAlgorithmOid() {
		return digestAlgorithmOid;
	}

	public String getDigestEncryptionAlgorithmOid() {
		return digestEncryptionAlgorithmOid;
	}

	public byte[] getSigAttr() {
		return sigAttr;
	}

	public byte[] getSigAttrDer() {
		return sigAttrDer;
	}

	public byte[] getDigestAttr() {
		return digestAttr;
	}
	
}
