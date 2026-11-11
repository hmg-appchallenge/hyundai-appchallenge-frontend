# 노래 생성 서버 API (v0.3)

앱(프론트엔드)과 노래 생성 서버(AI·백엔드)가 주고받는 형식이에요. 피그마 시안(S02 목소리 등록, S04 녹음, S06 완성, S07 보관함·다시 듣기)을 기준으로 맞췄고, 앱은 `server/SongApi.kt`가 이 명세대로 불러요 (아래 "앱 구현 위치").
"확인 필요" 표시가 있는 항목은 팀이 정하면 바꿔요.

## v0.3에서 바뀐 것 (11/11)

- 앱 쪽 연동을 모두 구현했어요 (녹음 세션, 목소리 등록, 진행 상태, 완성곡 저장, 보관함). 바뀐 파일은 맨 아래 "앱 구현 위치"에 있어요.
- **완성하기는 다시 불러도 돼요** (3-3). 응답을 못 받고 다시 부르면 새로 만들지 않고 같은 `songId`를 돌려줘요.
- **조각 하나를 끝내 못 보내도 녹음이 멈추지 않아요** (3-2). 뒤 조각이 3개 쌓이면 서버가 빠진 조각을 건너뛰어요.
- 완성하기 뒤에 세션을 지우면(3-4) 만들던 노래도 멈춰요.
- `TIMEOUT` 기준을 240초로 고쳤어요 (실제 서버 값). 에뮬레이터를 같이 켠 노트북에서 노래 하나에 40~50초 걸려요.

## v0.2에서 바뀐 것 (11/11)

- 녹음 중 실시간 표시용 **녹음 세션 API**를 추가했어요 (3번). 시안 S04의 "소리 낸 사람의 악기가 울려요", "지금까지 들린 단어" 때문이에요.
- 결과 가사에 **줄마다 시작·끝 시간과 강조 위치**를 넣었어요 (5번). 시안의 지금 부르는 줄 강조, '사과' 색 강조, "가사 줄을 누르면 그 부분부터 들어요"에 써요.
- 결과에 `firstBeatMs`를 넣었어요. 캐릭터 춤을 박자에 맞출 때 써요.
- `beatsPerBar`를 `beatType`으로 바꿨어요. 8비트는 한 마디에 8박이라는 뜻이 아니라서요.
- 앱에 넣을 소리 파일(베이스 비트, 악기 소리) 제안과 확인 필요 항목을 더했어요.

## 기본 원칙

- **서버는 아무것도 오래 갖고 있지 않아요.** 동의 화면에 적은 약속("녹음 원본은 노래가 완성되면 바로 지우고, 노래는 이 차에만 저장해요")을 서버가 그대로 지켜요.
  - 녹음(조각 포함): 노래 만들기가 끝나는 즉시(완성·실패·취소 모두) 지워요. 녹음 세션에 10분 동안 조각이 안 오면 그때도 지워요.
  - 완성된 mp3: 앱이 받아 간 뒤 `DELETE`를 부르면 지워요. 앱이 안 부르더라도 완성 10분 뒤 자동으로 지워요.
  - 목소리 특징(숫자 배열): 서버는 계산만 해서 돌려주고 저장하지 않아요. 앱이 가족 정보와 함께 기기에 저장하고, 녹음할 때마다 같이 보내요.
- 가족 이름은 서버로 보내지 않아요. 서버에는 `memberId`와 악기만 있으면 돼요. (가사에 호칭을 넣을지는 확인 필요 6)
- 노래 만들기는 시간이 걸려서 **작업 번호를 받고 진행 상태를 물어보는 방식**이에요.

## 접속

| 항목 | 값 |
|---|---|
| 주소 | 데모: 에뮬레이터 안의 앱에서 `http://10.0.2.2:8000` (같은 PC에서 도는 서버). 다른 기기는 프론트 `local.properties`의 `kidsrhythm.serverUrl` |
| 인증 | 서버에 `KR_API_KEY`를 정했을 때만 헤더 `X-Api-Key: <팀 공유 키>` (앱은 `kidsrhythm.apiKey`). 안 정하면 인증 없이 받아요 |
| 형식 | JSON(UTF-8). 시간은 따로 적지 않으면 초 단위, 이름이 `Ms`로 끝나면 밀리초 |
| 녹음 형식 | PCM 16비트(little endian), 16kHz, 모노. `AudioRecord`로 받은 그대로 |

앱 설정 (프론트엔드, 반영됨):
- `AndroidManifest.xml`에 `INTERNET`, `RECORD_AUDIO`, 차량 속도·바깥 온도 권한
- `http://10.0.2.2`는 암호화되지 않은 주소라 `network_security_config`로 이 주소만 허용해요. 디버그 빌드는 같은 와이파이의 PC 주소로도 붙도록 모두 허용해요

## 공통 값

**악기** (앱의 `Instrument` 이름 그대로)

`DRUM` 북 · `TAMBOURINE` 탬버린 · `SHAKER` 셰이커 · `BELL` 벨 · `UKULELE` 우쿨렐레 · `STEELDRUM` 스틸드럼 · `GUITAR` 기타 · `XYLOPHONE` 실로폰

시안의 악기 고르기는 "전체 12"예요. 12종이 확정되면 나머지 4개 이름을 더해요. (확인 필요 3)

**단계** (앱의 `GenerationStep` 이름 그대로)

`FIND_WORDS` 단어 찾기 → `WRITE_LYRICS` 노래 가사 만들기 → `SPLIT_VOICES` 목소리 나누기 → `ADD_INSTRUMENTS` 목소리마다 악기 입히기 → `FINISH` 노래 완성

**상태**: `QUEUED` 대기 · `RUNNING` 만드는 중 · `DONE` 완성 · `FAILED` 실패 · `CANCELED` 취소

**속도 → 비트 표** (앱과 서버가 같은 표를 써요. 피그마 스피드 칩 메모 기준)

| 속도 | BPM | `beatType` | 구간 이름 |
|---|---|---|---|
| 0 km/h | 80 | 2 (2비트) | 정차 |
| 1~30 km/h | 95 | 4 (4비트) | 저속 |
| 31~70 km/h | 110 | 8 (8비트) | 중속 |
| 71 km/h 이상 | 125 | 16 (16비트) | 고속 |

`beatType`은 리듬을 얼마나 잘게 쪼개는지예요. 박자는 항상 4/4라서 한 박은 `60000 / bpm` 밀리초, 한 마디는 그 4배예요.
앱은 `beat/SpeedBand.kt`의 `beatTypeForSpeed`·`beatTypeForBpm`으로 이 표를 써요. S03에서 들려준 BPM을 3-3 `beat`로 보내서 완성곡도 같은 비트가 돼요.

## 1. 서버 상태 확인

`GET /v1/health`

```json
{ "ok": true, "version": "0.3.0", "mode": "real", "ready": true, "speakerModel": "campplus-zh-en-advanced", "vad": "silero", "error": null }
```

- `ready`: AI 모델을 다 불러왔는지. 서버를 켠 직후 1~2분은 `false`일 수 있어요 (그동안 온 요청도 받아 두고, 모델이 준비되면 처리해요)
- `mode`: `real`(진짜 AI) 또는 `mock`(정해진 응답, 앱 연동 시험용)

## 2. 목소리 특징 만들기 (가족 목소리 등록, S02-2·S02-3)

`POST /v1/voices/embed` (multipart)

| 필드 | 내용 |
|---|---|
| `audio` | 등록 녹음. wav 권장. 헤더 없는 PCM(위 녹음 형식)이나 m4a도 받아요 |

응답 `200`

```json
{
  "embedding": [0.0132, -0.0841, "... 192개"],
  "model": "campplus-zh-en-advanced",
  "speechSec": 6.4,
  "quality": "ok"
}
```

- `quality`가 `ok`가 아니면 다시 녹음하게 해 주세요: `too_short`(말한 시간이 3초 미만), `too_quiet`(소리가 작음), `noisy`(잡음이 큼). 이때 `embedding`은 `null`이에요
- 서버는 받은 녹음을 응답 직후 지워요
- 앱은 `embedding`을 그 가족(`FamilyMember`)과 함께 기기에 저장해요. "목소리 다시 등록하기"를 하면 새 값으로 바꿔요
- 서버의 화자 모델(`model`)이 바뀌면 숫자 배열 길이가 달라져요. 길이가 안 맞는 값은 서버가 '등록 전'으로 보고 넘어가니, 그때는 목소리를 다시 등록하면 돼요
- 녹음 길이는 확인 필요 4

## 3. 녹음 세션 (S04 같이 노래해요)

녹음하는 동안 2초마다 조각을 보내면, 서버가 누가 말했는지와 새로 들린 단어를 돌려줘요. "완성하기"를 누르면 모아 둔 녹음으로 노래를 만들어요. 녹음하는 동안 미리 처리해 두니까 S05에서 기다리는 시간도 줄어요.

흐름: 녹음 시작 → `POST /v1/sessions` → 2초마다 `POST .../chunks` → 완성하기 → `POST .../finish` → 5번(진행 상태)

### 3-1. 세션 시작

`POST /v1/sessions` (JSON)

```json
{
  "members": [
    { "memberId": 1, "instrument": "DRUM", "embedding": [0.0132, -0.0841] },
    { "memberId": 2, "instrument": "SHAKER", "embedding": [0.0517, 0.0093] },
    { "memberId": 3, "instrument": "TAMBOURINE", "embedding": null }
  ]
}
```

- `members`: 최대 4명. `embedding`이 없는 가족(목소리 등록 전)은 녹음하는 동안에는 누군지 몰라요. 완성할 때 서버가 목소리를 비슷한 것끼리 묶어서 남은 악기를 붙여요 (S02-1 안내 문구 "등록 전에는 노래가 완성될 때 목소리를 나눠 악기를 붙여요"와 같은 동작)

응답 `201`

```json
{ "sessionId": "s8d2f41", "chunkMs": 2000, "maxRecordingSec": 180 }
```

### 3-2. 녹음 조각 보내기

`POST /v1/sessions/{sessionId}/chunks?seq=12`

본문: 녹음 바이트 그대로 (`Content-Type: application/octet-stream`). 2초면 64,000바이트(16000 × 2바이트 × 2초)예요.

- `seq`는 0부터 1씩 늘려요. 조각은 순서대로 하나씩 보내 주세요
- 보내다 실패하면 같은 `seq`로 다시 보내요. 같은 `seq`를 두 번 받아도 한 번만 써요
- 끝내 못 보낸 조각은 건너뛰고 다음 `seq`를 보내도 돼요. 뒤 조각이 3개(6초) 쌓이도록 안 오면 서버가 빠진 조각을 건너뛰어요
- "녹음 정지"를 누르면 조각을 안 보내면 돼요. "이어서 녹음하기"를 누르면 다음 `seq`부터 이어서 보내요. 서버는 쉰 동안을 빼고 이어 붙여요
- 녹음이 `maxRecordingSec`(3분)을 넘으면 `413`(`TOO_LONG`)을 돌려줘요. 앱은 3분이 되면 스스로 멈추고 완성하기를 안내해요

응답 `200`

```json
{
  "seq": 12,
  "receivedSec": 26.0,
  "speaking": [1, 2],
  "unregisteredVoice": false,
  "newWords": [
    { "text": "사과", "memberId": 2, "kind": "word" },
    { "text": "둥둥", "memberId": 1, "kind": "onomatopoeia" }
  ]
}
```

- `speaking`: 이 조각에서 목소리가 들린 가족이에요. S04 카드를 '연주 중'으로, 나머지는 '대기'로 바꾸고, 앱은 이 가족의 악기 소리를 비트에 맞춰 울려요
- `unregisteredVoice`: 목소리 등록을 안 한 사람의 목소리가 들렸는지
- `newWords`: 이번에 새로 찾은 단어예요. "지금까지 들린 단어"에 붙여요. 같은 단어는 한 번만 와요. 누가 말했는지 모르면 `memberId`가 `null`이에요. 아이 노래에 맞지 않는 말(욕·어른 말·무서운 말)과, 자신 없게 들린 낯선 명사(잘못 알아들었을 수 있는 말)는 서버가 빼고 보내요. 가사에도 쓰지 않아요
- 말이 조각 경계에 걸리면 단어가 다음 조각 응답에 와서, 화면에는 1~3초 늦게 나타날 수 있어요

### 3-3. 완성하기

`POST /v1/sessions/{sessionId}/finish` (JSON). 마지막 조각 응답을 받은 뒤 불러 주세요.

```json
{
  "drive": {
    "speedKmh": [42.0, 41.5, 40.8, 38.0],
    "intervalSec": 1.0,
    "outsideTempC": 14.5,
    "startedAt": "2026-11-11T13:40:00+09:00"
  },
  "beat": { "bpm": 110, "beatType": 8 }
}
```

- `drive.speedKmh`: 녹음하는 동안 1초마다 잰 속도. 가속·감속 위치를 필인으로 바꾸는 데 써요. 없으면 `beat` 값만 써요
- `beat`: S03 화면에서 보여 준 값. 완성곡도 이 BPM과 비트로 만들어요

응답 `202`

```json
{ "songId": "c7f3a9", "status": "QUEUED" }
```

- 응답을 못 받았으면(연결 끊김 등) 같은 요청을 다시 보내도 돼요. 새로 만들지 않고 같은 `songId`를 돌려줘요. 앱은 S04에서 "다시 완성하기"로 이걸 해요

### 3-4. 녹음 그만두기

`DELETE /v1/sessions/{sessionId}` → `204`

S04에서 뒤로 가면 불러 주세요. 모아 둔 조각을 바로 지워요. 완성하기를 부른 뒤라면 만들던 노래도 멈추고 지워요.

## 4. 녹음 파일 하나로 노래 만들기 (테스트·예비용)

실시간 표시 없이 녹음 파일 하나를 통째로 보낼 때 써요. 연동 테스트나, 녹음 세션이 안 될 때를 위한 길이에요.

`POST /v1/songs` (multipart)

| 필드 | 내용 |
|---|---|
| `recording` | 녹음 전체. wav 권장. 헤더 없는 PCM(위 녹음 형식)도 받아요 |
| `meta` | 3-1의 `members`와 3-3의 `drive`, `beat`를 합친 JSON 문자열 |

응답 `202`는 3-3과 같아요.

## 5. 진행 상태와 결과 (S05 → S06)

`GET /v1/songs/{songId}`

앱은 S05에 있는 동안 1초마다 불러요. `step`을 `GenerationProgress` 대신 써서 단계 표시를 진짜로 바꿔요.

만드는 중:

```json
{ "songId": "c7f3a9", "status": "RUNNING", "step": "SPLIT_VOICES", "progress": 0.55, "result": null, "error": null }
```

완성 (`DONE`):

```json
{
  "songId": "c7f3a9",
  "status": "DONE",
  "step": "FINISH",
  "progress": 1.0,
  "result": {
    "title": "사과 노래",
    "durationMs": 102000,
    "bpm": 110,
    "beatType": 8,
    "firstBeatMs": 0,
    "lyrics": [
      {
        "text": "사과는 빨갛다, 둥둥 둥둥",
        "startMs": 4364,
        "endMs": 8727,
        "section": "verse",
        "highlights": [
          { "start": 0, "end": 2, "memberId": 2, "kind": "word" },
          { "start": 9, "end": 11, "memberId": 1, "kind": "onomatopoeia" },
          { "start": 12, "end": 14, "memberId": 1, "kind": "onomatopoeia" }
        ]
      },
      {
        "text": "사과는 맛있다, 랄랄라 랄라",
        "startMs": 8727,
        "endMs": 13091,
        "section": "verse",
        "highlights": [
          { "start": 0, "end": 2, "memberId": 2, "kind": "word" },
          { "start": 9, "end": 12, "memberId": 3, "kind": "onomatopoeia" }
        ]
      }
    ],
    "words": [{ "text": "사과", "memberId": 2 }],
    "onomatopoeia": [{ "text": "둥둥", "memberId": 1 }, { "text": "랄랄라", "memberId": 3 }],
    "members": [
      { "memberId": 1, "instrument": "DRUM", "speechSec": 4.2 },
      { "memberId": 2, "instrument": "SHAKER", "speechSec": 3.1 },
      { "memberId": 3, "instrument": "TAMBOURINE", "speechSec": 2.0 }
    ],
    "audio": { "url": "/v1/songs/c7f3a9/audio", "format": "mp3", "expiresInSec": 600 }
  },
  "error": null
}
```

- `title`: 가사에 쓴 첫 단어 + "노래". 단어가 하나도 없으면 "11월 11일 드라이브 노래"처럼 날짜로 지어요
- `lyrics[].startMs`, `endMs`: 그 줄을 부르는 시간이에요. 재생 위치로 지금 부르는 줄을 찾아 강조하고(S06·다시 듣기의 가사 상자, 아래 한 줄 가사), 줄을 누르면 `startMs`로 이동해요
- `lyrics[].highlights`: 가족 목소리에서 나온 단어가 그 줄 몇 번째 글자에 있는지예요. `start`는 넣고 `end`는 빼고 세요 (Kotlin `text.substring(start, end)` 그대로). 시안의 '사과' 색 강조에 써요. 의성어까지 칠할지는 `kind`로 골라요
- `lyrics[].section`: `intro`·`verse`·`chorus`·`outro`. 값이 바뀌는 곳에 빈 줄을 넣으면 시안처럼 보여요
- `firstBeatMs`: 첫 박이 나오는 시간이에요. n번째 박은 `firstBeatMs + n × 60000 / bpm`. 캐릭터 춤을 박자에 맞출 때 써요
- `members[].speechSec`: 그 가족 목소리가 들린 시간. 0이면 이번 녹음에서 안 들린 거예요
- 지금 앱의 `GeneratedSong.seatZone`은 좌석 기준이라 빼고, 누가 말했는지는 `memberId`로 줘요

실패 (`FAILED`):

```json
{ "songId": "c7f3a9", "status": "FAILED", "step": "FIND_WORDS", "progress": 0.1, "result": null,
  "error": { "code": "NO_SPEECH", "message": "목소리가 들리지 않았어요" } }
```

| `error.code` | 뜻 |
|---|---|
| `NO_SPEECH` | 녹음에서 말소리를 못 찾음 |
| `TOO_SHORT` | 녹음이 3초보다 짧음 |
| `BAD_AUDIO` | 파일을 읽을 수 없음 |
| `TIMEOUT` | 240초 안에 끝나지 않음 (느린 PC에서도 넉넉하게) |
| `INTERNAL` | 그 밖의 서버 오류 |

## 6. 완성곡 받기 (S06)

`GET /v1/songs/{songId}/audio` → `audio/mpeg`

앱은 받은 파일을 앱 저장 공간에 넣고, 저장이 끝나면 바로 7번을 불러요.
`result` JSON도 mp3와 같이 저장해 두면 S07 보관함(제목·날짜·길이)과 다시 듣기(가족·악기, 가사 강조, 줄 눌러 듣기)를 서버 없이 그릴 수 있어요.

## 7. 지우기·그만두기

`DELETE /v1/songs/{songId}` → `204`

- 만드는 중이면 멈추고 지워요. S05에서 뒤로 가면 불러 주세요
- 완성된 뒤면 서버에 남은 mp3를 지워요

## HTTP 오류

| 코드 | 뜻 |
|---|---|
| `400` | 요청 형식이 틀림 (본문에 `{ "code": "...", "message": "..." }`) |
| `401` | `X-Api-Key`가 없거나 틀림 |
| `404` | 없는 세션·노래 (이미 지웠거나 10분이 지남) |
| `409` | 이미 완성하기를 부른 세션에 조각을 보냄 |
| `413` | 녹음이 3분을 넘음 |

## 앱에 넣은 소리 파일

앱에서 트는 비트와 악기 소리는 완성곡과 같은 소리가 나도록 노래 엔진에서 뽑았어요 (`ai/songmaker/app_sounds.py`, `python -m songmaker.app_sounds <폴더>`). 앱의 `app/src/main/res/raw/`에 들어 있어요. 모두 22.05kHz 모노 16비트 wav, 합쳐서 약 2MB예요.

| 파일 | 쓰는 곳 | 내용 |
|---|---|---|
| `beat_080_2`, `beat_095_4`, `beat_110_8`, `beat_125_16` | S03~S05에서 계속 재생 (`BeatPlayer`) | 속도 구간마다 끊김 없이 반복되는 2마디 |
| `inst_<악기>_preview` (8개) | 멤버 추가의 "누르면 3초 미리 듣기" | 악기마다 3초 |
| `inst_<악기>_hit` (8개) | S04 "소리 낸 사람의 악기가 울려요" | 0.6초 안팎 |

악기가 12종으로 늘면 같은 명령으로 다시 뽑아요.

## 앱 구현 위치

경로는 `app/src/main/java/com/hmgappchallenge/kidsrhythm/` 기준이에요.

| 명세 | 앱 코드 |
|---|---|
| 호출 전체 (1~7번) | `server/SongService.kt`(인터페이스), `server/SongApi.kt`(HTTP 구현), `server/SongModels.kt`(모델·JSON 읽기). 미리보기·테스트용 `server/FakeSongService.kt` |
| 2. 목소리 등록 | `audio/VoiceEnrollment.kt` → `ui/screen/MemberEditorDialog.kt`. 특징은 `family/FamilyRepository.kt`(`PrefsFamilyRepository`)가 기기에 저장 |
| 3. 녹음 세션 | `recording/RecordingController.kt`(조각 전송·재시도·3분 제한·다시 완성하기), `audio/PcmRecorder.kt`(마이크 16kHz), `ui/screen/RecordingScreen.kt`(S04) |
| 3-3 `drive` | `vehicle/CarVehicleSignals.kt`(AAOS Car API 속도·바깥 온도). 녹음하는 동안 1초마다 모아요 |
| 5. 진행 상태 | `ui/screen/GeneratingScreen.kt`(1초마다 조회), `generating/GenerationProgress.kt`의 `doneCountForServer` |
| 6·7. 완성곡 받기·지우기 | `GeneratingScreen.kt`가 받은 뒤 바로 서버에서 지워요. 보관함은 `archive/FileSongArchive.kt`(mp3 + `result` JSON) |
| 가사·춤 | `ui/component/SongParts.kt`(`LyricsBox` 지금 줄 강조·줄 눌러 이동·단어 색, `DancingMascot` 박자 춤), S06 `CompletedScreen.kt`, S07 `ArchiveScreen.kt`·`ReplayScreen.kt` |
| 속도 → 비트 | `beat/SpeedBand.kt`, 비트 재생 `audio/BeatPlayer.kt` |
| 서버 주소·키 | `app/build.gradle.kts`(`local.properties`의 `kidsrhythm.serverUrl`, `kidsrhythm.apiKey`) |
| 응답 모양 확인 테스트 | `app/src/test/.../server/SongJsonTest.kt` (이 문서의 예시 JSON 그대로 읽어 봐요) |

## 확인 필요

1. **음성 인식 위치**: 동의 화면에는 "음성 인식(Pleos Gleo AI)"이라고 적혀 있어요. 그런데 아이 목소리를 잘라 노래에 넣으려면 단어마다 시간이 필요해서 서버도 음성 인식을 해요. 문구를 "음성 인식과 노래 생성 서버로 보내요"로 바꿀지 정해야 해요.
2. **녹음 중 실시간 표시**: 3번처럼 2초 조각을 보내면 시안대로 보여 줄 수 있지만, 화면이 1~3초 늦게 바뀌어요. 이 정도면 괜찮은지 봐 주세요. '연주 중' 가족은 바로 바뀌지만, '지금까지 들린 단어'는 음성 인식이 느린 PC(에뮬레이터를 같이 켠 노트북)에서는 녹음이 끝날 무렵에야 나와요.
3. **악기 개수**: 앱은 8종, 시안은 12종이에요. 12종이면 나머지 4개를 정해 주세요.
4. **목소리 등록 길이**: 앱은 10초예요. 서버 기준으로는 실제로 말하는 시간이 5초 이상이면 충분해서, 시안 문장 "안녕, 우리 같이 노래하자!"를 두 번 따라 말하게 하는 걸 추천해요.
5. **곡 길이**: 시안 예시는 1분 28초~2분 05초인데, 지금 엔진은 전주·벌스·후렴·벌스·후렴·후주 28마디라 110 BPM에서 1분 남짓이에요. 목표 길이를 정해 주세요. 길수록 만드는 시간도 늘어나요. S05 문구는 시안의 "보통 5~20초" 대신 "보통 1분쯤 걸려요"로 바꿔 두었어요. 에뮬레이터를 같이 켠 노트북(Ryzen 5 7520U)에서 서버를 돌리면 30~65초 걸려서예요 (짧게 녹음하고 바로 완성하면 음성 인식이 밀려 있어서 더 걸려요). GPU 서버처럼 빠른 곳에서 돌리면 시안 문구로 되돌려도 돼요.
6. **가사에 가족 호칭**: 시안 가사에 "엄마랑 같이 사과 먹자", "둘째도 랄랄라"가 있어요. 호칭을 넣으려면 앱이 이름을 서버로 보내야 하고(지금은 안 보냄), 동의 문구에도 적어야 해요. 안 보내면 녹음에서 들린 단어로만 가사를 만들어요.
7. **한 곡 더**: 새로 녹음하는 건지(S03으로), 같은 녹음으로 다른 곡을 만드는 건지 정해 주세요. 같은 녹음이면 "원본은 바로 지운다"는 약속과 부딪혀서, 지금은 새로 녹음하는 걸로 생각하고 있어요.
