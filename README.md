# Pinco
> 나만의 스팟을 pick! 하는 **Pinco**

## 💡서비스 소개

**Pinco**는 내가 기록하고 싶은 위치를 등록하고,

등록한 위치에 메모를 남기고,

다른 사람과 공유할 수 있습니다.

---

## **🎯 서비스 목표**

1. **위치 등록 및 공유**
    - 장소에만 국한되지않고 위치 좌표를 통해 **어디든지** 나만의 위치 등록 가능
    - 등록한 위치에 메모를 남기고, 다른 사람들과 공유 가능
      
2. **기록 관리**
    - 마이페이지에서 내가 등록한 장소, 북마크한 장소 확인 가능
    - 내가 받은 좋아요 수 확인 가능
---

## 🧑‍💻 개발 기간 & 팀원

### **개발 기간**
> 2025.10.10 (금) 09:00 ~ 2025.10.27 (월) 18:00

### **팀원**
| <a href="https://github.com/ys0221"><img src="https://github.com/ys0221.png" width="100"/></a> | <a href="https://github.com/kheeyoung"><img src="https://github.com/kheeyoung.png" width="100"/></a> | <a href="https://github.com/77r77r"><img src="https://github.com/77r77r.png" width="100"/></a> | <a href="https://github.com/lh922"><img src="https://github.com/lh922.png" width="100"/></a> |
| :---: | :---: | :---: | :---: |
| **정윤서** | **김희영** | **노미경** | **이현** 
| 팀장 | 팀원 | 팀원 | 팀원 |


---

## 🧩 핵심 기능

**1. pin(위치 좌표) 등록**

**2. 위치 별 메모, 태그 등록**

**3. 현재 위치 기반 지도 탐색**

**4. pin 에 좋아요, 북마크 등록**

**5. 일자별 등록 위치 트래킹 서비스**

**6. 마이페이지**

---

## ⚙️ 환경 변수 설정

**BACKEND (IntelliJ 환경 변수 설정)**

```java
# Database
URL: jdbc:postgresql://localhost:5432/pinco
USERNAME: user
PASSWORD: password
DRIVER-CLASS-NAME: org.postgresql.Driver

# User
EMAIL=user1@example.com
PASSWORD=12345678

# JWT
SECRET_PATTERN=your_secret_pattern
```

**FRONTEND (.env)**

```
NEXT_PUBLIC_API_BASE_URL ="http://localhost:8080"

# Payment
NEXT_PUBLIC_TOSS_CLIENT_KEY = your_payment_secret_key
```

---

## 🧾 API 명세서 (Swagger)

 http://localhost:8080/swagger-ui/index.html

---

## 🔗 ERD

 <img width="2161" height="941" alt="Image" src="https://github.com/user-attachments/assets/e787ad51-fe7e-4239-ac00-0b1ac38351bd" />
 
---

## ☁️ 시스템 아키텍처
<img width="690" height="575" alt="Image" src="https://github.com/user-attachments/assets/713206f2-fa0f-41ed-ab22-9df0228fa620" />

---

## 🧱 기술 스택
**Frontend**

![Next.js](https://img.shields.io/badge/Next.js-000000?style=for-the-badge&logo=nextdotjs&logoColor=white)
![React](https://img.shields.io/badge/React-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white)
![TailwindCSS](https://img.shields.io/badge/TailwindCSS-38B2AC?style=for-the-badge&logo=tailwindcss&logoColor=white)


**Backend**

![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-007396?style=for-the-badge&logo=hibernate&logoColor=white)
![QueryDSL](https://img.shields.io/badge/QueryDSL-4479A1?style=for-the-badge&logo=databricks&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![H2 Database](https://img.shields.io/badge/H2%20Database-003B57?style=for-the-badge&logo=h2&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![OpenAI API](https://img.shields.io/badge/OpenAI%20API-412991?style=for-the-badge&logo=openai&logoColor=white)
![Toss Payments](https://img.shields.io/badge/Toss%20Payments-0064FF?style=for-the-badge&logo=toss&logoColor=white)
![Gmail SMTP](https://img.shields.io/badge/Gmail%20SMTP-EA4335?style=for-the-badge&logo=gmail&logoColor=white)

---

## 🤖 Github Actions CI 자동화 Test

### Workflow 개요

- **트리거 조건**
    - 브랜치: `main`, `feature/*`, `fix/*`, `refactor/*`
    - 경로: `backend/**`
- **실행 환경**
    - JDK 21
- **CI 단계**
    1. 저장소 체크아웃
    2. Java 환경 세팅
    3. Gradle 실행 권한 부여
    4. 테스트 프로파일(`spring.profiles.active=test`)로 빌드 및 테스트 실행
    5. 테스트 결과를 PR 코멘트로 출력
    6. 실패한 코드 라인에 체크 코멘트 등록

---

## 💬 개발 컨벤션

### 🚀 GitHub Flow

- **main**
    - 실제 서비스에 배포되는 안정화 브랜치
    - 직접 커밋 금지 (feature, fix, refactor 브랜치를 통해 반영)
    - 브랜치 보호 규칙 적용 : PR을 통해 최소 1명의 리뷰 승인 후 머지 가능
- **feature/ & fix/ & refactor/**
    - 개별 기능 개발, 버그 수정, 코드 리팩토링용 브랜치
    - 이슈 단위로 생성하여 작업
    - 작업 완료 후 PR을 통해 main에 머지
    

---

### **🔄 작업 순서**

1. **이슈 생성** → 작업 단위 정의
2. **브랜치 생성** → main 브랜치에서 이슈별 작업 브랜치 생성
3. **Commit & Push**
4. **PR 생성 & 코드 리뷰** → 최소 1명 승인 필요
5. **Merge & 브랜치 정리**
    - 리뷰 완료 후 main 브랜치로 Merge
    - Merge 후 이슈별 작업 브랜치 삭제

---

### ⚙️ 네이밍 & 작성 규칙

1. **이슈**
    - 제목 규칙 : `[타입] 작업내용`
    - 예시 : `[feat] 로그인 기능 추가`
    - 본문은 템플릿에 맞춰서 작성
2. **PR**
    - 제목 규칙 : `[타입] 작업내용`
    - 예시 : `[feat] 로그인 기능 추가`
    - 본문은 템플릿에 맞춰서 작성 + close #이슈넘버
3. **브랜치**
    - 생성 기준 : `main` 브랜치에서 생성
    - 명명 규칙  : `타입/#이슈번호`
    - 예시: `feature/#1`
      
4. **Commit Message 규칙**
    
    
    | 타입 | 의미 |
    | --- | --- |
    | **feat** | 새로운 기능 추가 |
    | **fix** | 버그 수정 |
    | **docs** | 문서 수정 (README, 주석 등) |
    | **style** | 코드 스타일 변경 (포맷팅, 세미콜론 등. 기능 변화 없음) |
    | **refactor** | 코드 리팩토링 (동작 변화 없음) |
    | **test** | 테스트 코드 추가/수정 |
    | **chore** | 빌드, 패키지 매니저, 설정 파일 등 유지보수 작업(환경 설정) |
    | **remove** | 파일, 폴더 삭제 |
    | **rename** | 파일, 폴더명 수정 |
    - `타입 : 작업내용 #이슈번호`
    - 예시: `feat : 로그인 기능 추가#1`
