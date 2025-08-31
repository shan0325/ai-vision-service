package com.example.service;

import com.example.dto.OcrOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
@Service
public class OcrService {

    private final Tesseract tesseract;

    @Value("${ocr.tesseract.preprocessing.enabled:true}")
    private boolean preprocessingEnabled;

    @Value("${ocr.tesseract.preprocessing.resize-threshold:2000}")
    private int resizeThreshold;

    /**
     * 기본 옵션으로 이미지에서 텍스트 추출
     */
    public String extractTextFromImage(MultipartFile imageFile) throws IOException, TesseractException {
        return extractTextFromImage(imageFile, OcrOptions.defaultOptions());
    }

    /**
     * 사용자 정의 옵션으로 이미지에서 텍스트 추출
     */
    public String extractTextFromImage(MultipartFile imageFile, OcrOptions options) throws IOException, TesseractException {
        if (imageFile.isEmpty()) {
            throw new IllegalArgumentException("Image file is empty");
        }

        log.info("Starting OCR for file: {} (size: {} bytes)",
                imageFile.getOriginalFilename(), imageFile.getSize());

        try {
            BufferedImage image = ImageIO.read(imageFile.getInputStream());

            if (image == null) {
                throw new IllegalArgumentException("Invalid image format");
            }

            log.info("Original image size: {}x{}", image.getWidth(), image.getHeight());

            // 이미지 전처리
            BufferedImage processedImage = preprocessingEnabled ?
                    preprocessImage(image, options) : image;

            // 임시 Tesseract 설정 적용
            applyTemporaryOptions(options);

            // OCR 수행
            String extractedText = tesseract.doOCR(processedImage);

            log.info("OCR completed successfully. Extracted {} characters",
                    extractedText != null ? extractedText.length() : 0);

            return extractedText != null ? extractedText.trim() : "";

        } catch (TesseractException e) {
            log.error("OCR failed for file: {}", imageFile.getOriginalFilename(), e);
            throw new TesseractException("OCR processing failed: " + e.getMessage());
        }
    }

    /**
     * 문서 타입 최적화 OCR (스캔 문서용)
     */
    public String extractTextFromDocument(MultipartFile imageFile) throws IOException, TesseractException {
        return extractTextFromImage(imageFile, OcrOptions.documentMode());
    }

    /**
     * 단일 텍스트 블록 OCR (간판, 표지판 등)
     */
    public String extractTextFromSingleBlock(MultipartFile imageFile) throws IOException, TesseractException {
        return extractTextFromImage(imageFile, OcrOptions.singleTextBlockMode());
    }

    /**
     * 고정밀 OCR (중요 문서용)
     */
    public String extractTextWithHighAccuracy(MultipartFile imageFile) throws IOException, TesseractException {
        return extractTextFromImage(imageFile, OcrOptions.highAccuracyMode());
    }

    /**
     * 숫자만 추출 (영수증, 금액 등)
     */
    public String extractNumbersOnly(MultipartFile imageFile) throws IOException, TesseractException {
        return extractTextFromImage(imageFile, OcrOptions.numbersOnlyMode());
    }

    /**
     * 이미지 전처리
     */
    private BufferedImage preprocessImage(BufferedImage originalImage, OcrOptions options) {
        BufferedImage processedImage = deepCopy(originalImage);

        log.debug("Starting image preprocessing...");

        // 크기 조정
        if (processedImage.getWidth() > resizeThreshold || processedImage.getHeight() > resizeThreshold) {
            processedImage = resizeImage(processedImage, resizeThreshold);
            log.debug("Image resized to fit within {}px threshold", resizeThreshold);
        }

        // 그레이스케일 변환
        if (options.isConvertToGrayscale()) {
            processedImage = convertToGrayscale(processedImage);
            log.debug("Converted to grayscale");
        }

        // 대비 향상
        if (options.isEnhanceContrast()) {
            processedImage = enhanceContrast(processedImage, options.getContrastFactor());
            log.debug("Enhanced contrast with factor: {}", options.getContrastFactor());
        }

        // 노이즈 제거
        if (options.isRemoveNoise()) {
            processedImage = removeNoise(processedImage);
            log.debug("Applied noise reduction");
        }

        // 선명화
        if (options.isSharpen()) {
            processedImage = sharpenImage(processedImage);
            log.debug("Applied image sharpening");
        }

        return processedImage;
    }

    /**
     * 임시 Tesseract 옵션 적용
     */
    private void applyTemporaryOptions(OcrOptions options) {
        try {
            if (options.getPageSegMode() != null) {
                tesseract.setPageSegMode(options.getPageSegMode());
            }

            if (options.getOcrEngineMode() != null) {
                tesseract.setOcrEngineMode(options.getOcrEngineMode());
            }

            if (options.getDpi() != null) {
                tesseract.setTessVariable("user_defined_dpi", String.valueOf(options.getDpi()));
            }

            if (options.isPreserveInterwordSpaces()) {
                tesseract.setTessVariable("preserve_interword_spaces", "1");
            }

            if (options.getCharWhitelist() != null && !options.getCharWhitelist().isEmpty()) {
                tesseract.setTessVariable("tessedit_char_whitelist", options.getCharWhitelist());
            }

        } catch (Exception e) {
            log.warn("Failed to apply some OCR options: {}", e.getMessage());
        }
    }

    // 이미지 처리 유틸리티 메소드들
    private BufferedImage deepCopy(BufferedImage original) {
        BufferedImage copy = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
        Graphics2D g = copy.createGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();
        return copy;
    }

    private BufferedImage resizeImage(BufferedImage original, int maxSize) {
        int width = original.getWidth();
        int height = original.getHeight();

        double scale = Math.min((double) maxSize / width, (double) maxSize / height);
        if (scale >= 1.0) return original;

        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);

        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return resized;
    }

    private BufferedImage convertToGrayscale(BufferedImage original) {
        BufferedImage grayscale = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = grayscale.createGraphics();
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();
        return grayscale;
    }

    private BufferedImage enhanceContrast(BufferedImage original, float factor) {
        RescaleOp rescaleOp = new RescaleOp(factor, 0, null);
        return rescaleOp.filter(original, null);
    }

    private BufferedImage removeNoise(BufferedImage original) {
        float[] blurKernel = {
                1f/9f, 1f/9f, 1f/9f,
                1f/9f, 1f/9f, 1f/9f,
                1f/9f, 1f/9f, 1f/9f
        };
        Kernel kernel = new Kernel(3, 3, blurKernel);
        ConvolveOp convolveOp = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return convolveOp.filter(original, null);
    }

    private BufferedImage sharpenImage(BufferedImage original) {
        float[] sharpenKernel = {
                0.0f, -1.0f, 0.0f,
                -1.0f, 5.0f, -1.0f,
                0.0f, -1.0f, 0.0f
        };
        Kernel kernel = new Kernel(3, 3, sharpenKernel);
        ConvolveOp convolveOp = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return convolveOp.filter(original, null);
    }

    /**
     * 지원되는 이미지 형식 확인
     */
    public boolean isSupportedImageFormat(String contentType) {
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/bmp") ||
                        contentType.equals("image/tiff") ||
                        contentType.equals("image/gif")
        );
    }
}