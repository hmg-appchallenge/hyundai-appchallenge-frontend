# 차량 리듬 동요 (가칭)

주행 리듬과 아이의 목소리로 세상에 하나뿐인 동요를 만드는 Pleos Connect Kids 앱이에요.

> 서비스명은 아직 정해지지 않았어요. 화면 흐름(S00~S07)과 노래 생성 서버 연동(녹음 세션, 목소리 등록, 완성곡 저장)이 들어가 있어요.

## 실행 방법

1. JDK 17과 Android Studio를 준비해요.
2. [Pleos Connect Emulator 설치 문서](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator)대로 AVD를 만들고 실행해요.
3. 서버 주소를 정해요. 고정 서버(늘 켜져 있어요)를 쓰면 `local.properties`에 두 줄만 적고 따로 켤 건 없어요. 같은 PC에서 서버를 켜서 쓸 수도 있어요 (아래 "노래 생성 서버").
4. 앱을 빌드해서 설치해요.

```bash
./gradlew installDebug
```

5. 에뮬레이터 마이크를 켜요. 에뮬레이터는 마이크가 기본으로 꺼져 있어서, 끈 채로 녹음하면 아무 소리도 들어가지 않아요.
   - 에뮬레이터 도구 막대의 **More(⋯)** → **Microphone** → **Enable Host Microphone Access**를 켜요. 에뮬레이터 버전에 따라 이름이 **Virtual microphone uses host audio input**이에요.
   - 명령줄로 에뮬레이터를 켤 때는 `-allow-host-audio` 옵션을 붙여도 돼요.
   - 처음 켤 때 Windows가 마이크 권한을 물으면 허용해요. 에뮬레이터를 다시 켜면 꺼져 있을 수 있으니 녹음 전에 한 번 확인해요.

## 노래 생성 서버

앱은 노래 생성 서버와 [docs/API.md](docs/API.md) 형식으로 주고받아요.

| 항목 | 값 |
|---|---|
| 기본 주소 | `http://10.0.2.2:8000` (에뮬레이터 안에서 본 같은 PC) |
| 주소 바꾸기 | `local.properties`에 `kidsrhythm.serverUrl=http://192.168.0.10:8000`처럼 적어요. 실제 기기에서 PC 서버에 붙을 때 써요 (http는 디버그 빌드에서만 모든 주소를 허용해요) |
| 팀 서버에 붙기 | 고정 서버(Hugging Face Space, 늘 켜져 있어요)에 붙으려면 `local.properties`에 두 줄 적어요: `kidsrhythm.serverUrl=https://awqdqwd-kidsrhythm-server.hf.space`, `kidsrhythm.apiKey=…`(팀 키, 서버 담당에게 따로 받아요) |
| 인증 키 | 서버에 `KR_API_KEY`가 있으면 바깥에서 붙을 때 `local.properties`에 `kidsrhythm.apiKey=...`가 꼭 있어야 해요. 같은 PC의 에뮬레이터(`10.0.2.2`)는 키 없이 돼요 |

- 서버 실행 방법은 AI·백엔드 담당이 따로 안내해요. 화면만 확인할 때는 서버를 mock 모드(`python -m server --mode mock`)로 켜면 녹음 내용과 상관없이 정해진 가사·노래가 와요.
- 에뮬레이터 마이크는 "실행 방법" 5번처럼 켜요.
- 사람 목소리 없이 시험하려면 16kHz 모노 16비트 wav를 앱 내부 폴더에 `debug_mic.wav`라는 이름으로 넣어요. 디버그 빌드는 마이크 대신 이 파일을 실제 시간처럼 흘려보내요. AAOS 에뮬레이터는 운전자가 사용자 10이라 `run-as`로 넣어요 (`adb shell am get-current-user`로 번호 확인).

```bash
adb push debug_mic.wav /data/local/tmp/
adb shell "cat /data/local/tmp/debug_mic.wav | run-as com.hmgappchallenge.kidsrhythm --user 10 sh -c 'cat > files/debug_mic.wav'"
```

- 차량 속도는 에뮬레이터의 차량 데이터 화면이나 adb로 바꿔요 (값은 m/s, 11.67 = 42 km/h).

```bash
adb shell cmd car_service inject-vhal-event 0x11600207 11.67
```

- 에뮬레이터는 adb로 넣은 속도가 다음 측정 때 0으로 돌아가요. 시험·데모에서 속도를 고정하려면 디버그 빌드 앱 내부 폴더에 `debug_speed.txt`(숫자, km/h)를 넣어요. 1초마다 읽고, 파일을 지우면 다시 차량 값을 써요.

```bash
adb shell "echo 42 | run-as com.hmgappchallenge.kidsrhythm --user 10 sh -c 'cat > files/debug_speed.txt'"
adb shell run-as com.hmgappchallenge.kidsrhythm --user 10 rm files/debug_speed.txt
```

## 노래는 어디에 저장되나요

| 어디 | 남는 것 |
|---|---|
| 서버 | 아무것도 남지 않아요. 녹음은 노래를 다 만들면 바로 지우고, 완성 mp3는 앱이 받자마자 지워요. 앱이 받아 가지 않아도 완성 10분 뒤에 지워져요 ([docs/API.md](docs/API.md) "기본 원칙") |
| S06 완성 화면 | 받은 mp3를 앱 임시 폴더에 두고 바로 틀어요. **보관함에 저장**을 눌러야 보관함에 남아요 |
| S07 보관함 | 앱 전용 저장소 `files/songs/`에 곡마다 mp3와 정보 파일(json: 제목, 만든 시간, 줄마다 시간이 있는 가사, 가족 이름과 악기)이 있어요. 동의 화면의 "노래는 이 차에만 저장해요"를 지키는 구조예요 |

- 처음 설치하면 보관함이 비어 있어요. 예시 곡은 넣어 두지 않았어요.
- 심사·시연에서 만든 노래는 그 PC에서 돌아가는 에뮬레이터 안, 앱 전용 저장소에 저장돼요. PC의 다운로드 폴더 같은 곳에는 파일이 생기지 않아요.
- 앱을 지우거나 에뮬레이터 데이터를 지우면(Wipe Data) 보관함도 같이 지워져요.
- 저장된 파일은 디버그 빌드에서 adb로 볼 수 있어요.

```bash
adb shell run-as com.hmgappchallenge.kidsrhythm --user 10 ls files/songs
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
