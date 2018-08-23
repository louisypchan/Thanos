package com.thanos.test.pdf;

import com.alibaba.fastjson.JSON;
import com.thanos.common.pdf.signature.*;
import com.thanos.common.security.SecurityIDs;
import com.thanos.common.security.RSAUtil;
import com.thanos.common.security.SM2Util;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.util.encoders.Base64;
import org.junit.Test;

import java.io.File;
import java.net.URLDecoder;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

/**
 * @Auther: Llx
 * @Date: 2018/8/21
 * @Description:
 */
public class SignTest {

    private static byte[] pdfFileBytes;
    private static byte[] pfxByte;
    private static byte[] stampBytes;
    private static byte[] rsaCertBytes;
    private static byte[] rsaPrivateKeyBytes;
    private static byte[] sm2CertBytes;
    private static byte[] sm2PrivateKeyBytes;
    private static X509Certificate rsaX509cert;
    private static X509Certificate sm2X509cert;

    private static String basePath = "G:/test/";
    private static String pdfFilePath = basePath + "/files/pdf/测试文档.pdf";
    private static String pfxPath = basePath + "/files/pfx/test_cert.pfx";
    private static String stampPath = basePath + "/files/img/stamp.png";

    static {
        try {
            pdfFileBytes = FileUtils.readFileToByteArray(new File(pdfFilePath));
            pfxByte = FileUtils.readFileToByteArray(new File(pfxPath));
            stampBytes = FileUtils.readFileToByteArray(new File(stampPath));
            rsaCertBytes = CertificateUtil.getCertificateFromPkcs12(pfxByte, "123456").getEncoded();
            rsaPrivateKeyBytes = CertificateUtil.getPrivateKeyFromPkcs12(pfxByte, "123456").getEncoded();

            sm2CertBytes = Base64.decode("MIIBpTCCAUmgAwIBAgIFAINcLJ0wCgYIKoEcz1UBg3UwKTEMMAoGA1UEAwwDTGx4MQwwCgYDVQQKDANMbHgxCzAJBgNVBAYTAkNOMB4XDTE4MDgyMzA1NDUzOFoXDTIwMDgyMzA1NDUzOFowNTEVMBMGA1UEAwwM5rWL6K+V5YWs5Y+4MQ8wDQYDVQQKDAbmtYvor5UxCzAJBgNVBAYTAkNOMFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAE3Wq63XH6tGe3OVH9nnmBDFICGpEj9nRoD4FWj7DrLB+4jJT1AJP3V9fHajFap8AH8O6MaRqpC6YoeSV/86AVbaNSMFAwDgYDVR0PAQH/BAQDAgTwMB0GA1UdDgQWBBRAr90lypqmi1EL+0e34Urw+Zw9qzAfBgNVHSMEGDAWgBRAr90lypqmi1EL+0e34Urw+Zw9qzAMBggqgRzPVQGDdQUAA0gAMEUCIBlXc7ZeKuY+jMDOYh1Y+7xlQUmfmiW05YlLKzg8u9+fAiEA2Vn54/gbhkYpo7EAG5TUNEOMkLJdyS+3gK1amHz2F7Y=");
            sm2PrivateKeyBytes = Base64.decode("MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgt60eQ67D8QpM3uJOuB+B3snXbJLFdSUTHelnoBJgu/qgCgYIKoEcz1UBgi2hRANCAATdarrdcfq0Z7c5Uf2eeYEMUgIakSP2dGgPgVaPsOssH7iMlPUAk/dX18dqMVqnwAfw7oxpGqkLpih5JX/zoBVt");

            rsaX509cert = CertificateUtil.getX509Certificate2(rsaCertBytes);
            sm2X509cert = CertificateUtil.getX509Certificate2(sm2CertBytes);

        } catch (Exception e) {
            e.printStackTrace();;
        }
    }

    @Test
    public void run() {
//        rsaSignWithAppearance();
        rsaSignNoAppearance();
        sm2SignNoAppearance();
    }


    private void rsaSignWithAppearance() {
        try {
            PdfSignHelper pdfSignHelper = new PdfSignHelper(rsaX509cert, 1);

            SigFieldAppearanceInfo sigFieldAppearanceInfo0 = new SigFieldAppearanceInfo();
            sigFieldAppearanceInfo0.setSignatureName("sign1");
            sigFieldAppearanceInfo0.setPageNum(1);
            sigFieldAppearanceInfo0.setX(0.06f);
            sigFieldAppearanceInfo0.setY(0.35f);
            sigFieldAppearanceInfo0.setImgBase64(Base64.toBase64String(stampBytes));

            SigFieldAppearanceInfo sigFieldAppearanceInfo1 = new SigFieldAppearanceInfo();
            sigFieldAppearanceInfo1.setSignatureName("sign2");
            sigFieldAppearanceInfo1.setPageNum(2);
            sigFieldAppearanceInfo1.setX(0.06f);
            sigFieldAppearanceInfo1.setY(0.35f);
            sigFieldAppearanceInfo1.setImgBase64(Base64.toBase64String(stampBytes));

            SignHash signHash = pdfSignHelper.calcSignHashWithSigFieldAppearance(pdfFileBytes, Base64.toBase64String(stampBytes),
                    Arrays.asList(sigFieldAppearanceInfo0, sigFieldAppearanceInfo1));

            byte[] sh = signHash.getSignHashBytes();

            byte[] signBytes = RSAUtil.pkcs1Sign(sh, rsaPrivateKeyBytes, SecurityIDs.SHA256_WITH_RSA);

            byte[] signPdfBytes = pdfSignHelper.embedSignWithPkcs1(signBytes);

            String signedPdfPath = URLDecoder.decode(basePath,"utf-8") + "signedWithAppearance.pdf";
            FileUtils.writeByteArrayToFile(new File(signedPdfPath), signPdfBytes);
            System.out.println(String.format("signed pdf file save to %s", signedPdfPath));

            PdfVerifyHelper pdfVerifyHelper = new PdfVerifyHelper();
            List<VerifyInfo> verifyInfoList = pdfVerifyHelper.verify(signPdfBytes);
            System.out.println(String.format("verify result: %s", JSON.toJSONString(verifyInfoList)));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void rsaSignNoAppearance() {
        try {
            PdfSignHelper pdfSignHelper = new PdfSignHelper(rsaX509cert, 1);

            SignHash signHash = pdfSignHelper.calcSignHash(pdfFileBytes);

            byte[] sh = signHash.getSignHashBytes();

            byte[] signBytes = RSAUtil.pkcs1Sign(sh, rsaPrivateKeyBytes, SecurityIDs.SHA256_WITH_RSA);

            byte[] signPdfBytes = pdfSignHelper.embedSignWithPkcs1(signBytes);

            String signedPdfPath = URLDecoder.decode(basePath,"utf-8") + "signedNoAppearance.pdf";
            FileUtils.writeByteArrayToFile(new File(signedPdfPath), signPdfBytes);
            System.out.println(String.format("signed pdf file save to %s", signedPdfPath));

            PdfVerifyHelper pdfVerifyHelper = new PdfVerifyHelper();
            List<VerifyInfo> verifyInfoList = pdfVerifyHelper.verify(signPdfBytes);
            System.out.println(String.format("verify result: %s", JSON.toJSONString(verifyInfoList)));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sm2SignNoAppearance() {
        try {
            PdfSignHelper pdfSignHelper = new PdfSignHelper(sm2X509cert, 2);

            SignHash signHash = pdfSignHelper.calcSignHash(pdfFileBytes);

            byte[] sh = signHash.getSignHashBytes();

            byte[] signBytes = SM2Util.sign(sh, Base64.toBase64String(sm2PrivateKeyBytes));

            byte[] signPdfBytes = pdfSignHelper.embedSignWithPkcs1(signBytes);

            String signedPdfPath = URLDecoder.decode(basePath,"utf-8") + "signedNoAppearance.pdf";
            FileUtils.writeByteArrayToFile(new File(signedPdfPath), signPdfBytes);
            System.out.println(String.format("signed pdf file save to %s", signedPdfPath));

            PdfVerifyHelper pdfVerifyHelper = new PdfVerifyHelper();
            List<VerifyInfo> verifyInfoList = pdfVerifyHelper.verify(signPdfBytes);
            System.out.println(String.format("verify result: %s", JSON.toJSONString(verifyInfoList)));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
