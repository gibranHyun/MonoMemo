# 모노메모 (MonoMemo) TASKS

> 규칙:
> - 한 번에 하나의 태스크만 구현한다.
> - 각 태스크는 "빌드 성공 + DoD 충족" 후 커밋한다.
> - 태스크 완료 시 체크박스를 체크하고, 변경 파일 목록을 기록한다.

---

## Phase 0 — Repo / 프로젝트 준비

- [x] T0. 프로젝트 생성: Compose(Material3) 앱 스켈레톤
  - DoD: 앱 실행 OK, 빈 화면 표시
  - Files: app/*, MainActivity, theme, MonoMemoApp.kt
  - Notes: 최소 패키지 구조(ui/data/domain)만 잡기, 수동 DI용 Application 클래스 포함

- [x] T1. 기본 Scaffold + Drawer shell 구성
  - DoD: ☰ 누르면 drawer open/close, 메인 영역에 Placeholder
  - Files: ui/AppScaffold.kt, ui/DrawerContent.kt, MainActivity.kt

---

## Phase 1 — Room(노트/휴지통 필드 포함)

- [x] T2. NoteEntity/Dao/Database 추가
  - DoD: Room 마이그레이션 없이 동작(버전 1), insert/update/select 가능
  - Files: data/db/NoteEntity.kt, NoteDao.kt, AppDatabase.kt, MonoMemoApp.kt

- [x] T3. NoteRepository 추가
  - DoD: repo API로 활성 노트 리스트/휴지통 리스트/노트 단건 조회 가능
  - Files: data/NoteRepository.kt, MonoMemoApp.kt

- [x] T4. 앱 시작 시 기본 노트 1개 생성(데모)
  - DoD: 실행하면 노트가 비어있지 않음(최초 1개)
  - Files: ui/AppViewModel.kt, ui/AppScaffold.kt

---

## Phase 2 — DataStore(설정 + lastOpenedNoteId)

- [x] T5. Preferences DataStore 추가
  - Keys: lastOpenedNoteId, themeMode, fontSizeSp, wrapEnabled
  - DoD: 읽기/쓰기 동작 확인(로그)
  - Files: data/settings/SettingsDataStore.kt, MonoMemoApp.kt

- [x] T6. lastOpenedNoteId 기반으로 "마지막 노트 자동 열기"
  - DoD: 노트 전환 후 앱 재실행 시 마지막 노트가 열린다
  - Files: ui/AppViewModel.kt, ui/AppScaffold.kt

---

## Phase 3 — Editor 기본(제목/본문/저장 디바운스)

- [x] T7. EditorScreen (Normal AppBar + Body BasicTextField)
  - DoD: 제목 1줄, 본문 입력 가능
  - Files: ui/editor/EditorScreen.kt, ui/editor/EditorViewModel.kt, ui/AppScaffold.kt

- [x] T8. 자동 저장 디바운스(500~1000ms) + updatedAt 갱신
  - DoD: 타이핑 후 DB에 반영(지연 저장), UI는 즉시 반영
  - Files: ui/editor/EditorViewModel.kt

- [x] T9. 제목 자동 생성 규칙 A 구현
  - DoD:
    - 제목 수동 편집 전: 본문 첫 의미 줄로 자동 생성
    - 제목 수동 편집 후: 자동 갱신 중단
  - Files: ui/editor/EditorViewModel.kt

---

## Phase 4 — Drawer에 노트 리스트 붙이기(목록 화면 최소화)

- [x] T10. Drawer에 "+ 새 노트", 노트 목록(최근 수정순) 표시
  - DoD: 드로어에서 노트 선택하면 즉시 전환
  - Files: ui/DrawerContent.kt, ui/AppViewModel.kt, ui/AppScaffold.kt

- [x] T11. 노트 삭제 → 휴지통 이동(deletedAt set)
  - DoD: 삭제하면 활성 목록에서 사라지고 휴지통에 나타남
  - Files: ui/AppViewModel.kt, ui/AppScaffold.kt (⋮ 메뉴)

---

## Phase 5 — 휴지통 화면 + 30일 정리

- [x] T12. TrashScreen 구현(복원/영구삭제)
  - DoD: 복원하면 활성으로 돌아온다
  - Files: ui/trash/TrashScreen.kt, ui/AppViewModel.kt, ui/AppScaffold.kt

- [x] T13. 앱 시작 시 30일 지난 deletedAt 영구 삭제(런치 1회)
  - DoD: 기한 지난 노트가 자동 정리됨
  - Files: ui/AppViewModel.kt (cleanupOldTrash)

---

## Phase 6 — Search Mode + Find/Replace 엔진(정규식 없음)

- [x] T14. EditorMode( Normal/Search ) + AppBar 전환
  - DoD: 🔍 진입, X 종료
  - Files: ui/editor/EditorViewModel.kt, ui/editor/SearchTopBar.kt, ui/AppScaffold.kt

- [x] T15. FindOptions + findMatches 엔진 구현
  - DoD: caseSensitive/wholeWord 옵션으로 matches 계산
  - Files: domain/find/FindOptions.kt, FindEngine.kt, FindResult.kt

- [x] T16. n/total 표시 + Prev/Next 이동
  - DoD: Prev/Next 누르면 currentIndex가 바뀌고 카운터 갱신
  - Files: ui/editor/EditorViewModel.kt, ui/editor/SearchTopBar.kt

---

## Phase 7 — 전체 하이라이트 + 현재 매치 강조 + 센터링 스크롤

- [x] T17. AnnotatedString 하이라이트 렌더링(전체/현재)
  - Colors: all #FFF4B2, current #FFE070
  - DoD: 검색어 입력 시 전체 하이라이트, current가 더 진하게 표시
  - Files: ui/editor/EditorScreen.kt (HighlightVisualTransformation)

- [x] T18. 현재 매치 센터링 자동 스크롤
  - DoD: Prev/Next 시 현재 매치가 화면 중앙 근처로 이동
  - Files: ui/editor/EditorScreen.kt (LaunchedEffect + scrollState)

- [x] T19. 매치 폭발 soft limit(예: 1000) + 안내 ★ Phase 6으로 이동
  - DoD: 너무 많은 매치에서 프리즈 없이 동작 + "너무 많음" 안내
  - Files: domain/find/FindEngine.kt, ui/editor/SearchTopBar.kt

---

## Phase 8 — Replace One / Replace All + Undo Snackbar

- [x] T20. Replace One 구현
  - DoD: current match 1개만 치환되고 다음 매치로 이동
  - Files: ui/editor/EditorViewModel.kt

- [x] T21. Replace All 구현(실행 전/후 안내)
  - DoD: N건 치환, matches 재계산
  - Files: ui/editor/EditorViewModel.kt

- [x] T22. Replace All Undo(Snackbar 5~8초) 구현
  - DoD: Undo 누르면 이전 텍스트로 완전 복구
  - Files: ui/editor/EditorViewModel.kt, ui/AppScaffold.kt

---

## Phase 9 — 폰트(D2Coding) + 설정 적용 + 라이선스

- [x] T23. D2Coding 폰트 리소스 추가 및 Typography 적용
  - DoD: 에디터에서 D2Coding 적용 확인
  - Files: res/font/d2coding.ttf, d2coding_bold.ttf, ui/theme/Type.kt, ui/editor/EditorScreen.kt

- [x] T24. SettingsScreen 구현(테마/폰트 크기/줄바꿈)
  - DoD: 설정 변경이 즉시 에디터에 반영되고 재실행 후 유지
  - Files: ui/settings/SettingsScreen.kt, ui/AppScaffold.kt

- [x] T25. 정보/라이선스 화면(폰트 라이선스 포함)
  - DoD: ⋮ 메뉴에서 진입 가능
  - Files: ui/about/AboutScreen.kt, ui/AppScaffold.kt

---

## Phase 10 — (선택) CI: GitHub Actions로 AAB 빌드 자동화

- [x] T26. GitHub Actions: PR 빌드/테스트 워크플로우
  - DoD: PR 생성 시 gradle build/test 실행, 성공/실패 표시
  - Files: .github/workflows/android-ci.yml

- [x] T27. GitHub Actions: main에서 AAB artifact 생성(bundleRelease)
  - DoD: AAB가 Actions artifact로 업로드됨
  - Files: .github/workflows/android-release.yml

---

## 완료 정의(Definition of Done)
- PRD의 v1.0 핵심 기능이 모두 구현됨
- 앱이 켜지면 마지막 노트 즉시 열림
- 드로어에서 새 노트/전환/휴지통/설정 접근 가능
- 검색 모드에서 전체 매치 하이라이트 + 현재 매치 센터링
- Replace All 후 Undo 스낵바로 완전 복구
- 휴지통 30일 자동 삭제(런치 1회 정리)
- D2Coding 적용 및 라이선스 표기
