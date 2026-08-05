# MonoMemo 출시 진행 상황

> 최종 갱신: 2026-08-05
> 목적: Google Play 비공개 테스트 → 프로덕션 출시 진행 상황 기록 (다음 세션 이어가기용)

---

## 1. 빌드/버전 현황

- **현재 버전**: v1.0.2 (versionCode 3)
- **targetSdk / compileSdk**: 36 (Android 16) — 2026-08-31 API 수준 마감 대응 완료
- **패키지**: com.monomemo.app
- **서명**: `monomemo-upload.keystore` + `keystore.properties` (둘 다 gitignore, 절대 커밋 금지)
- **배포용 AAB**: `app/build/outputs/bundle/release/app-release.aab`

### 이번 세션에서 고친 것 (v1.0.2)
- 🐛 에디터 키보드 안 뜨던 버그 (`fillMaxHeight` → `defaultMinSize(minHeight=availableHeight)`)
- 🔧 버전 표기 3곳 불일치 → `BuildConfig.VERSION_NAME`으로 일원화
- 🔧 설정 화면 License/개인정보처리방침 링크 죽어있던 것 → `LocalUriHandler`로 실제 동작
- 🔧 휴지통 영구삭제·전체비우기 확인 다이얼로그 없던 것 → `AlertDialog` 추가
- 🔧 치환/실행취소 후 커서가 맨 앞으로 튀던 것 → `TextRange` 명시로 커서 위치 보존
- 🎨 D2Coding 본문 글꼴이 세로로 길어 보이는 문제 → 자간(letterSpacing) -5% 축소로 완화 (줄간격은 이미 한계라 큰 효과 없었음)
- 🖼️ 스토어 피처 그래픽에 태그라인 텍스트 있던 것 → 아이콘+이름만 있는 클린 버전으로 교체

## 2. Play Console 상태

- **비공개 테스트 트랙**: Alpha
- **v1.0.2 + 새 피처 그래픽**: 2026-08-02 "검토를 위해 제출" 완료 → 검토 중
- **프로덕션 액세스 요건**: 12명 이상 테스터 옵트인 + 14일 연속 유지
- **현재 옵트인 인원**: 4명 (2026-08-02 기준, 이후 갱신 필요)
- **개발자 계정**: entropy.gibran@gmail.com (Entropy by Gibran, 개인 계정)

## 3. Reddit 테스터 모집

### 게시글
- r/GooglePlayClosedTest: https://www.reddit.com/r/GooglePlayClosedTest/comments/1vch2hj/
- r/AndroidAppTesters: https://www.reddit.com/r/AndroidAppTesters/comments/1vch302/

### 테스터 그룹 (모두 웹 공개로 설정 완료)
- 그룹 가입: https://groups.google.com/g/monomemo-testers
- 옵트인: https://play.google.com/apps/testing/com.monomemo.app

### 상호테스트 진행 상황

| 상대 | 앱 | 상태 | 비고 |
|---|---|---|---|
| Blobby72 | DMR Launcher (런처) | 🔜 대기 | 상대 그룹도 members-only 오류 있어서 영문으로 해결법 안내 댓글 남김. 상대가 고치면 재시도 필요 |
| Lanky-Bed2154 | Chutebol (축구 게임) | ✅ 완료 | 설치·테스트·피드백 댓글 게시 완료 |
| ramonfsk_ | SipPace (음주 페이스 앱, 성인 인증 필요) | ⏳ 보류 | 사용법 설명 부족하다고 피드백만 남김, 옵트인은 아직 안 함 (내용 이해 후 판단) |
| IceTrue7012 | Color Sudoku + Alarminator (2개 요구) | ✅ 완료 | 둘 다 설치·테스트·피드백 댓글 게시 완료 (아래 버그 발견) |

### 설치된 상호테스트 앱 (14일 유지 필요 — 삭제 금지)

| 앱 | 패키지명 | 설치일(추정) | 삭제 가능일 |
|---|---|---|---|
| Chutebol | com.gunnarpolte.chutebol | 2026-08-04 | 2026-08-18 이후 |
| Color Sudoku | com.deepdyno.colorsudoku | 2026-08-04 | 2026-08-18 이후 |
| Alarminator | com.alarminator | 2026-08-04 | 2026-08-18 이후 |

> ⚠️ 정확한 `firstInstallTime`은 `adb shell dumpsys package <pkg> | grep firstInstallTime`으로 재확인 필요 (다음 세션에서 폰 연결 시)

### 테스트 중 발견한 버그 (상대 개발자에게 피드백 완료)
- **Alarminator**: "+"(알람 추가) FAB가 제스처 내비게이션 바에 가까이 붙어있어 상단 일부만 터치됨 → 인셋 패딩 조정 제안함
- **Color Sudoku**: 버그는 아니지만 색맹 접근성(색 구분 지역에 패턴/텍스처 추가) 질문 남김

## 4. 다음 세션에서 할 일

1. [ ] 폰 재연결 후 상호테스트 앱들 정확한 설치일 재확인
2. [ ] Blobby72(DMR Launcher) 그룹 수정 여부 확인 → 수정됐으면 가입·옵트인·설치 진행
3. [ ] ramonfsk_(SipPace) 답변 왔는지 확인 → 사용법 이해되면 옵트인 여부 결정
4. [ ] Play Console 옵트인 인원수 갱신 확인 (목표: 12명 이상, 여유 있게 14~15명 권장)
5. [ ] 14일 경과(2026-08-18) 후 상호테스트 앱들 정리(삭제) 가능
6. [ ] 12명·14일 요건 충족 시 → 프로덕션 액세스 신청

## 5. 참고 — 개발 환경 팁

- SDK 36 로컬 설치됨, AGP 8.7.3 + compileSdk 36은 `gradle.properties`에 `android.suppressUnsupportedCompileSdk=36` 필요
- 릴리스 AAB는 11MB+ 라 브라우저 자동 업로드(10MB 제한) 불가 → Play Console에 항상 수동 드래그드롭 필요
- adb 연결 끊기면 `adb kill-server && adb start-server` 후 폰에서 USB 디버깅 허용 팝업 재확인
