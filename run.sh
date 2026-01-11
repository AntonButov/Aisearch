#!/bin/bash

# Скрипт для запуска AI Search приложения локально

set -e

echo "🚀 Запуск AI Search приложения..."

# Проверка наличия JDK
if ! command -v java &> /dev/null; then
    echo "❌ Ошибка: Java не найдена. Установите JDK 17 или выше."
    exit 1
fi

# Проверка версии Java
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Ошибка: Требуется JDK 17 или выше. Текущая версия: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java версия: $(java -version 2>&1 | head -n 1)"

# Запуск dev сервера
echo "📦 Запуск development сервера..."
echo "🌐 Приложение будет доступно по адресу: http://localhost:8080"
echo ""
echo "Для остановки нажмите Ctrl+C"
echo ""

./gradlew composeApp:wasmJsBrowserDevelopmentRun
