package com.giraone.thymeleaf.service.convert;

public class PdfCreationOptions {

    public static final String DEFAULT_DOCUMENT_TITLE = "Dokument";
    public static final String DEFAULT_DOCUMENT_AUTHOR = "Thymeleaf Processor";

    private String documentTitle;
    private String documentAuthor;
    private boolean pdfA;
    private WatermarkOptions watermarkOptions;

    public PdfCreationOptions() {
        this.documentTitle = DEFAULT_DOCUMENT_TITLE;
        this.documentAuthor = DEFAULT_DOCUMENT_AUTHOR;
        this.pdfA = false;
        this.watermarkOptions = null;
    }

    public PdfCreationOptions(String documentTitle, String documentAuthor, boolean pdfA) {
        this.documentTitle = documentTitle;
        this.documentAuthor = documentAuthor;
        this.pdfA = pdfA;
        this.watermarkOptions = null;
    }

    public static WatermarkOptions buildWatermark(String text) {
        return new WatermarkOptions(text);
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public String getDocumentAuthor() {
        return documentAuthor;
    }

    public void setDocumentAuthor(String documentAuthor) {
        this.documentAuthor = documentAuthor;
    }

    public boolean isPdfA() {
        return pdfA;
    }

    public void setPdfA(boolean pdfA) {
        this.pdfA = pdfA;
    }

    public WatermarkOptions getWatermarkOptions() {
        return watermarkOptions;
    }

    @SuppressWarnings("UnusedReturnValue")
    public PdfCreationOptions withWatermarkOptions(WatermarkOptions watermarkOptions) {
        this.watermarkOptions = watermarkOptions;
        return this;
    }

    @Override
    public String toString() {
        return "PdfCreationOptions{" +
            "documentTitle='" + documentTitle + '\'' +
            ", authorName='" + documentAuthor + '\'' +
            ", pdfA=" + pdfA +
            '}';
    }

    public static class WatermarkOptions {
        private final String text;
        private float left = 100.0F;
        private float top = 100.0F;
        private float opacity = 0.20f;
        private float angle = 30.0F;
        private float scale = 10.0F;

        public WatermarkOptions(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public float getLeft() {
            return left;
        }

        public void setLeft(float left) {
            this.left = left;
        }

        @SuppressWarnings({"unused", "UnusedReturnValue"})
        public WatermarkOptions withLeft(float left) {
            this.setLeft(left);
            return this;
        }

        public float getTop() {
            return top;
        }

        public void setTop(float top) {
            this.top = top;
        }

        @SuppressWarnings({"unused", "UnusedReturnValue"})
        public WatermarkOptions withTop(float top) {
            this.setTop(top);
            return this;
        }

        public float getOpacity() {
            return opacity;
        }

        public void setOpacity(float opacity) {
            this.opacity = opacity;
        }

        @SuppressWarnings({"unused", "UnusedReturnValue"})
        public WatermarkOptions withOpacity(float opacity) {
            this.setOpacity(opacity);
            return this;
        }

        public float getAngle() {
            return angle;
        }

        public void setAngle(float angle) {
            this.angle = angle;
        }

        @SuppressWarnings({"unused", "UnusedReturnValue"})
        public WatermarkOptions withAngle(float angle) {
            this.setAngle(angle);
            return this;
        }

        public float getScale() {
            return scale;
        }

        public void setScale(float scale) {
            this.scale = scale;
        }

        @SuppressWarnings({"unused", "UnusedReturnValue"})
        public WatermarkOptions withScale(float scale) {
            this.setScale(scale);
            return this;
        }
    }
}
