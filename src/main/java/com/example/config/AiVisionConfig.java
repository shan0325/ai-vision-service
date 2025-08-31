package com.example.config;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Configuration
public class AiVisionConfig {

    @Value("${ocr.tesseract.data-path}")
    private String tessDataPath;

    @Value("${ocr.tesseract.language}")
    private String tessLanguage;

    @Value("${file.upload.directory}")
    private String uploadDirectory;

    @Bean
    public Tesseract tesseract() {
        Tesseract tesseract = new Tesseract();

        // Tesseract 데이터 경로 설정
        File tessDataDir = new File(tessDataPath);
        if (tessDataDir.exists() && tessDataDir.isDirectory()) {
            tesseract.setDatapath(tessDataPath);
            log.info("Tesseract data path set to: {}", tessDataPath);
        } else {
            log.warn("Tesseract data path not found: {}. Using system default.", tessDataPath);
        }

        // 언어 설정
        tesseract.setLanguage(tessLanguage);
        log.info("Tesseract language set to: {}", tessLanguage);

        // OCR 엔진 모드 설정 (LSTM + Legacy)
        tesseract.setOcrEngineMode(1);

        return tesseract;
    }

    @Bean
    public String uploadDirectory() {
        try {
            Path uploadPath = Paths.get(uploadDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadDirectory);
            }
            return uploadDirectory;
        } catch (Exception e) {
            log.error("Failed to create upload directory: {}", uploadDirectory, e);
            return System.getProperty("java.io.tmpdir");
        }
    }
}