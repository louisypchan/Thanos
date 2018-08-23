package com.thanos.common.pdf.signature;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.forms.fields.PdfSignatureFormField;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteBuffer;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.annot.PdfAnnotation;
import com.itextpdf.kernel.pdf.annot.PdfWidgetAnnotation;
import com.itextpdf.layout.element.Image;
import com.itextpdf.signatures.*;
import com.thanos.common.security.SecurityIDs;
import com.thanos.common.security.DefaultSignProvider;
import com.thanos.common.security.SM2PdfPKCS7;
import com.thanos.common.security.SignProvider;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * @author Llx
 * @create 2018/8/21
 **/
public class PdfSignHelper {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private X509Certificate certificate;
    private String digestAlgorithm;
    private String signatureAlgorithm;
    private Calendar calendar;
    private byte[] hashBytes;
    private byte[] pdfTempBytes;
    private ITSAClient tsaClient;

    private final int contentEstimated = 4000;

    public PdfSignHelper(X509Certificate certificate, int type){
        this(certificate, null, type);
    }

    public PdfSignHelper(X509Certificate certificate, ITSAClient tsaClient, int type) {
        this.calendar = Calendar.getInstance();
        this.calendar.setTime(new Date());

        this.certificate = certificate;
        this.tsaClient = tsaClient;

        if(1 == type) {
            this.digestAlgorithm = SecurityIDs.SHA256;
            this.signatureAlgorithm = SecurityIDs.SHA256_WITH_RSA;
        } else {
            this.digestAlgorithm = SecurityIDs.SM3;
            this.signatureAlgorithm = SecurityIDs.SM3_WITH_SM2;
        }
    }

    private int getContentEstimated() {
        int contentEstimatedSize = contentEstimated;

        if(null != tsaClient)
            contentEstimatedSize =+ 2000;

        return contentEstimatedSize;
    }

    public SignHash calcSignHash(byte[] pdfFileBytes) throws Exception {
        return calcSignHash(pdfFileBytes, null);
    }

    public SignHash calcSignHash(byte[] pdfFileBytes, String reason) throws Exception {
        return calcSignHashWithSigFieldAppearance(pdfFileBytes, null, null, reason);
    }

    public SignHash calcSignHashWithSigFieldAppearance(byte[] pdfFileBytes, List<SigFieldAppearanceInfo> positionList) throws Exception {
        return calcSignHashWithSigFieldAppearance(pdfFileBytes, null, positionList, null);
    }

    public SignHash calcSignHashWithSigFieldAppearance(byte[] pdfFileBytes, String imgBase64, List<SigFieldAppearanceInfo> positionList) throws Exception {
        return calcSignHashWithSigFieldAppearance(pdfFileBytes, imgBase64, positionList, null);
    }

    public SignHash calcSignHashWithSigFieldAppearance(byte[] pdfFileBytes, String imgBase64, List<SigFieldAppearanceInfo> positionList, String reason) throws Exception{
        if(logger.isDebugEnabled())
            logger.debug("process calc sign hash...");

        SignHash signHash;

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        PdfReader reader = null;
        DefaultPdfSigner stp = null;
        PdfSignatureAppearance sap = null;

        try{
            InputStream is = new ByteArrayInputStream(((ByteArrayOutputStream) compressPdf(pdfFileBytes)).toByteArray());
//            InputStream is = new ByteArrayInputStream(pdfFileBytes);
            reader = new PdfReader(is);

            StampingProperties properties = new StampingProperties();
            properties.useAppendMode();
            stp = new DefaultPdfSigner(reader, os, properties);
            stp.getDocument().getWriter().setCompressionLevel(CompressionConstants.DEFAULT_COMPRESSION);
//            stp.getDocument().getWriter().setSmartMode(false);
            sap = stp.getSignatureAppearance();

            PdfSignature dic = new PdfSignature(PdfName.Adobe_PPKLite, PdfName.Adbe_pkcs7_detached);
            dic.put(PdfName.FT, PdfName.Sig);
            dic.setReason(sap.getReason());
            dic.setLocation(sap.getLocation());
            dic.setSignatureCreator(sap.getSignatureCreator());
            dic.setContact(sap.getContact());
            dic.setDate(new PdfDate(calendar)); // time-stamp will over-rule this
            stp.setCryptoDictionary(dic);

            if(null != positionList && !positionList.isEmpty())
                this.addMultiSigFieldAppearance(stp, calendar, imgBase64, positionList);

            signHash = this.getSignHash(stp, certificate);

            pdfTempBytes = os.toByteArray();

        } catch (Exception e) {
            logger.error("calc sign hash with multi signature field fail", e);
            throw e;
        } finally{
            if(null != os){
                os.flush();
                os.close();
            }
            if(null != reader)
                reader.close();
        }

        return signHash;
    }

    private SignHash getSignHash(DefaultPdfSigner stp, X509Certificate certificate) throws Exception {
        SignHash signHash = new SignHash();
        byte[] signHashBytes = null;

        stp.setCertificationLevel(com.itextpdf.signatures.PdfSigner.NOT_CERTIFIED);

        stp.getSignatureAppearance().setCertificate(certificate);

        int estimatedSize = contentEstimated;
        Map<PdfName, Integer> exc = new HashMap<>();
        exc.put(PdfName.Contents, Integer.valueOf(getContentEstimated() * 2 + 2));
        stp.preClose(exc);

        InputStream data = stp.getRangeStream();

        byte[] encodedSig = null;
        if(SecurityIDs.SM3.endsWith(digestAlgorithm)){
            SM3Digest sm3Digest = new SM3Digest();
            hashBytes = new byte[sm3Digest.getDigestSize()];

            byte buf[] = new byte[8192];
            int n;
            while ((n = data.read(buf)) > 0) {
                sm3Digest.update(buf, 0, n);
            }
            sm3Digest.doFinal(hashBytes, 0);

            SM2PdfPKCS7 sm2Sig = new SM2PdfPKCS7(certificate.getEncoded());

            signHashBytes = sm2Sig.getAuthenticatedAttributeBytes(hashBytes, null, null);

            sm2Sig.setExternalDigest(new byte[64], SecurityIDs.SM2);
            encodedSig = sm2Sig.getEncodedPKCS7(hashBytes, null, null, null, com.itextpdf.signatures.PdfSigner.CryptoStandard.CMS);

        } else{
            MessageDigest messageDigest = MessageDigest.getInstance(digestAlgorithm);
            byte buf[] = new byte[8192];
            int n;
            while ((n = data.read(buf)) > 0) {
                messageDigest.update(buf, 0, n);
            }
            hashBytes = messageDigest.digest();

            X509Certificate[] chain = new X509Certificate[]{certificate};
            IExternalDigest externalDigest = new BouncyCastleDigest();
            PdfPKCS7 sgn = new PdfPKCS7((PrivateKey) null, chain, "SHA-256", null, externalDigest, false);

            signHashBytes = sgn.getAuthenticatedAttributeBytes(hashBytes, null, null, com.itextpdf.signatures.PdfSigner.CryptoStandard.CMS);

            sgn.setExternalDigest(new byte[256], null, SecurityIDs.RSA);
            encodedSig = sgn.getEncodedPKCS7(hashBytes, null, null, null, com.itextpdf.signatures.PdfSigner.CryptoStandard.CMS);
        }

        if (getContentEstimated() < encodedSig.length)
            throw new IOException("Not enough space");

        if (estimatedSize < encodedSig.length)
            throw new IOException("Not enough space");

        byte[] paddedSig = new byte[estimatedSize];
        System.arraycopy(encodedSig, 0, paddedSig, 0, encodedSig.length);
        PdfDictionary dic2 = new PdfDictionary();
        dic2.put(PdfName.Contents, new PdfString(paddedSig).setHexWriting(true));
        stp.close(dic2);
        stp.setClose();

        signHash.setHashBytes(hashBytes);
        signHash.setSignHashBytes(signHashBytes);

        return signHash;
    }

    public byte[] embedSignWithPkcs1(byte[] signValueBytes) throws Exception{
        return embedSignWithPkcs1(pdfTempBytes, hashBytes, signValueBytes);
    }

    public byte[] embedSignWithPkcs1(byte[] pdfFileBytes, byte[] hashBytes, byte[] signValueBytes) throws Exception {
        if (logger.isDebugEnabled())
            logger.debug("process embed sign with pkcs1...");

        if(null == pdfFileBytes)
            throw new Exception("pdfFileBytes not be null");

        if(null == hashBytes)
            throw new Exception("hashBytes not be null");

        if(null == signValueBytes)
            throw new Exception("signValueBytes not be null");

        try{
            PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfFileBytes));

            byte[] encodedSig;
            if(SecurityIDs.SM3.endsWith(digestAlgorithm)){
                SM2PdfPKCS7 sm2Sig = new SM2PdfPKCS7(certificate.getEncoded());

                sm2Sig.setExternalDigest(signValueBytes, SecurityIDs.SM2);
                encodedSig = sm2Sig.getEncodedPKCS7(hashBytes, null, null, null, com.itextpdf.signatures.PdfSigner.CryptoStandard.CMS);
            } else{
                X509Certificate[] chain = new X509Certificate[]{certificate};
                IExternalDigest digest = new BouncyCastleDigest();
                PdfPKCS7 sgn = new PdfPKCS7(null, chain, digestAlgorithm, null, digest, false);

                sgn.setExternalDigest(signValueBytes, null, SecurityIDs.RSA);
                encodedSig = sgn.getEncodedPKCS7(hashBytes, tsaClient, null, null, com.itextpdf.signatures.PdfSigner.CryptoStandard.CMS);
            }
            if(logger.isDebugEnabled())
                logger.debug(String.format("pkcs7: %s", Base64.toBase64String(encodedSig)));

            updateSig(reader, encodedSig);
        } catch(Exception e){
            logger.error("embed sign with pkcs1 fail", e);
            throw e;
        }

        return pdfTempBytes;
    }

    public byte[] embedSignatureWithPksc7(byte[] pkcs7Bytes) throws Exception{
        return embedSignatureWithPkcs7(pdfTempBytes, pkcs7Bytes);
    }

    public byte[] embedSignatureWithPkcs7(byte[] pdfFileBytes, byte[] pkcs7Bytes) throws Exception {
        if(logger.isDebugEnabled())
            logger.debug("process embed sign with pkcs7");

        try{
            PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfFileBytes));

            updateSig(reader, pkcs7Bytes);
        }catch (Exception e){
            logger.error("embed sign with pkcs7 fail", e);
            throw e;
        }

        return pdfTempBytes;
    }

    private void updateSig(PdfReader reader, byte[] encodedSig) throws Exception {
        PdfDictionary update = new PdfDictionary();
        byte[] destinationArray = new byte[getContentEstimated()];
        System.arraycopy(encodedSig, 0, destinationArray, 0, encodedSig.length);
        update.put(PdfName.Contents, new PdfString(destinationArray).setHexWriting(true));
        ByteBuffer bf = new ByteBuffer();
        PdfObject pdfObj = update.get(PdfName.Contents);

        ByteArrayOutputStream bous = new ByteArrayOutputStream();
        PdfOutputStream os = new PdfOutputStream(bous);

        bous.reset();
        os.write(pdfObj);

        SignatureUtil signatureUtil = new SignatureUtil(new PdfDocument(reader));

        String fieldName = getLastSignatureFieldName(signatureUtil);
        PdfSignature signature = signatureUtil.getSignature(fieldName);
        PdfArray b = signature.getByteRange();
//        System.arraycopy(bous.toByteArray(), 0, pdfTempBytes, (int) SignatureUtil.asLongArray(b)[1], bous.size());
        System.arraycopy(bous.toByteArray(), 0, pdfTempBytes, (int) b.toLongArray()[1], bous.size());

    }

    public byte[] excSignUsePrivateKey(byte[] pdfFileBytes, String priKeyBase64) throws Exception {
        return excSignUsePrivateKey(pdfFileBytes, priKeyBase64, null);
    }

    public byte[] excSignUsePrivateKey(byte[] pdfFileBytes, String priKeyBase64, String reason) throws Exception {
        return excSignUsePrivateKeyWithSigFieldAppearance(pdfFileBytes, priKeyBase64, null, null, reason);
    }

    public byte[] excSignUsePrivateKeyWithSigFieldAppearance(byte[] pdfFileBytes, String imgBase64, List<SigFieldAppearanceInfo> positionList) throws Exception {
        return excSignUsePrivateKeyWithSigFieldAppearance(pdfFileBytes, null, imgBase64, positionList, null);
    }

    public byte[] excSignUsePrivateKeyWithSigFieldAppearance(byte[] pdfFileBytes, String priKeyBase64, String imgBase64, List<SigFieldAppearanceInfo> positionList) throws Exception {
        return excSignUsePrivateKeyWithSigFieldAppearance(pdfFileBytes, priKeyBase64, imgBase64, positionList, null);
    }

    public byte[] excSignUsePrivateKeyWithSigFieldAppearance(byte[] pdfFileBytes, String priKeyBase64, String imgBase64, List<SigFieldAppearanceInfo> positionList, String reason) throws Exception{
        if(logger.isDebugEnabled())
            logger.debug("process sign use private key...");

        SignProvider signProvider = new DefaultSignProvider();
        signProvider.setPrivateKeyByte(Base64.decode(priKeyBase64));
        signProvider.setSignatureAlgorithm(signatureAlgorithm);

        return signWithSigFieldAppearance(pdfFileBytes, imgBase64, positionList, reason, signProvider);
    }

    private byte[] signWithSigFieldAppearance(byte[] pdfFileBytes, String imgBase64, List<SigFieldAppearanceInfo> positionList, String reason, SignProvider signProvider) throws Exception{
        Security.addProvider(new BouncyCastleProvider());

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try{
            InputStream is = new ByteArrayInputStream(((ByteArrayOutputStream) compressPdf(pdfFileBytes)).toByteArray());
//            InputStream is = new ByteArrayInputStream(pdfFileBytes);
            PdfReader reader = new PdfReader(is);

            StampingProperties properties = new StampingProperties();
            properties.useAppendMode();
            DefaultPdfSigner stp = new DefaultPdfSigner(reader, os, properties);
            stp.getDocument().getWriter().setCompressionLevel(CompressionConstants.DEFAULT_COMPRESSION);
//            stp.getDocument().getWriter().setSmartMode(false);
            PdfSignatureAppearance sap = stp.getSignatureAppearance();

            PdfSignature dic = new PdfSignature(PdfName.Adobe_PPKLite, PdfName.Adbe_pkcs7_detached);
            dic.put(PdfName.FT, PdfName.Sig);
            dic.setReason(sap.getReason());
            dic.setLocation(sap.getLocation());
            dic.setSignatureCreator(sap.getSignatureCreator());
            dic.setContact(sap.getContact());
            dic.setDate(new PdfDate(calendar));
            stp.setCryptoDictionary(dic);

            if(null != positionList && !positionList.isEmpty())
                this.addMultiSigFieldAppearance(stp, calendar, imgBase64, positionList);

            this.sign(stp, digestAlgorithm, signProvider);

            return os.toByteArray();

        } catch (Exception e) {
            logger.error("execute pdf sign fail", e);
            throw e;
        } finally{
            if(null != os){
                os.flush();
                os.close();
            }
        }
    }

    private void sign(DefaultPdfSigner stp, String digestAlgorithm, SignProvider signProvider) throws Exception{
        HashMap<PdfName,Integer> exc = new HashMap();
        exc.put(PdfName.Contents, Integer.valueOf(getContentEstimated() * 2 + 2));

        stp.preClose(exc);

        InputStream data = stp.getRangeStream();

        byte[] encodedSig;
        if(SecurityIDs.SM3.equals(digestAlgorithm)){
            SM3Digest sm3Digest = new SM3Digest();
            hashBytes = new byte[sm3Digest.getDigestSize()];

            byte buf[] = new byte[8192];
            int n;
            while ((n = data.read(buf)) > 0) {
                sm3Digest.update(buf, 0, n);
            }
            sm3Digest.doFinal(hashBytes, 0);

            SM2PdfPKCS7 sm2Sig = new SM2PdfPKCS7(certificate.getEncoded());

            byte[] sh = sm2Sig.getAuthenticatedAttributeBytes(hashBytes, null, null);

            byte[] signData = signProvider.excSign(sh);
            sm2Sig.setExternalDigest(signData, SecurityIDs.SM2);

            encodedSig = sm2Sig.getEncodedPKCS7(hashBytes, null, null, null, com.itextpdf.signatures.PdfSigner.CryptoStandard.CMS);
        } else{
            MessageDigest messageDigest = MessageDigest.getInstance(digestAlgorithm);
            byte buf[] = new byte[8192];
            int n;
            while ((n = data.read(buf)) > 0) {
                messageDigest.update(buf, 0, n);
            }
            byte[] hash = messageDigest.digest();

            X509Certificate[] chain = new X509Certificate[]{};
            IExternalDigest digest = new BouncyCastleDigest();
            PdfPKCS7 sgn = new PdfPKCS7(null, chain, digestAlgorithm, null, digest, false);

            byte[] sh = sgn.getAuthenticatedAttributeBytes(hash, null, null, com.itextpdf.signatures.PdfSigner.CryptoStandard.CMS);

            byte[] signData = signProvider.excSign(sh);
            sgn.setExternalDigest(signData, null, SecurityIDs.RSA);

            encodedSig = sgn.getEncodedPKCS7(hash, tsaClient, null, null, com.itextpdf.signatures.PdfSigner.CryptoStandard.CMS);
        }

        PdfDictionary update = new PdfDictionary();
        byte[] destinationArray = new byte[getContentEstimated()];
        System.arraycopy(encodedSig, 0, destinationArray, 0, encodedSig.length);
        update.put(PdfName.Contents, new PdfString(destinationArray).setHexWriting(true));
        stp.close(update);
    }

    private void addMultiSigFieldAppearance(DefaultPdfSigner stp, Calendar cal, String imgBase64, List<SigFieldAppearanceInfo> positionList) throws Exception{
        if(logger.isDebugEnabled())
            logger.debug("process add multi signature field...");

        List<String> positionParamList = getPositionParamList(stp.getDocument(), imgBase64, positionList);

        byte[] imageBytes = null;
        if(StringUtils.isNotBlank(imgBase64))
            imageBytes = Base64.decode(imgBase64);

        List<SigFieldAppearance> sigFieldList = SigFieldAppearance.getSignatureFieldList(positionParamList);

        ImageData imageData;
        Image image;
        float height;
        float width;
        PdfAcroForm acroForm;
        DefaultPdfSignatureAppearance defaultAppearance;
        int count = 0;
        for (SigFieldAppearance item : sigFieldList) {
            if (0 == count) {
                count++;
                continue;
            }

            if(StringUtils.isNotBlank(item.getImgBase64()))
                imageBytes = Base64.decode(item.getImgBase64());

            imageData = ImageDataFactory.create(imageBytes);

            acroForm = PdfAcroForm.getAcroForm(stp.getDocument(), true);
            defaultAppearance = new DefaultPdfSignatureAppearance(stp.getDocument(), new Rectangle(item.getX(), item.getY(), item.getFitWidth(), item.getFitHeight()), item.getPageNum());
            defaultAppearance.setSignDate(cal);
            defaultAppearance.setSignatureGraphic(imageData);
            defaultAppearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC);

            PdfWidgetAnnotation widget = new PdfWidgetAnnotation(defaultAppearance.getPageRect());
            widget.setFlags(PdfAnnotation.PRINT | PdfAnnotation.LOCKED);
            PdfSignatureFormField sigField = PdfFormField.createSignature(stp.getDocument());
            sigField.put(PdfName.V, stp.getSignatureDictionary().getPdfObject());
            PdfDictionary ap = new PdfDictionary();
            ap.put(PdfName.N, defaultAppearance.getAppearance().getPdfObject());
            sigField.put(PdfName.AP, ap);
            sigField.setFieldName(item.getSignatureName());
            sigField.addKid(widget);
            acroForm.addField(sigField, stp.getDocument().getPage(item.getPageNum()));

        }

        SigFieldAppearance sigField = sigFieldList.get(0);
        if(StringUtils.isNotBlank(sigField.getImgBase64()))
            imageBytes = Base64.decode(sigField.getImgBase64());

        imageData = ImageDataFactory.create(imageBytes);
        image = new Image(imageData);
        height = image.getImageHeight();
        width = image.getImageWidth();
        stp.setFiledName(sigField.getSignatureName());
        stp.getSignatureAppearance().setPageNumber(sigField.getPageNum());
        stp.getSignatureAppearance().setPageRect(new Rectangle(sigField.getX(), sigField.getY(), width, height));
        stp.getSignatureAppearance().setSignatureGraphic(imageData);
        stp.getSignatureAppearance().setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC);

    }

    private List<String> getPositionParamList(PdfDocument pdfDocument, String imgBase64, List<SigFieldAppearanceInfo> positionList) throws Exception {
        List<String> positionParamList = new ArrayList();

        Image image = null;

        if(StringUtils.isNotBlank(imgBase64)) {
            ImageData imageData = ImageDataFactory.create(Base64.decode(imgBase64));
            image = new Image(imageData);
        }

        float imageWidth = null == image ? 0 : image.getImageWidth();
        float imageHeight = null == image ? 0 : image.getImageHeight();

        int pdfPageNumSize = pdfDocument.getNumberOfPages();

        float w;
        float h;

        for(SigFieldAppearanceInfo item : positionList) {
            w = pdfDocument.getPage(item.getPageNum()).getPageSize().getWidth();
            h = pdfDocument.getPage(item.getPageNum()).getPageSize().getHeight();

            positionParamList.add(item.init(item.getSignatureName(), pdfPageNumSize, w, h, imageWidth, imageHeight).calcSignaturePosition().toSignatureFieldString());
        }

        return positionParamList;
    }

    private String getLastSignatureFieldName(SignatureUtil signatureUtil) throws Exception {
        if(logger.isDebugEnabled())
            logger.debug("process get last signature field name...");

        String result = null;
        List<String> arList;

        arList = signatureUtil.getSignatureNames();

        if(null == arList || arList.isEmpty())
            throw new Exception("not have signature in this pdf document");

        for (String name : signatureUtil.getSignatureNames()) {
            if (signatureUtil.getRevision(name) == arList.size())
            {
                result = name;
                break;
            }
        }

        return result;
    }

    private static OutputStream compressPdf(byte[] pdfFileBytes) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        InputStream is = new ByteArrayInputStream(pdfFileBytes);

        PdfDocument destDoc = new PdfDocument(new PdfWriter(os).setSmartMode(true));
        destDoc.initializeOutlines();
        PdfDocument srcDoc = new PdfDocument(new PdfReader(is));

        srcDoc.copyPagesTo(1, srcDoc.getNumberOfPages(), destDoc);
        destDoc.close();

        return os;
    }

    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public byte[] getHashBytes() {
        return hashBytes;
    }

    public void setHashBytes(byte[] hashBytes) {
        this.hashBytes = hashBytes;
    }

    public byte[] getPdfTempBytes() {
        return pdfTempBytes;
    }

    public void setPdfTempBytes(byte[] pdfTempBytes) {
        this.pdfTempBytes = pdfTempBytes;
    }
}
