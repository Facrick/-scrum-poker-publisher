# Сборка Scrum Poker в EXE

## Что получится

Основной вариант:

```text
dist/ScrumPoker/ScrumPoker.exe
```

Это portable app-image от `jpackage`. Важно: это не один-единственный файл, а папка приложения с `.exe` и встроенным Java runtime. Пользователь запускает именно `ScrumPoker.exe`.

## Требования

На компьютере сборки должны быть установлены:

- JDK 17 или JDK 21;
- Maven;
- Node.js;
- npm.

Проверка:

```powershell
java -version
mvn -version
node -v
npm -v
jpackage --version
```

## Сборка EXE

Из корня проекта:

```powershell
.\build-exe.ps1
```

Результат:

```text
dist/ScrumPoker/ScrumPoker.exe
```

После запуска открыть:

```text
http://localhost:8080
```

## Сборка одного JAR

```powershell
.\build-jar.ps1
```

Запуск:

```powershell
java -jar backend\target\scrum-poker-backend-1.0.0.jar
```

## Installer EXE

Для installer `.exe` нужен WiX Toolset.

```powershell
.\build-installer-exe.ps1
```

Результат будет в:

```text
dist-installer/
```

## Что изменено в проекте

- React build копируется в `backend/src/main/resources/static`;
- Spring Boot отдает frontend сам;
- API работает на том же host;
- WebSocket URL вычисляется автоматически;
- React Router работает через `FrontendForwardController`.
