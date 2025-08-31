# AI Vision Service

Spring Boot 기반의 AI 이미지 처리 서비스로, OCR(광학 문자 인식)과 AI 이미지 분석 기능을 제공합니다.

## 주요 기능

### 1. OCR (광학 문자 인식)
- Tesseract OCR을 사용한 고정밀 텍스트 추출
- 한국어 + 영어 동시 지원
- 다양한 이미지 형식 지원 (JPG, PNG, BMP, TIFF, GIF)
- 드래그 앤 드롭 업로드 지원
- 추출된 텍스트 복사 및 다운로드 기능

### 2. AI 이미지 분석
- Spring AI + Ollama 통합
- LLaVA 7B: 이미지 인식에 특화된 경량 모델
- 전체 이미지 분석 및 질의응답 지원
- 완전한 로컬 AI (외부 API 불필요)

## 기술 스택

- **Backend**: Spring Boot 3.2.0, Java 17
- **AI Framework**: Spring AI 1.0.0-M3
- **OCR Engine**: Tesseract OCR (Tess4J)
- **Local LLM**: Ollama (LLaVA 7B)
- **Template Engine**: Thymeleaf
- **Build Tool**: Gradle
- **Frontend**: Bootstrap 5, HTML5, JavaScript

## 시스템 요구사항

- Java 17 이상
- 메모리: 4GB 이상 권장 (AI 모델 로딩용)
- 디스크: 5GB 이상 (AI 모델 저장용)

## 설치 및 실행

### 1. 사전 준비

#### Tesseract OCR 설치
```bash
# Mac
brew install tesseract tesseract-lang

# Linux (Ubuntu/Debian)
sudo apt-get install tesseract-ocr tesseract-ocr-kor

# Windows
# https://github.com/UB-Mannheim/tesseract/wiki 에서 다운로드
```

#### Ollama 설치 및 모델 다운로드
```bash
# Mac
brew install ollama

# Linux
curl -fsSL https://ollama.ai/install.sh | sh

# Ollama 서비스 시작
ollama serve

# LLaVA 모델 다운로드 (약 4GB)
ollama pull llava:7b
```

### 2. 프로젝트 실행

```bash
# 프로젝트 클론
git clone <repository-url>
cd ai-vision-service

# 의존성 설치 및 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

### 3. 웹 브라우저 접속
- 메인 페이지: http://localhost:8080
- OCR 페이지: http://localhost:8080/ocr
- AI 이미지 분석: http://localhost:8080/vision

## 설정

### application.yml
```yaml
# OCR 설정
ocr:
  tesseract:
    data-path: /opt/homebrew/share/tessdata  # Mac Homebrew 경로
    language: eng+kor  # 영어 + 한국어

# AI 모델 설정
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: llava:7b
          temperature: 0.7
```

## 사용법

### OCR 기능
1. `/ocr` 페이지 접속
2. 이미지 파일을 드래그하거나 선택
3. "텍스트 추출하기" 버튼 클릭
4. 추출된 텍스트 확인 및 복사/다운로드

### AI 이미지 분석
1. `/vision` 페이지 접속
2. 이미지 파일을 업로드
3. (선택사항) 특정 질문 입력
4. "이미지 분석하기" 버튼 클릭
5. AI 분석 결과 확인

## 프로젝트 구조

```
src/
├── main/
│   ├── java/
│   │   └── com/example/
│   │       ├── AiVisionServiceApplication.java
│   │       ├── config/
│   │       │   └── AiVisionConfig.java
│   │       ├── controller/
│   │       │   └── MainController.java
│   │       └── service/
│   │           ├── OcrService.java
│   │           └── VisionService.java
│   └── resources/
│       ├── application.yml
│       └── templates/
│           ├── index.html
│           ├── ocr.html
│           └── vision.html
└── build.gradle
```

## 트러블슈팅

### Tesseract 관련 오류
```bash
# Mac에서 네이티브 라이브러리 로딩 오류 시
export DYLD_LIBRARY_PATH=/opt/homebrew/lib:$DYLD_LIBRARY_PATH
export JNA_LIBRARY_PATH=/opt/homebrew/lib
export TESSDATA_PREFIX=/opt/homebrew/share/tessdata
./gradlew bootRun
```

### Ollama 연결 오류
```bash
# Ollama 서비스 상태 확인
ollama list
ollama serve

# 모델 다운로드 확인
ollama pull llava:7b
```

### 메모리 부족
```bash
# JVM 힙 메모리 증가
./gradlew bootRun -Xmx4g
```

## 성능 최적화

### 추천 모델 설정
- **개발환경**: `llava:7b` (4GB 메모리, 빠른 속도)
- **프로덕션**: `llava:13b` (8GB 메모리, 높은 정확도)
- **경량화**: `moondream` (1.7GB 메모리, 기본 분석)

### 이미지 최적화
- 권장 해상도: 2000px 이하
- 지원 형식: JPG, PNG (최적화됨)
- 파일 크기: 10MB 이하

## API 엔드포인트

| 메소드 | 엔드포인트 | 설명 |
|--------|------------|------|
| GET | `/` | 메인 페이지 |
| GET | `/ocr` | OCR 페이지 |
| POST | `/ocr/process` | OCR 처리 |
| GET | `/vision` | AI 분석 페이지 |
| POST | `/vision/analyze` | AI 이미지 분석 |

## 확장 계획

### 예정 기능
- [ ] 배치 이미지 처리
- [ ] REST API 제공
- [ ] 이미지 전처리 옵션
- [ ] 다국어 지원 확대
- [ ] 분석 결과 히스토리

### 기술 개선
- [ ] Redis 캐싱
- [ ] 비동기 처리
- [ ] Docker 컨테이너화
- [ ] 클라우드 배포

## 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

## 기여

1. 이 저장소를 포크합니다
2. 기능 브랜치를 생성합니다 (`git checkout -b feature/AmazingFeature`)
3. 변경사항을 커밋합니다 (`git commit -m 'Add some AmazingFeature'`)
4. 브랜치에 푸시합니다 (`git push origin feature/AmazingFeature`)
5. Pull Request를 열어주세요

## 문의

프로젝트에 대한 질문이나 제안사항이 있으시면 이슈를 생성해 주세요.

---

**Built with Spring Boot + Spring AI + Local LLM**