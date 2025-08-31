package com.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Media;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisionService {

    private final OllamaChatModel chatModel;

    /**
     * 이미지 분석 및 설명 생성
     */
    public String analyzeImage(MultipartFile imageFile) throws IOException {
        if (imageFile.isEmpty()) {
            throw new IllegalArgumentException("Image file is empty");
        }

        log.info("Starting image analysis for file: {} (size: {} bytes)",
                imageFile.getOriginalFilename(), imageFile.getSize());

        try {
            // 이미지를 Media 객체로 변환
            Media imageMedia = new Media(
                    MimeTypeUtils.parseMimeType(imageFile.getContentType()),
                    new ByteArrayResource(imageFile.getBytes())
            );

            // 프롬프트 생성
            String promptText = createAnalysisPrompt();

            UserMessage userMessage = new UserMessage(promptText, List.of(imageMedia));
            Prompt prompt = new Prompt(List.of(userMessage));

            // AI 모델을 통한 이미지 분석
            ChatResponse response = chatModel.call(prompt);

            String analysis = response.getResult().getOutput().getContent();

            log.info("Image analysis completed successfully. Response length: {} characters",
                    analysis.length());

            return analysis;

        } catch (Exception e) {
            log.error("Image analysis failed for file: {}", imageFile.getOriginalFilename(), e);
            throw new RuntimeException("Image analysis failed: " + e.getMessage(), e);
        }
    }

    /**
     * 이미지 분석을 위한 프롬프트 생성
     */
    private String createAnalysisPrompt() {
        return """
                이 이미지를 자세히 분석해주세요. 다음 내용을 포함해서 설명해주세요:
                
                1. 전체적인 장면 설명
                2. 주요 객체나 사물들
                3. 색상과 분위기
                4. 사람이 있다면 활동 내용
                5. 배경 환경
                6. 특이한 점이나 인상적인 요소
                
                한국어로 상세하고 친근하게 설명해주세요.
                """;
    }

    /**
     * 특정 질문에 대한 이미지 분석
     */
    public String analyzeImageWithQuestion(MultipartFile imageFile, String question) throws IOException {
        if (imageFile.isEmpty()) {
            throw new IllegalArgumentException("Image file is empty");
        }

        log.info("Starting targeted image analysis for file: {} with question: {}",
                imageFile.getOriginalFilename(), question);

        try {
            Media imageMedia = new Media(
                    MimeTypeUtils.parseMimeType(imageFile.getContentType()),
                    new ByteArrayResource(imageFile.getBytes())
            );

            String promptText = String.format(
                    "이 이미지를 보고 다음 질문에 답해주세요: %s\n\n상세하고 정확하게 한국어로 답변해주세요.",
                    question
            );

            UserMessage userMessage = new UserMessage(promptText, List.of(imageMedia));
            Prompt prompt = new Prompt(List.of(userMessage));

            ChatResponse response = chatModel.call(prompt);
            String analysis = response.getResult().getOutput().getContent();

            log.info("Targeted image analysis completed successfully");

            return analysis;

        } catch (Exception e) {
            log.error("Targeted image analysis failed", e);
            throw new RuntimeException("Image analysis failed: " + e.getMessage(), e);
        }
    }
}