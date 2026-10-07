# MonoMemo 출시 진행 상황

> 최종 갱신: 2026-10-08 (집 PC, `E:\workspace\MonoMemo`)
> 목적: Google Play 비공개 테스트 → 프로덕션 출시 진행 상황 기록 (다른 컴퓨터/다음 세션 이어가기용)

---

## 0. 2026-10-08 집 PC에서 처리한 것 (회사 PC에서 이어받을 때 참고)

1. **`monomemo-upload.keystore` 찾음** — 회사 PC에는 없었지만 **집 PC 프로젝트 루트(`E:\workspace\MonoMemo\monomemo-upload.keystore`)에 계속 있었음**. `keystore.properties`의 경로·비밀번호와 일치 확인함.
   - ⚠️ **회사 PC에는 아직 이 파일이 없음.** 서명 빌드를 회사에서도 하려면 이 keystore 파일을 비밀번호 관리자 첨부파일/암호 zip+개인 클라우드/USB 중 하나로 직접 옮길 것 (git에는 절대 올리지 않음, `.gitignore`됨).
2. **서명된 AAB 빌드 완료** — `./gradlew :app:bundleRelease` 성공, `app/build/outputs/bundle/release/app-release.aab` (versionCode 5, 1.0.4). 경고 2건(proguard 매핑/네이티브 심볼 파일 미업로드)은 출시를 막지 않는 권장사항이라 무시하고 진행함.
3. **Play Console 프로덕션 액세스 요건 전부 충족 확인** — 12명 이상 옵트인 + 14일 이상 비공개 테스트, 체크리스트 3개 전부 녹색.
4. **버전 5(1.0.4)를 비공개 테스트(Alpha)에 업로드·제출 완료 → 검토 통과 → 게시됨** (Play Console 알림 "앱 업데이트가 게시되었습니다", 10월 8일자로 확인). 릴리스 노트는 섹션 1의 "릴리스 노트 초안"을 그대로 사용함.

## 1. 지금 당장 막혀 있는 것 — 서명 키 파일 (회사 PC 기준, 2026-10-07 작성분, 참고용)

- `keystore.properties`는 회사 컴퓨터에 있음 (storeFile, storePassword, keyAlias, keyPassword 4줄, gitignore됨, 커밋 안 함).
- `monomemo-upload.keystore` 파일 자체는 **집 PC에 있었음** (위 0번 항목 참고). 회사 PC로 옮기면 양쪽에서 서명 빌드 가능.
- 참고: Play Console 설정 > 앱 무결성 > 앱 서명에서 "Google Play 앱 서명"으로 등록되어 있다면, 이 업로드 키를 완전히 분실해도 업로드 키 재설정으로 복구 가능 (아직 미확인).

## 1. 빌드/버전 현황 (이 컴퓨터 기준, 2026-10-07)

- **repo 버전**: versionCode 5, versionName "1.0.4" (`app/build.gradle.kts`)
- **targetSdk / compileSdk**: 36
- **패키지**: com.monomemo.app
- **Play Console에 실제 배포된 마지막 버전**: versionCode 3 (1.0.2), 사용자가 확인한 값 (2026-09-30 기준)
  - 이후 9월 23일에 한 번 더 업데이트를 올렸다고 들었으나, 그 버전의 실제 versionCode는 Play Console에서 재확인 필요
- **AAB는 아직 못 만듦** (0번 항목 참고)

### git 상태 (이 컴퓨터, origin/main과 동기화됨)
```
a2b1680 Merge remote-tracking branch 'origin/main'
59bf800 버전 4 (1.0.3)로 업데이트
63f58dd 검색 옵션 토글에 접근성 설명 및 on/off 상태 노출
b507f7e 아이콘 전용 버튼에 접근성 설명 추가
9f31f86 줄 번호 렌더링을 Text 1개로 합쳐 컴포저블 폭증 방지
cfdf809 찾기 연산을 메인 스레드 밖에서 실행
07301bb 백그라운드 전환 시 편집 내용 즉시 저장
48ce259 찾기/바꾸기: 오래된 매치 범위로 인한 크래시 방지
5eac5d9 스토어 스크린샷 교체 및 재생성 스크립트 추가
8b8a011 v1.0.2: 에디터 키보드 버그 수정 및 UX 개선, targetSdk 36 대응  (집 PC에서 작업한 커밋)
73997c3 출시 준비: release 서명 설정, 개인정보처리방침, 스토어 그래픽 에셋 추가
```
`8b8a011`(집 PC, v1.0.2)과 이 세션의 안정성 수정 6건이 `app/build.gradle.kts`, `EditorScreen.kt`에서 충돌해 머지했음 — 양쪽 수정 모두 반영됨 (줄 간격/자간 스타일 + 줄 번호 Text 1개 최적화, targetSdk 36 + stale-match 가드 등). 에뮬레이터에서 정상 동작 확인함.

### 이번 세션(회사 PC, 2026-09-30~10-07)에서 고친 것 — 안정성/접근성 6건
1. 찾기/바꾸기 중 디바운스 전에 치환을 누르면 발생하던 크래시 수정 (stale match 범위 가드)
2. 백그라운드 전환(ON_STOP) 시 편집 내용 즉시 저장 — `onCleared()`의 flushSave가 실제로는 동작 안 했던 문제
3. 찾기 연산을 메인 스레드 밖(Dispatchers.Default)으로 이동 — 긴 노트에서 ANR 위험 완화
4. 줄 번호 렌더링을 줄마다 Text 생성 → Text 1개로 합쳐 컴포저블 수 O(1)로
5. Undo/Redo, 휴지통 복원/삭제 버튼에 접근성 설명(contentDescription) 추가
6. 찾기 옵션(대소문자/전체단어) 토글에 접근성 설명 + on/off 상태 노출

### 집 PC(8b8a011, v1.0.2)에서 고친 것 (이미 origin에 있던 것, 참고용)
- 에디터 키보드 안 뜨던 버그 (`fillMaxHeight` → `defaultMinSize(minHeight=availableHeight)`)
- 버전 표기 3곳 불일치 → `BuildConfig.VERSION_NAME`으로 일원화
- 설정 화면 License/개인정보처리방침 링크 활성화
- 휴지통 영구삭제·전체비우기 확인 다이얼로그 추가
- 치환/실행취소 후 커서 위치 보존 (TextRange 명시)
- D2Coding 자간 축소로 세로로 길어 보이는 문제 완화
- 스토어 피처 그래픽 교체

### 릴리스 노트 초안 (이번 세션 수정분 기준, 아직 미사용)
> - 찾기/바꾸기 도중 빠르게 편집한 직후 치환을 누르면 발생할 수 있던 크래시를 수정했습니다.
> - 앱을 백그라운드로 전환할 때 편집 중이던 내용이 즉시 저장되어, 최근 메모가 사라지는 문제를 막았습니다.
> - 긴 노트에서 찾기 기능이 버벅이지 않도록 검색 연산을 최적화했습니다.
> - 긴 노트에서 줄 번호 표시로 인한 지연을 개선했습니다.
> - 스크린리더 사용자를 위해 되돌리기/다시 실행, 휴지통 복원/삭제, 찾기 옵션 버튼에 설명을 추가했습니다.

## 2. Play Console 상태 (마지막 확인 2026-09-30, 갱신 필요)

- **개발자 계정**: entropy.gibran@gmail.com (Entropy by Gibran, 개인 계정)
- **프로덕션 액세스**: 2026-09-19에 "추가 테스트 필요"로 1차 반려됨
- **현재 비공개 테스트 재진행 중**: 12명 이상 테스터로 14일 연속 유지, 9/30 기준 약 11일차 (10/3 전후 14일 충족 예상 — **이 컴퓨터에서는 그 이후 상황 확인 안 됨**)
- **테스터들에게 피드백 요청 메일 발송함** (9/30 기준) — 응답 확인 안 됨
- 1차 신청서에 넣은 "비공개 테스트 참여도" 답변은 Reddit(r/AndroidAppTesters) 테스터 2명이 보고한 온보딩 버그(그룹 권한 오류, 설치 버튼 무반응)를 근거로 작성해 제출했음

### 테스터 그룹
- 그룹 가입: https://groups.google.com/g/monomemo-testers (전체 공개로 설정됨)
- 옵트인: https://play.google.com/apps/testing/com.monomemo.app
- 2026-09-11 확인 시 그룹 회원 15명, 대기 초대 1명(leehyoinn77)

### 스토어 등록정보 (완료)
- 휴대전화 스크린샷 5장 전부 1080×2160(2:1)으로 재촬영·교체 완료, Play Console에 업로드·반영 확인함 (`screenshots/`, `tools/seed-screenshots.sql`, `tools/README.md` 참고)
- 개인 메모/AI 프롬프트 등 부적절한 데모 콘텐츠는 모두 정책 안전한 샘플로 교체함

## 3. 다음 세션(회사 PC)에서 할 일

1. [x] `monomemo-upload.keystore` 파일 찾기 — 집 PC에서 찾음 (0번 항목 참고)
2. [x] Play Console 프로덕션 액세스 요건(14일) 충족 확인
3. [ ] 테스터 피드백 메일 응답 확인
4. [x] 새 버전(versionCode 5, 1.0.4)을 비공개 테스트에 업로드 → 검토 통과 → 게시됨 (2026-10-08)
5. [x] 서명 키로 AAB 생성 완료 (집 PC)
6. [ ] **`monomemo-upload.keystore`를 회사 PC로도 옮기기** (안전한 채널로) — 회사에서도 서명 빌드 하려면 필요
7. [ ] 1.0.4 설치해서 안정성 개선 6건이 실제로 잘 동작하는지 며칠 지켜보기
8. [ ] 안정화 확인되면 → **Play Console에서 프로덕션 액세스 재신청** (1차는 "추가 테스트 필요"로 반려됐었음 — 이번엔 요건 충족 + 안정성 개선판이라 통과 가능성 높음)

## 4. 참고 — 개발 환경 팁

- SDK 36 필요, AGP 8.7.3 + compileSdk 36은 `gradle.properties`에 `android.suppressUnsupportedCompileSdk=36` 필요 (이미 반영됨)
- 릴리스 AAB는 11MB+ 라 브라우저 자동 업로드(10MB 제한) 불가 → Play Console에 항상 수동 드래그드롭 필요
- adb 연결 끊기면 `adb kill-server && adb start-server` 후 폰에서 USB 디버깅 허용 팝업 재확인
- `local.properties`, `keystore.properties`는 gitignore됨 — 컴퓨터마다 각자 만들어야 함 (내용은 채팅에 붙여넣지 말 것)
