package com.thanos.common.pdf.signature;

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.PdfSignature;
import com.itextpdf.signatures.PdfSigner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Map;

/**
 * @author Llx
 * @create 2018/8/21
 **/
public class DefaultPdfSigner extends PdfSigner {
    public DefaultPdfSigner(PdfReader reader, OutputStream outputStream, StampingProperties properties) throws IOException {
        super(reader, outputStream, properties);
    }

    @Override
    public void preClose(Map<PdfName, Integer> exclusionSizes) throws IOException {
        super.preClose(exclusionSizes);
    }

    @Override
    public InputStream getRangeStream() throws IOException {
        return super.getRangeStream();
    }

    @Override
    public void close(PdfDictionary update) throws IOException {
        super.close(update);
    }

    public void setCryptoDictionary(PdfSignature cryptoDictionary) {
        super.cryptoDictionary = cryptoDictionary;
    }

    public void setClose() {
        super.closed = true;
    }

    public void closeOutputStream(){
        try {
            super.originalOS.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFiledName(String fieldName){
        super.fieldName = fieldName;
    }

    public RandomAccessFile getRandomAccessFile(){
        return super.raf;
    }
}
