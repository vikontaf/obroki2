# Оброки2

Лёгкое Android-приложение для учёта ежемесячных платежей: отметить оплату → при необходимости клон на следующий месяц (с опциональным остатком к оплате).

**Репозиторий:** [github.com/vikontaf/obroki2](https://github.com/vikontaf/obroki2)

**Автор:** Alexey Firsov

**Стек:** Kotlin, одна Activity, Jetpack Compose, Room, DataStore Preferences. Без сети и аккаунтов.

## Открыть в Android Studio

1. Установите [Android Studio](https://developer.android.com/studio) (Ladybug / 2024.2+ или новее) с Android SDK 35 и JDK 17+.
2. **File → Open** → корень репозитория (папка с `settings.gradle.kts`).
3. Дождитесь синхронизации Gradle.
4. Эмулятор (API 26+) или устройство с USB-отладкой → **Run** ▶ на конфигурацию `app`.

Из командной строки (если SDK настроен):

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

## Release APK для RuStore

Первый upload в [RuStore](https://www.rustore.ru/) удобнее делать **подписанным release APK** (AAB — позже, если понадобится).

**Package name / `applicationId`:** `ru.obroki.app` — при создании приложения в [RuStore Консоль](https://console.rustore.ru/) укажите тот же идентификатор; он должен быть уникален в магазине.

**Версия:** `versionName` `1.0.0`, `versionCode` `1` (каждый апдейт — больший `versionCode`).

### 1. Создать release keystore (один раз, локально)

В корне репозитория (или в безопасном месте вне git):

```bash
keytool -genkey -v -keystore obroki-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias obroki
```

Храните `.jks` и пароли **вне git** (бэкап отдельно). Файлы `*.jks`, `*.keystore` и `keystore.properties` в `.gitignore`.

### 2. Заполнить `keystore.properties`

```bash
cp keystore.properties.example keystore.properties
```

Укажите путь к keystore (относительно корня проекта), alias и пароли. Пример — в `keystore.properties.example`.

### 3. Собрать подписанный APK

```bash
./gradlew.bat :app:assembleRelease
```

APK: `app/build/outputs/apk/release/app-release.apk`. Без `keystore.properties` release-сборка не будет подписана вашим ключом — для RuStore нужен подписанный APK.

Автор: Alexey Firsov. Keystore создаёт и хранит владелец проекта локально; секреты в репозиторий не коммитятся.

## Требования

- minSdk 26, targetSdk / compileSdk 35
- Kotlin 2.0, AGP 8.7
