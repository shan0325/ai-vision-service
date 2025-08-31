package com.example.controller;

import com.example.service.OcrService;
import com.example.service.VisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MainController {

    private final OcrService ocrService;
    private final VisionService visionService;

    /**
     * 메인 페이지
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * OCR 페이지
     */
    @GetMapping("/ocr")
    public String ocrPage() {
        return "ocr";
    }

    /**
     * OCR 처리
     */
    @PostMapping("/ocr/process")
    public String processOcr(@RequestParam("image") MultipartFile imageFile,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        if (imageFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "이미지 파일을 선택해주세요.");
            return "redirect:/ocr";
        }

        if (!ocrService.isSupportedImageFormat(imageFile.getContentType())) {
            redirectAttributes.addFlashAttribute("error", "지원하지 않는 이미지 형식입니다. (JPG, PNG, BMP, TIFF, GIF만 지원)");
            return "redirect:/ocr";
        }

        try {
            String extractedText = ocrService.extractTextFromImage(imageFile);

            model.addAttribute("extractedText", extractedText);
            model.addAttribute("fileName", imageFile.getOriginalFilename());
            model.addAttribute("fileSize", String.format("%.2f KB", imageFile.getSize() / 1024.0));

            return "ocr";

        } catch (Exception e) {
            log.error("OCR processing failed", e);
            redirectAttributes.addFlashAttribute("error", "텍스트 추출 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/ocr";
        }
    }

    /**
     * 이미지 분석 페이지
     */
    @GetMapping("/vision")
    public String visionPage() {
        return "vision";
    }

    /**
     * 이미지 분석 처리
     */
    @PostMapping("/vision/analyze")
    public String analyzeImage(@RequestParam("image") MultipartFile imageFile,
                               @RequestParam(value = "question", required = false) String question,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        if (imageFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "이미지 파일을 선택해주세요.");
            return "redirect:/vision";
        }

        try {
            String analysis;

            if (question != null && !question.trim().isEmpty()) {
                analysis = visionService.analyzeImageWithQuestion(imageFile, question.trim());
                model.addAttribute("question", question.trim());
            } else {
                analysis = visionService.analyzeImage(imageFile);
            }

            model.addAttribute("analysis", analysis);
            model.addAttribute("fileName", imageFile.getOriginalFilename());
            model.addAttribute("fileSize", String.format("%.2f KB", imageFile.getSize() / 1024.0));

            return "vision";

        } catch (Exception e) {
            log.error("Image analysis failed", e);
            redirectAttributes.addFlashAttribute("error", "이미지 분석 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/vision";
        }
    }
}