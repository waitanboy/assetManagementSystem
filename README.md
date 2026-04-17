# IT Asset Management System (IT 자산 관리 시스템)

본 프로젝트는 기업용 IT 자산 관리 및 대여 시스템으로, 자산의 생애주기 관리부터 실시간 소통, 시스템 모니터링까지 통합된 관리 환경을 제공합니다.

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
*백엔드 실행 시 테이블 및 초기 데이터가 자동으로 초기화됩니다.*

### 2. AI 모델 설치 (Ollama)
신분증 OCR 기능을 사용하기 위해 Vision 인식이 가능한 모델을 다운로드해야 합니다.
```bash
ollama pull qwen3-vl:8b
```

### 3. Backend 보안 및 설정 (`application.yml`)
보안을 위해 초기 관리자 정보를 환경 변수로 관리합니다. 실행 시 다음과 같이 설정할 수 있습니다:
- `ADMIN_EMAIL`: 초기 관리자 이메일 (기본값: admin@asset.com)
- `ADMIN_PASSWORD`: 초기 관리자 비밀번호 (기본값: admin123)
- `DB_URL`: JDBC 연결 URL
- `DB_USERNAME/PASSWORD`: DB 접속 계정

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

### 🖥 Dashboard & Assets
- **통합 대시보드**: 실시간 자산 통계 및 최근 활동 요약.
- **자산 라이프사이클**: 등록, 수정, 수리(Maintenance), 폐기 프로세스 관리.
- **디지털 서명**: 자산 대여/반납 시 수령인의 디지털 서명 기록.

### 👤 User Management (Redesigned)
- **멤버 관리**: 리스트 형태의 사용자 목록 및 상세 프로필 뷰.
- **활동 로그**: 특정 사용자의 전체 대여 이력 및 활동 타임라인 제공.
- **프로필 수정**: 관리자가 직접 사용자 역할, 부서, 상태 수정 가능.

### 💬 Communication & Support
- **실시간 기술 지원**: WebSocket 기반의 관리자-사용자 1:1 채팅 상담 시스템.
- **커뮤니티 게시판**: Quill 엔진 기반의 리치 텍스트 게시판 및 댓글 기능.
- **공지사항**: 시스템 공지 및 알림 관리.

### 🛡 Security & Monitoring
- **보안 장비 OCR**: AI 모델(Ollama)을 통한 신분증-사용자 일치 여부 실시간 검증.
- **System Console**: 서버 로그(`logs/app.log`)를 웹에서 실시간으로 모니터링(Jenkins 스타일).
- **JWT 보안**: 모든 API 호출에 대한 토큰 기반 인증 및 권한(RBAC) 관리.

## 🎨 Tech Stack
- **Frontend**: Vue 3 (Composition API), Vite, Tailwind CSS 4, Pinia, Lucide Icons, Quill Editor.
- **Backend**: Spring Boot 3, MyBatis, MySQL, Spring Security (JWT), WebSocket (STOMP).
