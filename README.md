# 차량 리듬 동요 (가칭)

주행 리듬과 아이의 목소리로 세상에 하나뿐인 동요를 만드는 Pleos Connect Kids 앱이에요.

> 서비스명은 아직 정해지지 않았어요. 현재는 초기 구성 단계로, 화면은 뼈대만 있고 AI 서버 응답은 목(mock)으로 대체돼요.

## 실행 방법

1. JDK 17과 Android Studio를 준비해요.
2. [Pleos Connect Emulator 설치 문서](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator)대로 AVD를 만들고 실행해요.
3. 앱을 빌드해서 설치해요.

```bash
./gradlew installDebug
```

## 테스트 환경 및 설정

| 항목 | 값 |
|---|---|
| minSdk / targetSdk | 28 / 34 |
| 에뮬레이터 | Pleos Connect Emulator 2.1.0 (API 34, Automotive, Cold boot, RAM 4 GB) |
| 빌드 도구 | JDK 17, Gradle wrapper |

```bash
./gradlew compileDebugKotlin lintDebug testDebugUnitTest assembleDebug
```

## APK 위치

제출용 release APK는 `apk/팀명_앱이름_release.apk`에 넣어요. 용량이 크면 GitHub Releases에 등록하고 이곳에 링크를 적어요.

## Release 서명

1. `keystore.properties.example`을 `keystore.properties`로 복사하고, 팀에서 보관 중인 서명 키 파일 경로와 비밀번호를 적어요.
2. 서명된 APK를 빌드해요.

```bash
./gradlew assembleRelease
```

결과물은 `app/build/outputs/apk/release/app-release.apk`예요. `keystore.properties`가 없으면 서명되지 않은 APK가 만들어져요. 서명 키 파일과 `keystore.properties`는 저장소에 올리지 않아요.
