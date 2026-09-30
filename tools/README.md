# tools/

## 스토어 스크린샷 재생성

`screenshots/*.png`는 Play 스토어 등록용 이미지다. 아래 절차로 다시 만든다.

### 1. 디버그 빌드 설치 + 데모 데이터 시드

```bash
./gradlew :app:installDebug
adb shell am start -n com.monomemo.app/.MainActivity   # 최초 실행 → Room DB 생성
adb shell am force-stop com.monomemo.app
```

`sqlite3`가 기기에 없고 프로덕션 이미지라 `adb root`가 안 되므로,
앱 샌드박스의 DB를 `run-as`로 꺼내 로컬에서 수정 후 되돌린다.

```bash
adb exec-out run-as com.monomemo.app cat databases/monomemo.db > monomemo.db
python - <<'PY'
import sqlite3
c = sqlite3.connect("monomemo.db")
c.executescript(open("tools/seed-screenshots.sql", encoding="utf-8").read())
c.commit(); c.execute("PRAGMA wal_checkpoint(TRUNCATE)"); c.commit(); c.close()
PY
adb push monomemo.db /data/local/tmp/seed.db
adb shell "run-as com.monomemo.app sh -c 'cp /data/local/tmp/seed.db /data/data/com.monomemo.app/databases/monomemo.db && rm -f /data/data/com.monomemo.app/databases/monomemo.db-wal /data/data/com.monomemo.app/databases/monomemo.db-shm'"
adb shell rm /data/local/tmp/seed.db
```

### 2. 상태바 정리 (데모 모드)

```bash
adb shell settings put global sysui_demo_allowed 1
adb shell am broadcast -a com.android.systemui.demo -e command enter
adb shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 0930
adb shell am broadcast -a com.android.systemui.demo -e command battery -e level 82 -e plugged false
adb shell am broadcast -a com.android.systemui.demo -e command network -e wifi show -e level 4 -e fully true
adb shell am broadcast -a com.android.systemui.demo -e command network -e mobile hide
adb shell am broadcast -a com.android.systemui.demo -e command notifications -e visible false
# 끝나면: adb shell am broadcast -a com.android.systemui.demo -e command exit
```

### 3. 캡처

- `editor.png` — 앱 실행 시 열리는 노트(배포 체크리스트). 설정에서 줄 번호 ON.
- `drawer.png` — 햄버거 메뉴로 서랍 열기.
- `settings.png` — 서랍 → 설정.
- `editor_dark.png` / `settings_dark.png` — `adb shell cmd uimode night yes` 후 동일 화면.

```bash
adb exec-out screencap -p > screenshots/editor.png   # Windows PowerShell은 cmd /c 로 리다이렉트할 것
```

> 주의: 시드 콘텐츠에는 실명·위치·브랜드·금융/의료/정치 소재를 넣지 않는다.
