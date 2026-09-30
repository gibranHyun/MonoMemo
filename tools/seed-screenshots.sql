-- Curated demo notes for store-listing screenshots.
-- Loaded into the debug build's Room DB before capturing screenshots.
-- Safe content only: no real names, no location/PII, no brands,
-- no finance/medical/political content.

PRAGMA foreign_keys=OFF;
DELETE FROM notes;

INSERT INTO notes (id, title, content, titleManuallyEdited, createdAt, updatedAt, deletedAt) VALUES
(1, '배포 체크리스트',
'배포 체크리스트

[x] 유닛 테스트 통과
[x] 버전 태그 v1.2.0
[ ] 릴리스 노트 작성
[ ] 스토어 스크린샷 교체
[ ] AAB 서명 확인
[ ] 배포 후 크래시 모니터링',
1, 1791536400000, 1791622800000, NULL),

(2, 'api 메모',
'api 메모

GET   /v1/notes?limit=20&cursor=
POST  /v1/notes      { title, body }
PATCH /v1/notes/:id  { body }

- 시각 값은 항상 UTC epoch(ms)
- 429 응답은 지수 백오프로 재시도',
1, 1791530000000, 1791619200000, NULL),

(3, '정규식 치트시트',
'정규식 치트시트

\d{4}-\d{2}-\d{2}    날짜
^\s*//               주석 줄
\bTODO\b             할 일 표시
(?<=\()[^)]+(?=\))   괄호 안 내용',
1, 1791520000000, 1791615600000, NULL),

(4, '회의 메모 09-12',
'회의 메모 09-12

- 이번 스프린트 목표: 검색 속도 개선
- 자동 저장 디바운스 800ms -> 500ms 검토
- 다음 회의: 금요일 10:00',
1, 1791510000000, 1791612000000, NULL),

(5, '셸 스니펫',
'셸 스니펫

# 병합된 로컬 브랜치 정리
git branch --merged main | grep -v main | xargs -r git branch -d

# 특정 포트를 쓰는 프로세스
lsof -i :8080',
1, 1791500000000, 1791608400000, NULL);
