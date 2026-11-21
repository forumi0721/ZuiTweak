# ZuiTweak

[English Version](README_en.md)

## 개요
**ZuiTweak**은 Lenovo Zui 기반 롬에 여러 가지 트윅을 적용하는 Xposed 모듈입니다.

## 주요 기능
- **공통**
  1. **카메라 무음 설정 활성화:** 기본 카메라의 소리 설정을 통해 무음 동작이 가능하도록 합니다.
  2. **잠금 화면 바로 가기 제거:** 잠금 화면의 하단 좌우의 바로가기를 제거합니다.
  3. **Game Service 아이콘 제거:** 게임 서비스의 불필요한 아이콘을 제거합니다.
  4. **숨겨진 Display 설정 추가:** 비활성화되어 숨겨진 디스플레이 설정에 모든 설정을 추가합니다.
  5. **작업표시줄 비활성화:** 동작탐색 시 작업 표시줄 표시 기능을 제거합니다. (Unfuck Zui 기능)
     * 주의: 3버튼 탐색기 버튼이 사라짐
  6. **알림 아이콘 변경:** 알림 아이콘의 배경을 테마에 맞게 변경합니다. (Unfuck Zui 기능)
  7. **내장 스피커 Doby Atmos 비활성화:** 내장 스피커에 대한 Dolby Atmos 비활성화가 가능하도록 합니다. (Unfuck Zui 기능)
  8. **앱 강제 종료 기능 비활성화:** App Switcher에서 제거시 앱이 강제 종료되지 않도록 합니다. (Unfuck Zui 기능)

- ** ROW **
  1. **Multiple Space 기능 활성화:** ROW 기반 롬에서 Multiple Space 기능을 활성화합니다.
     * 주의: [ZuiTweak-magisk](https://github.com/forumi0721/ZuiTweak-magisk) 설치 필요
  2. **WLAN 핫스팟 기능 활성화:** WLAN 핫스팟 기능을 활성화합니다.
  3. **Smart Split 기능 활성화:** Smart Split 기능을 활성화합니다.
  4. **배터리 전력 소비 경고 활성화:** 배터리 전력 소비 경고 기능을 활성화합니다.

- ** PRC **
  1. **한국어 설정 활성화:** PRC 기반 롬에서 설정 화면에서 한국어 선택이 가능하도록 합니다.
     * 주의: [ZuiTweak-magisk](https://github.com/forumi0721/ZuiTweak-magisk) 또는 CTS를 통한 한글화 필요
  2. **런처 온라인 검색 제거:** PRC 기반 롬에서 런처의 Online 검색 기능을 제거하고 PRC 기반으로 전환합니다.
     * 주의: 좌측 점을 제거하기 위해서는 [Entertainment Space](https://play.google.com/store/search?q=entertainment+space&c=apps&hl=en) 설치 필요
  3. **DocumentUI 강제 종료 수정:** PRC 기반 롬에서 DocumentsUI에서 마우스 드래그 시 발생하는 강제 종료 오류를 수정합니다.
  4. **Smart Rotation 기능 활성화:** Smart Rotation 기능을 활성화합니다.
  5. **Study launcher 비활성화:** Study launcher를 설정화면에서 가립니다.
  6. **PRC 정보 삭제:** 태블릿 정보 등에 보이는 PRC 정보를 제거합니다.
  7. **배터리 아이콘 사라짐 수정:** PRC 기반 롬에서 test_mode 활성화 시 사라지는 배터리 아이콘을 표시합니다.
  8. **Virus Scan 비활성화:** SafeCenter의 Virus Scan 기능을 비활성화 합니다. (Unfuck Zui 기능)
  9. **Package Installer 변경:** Package Installer를 AOSP 스타일로 변경합니다. (Unfuck Zui 기능)
  10. **Permission Controller 변경:** Permission Controller를 AOSP 스타일로 변경합니다. (Unfuck Zui 기능)

- ** TB371FC **
  1. **키보드 단축키 변경:** PRC 기반 롬에서 fn+E, fn+S, fn+P가 정상 동작하도록 변경합니다.

## 빌드

로컬 debug APK는 `./build.sh debug`, 서명된 release APK는 `./build.sh release`로 빌드합니다. Release 서명 정보는 로컬 `~/.bin/KeyStore/`에서 읽습니다. GitHub Actions는 별도 workflow에서 빌드합니다.

## 소스 코드
소스 코드는 GitHub에서 확인할 수 있습니다: [ZuiTweak 소스 코드](https://github.com/forumi0721/ZuiTweak)

## License
이 프로젝트는 Apache License v2.0을 따릅니다. 자세한 내용은 [LICENSE](https://github.com/forumi0721/ZuiTweak/blob/main/LICENSE)를 참고하세요.

## 설치 방법
1. **LSPosed 설치**
   - ZuiTweak을 사용하려면 먼저 LSPosed를 설치해야 합니다. (루팅된 기기에서만 가능)
2. **ZuiTweak 다운로드 및 설치**
   - [Release 페이지](https://github.com/forumi0721/ZuiTweak/releases)에서 최신 버전을 다운로드하여 설치합니다.
3. **LSPosed에서 활성화**
   - LSPosed에서 ZuiTweak 모듈을 활성화합니다.
4. **ZuiTweak에서 설정**
   - Launcher에 보이는 ZuiTweak을 실행하여 필요한 기능을 활성화합니다.

## 중요 사항
- 무음 기능 활성화는 법적 규제를 따르는 범위 내에서 사용해야 합니다.
- 무수한 오류가 숨겨져 있습니다.

## 문의
앱 사용 중 문제가 발생하거나 문의사항이 있으면 [Issues 페이지](https://github.com/forumi0721/ZuiTweak/issues)를 통해 알려주세요.

