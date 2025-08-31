package com.example.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * OCR 처리 옵션 설정 클래스
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrOptions {
    private Integer pageSegMode;           // 페이지 세그멘테이션 모드
    private Integer ocrEngineMode;         // OCR 엔진 모드
    private Integer dpi;                   // DPI 설정

    @Builder.Default
    private boolean convertToGrayscale = false;     // 그레이스케일 변환

    @Builder.Default
    private boolean enhanceContrast = false;       // 대비 향상

    @Builder.Default
    private boolean removeNoise = false;           // 노이즈 제거

    @Builder.Default
    private boolean sharpen = false;              // 선명화

    @Builder.Default
    private float contrastFactor = 1.2f;          // 대비 강도

    @Builder.Default
    private boolean preserveInterwordSpaces = false; // 단어간 공백 유지

    @Builder.Default
    private String charWhitelist = "";            // 허용 문자 목록

    /**
     * 기본 옵션
     */
    public static OcrOptions defaultOptions() {
        return OcrOptions.builder().build();
    }

    /**
     * 문서 스캔 최적화 (A4 문서, PDF 스캔 등)
     */
    public static OcrOptions documentMode() {
        return OcrOptions.builder()
                .pageSegMode(1)          // 자동 페이지 세그멘테이션 (OSD 포함)
                .enhanceContrast(true)
                .contrastFactor(1.3f)
                .removeNoise(true)
                .build();
    }

    /**
     * 단일 텍스트 블록 (간판, 표지판 등)
     */
    public static OcrOptions singleTextBlockMode() {
        return OcrOptions.builder()
                .pageSegMode(6)          // 단일 텍스트 블록
                .sharpen(true)
                .contrastFactor(1.4f)
                .build();
    }

    /**
     * 한 줄 텍스트 (제목, 라벨 등)
     */
    public static OcrOptions singleLineMode() {
        return OcrOptions.builder()
                .pageSegMode(7)          // 한 줄 텍스트
                .sharpen(true)
                .removeNoise(false)
                .build();
    }

    /**
     * 단일 단어 (번호판, 단어 하나 등)
     */
    public static OcrOptions singleWordMode() {
        return OcrOptions.builder()
                .pageSegMode(8)          // 단일 단어
                .sharpen(true)
                .removeNoise(false)
                .contrastFactor(1.5f)
                .build();
    }

    /**
     * 고정밀 모드 (중요한 문서, 시간이 오래 걸려도 정확도 우선)
     */
    public static OcrOptions highAccuracyMode() {
        return OcrOptions.builder()
                .ocrEngineMode(1)        // LSTM + Legacy
                .pageSegMode(1)
                .dpi(300)
                .contrastFactor(1.4f)
                .sharpen(true)
                .removeNoise(true)
                .build();
    }

    /**
     * 빠른 처리 모드 (속도 우선, 정확도 타협)
     */
    public static OcrOptions fastMode() {
        return OcrOptions.builder()
                .ocrEngineMode(0)        // Legacy 엔진 (빠름)
                .pageSegMode(6)
                .convertToGrayscale(true)
                .enhanceContrast(false)
                .removeNoise(false)
                .sharpen(false)
                .build();
    }

    /**
     * 숫자만 추출 (영수증, 금액 등)
     */
    public static OcrOptions numbersOnlyMode() {
        return OcrOptions.builder()
                .pageSegMode(6)
                .charWhitelist("0123456789.,")
                .contrastFactor(1.5f)
                .sharpen(true)
                .build();
    }

    /**
     * 영문자만 추출
     */
    public static OcrOptions englishOnlyMode() {
        return OcrOptions.builder()
                .pageSegMode(6)
                .charWhitelist("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz ")
                .build();
    }

    @Override
    public String toString() {
        return String.format("OcrOptions{pageSegMode=%d, ocrEngineMode=%d, dpi=%d, contrast=%.1f}",
                pageSegMode, ocrEngineMode, dpi, contrastFactor);
    }
}