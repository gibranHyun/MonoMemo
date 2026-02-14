# MonoMemo

오프라인 초경량 노트 에디터 Android 앱

## Features

- **에디터** — D2Coding 모노스페이스 폰트, 줄 번호, 글자/줄 카운터
- **찾기 & 바꾸기** — 대소문자 구분, 단어 단위 검색, 하이라이트, 현재 매치 센터링 스크롤
- **Undo / Redo** — 디바운스 기반 스냅샷 (최대 50단계)
- **파일 관리** — 파일로 저장 (Export), 파일 열기 (Import), 공유
- **Drawer 내비게이션** — 노트 목록, 노트 검색, 새 노트 생성
- **휴지통** — 소프트 삭제, 복원, 30일 후 자동 영구 삭제
- **설정** — 테마 (시스템/라이트/다크), 폰트 크기, 줄바꿈, 줄 번호
- **오프라인** — 네트워크 불필요, Room DB 로컬 저장

## Tech Stack

| 항목 | 기술 |
|------|------|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material3 |
| DB | Room 2.6 |
| Settings | DataStore Preferences |
| Font | D2Coding (SIL OFL) |
| Min SDK | 26 (Android 8.0) |
| DI | Manual (Application class) |

## Build

```bash
./gradlew assembleDebug
```

## Install

```bash
./gradlew installDebug
```

## License

D2Coding font is licensed under the [SIL Open Font License 1.1](http://scripts.sil.org/OFL) by NAVER Corporation.
