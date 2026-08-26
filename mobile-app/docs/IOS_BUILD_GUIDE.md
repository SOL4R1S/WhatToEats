# WhatToEat 모바일 앱 빌드 가이드 (iOS 중심)

> 작성일: 2026-08-26 · mobile-app/ (Expo SDK 57, React Native 0.86)
> 검증 상태: 안드로이드 로컬 빌드 ✅ (이 서버에서 수행) / iOS ❌ 이 OCI 서버(Linux)에서는 불가 → 아래 가이드대로 진행

---

## 0. 핵심 개념 — iOS 빌드 방식 3가지

| 방식 | 필요 것 | 추천도 |
|---|---|---|
| **① EAS Build (클라우드)** | Expo 계정만 있으면 됨. Apple 개발자 계정은 심사/배포 시점에 필요 | ⭐⭐⭐ **강력 추천** |
| ② 로컬 Mac + Xcode | Mac + Xcode 16+ + Apple 개발자 계정 ($99/년) | Mac 있으면 OK |
| ③ 이 OCI 서버 | ❌ 불가능 — iOS 빌드는 Xcode(macOS) 필수. 물리적으로 Linux에서 못 함 |

**결론: iOS는 EAS Build로 Expo 클라우드에서 빌드하는 게 정답입니다.**
무료 티어로도 월 30회 빌드 가능(큐 대기 있음), Mac 불필요.

---

## 1. 사전 준비물

| 준비물 | 어디서 | 비용 |
|---|---|---|
| Expo 계정 | https://expo.dev/signup | 무료 |
| Apple Developer Program 가입 | https://developer.apple.com | **$99/년** (앱스토어 배포에 필수) |
| EAS CLI | `npm i -g eas-cli` | 무료 |

⚠️ Apple 개발자 심사는 1~2일 걸릴 수 있으니 **먼저 신청해두세요.**

---

## 2. EAS 초기 설정 (5분)

```bash
cd mobile-app

# 1) 로그인
eas login

# 2) 프로젝트 등록 (최초 1회)
eas build:configure
```

그러면 `eas.json`이 생성됩니다. 아래처럼 작성하세요:

```json
{
  "cli": { "version": ">= 12.0.0" },
  "build": {
    "development": {
      "developmentClient": true,
      "distribution": "internal"
    },
    "preview": {
      "distribution": "internal",
      "android": { "buildType": "apk" }
    },
    "production": {}
  },
  "submit": {
    "production": {
      "ios": {
        "appleId": "<당신의AppleID@이메일>",
        "ascAppId": "<App Store Connect 앱 ID>",
        "appleTeamId": "<팀ID 10자리>"
      }
    }
  }
}
```

### 환경변수(백엔드 URL) 주입 — 이미 논의한 그 방식
```bash
eas secret:create --name EXPO_PUBLIC_API_BASE_URL --value https://api.whattoeats.app
```

---

## 3. iOS 빌드 실행

### 3-1. 시뮬레이터/실기기 테스트용 (심사 전 확인)
```bash
eas build --platform ios --profile development
```
- 처음 실행 시 Apple 계정 로그인을 요구 → 화면 지시대로
- 빌드는 클라우드에서 15~25분 소요
- 완료되면 링크로 .tar.gz(시뮬레이터용) 또는 기기용 앱 설치

### 3-2. 앱스토어 제출용
```bash
eas build --platform ios --profile production
```

### 3-3. 안드로이드도 동시에 (선택)
```bash
eas build --platform android --profile production   # .aab (스토어 업로드용)
eas build --platform android --profile preview      # .apk (직접 설치 테스트용)
```

---

## 4. iOS 서명(Signing) — EAS가 자동 처리

EAS 빌드가 최초 실행될 때:
1. "Do you want to automatically create an Apple Team?" → Yes
2. Bundle identifier 확인: `app.json`의 ios.bundleIdentifier (예: `app.whattoeat.mobile`)
3. 인증서·프로비저닝 프로파일을 **자동 생성/관리**

> 수동 관리를 원하면 Apple Developer 콘솔에서 Identifiers → New App ID로
> `app.whattoeat.mobile`을 먼저 만들고 EAS에 알려줄 수도 있지만, 자동이 편합니다.

---

## 5. TestFlight → 앱스토어 심사

```bash
# 1) 빌드된 파일을 TestFlight로 업로드
eas submit --platform ios --latest

# 2) TestFlight에서 내부 테스터로 설치 테스트 (1~2일 처리 후 사용 가능)

# 3) 문제 없으면 App Store Connect(https://appstoreconnect.apple.com)에서
#    심사 제출 → 스크린샷·설명·개인정보 처리방침 URL 입력
```

### 심사 제출 때 꼭 입력할 것들 (미리 준비!)
| 항목 | 값 |
|---|---|
| 스크린샷 | 6.7" 및 5.5" 각 최소 1장 (피드/검색/마이 캡처) |
| 개인정보 처리방침 URL | `https://whattoeats.app/legal/privacy.html` ✅ 이미 있음 |
| 지원 URL | 이메일 또는 웹폼 |
| 연령등급 | 4+ (음란물/도박 없음) |
| 개인정보 수집 고지 | 닉네임·기기 식별자 수집, 국외(일본 오사카) 저장 명시 |

---

## 6. Android (참고 — 이미 이 서버에서 검증됨)

```bash
# APK 직접 설치 테스트
eas build --platform android --profile preview

# Play Store 업로드용 AAB
eas build --platform android --profile production
```
Play Console 등록비 $25 (1회), 심사는 보통 1~7일.
Google Play도 "개인정보 처리방침 URL" 요구 → 같은 URL 사용.

---

## 7. 자주 나오는 문제 & 해결

| 증상 | 해결 |
|---|---|
| `bundle identifier already used` | 다른 식별자로 변경 (예: `app.whattoeat.mobile2`) 또는 기존 앱 재사용 |
| 빌드 큐가 너무 김 | 무료 티어 한계. 유료($29/월)로 우선순위 가능 |
| TestFlight 처리 느림 | Apple 심사 병목 — 보통 몇 시간~1일 |
| 백엔드 연결 실패 (기기에서) | `EXPO_PUBLIC_API_BASE_URL` secret 등록 여부 확인. http://IP 직접 접근은 iOS ATS 차단 → 반드시 https 도메인 사용 |

---

## 8. 체크리스트 요약

- [ ] Expo 계정 생성
- [ ] Apple Developer Program 가입 ($99/년) ← **지금 신청 (승인 1~2일)**
- [ ] `npm i -g eas-cli` → `eas login`
- [ ] `eas build:configure` + eas.json 작성 (위 템플릿)
- [ ] `eas secret:create --name EXPO_PUBLIC_API_BASE_URL --value https://api.whattoeats.app`
- [ ] `eas build --platform ios --profile development` (시뮬레이터 테스트)
- [ ] TestFlight 업로드 → 실기기 테스트
- [ ] App Store Connect에서 심사 제출
