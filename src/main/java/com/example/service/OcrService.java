package com.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
@Service
public class OcrService {

    private final Tesseract tesseract;

    /**
     * 이미지에서 텍스트 추출
     */
    public String extractTextFromImage(MultipartFile imageFile) throws IOException, TesseractException {
        if (imageFile.isEmpty()) {
            throw new IllegalArgumentException("Image file is empty");
        }

        log.info("Starting OCR for file: {} (size: {} bytes)",
                imageFile.getOriginalFilename(), imageFile.getSize());

        try {
            // MultipartFile을 BufferedImage로 변환
            BufferedImage image = ImageIO.read(imageFile.getInputStream());

            if (image == null) {
                throw new IllegalArgumentException("Invalid image format");
            }

            // 이미지 전처리 (필요시)
            BufferedImage processedImage = preprocessImage(image);

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
     * 이미지 전처리 (OCR 정확도 향상을 위해)
     */
    private BufferedImage preprocessImage(BufferedImage originalImage) {
        // 기본적으로 원본 이미지 반환
        // 필요시 이미지 크기 조정, 대비 조정, 노이즈 제거 등 구현
        return originalImage;
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