# MonoMemo 출시 진행 상황

> 최종 갱신: 2026-10-17 (집 PC, `E:\workspace\MonoMemo`)
> 목적: Google Play 비공개 테스트 → 프로덕션 출시 진행 상황 기록 (다른 컴퓨터/다음 세션 이어가기용)

---

## 🚨 0. 2026-10-17 핵심 업데이트 — 2차 신청도 반려됨, 근본 원인 파악

- **2026-10-10 프로덕션 재신청 → 같은 날 저녁(18:21) 2차 반려.** 반려 사유가 1차와 동일: **"비공개 테스트 중에 테스터가 앱에 참여하지 않았습니다"** + "테스트 권장사항(업데이트로 피드백 수집·반영)을 따르지 않음". 메일 원문은 Gmail에서 `subject:프로덕션 newer_than:8d`로 검색 확인 가능.
- **핵심 진단**: Play Console 대시보드의 "12명 이상 옵트인 + 14일" 체크리스트는 **옵트인 여부만** 보는 것이고, 프로덕션 심사는 **실제 사용 참여도(engagement)**를 별도로 봄. Reddit(r/GooglePlayClosedTest 등)에서 모집한 상호테스트 테스터 다수가 **옵트인만 하고 실제 설치·실행은 안 했을 가능성이 높음** — 이게 1·2차 모두 반려된 진짜 원인으로 보임.
- **다음 전략 제안** (아래 섹션 5 참고): 그룹 인원수를 늘리는 게 아니라, **옵트인한 사람들이 실제로 설치해서 며칠 써보도록 유도**하는 쪽으로 바꿔야 함. Play Console > 통계 > 사용자 획득/활성 기기 수치로 실제 설치·실사용자가 몇 명인지 먼저 확인할 것.
- 자동으로 Gmail을 매일 확인해 알려주려던 클라우드 루틴(`trig_01QLc3G4mpS2u4mvvkSFFFif`)은 **설정 버그로 7일 내내 빈 세션만 돌고 실제로는 아무 작업도 안 했음** (update로 재시도해도 prompt가 저장 안 되는 API 이슈). 지금 비활성화함 — 이 방식은 재사용하지 말 것.

## 0-1. 2026-10-17 회사 PC(10/17 오전)에서 확인한 것 — 참고용, 위 내용이 최신

- origin/main을 pull 받아 집 PC(10/8)의 작업 내역을 그대로 반영함. 이 컴퓨터에서 새로 변경한 코드는 없음.
- `monomemo-upload.keystore`는 회사 PC에는 여전히 없음. 집 PC(`E:\workspace\MonoMemo\`)에만 있음.
- repo 버전은 versionCode 5 / versionName "1.0.4"로 확인됨.

## 1. 2026-10-08 집 PC에서 처리한 것

1. **`monomemo-upload.keystore` 찾음** — 회사 PC에는 없었지만 **집 PC 프로젝트 루트(`E:\workspace\MonoMemo\monomemo-upload.keystore`)에 계속 있었음**. `keystore.properties`의 경로·비밀번호와 일치 확인함.
2. **서명된 AAB 빌드 완료** — `./gradlew :app:bundleRelease` 성공, `app/build/outputs/bundle/release/app-release.aab` (versionCode 5, 1.0.4). 경고 2건(proguard 매핑/네이티브 심볼 파일 미업로드)은 출시를 막지 않는 권장사항이라 무시하고 진행함.
3. **Play Console 프로덕션 액세스 요건 전부 충족 확인** — 12명 이상 옵트인 + 14일 이상 비공개 테스트, 체크리스트 3개 전부 녹색.
4. **버전 5(1.0.4)를 비공개 테스트(Alpha)에 업로드·제출 완료 → 검토 통과 → 게시됨** (Play Console 알림 "앱 업데이트가 게시되었습니다", 10월 8일자로 확인). 릴리스 노트는 3번 섹션의 "릴리스 노트 초안"을 그대로 사용함.

## 2. 빌드/버전 현황

- **repo 버전**: versionCode 5, versionName "1.0.4" (`app/build.gradle.kts`)
- **targetSdk / compileSdk**: 36
- **패키지**: com.monomemo.app
- **서명된 AAB**: 집 PC에서 생성 완료 (`app/build/outputs/bundle/release/app-release.aab`, 1번 섹션 참고). 회사 PC에서는 keystore 파일이 없어 아직 생성 불가.
- **Play Console에 실제 게시된 최신 버전**: versionCode 5 (1.0.4), 2026-10-08 게시 확인

### git 커밋 이력 (주요 지점)
```
4d6ecf8 진행상황 갱신: keystore 집 PC에서 발견, 1.0.4 비공개 테스트 게시 완료  (집 PC, 2026-10-08)
f1962fb 진행상황 기록 갱신: 서명 키 미비, 다음 세션(집 PC) 할 일 정리        (회사 PC, 2026-10-07)
a2b1680 Merge remote-tracking branch 'origin/main'
59bf800 버전 4 (1.0.3)로 업데이트
63f58dd 검색 옵션 토글에 접근성 설명 및 on/off 상태 노출
b507f7e 아이콘 전용 버튼에 접근성 설명 추가
9f31f86 줄 번호 렌더링을 Text 1개로 합쳐 컴포저블 폭증 방지
cfdf809 찾기 연산을 메인 스레드 밖에서 실행
07301bb 백그라운드 전환 시 편집 내용 즉시 저장
48ce259 찾기/바꾸기: 오래된 매치 범위로 인한 크래시 방지
5eac5d9 스토어 스크린샷 교체 및 재생성 스크립트 추가
8b8a011 v1.0.2: 에디터 키보드 버그 수정 및 UX 개선, targetSdk 36 대응       (집 PC)
73997c3 출시 준비: release 서명 설정, 개인정보처리방침, 스토어 그래픽 에셋 추가
```
`8b8a011`(집 PC, v1.0.2)과 회사 PC 세션의 안정성 수정 6건이 `app/build.gradle.kts`, `EditorScreen.kt`에서 충돌해 머지했음 — 양쪽 수정 모두 반영됨. 에뮬레이터에서 정상 동작 확인함.

## 3. 안정성/UX 개선 내역

### 회사 PC(2026-09-30~10-07)에서 고친 것 — 안정성/접근성 6건
1. 찾기/바꾸기 중 디바운스 전에 치환을 누르면 발생하던 크래시 수정 (stale match 범위 가드)
2. 백그라운드 전환(ON_STOP) 시 편집 내용 즉시 저장 — `onCleared()`의 flushSave가 실제로는 동작 안 했던 문제
3. 찾기 연산을 메인 스레드 밖(Dispatchers.Default)으로 이동 — 긴 노트에서 ANR 위험 완화
4. 줄 번호 렌더링을 줄마다 Text 생성 → Text 1개로 합쳐 컴포저블 수 O(1)로
5. Undo/Redo, 휴지통 복원/삭제 버튼에 접근성 설명(contentDescription) 추가
6. 찾기 옵션(대소문자/전체단어) 토글에 접근성 설명 + on/off 상태 노출

### 집 PC(8b8a011, v1.0.2)에서 고친 것
- 에디터 키보드 안 뜨던 버그 (`fillMaxHeight` → `defaultMinSize(minHeight=availableHeight)`)
- 버전 표기 3곳 불일치 → `BuildConfig.VERSION_NAME`으로 일원화
- 설정 화면 License/개인정보처리방침 링크 활성화
- 휴지통 영구삭제·전체비우기 확인 다이얼로그 추가
- 치환/실행취소 후 커서 위치 보존 (TextRange 명시)
- D2Coding 자간 축소로 세로로 길어 보이는 문제 완화
- 스토어 피처 그래픽 교체

### 릴리스 노트 (1.0.4에 사용됨)
> - 찾기/바꾸기 도중 빠르게 편집한 직후 치환을 누르면 발생할 수 있던 크래시를 수정했습니다.
> - 앱을 백그라운드로 전환할 때 편집 중이던 내용이 즉시 저장되어, 최근 메모가 사라지는 문제를 막았습니다.
> - 긴 노트에서 찾기 기능이 버벅이지 않도록 검색 연산을 최적화했습니다.
> - 긴 노트에서 줄 번호 표시로 인한 지연을 개선했습니다.
> - 스크린리더 사용자를 위해 되돌리기/다시 실행, 휴지통 복원/삭제, 찾기 옵션 버튼에 설명을 추가했습니다.

## 4. Play Console 상태 (최종 확인 2026-10-17)

- **개발자 계정**: entropy.gibran@gmail.com (Entropy by Gibran, 개인 계정)
- **프로덕션 액세스**: 2026-09-19 1차 반려, **2026-10-10 2차 반려** — 둘 다 사유 동일: "비공개 테스트 중 테스터가 앱에 참여하지 않음" (0번 섹션 참고)
- **비공개 테스트(Alpha)**: 옵트인 12명 이상 + 14일 체크리스트는 충족 표시되지만, 실제 사용 참여도는 부족한 것으로 추정됨
- **1.0.4가 비공개 테스트에 게시됨** (2026-10-08)
- 1차 신청서 "비공개 테스트 참여도" 답변은 Reddit(r/AndroidAppTesters) 테스터 2명이 보고한 온보딩 버그(그룹 권한 오류, 설치 버튼 무반응)를 근거로 작성했음 — 돌이켜보면 이것도 "설치는 했지만 못 들어간 사람이 많았다"는 신호였을 수 있음

### 테스터 그룹
- 그룹 가입: https://groups.google.com/g/monomemo-testers (전체 공개로 설정됨)
- 옵트인: https://play.google.com/apps/testing/com.monomemo.app

### 스토어 등록정보 (완료)
- 휴대전화 스크린샷 5장 전부 1080×2160(2:1)으로 재촬영·교체 완료 (`screenshots/`, `tools/seed-screenshots.sql`, `tools/README.md` 참고)
- 개인 메모/AI 프롬프트 등 부적절한 데모 콘텐츠는 모두 정책 안전한 샘플로 교체함

## 5. 다음에 할 일 (어느 컴퓨터든)

1. [x] `monomemo-upload.keystore` 파일 찾기 — 집 PC에서 찾음
2. [x] Play Console 프로덕션 액세스 요건(옵트인/14일) 충족 확인
3. [x] 새 버전(versionCode 5, 1.0.4)을 비공개 테스트에 업로드 → 검토 통과 → 게시됨
4. [x] 서명 키로 AAB 생성 완료 (집 PC)
5. [x] **프로덕션 액세스 2차 신청 → 2026-10-10 반려** (사유: 테스터 실참여 부족, 0번 섹션 참고)
6. [ ] **Play Console > 통계에서 실제 설치·활성 기기 수 확인** — 옵트인 인원 중 몇 명이 진짜로 설치했는지부터 파악
7. [ ] **기존 테스터들에게 "설치 후 며칠 써달라"고 재요청** (Reddit DM/댓글, 구글 그룹 공지 등) — 단순 옵트인이 아니라 실사용이 필요하다는 걸 명확히 전달
8. [ ] 실사용 테스터가 늘어난 게 확인되면(통계로) → 그때 3차 재신청. 반려 사유가 "참여 부족"이라 똑같이 신청만 반복하면 또 반려될 가능성 높음
9. [ ] `monomemo-upload.keystore`를 회사 PC로도 옮기기 (안전한 채널로)
10. [ ] 테스터 피드백 메일 응답 확인

## 6. 참고 — 개발 환경 팁

- SDK 36 필요, AGP 8.7.3 + compileSdk 36은 `gradle.properties`에 `android.suppressUnsupportedCompileSdk=36` 필요 (이미 반영됨)
- 릴리스 AAB는 11MB+ 라 브라우저 자동 업로드(10MB 제한) 불가 → Play Console에 항상 수동 드래그드롭 필요
- adb 연결 끊기면 `adb kill-server && adb start-server` 후 폰에서 USB 디버깅 허용 팝업 재확인
- `local.properties`, `keystore.properties`는 gitignore됨 — 컴퓨터마다 각자 만들어야 함 (내용은 채팅에 붙여넣지 말 것)
- 컴퓨터를 옮겨 작업할 때: 작업 끝나면 반드시 `git add -A && git commit && git push`까지 해야 다른 컴퓨터에서 이어받을 수 있음. 이 문서(`LAUNCH_STATUS.md`)도 같이 갱신해두면 다음 세션이 맥락을 빠르게 따라잡을 수 있음.
