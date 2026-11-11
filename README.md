# 차량 리듬 동요 (가칭)

주행 리듬과 아이의 목소리로 세상에 하나뿐인 동요를 만드는 Pleos Connect Kids 앱이에요.

> 서비스명은 아직 정해지지 않았어요. 화면 흐름(S00~S07)과 노래 생성 서버 연동(녹음 세션, 목소리 등록, 완성곡 저장)이 들어가 있어요.

## 실행 방법

1. JDK 17과 Android Studio를 준비해요.
2. [Pleos Connect Emulator 설치 문서](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator)대로 AVD를 만들고 실행해요.
3. 노래 생성 서버를 같은 PC에서 켜요 (아래 "노래 생성 서버").
4. 앱을 빌드해서 설치해요.

```bash
./gradlew installDebug
```

## 노래 생성 서버

앱은 노래 생성 서버와 [docs/API.md](docs/API.md) 형식으로 주고받아요.

| 항목 | 값 |
|---|---|
| 기본 주소 | `http://10.0.2.2:8000` (에뮬레이터 안에서 본 같은 PC) |
| 주소 바꾸기 | `local.properties`에 `kidsrhythm.serverUrl=http://192.168.0.10:8000`처럼 적어요. 실제 기기에서 PC 서버에 붙을 때 써요 (http는 디버그 빌드에서만 모든 주소를 허용해요) |
| 인증 키 | 서버에 `KR_API_KEY`를 정했을 때만 `local.properties`에 `kidsrhythm.apiKey=...` |

- 서버 실행 방법은 AI·백엔드 담당이 따로 안내해요. 화면만 확인할 때는 서버를 mock 모드(`python -m server --mode mock`)로 켜면 녹음 내용과 상관없이 정해진 가사·노래가 와요.
- 에뮬레이터에서 PC 마이크를 쓰려면 에뮬레이터를 `-allow-host-audio` 옵션으로 켜요.
- 사람 목소리 없이 시험하려면 16kHz 모노 16비트 wav를 `debug_mic.wav`라는 이름으로 넣어요. 디버그 빌드는 마이크 대신 이 파일을 실제 시간처럼 흘려보내요.

```bash
adb push debug_mic.wav /sdcard/Android/data/com.hmgappchallenge.kidsrhythm/files/debug_mic.wav
```

- 차량 속도는 에뮬레이터의 차량 데이터 화면이나 adb로 바꿔요 (값은 m/s, 11.67 = 42 km/h).

```bash
adb shell cmd car_service inject-vhal-event 0x11600207 11.67
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
