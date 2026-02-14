# 모노메모 (MonoMemo) DESIGN

> 목표: “심플한 메모장”처럼 보이지만, Find/Replace UX는 텍스트 에디터 급으로 강력하게.

## 0. 레퍼런스 링크(공식 문서)
- Drawer (Compose): https://developer.android.com/develop/ui/compose/components/drawer
- App bars (Compose): https://developer.android.com/develop/ui/compose/components/app-bars
- Search bar (Compose): https://developer.android.com/develop/ui/compose/components/search-bar
- 모노메모 목업(현재 UI 방향): https://www.genspark.ai/api/files/s/VLVuWmby?cache_control=3600

## 1. 화면 설계

### 1.1 EditorScreen (홈)
#### TopAppBar: Normal Mode
- Navigation icon: ☰ (drawer open)
- Title: 노트 제목 1줄(TextField or BasicTextField, 단 1줄)
- Actions: 🔍, ⋮

#### Body
- “표시 레이어” + “입력 레이어” 2중 구조
  1) 표시 레이어: Text(AnnotatedString)로 하이라이트 렌더링
  2) 입력 레이어: BasicTextField (텍스트는 보이되, 스타일/색상 조정으로 표시 레이어와 겹치게)
- 폰트: D2Coding 기본 적용
- 줄바꿈(wrap) 설정 적용

#### Bottom(선택)
- 아주 작은 상태 텍스트: “저장됨” / “저장 중…” 정도만

### 1.2 EditorScreen: Search Mode (앱바 변신)
#### SearchTopBar 구성
- Leading: X (검색 모드 종료)
- Find 입력: 1줄
- Prev/Next 버튼
- 카운터: n/total
- 옵션 토글:
  - Aa (case sensitive)
  - W (whole word)
- Replace 확장:
  - Replace 입력칸(접힘/펼침)
  - Replace One, Replace All

#### Search Mode UX 규칙
- findQuery 변경 시:
  - matches 재계산(디바운스 150~250ms)
  - currentIndex는 가능한 한 안정적으로 유지
- Prev/Next 시:
  - currentIndex 갱신
  - 현재 매치가 “화면 중앙”으로 오도록 자동 스크롤
- Replace One:
  - 현재 매치 1개 치환
  - matches 재계산
  - 다음 매치로 이동(가능하면)
- Replace All:
  - 적용 전: “총 N건 변경” 안내
  - 적용 후: Snackbar Undo 제공(이전 텍스트 통째로 복구)

### 1.3 DrawerContent (왼쪽)
구역 고정(심플/직관):
1) + 새 노트
2) 노트 목록(최근 수정순, 20개)
3) Divider
4) 휴지통, 설정

### 1.4 TrashScreen
- 상단: “휴지통”
- 안내: “30일 후 자동 삭제”
- 리스트: 제목/삭제일
- 각 아이템: [복원] 버튼
- 오버플로우: “전체 비우기”, “영구 삭제” 등 위험 액션은 숨김

### 1.5 SettingsScreen (최소)
- ThemeMode: System/Light/Dark
- fontSizeSp: Slider or Stepper
- wrapEnabled: Switch

## 2. 상태 머신(에디터)
### 2.1 EditorMode
- Normal
- Search

전이:
- Normal --(tap 🔍)--> Search
- Search --(tap X)--> Normal
- Search 상태에서 Replace/Prev/Next는 Search 유지

## 3. 데이터 모델

### 3.1 Room: NoteEntity
- id: Long (PK)
- title: String
- content: String
- titleManuallyEdited: Boolean
- createdAt: Long
- updatedAt: Long
- deletedAt: Long? (null이면 활성, 값이면 휴지통)

인덱스 권장:
- updatedAt
- deletedAt

### 3.2 DataStore(Preferences)
- lastOpenedNoteId: Long
- themeMode: String (system/light/dark)
- fontSizeSp: Int
- wrapEnabled: Boolean

## 4. 도메인 모델(Find/Replace)
### 4.1 FindOptions
- caseSensitive: Boolean
- wholeWord: Boolean

### 4.2 Match 표현
- IntRange(start, endExclusive) 리스트로 관리

### 4.3 하이라이트 색상
- all match: #FFF4B2
- current match: #FFE070

## 5. 알고리즘 설계

### 5.1 findMatches(text, query, options)
- query가 비어 있으면 matches=[]
- caseSensitive false면 비교용으로 lowerCase 매칭
- wholeWord:
  - “단어 문자” 기준: letter/digit/'_'
  - start-1, end 위치가 단어문자가 아니어야 whole word로 인정
- 매치 수 과다 방지:
  - soft limit (예: 1000) 넘으면 이후는 생략하고 UI에 “매치가 너무 많음” 안내

### 5.2 buildHighlightedAnnotatedString(text, matches, currentIndex)
- 기본 스타일 + 매치 구간에 background 색 적용
- currentIndex 구간은 더 진하게 덮어쓰기

### 5.3 scrollToCurrentMatchCenter(layoutResult, scrollState, matchStart)
- TextLayoutResult로 matchStart의 bounding box 확보
- 해당 y 위치가 화면 중앙 근처가 되도록 scrollTo/animateScrollTo

## 6. 저장/디바운스 정책
- content 변경은 UI 즉시 반영
- DB 저장은 500~1000ms 디바운스
- findQuery는 150~250ms 디바운스(성능 보호)

## 7. 에러/엣지 케이스
- query가 매우 짧고 반복되는 경우 매치 폭발 → soft limit
- replaceQuery가 빈 문자열인 경우(삭제)도 지원
- 한글/특수문자에서도 동작(wholeWord는 ASCII 기반으로 시작, 추후 개선 가능)
- 현재 매치가 사라진 경우 currentIndex 재조정

## 8. UI 체크리스트(완료 기준)
- 앱 실행 → 마지막 노트 열림
- 드로어에서 노트 전환/새 노트
- 🔍로 검색 모드 진입, X로 종료
- 전체 매치 하이라이트 + 현재 매치 강조
- Prev/Next로 이동 시 센터링 스크롤
- Replace One/All + Undo Snackbar
- 휴지통: 삭제/복원/30일 자동 삭제
- 설정: 테마/폰트/줄바꿈 적용
