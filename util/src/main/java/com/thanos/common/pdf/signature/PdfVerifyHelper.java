package com.thanos.common.pdf.signature;

import com.itextpdf.kernel.pdf.*;
import com.itextpdf.signatures.CertificateInfo;
import com.itextpdf.signatures.PdfPKCS7;
import com.itextpdf.signatures.SignatureUtil;
import com.thanos.common.cert.CertificateUtil;
import com.thanos.common.security.SM2Util;
import com.thanos.common.security.SecurityIDs;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: Llx
 * @Date: 2018/8/21
 * @Description:
 */
public class PdfVerifyHelper {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<VerifyInfo> verify(byte[] pdfFileBytes) throws Exception {
        if(logger.isDebugEnabled())
            logger.debug("proc verify pdf signatures");

        List<String> verifiedList = new ArrayList<>();
        List<VerifyInfo> verifyInfoList = new ArrayList<>();

        try {
            PdfReader pdfReader = new PdfReader(new ByteArrayInputStream(pdfFileBytes));
            PdfDocument pdfDocument = new PdfDocument(pdfReader);
            SignatureUtil signatureUtil = new SignatureUtil(pdfDocument);
            List<String> signatureNames = signatureUtil.getSignatureNames();

            VerifyInfo verifyInfo;
            Pkcs7Parser pkcs7Parser;
            for (String signatureName : signatureNames) {
                PdfDictionary pdfDictionary = signatureUtil.getSignatureDictionary(signatureName);
                PdfString contents = pdfDictionary.getAsString(PdfName.Contents);
                pkcs7Parser = new Pkcs7Parser(contents.getValueBytes());

                if(verifiedList.contains(signatureName))
                    continue;

                verifyInfo = new VerifyInfo();
                verifyInfo.setSignatureName(signatureName);
                verifyInfo.setRevisionNumber(signatureUtil.getRevision(signatureName));

                if(SecurityIDs.SM2_OID.equals(pkcs7Parser.getDigestEncryptionAlgorithmOid())) {
                    byte[] sh = pkcs7Parser.getSigAttr();
                    byte[] signedData = pkcs7Parser.getDigest();
                    byte[] signCertData = pkcs7Parser.getSignCert().getEncoded();

                    String str;
                    str = pdfDictionary.getAsString(PdfName.Reason) == null ? "" : pdfDictionary.getAsString(PdfName.Reason).toUnicodeString();
                    if (null != str)
                        verifyInfo.setReason(str);

                    str = pdfDictionary.getAsString(PdfName.Location) == null ? "" :  pdfDictionary.getAsString(PdfName.Location).toUnicodeString();
                    if (null != str)
                        verifyInfo.setLocation(str);

                    str = pdfDictionary.getAsString(PdfName.M) == null ? "" : pdfDictionary.getAsString(PdfName.M).getValue();
                    if (null != str)
                        verifyInfo.setSignDate(PdfDate.decode(str).getTime());

                    verifyInfo.setDigestAlgorithm(SecurityIDs.getAlgorithmName(pkcs7Parser.getDigestAlgorithmOid()));
                    verifyInfo.setEncryptionAlgorithm(SecurityIDs.getAlgorithmName(pkcs7Parser.getDigestEncryptionAlgorithmOid()));
                    verifyInfo.setValidity(SM2Util.verify(sh, signedData, Base64.toBase64String(signCertData)));

                    verifyInfo.setSignerName(CertificateInfo.getSubjectFields(CertificateUtil.getX509Certificate2(signCertData)).getField("CN"));
                } else {

                    PdfPKCS7 pdfPKCS7 = signatureUtil.verifySignature(signatureName , "BC");

                    verifyInfo.setSignDate(pdfPKCS7.getSignDate().getTime());
                    verifyInfo.setDigestAlgorithm(pdfPKCS7.getDigestAlgorithm());
                    verifyInfo.setLocation(pdfPKCS7.getLocation());
                    verifyInfo.setReason(pdfPKCS7.getReason());
                    verifyInfo.setEncryptionAlgorithm(pdfPKCS7.getEncryptionAlgorithm());
                    verifyInfo.setValidity(pdfPKCS7.verify());

                    X509Certificate signCert = pdfPKCS7.getSigningCertificate();

                    verifyInfo.setSignerName(CertificateInfo.getSubjectFields(signCert).getField("CN"));

                }

                PdfDictionary sigDict = signatureUtil.getSignatureDictionary(signatureName);
                PdfString contactInfo = sigDict.getAsString(PdfName.ContactInfo);
                if (contactInfo != null) {
                    verifyInfo.setContactInfo(contactInfo.toString());
                }

                verifyInfoList.add(verifyInfo);

                verifiedList.add(signatureName);

            }
        } catch (Exception e) {
            logger.error("verify pdf fail", e);
            throw e;
        }

        return verifyInfoList;
    }

}
