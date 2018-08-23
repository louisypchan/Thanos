package com.thanos.common.pdf.signature;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.signatures.PdfSignatureAppearance;

import java.io.IOException;
import java.util.Calendar;

/**
 * @author Llx
 * @create 2018/8/21
 **/
public class DefaultPdfSignatureAppearance extends PdfSignatureAppearance {
    protected DefaultPdfSignatureAppearance(PdfDocument document, Rectangle pageRect, int pageNumber) {
        super(document, pageRect, pageNumber);
    }

    @Override
    public PdfFormXObject getAppearance() throws IOException {
        return super.getAppearance();
    }

    @Override
    public PdfSignatureAppearance setSignDate(Calendar signDate) {
        return super.setSignDate(signDate);
    }
}
