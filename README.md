# ADB 토글

홈 화면 위젯을 한 번 눌러 USB 디버깅을 켜고 끄는 개인용 안드로이드 앱. root 없이 동작한다.

## 동작 방식

USB 디버깅 스위치는 `Settings.Global.adb_enabled` 값이다. 이 값을 쓰려면 `WRITE_SECURE_SETTINGS` 권한이 필요하고, 이 권한은 adb로만 부여할 수 있다. 처음 한 번만 부여하면 재부팅이나 앱 업데이트 뒤에도 유지되고, 앱을 삭제하면 사라진다.

| 파일 | 역할 |
| --- | --- |
| `UsbDebugging.kt` | 상태 읽기·쓰기와 권한 확인. 켤 때 개발자 옵션이 꺼져 있으면 함께 켠다 |
| `UsbDebugWidget.kt` | 1×1 위젯. 누르면 토글하고, 권한이 없으면 설정 화면을 연다 |
| `UsbDebugWatcher.kt` | 설정 앱 등 위젯 밖에서 바꿔도 위젯이 바로 따라가게 하는 감시 잡. 상주 서비스 대신 JobScheduler 콘텐츠 트리거를 쓴다 |
| `MainActivity.kt` | 권한 안내(명령어 복사), 스위치, 위젯 추가 버튼. PC에서 권한을 부여하면 화면이 저절로 바뀐다 |

## 빌드와 설치

JDK 17, Android SDK(platform 36)가 필요하다. 릴리스 빌드는 디버그 키로 서명한다.

```sh
./gradlew assembleRelease
adb install -r app/build/outputs/apk/release/app-release.apk
adb shell pm grant dev.ren.adbtoggle android.permission.WRITE_SECURE_SETTINGS
```

그다음 홈 화면을 길게 눌러 위젯 → ADB 토글을 추가하거나, 앱의 "홈 화면에 위젯 추가" 버튼을 누른다.

## 주의

- USB 디버깅을 끄면 PC와의 adb 연결도 바로 끊긴다. 다시 켤 때는 휴대폰에서 켠다.
- 다른 PC나 키로 빌드한 APK는 서명이 달라 덮어쓸 수 없다. 지우고 다시 설치한 뒤 권한을 다시 부여해야 한다.
