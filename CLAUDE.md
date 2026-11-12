# 차곡차곡

주행 데이터와 아이 음성으로 동요를 만드는 Pleos Connect Kids 앱의 프론트엔드예요. 스택은 Kotlin + Jetpack Compose(AAOS)이고, 가사·노래 생성은 별도 AI 서버가 맡아요.

## 하네스 설정

> `/setup-harness`가 채운다. 스킬·커맨드는 `{설정:항목}` 표기로 이 값을 참조한다. 값이 바뀌면 여기만 고친다.

### 원칙

- **의사결정은 혼자 하지 않는다.** 명세·시안에 없는 동작, 기존 화면 변경, 새 에셋·컴포넌트·라이브러리 추가, 범위 밖 수정, 서버 값 추정이 필요하면 멈추고 개발자에게 묻는다. 기준은 `.claude/commands/feature.md`의 "의사결정" 절
- **지시가 충돌하면** 사용자 지시 > 팀 룰(이 문서·하네스) > 플러그인·외부 스킬 > 기본 동작. 어느 쪽을 따랐는지 밝힌다
- 300줄 넘는 파일은 범위 없이 읽지 않는다 (hook이 막는다). `grep -n`으로 위치를 찾고 offset/limit으로 읽는다
- 새 기능은 `/feature`로 진행한다

### 저장소

| 항목 | 값 |
|---|---|
| 기본 브랜치 | main |
| 커밋 형식 | `[TYPE] 평서문` (FEAT, FIX, DOCS, CHORE, TEST, REFACTOR) |
| 문체 | 부드러운 존댓말, 문제와 제안을 짧게 |
| 리뷰어 | 없음 (셀프 리뷰. `--reviewer`, `--add-reviewer` 없이 PR을 만든다) |

### 명령

| 항목 | 값 |
|---|---|
| 패키지 매니저 | Gradle wrapper (`./gradlew`), JDK 17 |
| 타입체크 | `./gradlew compileDebugKotlin` (에러 수: `./gradlew compileDebugKotlin 2>&1 \| grep -c '^e: '`) |
| 린트 | `./gradlew lintDebug` |
| 테스트 | `./gradlew testDebugUnitTest` |
| 소스 확장자 | `\.(kt\|kts)$` |
| 개발 서버 포트 | 없음 |
| 앱 ID | com.hmgappchallenge.kidsrhythm |
| 앱 스킴 | 없음 |

### 아키텍처

이 프로젝트에서 각 역할을 하는 위치. 리뷰·분석은 여기를 기준으로 찾는다. 경로는 `app/src/main/java/com/hmgappchallenge/kidsrhythm/` 기준이다.

| 역할 | 위치 |
|---|---|
| 화면·라우트 | `ui/screen/`, `navigation/AppNavHost.kt` |
| 기능 모듈 | `beat/`, `vehicle/`, `audio/`, `archive/`, `recording/`, `family/` |
| API 호출 | `server/` (`SongService` 인터페이스, `SongApi` HTTP 구현, `SongModels`의 `SongJson`) |
| 전역 상태 | `AppServices.kt` (서버·보관함·가족·차량·소리를 앱에 하나씩 두고, S05→S06 완성곡과 고른 친구를 들고 있다) |
| 공용 컴포넌트 | `ui/component/` (가사·재생 막대·춤추는 친구는 `SongParts.kt`) |
| 아이콘 | 아직 없음 (`app/src/main/res/drawable/`에 추가) |
| 소리 파일 | `app/src/main/res/raw/` (비트·악기 wav. 노래 엔진에서 뽑는다, `docs/API.md` "앱에 넣은 소리 파일") |
| mocks 위치 | `server/FakeSongService.kt`, 미리보기 데이터 `recording/FakeRecording.kt`·`family/Family.kt`의 `FakeFamily`·`vehicle/FakeVehicleSignals.kt` |
| 스타일·테마 | `ui/theme/AppTheme.kt` |

### 외부 소스

| 항목 | 값 |
|---|---|
| API 스키마 | `docs/API.md` (AI 서버 담당과 같이 고친다. 명세에 없는 값은 추정하지 말고 확인) |
| 모킹 스위치 | `AppServices.kt`의 `songService` (`SongApi` ↔ `FakeSongService`). 또는 서버를 mock 모드로 켠다 |
| 서버 주소 | `local.properties`의 `kidsrhythm.serverUrl` (기본 `http://10.0.2.2:8000`), `kidsrhythm.apiKey` |
| 디자인 소스 | [Figma](https://www.figma.com/design/OIYJSznIEBAgnsbyMlrFOh) (기획 페이지에 명세, UI 시안) |
| 테스트 도구 | JUnit, adb + uiautomator |

### 문서 동기화

함께 바꿔야 하는 짝. 리뷰가 확인한다.

- 서비스명 변경: `app/src/main/res/values/strings.xml`의 `app_name`, `README.md`, 이 문서 제목
- 빌드 환경 변경(SDK·JDK): `app/build.gradle.kts`, `README.md`, `.github/workflows/ci.yml`
- 서버 응답 형식 변경: `docs/API.md`, `server/SongModels.kt`(`SongJson`), `app/src/test/.../server/SongJsonTest.kt`
- 속도 구간·비트 변경: `beat/SpeedBand.kt`, `beat/BpmMapper.kt`, `docs/API.md` "속도 → 비트 표", `res/raw/beat_*.wav`

### 고유 규칙

이 프로젝트에서 실제로 사고가 났던 항목. 사고가 나면 여기에 한 줄씩 추가한다.

없음
