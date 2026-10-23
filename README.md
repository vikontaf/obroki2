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

## Требования

- minSdk 26, targetSdk / compileSdk 35
- Kotlin 2.0, AGP 8.7
