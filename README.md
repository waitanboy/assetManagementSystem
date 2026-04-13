# IT Asset Management System (IT 자산 관리 시스템)

본 프로젝트는 기업용 IT 자산 관리 및 대여 시스템입니다. 관리자 승인 절차, 디지털 서명, 그리고 보안 장비 대여 시 신분증 OCR 검증 기능을 포함합니다.

## 🛠 Prerequisites (필수 요구 사항)

새 환경에서 실행하기 위해 다음 소프트웨어가 설치되어 있어야 합니다.

- **Java**: JDK 17 이상
- **Node.js**: v20.0.0 이상 (npm 포함)
- **Database**: MySQL 8.0 이상
- **AI Engine**: [Ollama](https://ollama.com/) (OCR 기능을 위해 필요)

## 🏗 Environment Setup (환경 설정)

### 1. Database 설정
MySQL에서 `asset_db`라는 이름의 데이터베이스를 생성합니다.
```sql
CREATE DATABASE asset_db;
```
*백엔드 실행 시 `DatabaseMigrationConfig` 및 `DataInitializer`에 의해 필요한 테이블과 초기 데이터가 자동 생성됩니다.*

### 2. AI 모델 설치 (Ollama)
신분증 OCR 기능을 사용하기 위해 Vision 인식이 가능한 모델을 다운로드해야 합니다.
```bash
ollama pull qwen3-vl:8b
```

### 3. Backend 설정 (`backend/src/main/resources/application.yml`)
다음 항목들이 환경에 맞게 설정되어 있는지 확인하세요:
- `spring.datasource.url`: `jdbc:mysql://localhost:3306/asset_db`
- `spring.datasource.username/password`: DB 접속 계정
- `spring.ai.ollama.chat.model`: `qwen3-vl:8b`
- `spring.mail.username/password`: 이메일 알림을 위한 Gmail 계정 및 앱 비밀번호

## 🚀 How to Run (실행 방법)

### Backend (Spring Boot)
```bash
cd backend
./mvnw spring-boot:run
```
서버는 기본적으로 `http://localhost:8080`에서 구동됩니다.

### Frontend (Vue 3 + Vite)
```bash
cd frontend
npm install
npm run dev
```
프론트엔드는 기본적으로 `http://localhost:5173`에서 구동됩니다.

## 📋 주요 기능
- **자산 관리**: 전체 IT 자산 등록, 수정, 삭제(소프트 삭제).
- **대여/반납**: 사용자 신청 -> 관리자 승인(디지털 서명) -> 반납 확인.
- **보안 장비 OCR**: 보안 카테고리 자산 대여 승인 시 실시간 카메라 촬영을 통한 신분증-사용자 이름 대조.
- **이메일 알림**: 자산 대여 승인 및 사용자 승인 시 자동 이메일 발송.
