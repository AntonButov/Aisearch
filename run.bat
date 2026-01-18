@echo off
REM Скрипт для запуска AI Search приложения локально (Windows)

echo 🚀 Запуск AI Search приложения...

REM Проверка наличия Java
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Ошибка: Java не найдена. Установите JDK 17 или выше.
    exit /b 1
)

echo ✅ Java найдена
echo 📦 Запуск development сервера...
echo 🌐 Приложение будет доступно по адресу: http://localhost:8080
echo.
echo Для остановки нажмите Ctrl+C
echo.

gradlew.bat composeApp:wasmJsBrowserDevelopmentRun
