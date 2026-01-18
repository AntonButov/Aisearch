#!/bin/bash

# Расширенный скрипт для запуска AI Search приложения локально с опциями

set -e

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Функция для вывода сообщений
info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

success() {
    echo -e "${GREEN}✅${NC} $1"
}

error() {
    echo -e "${RED}❌${NC} $1"
}

warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# Парсинг аргументов
MODE="development"
PORT="8080"
CLEAN=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --production|-p)
            MODE="production"
            shift
            ;;
        --port)
            PORT="$2"
            shift 2
            ;;
        --clean|-c)
            CLEAN=true
            shift
            ;;
        --help|-h)
            echo "Использование: $0 [опции]"
            echo ""
            echo "Опции:"
            echo "  --production, -p    Запустить production сборку"
            echo "  --port PORT         Указать порт (по умолчанию: 8080)"
            echo "  --clean, -c          Очистить build перед запуском"
            echo "  --help, -h           Показать эту справку"
            echo ""
            exit 0
            ;;
        *)
            error "Неизвестная опция: $1"
            echo "Используйте --help для справки"
            exit 1
            ;;
    esac
done

info "🚀 Запуск AI Search приложения..."

# Проверка наличия JDK
if ! command -v java &> /dev/null; then
    error "Java не найдена. Установите JDK 17 или выше."
    exit 1
fi

# Проверка версии Java
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    error "Требуется JDK 17 или выше. Текущая версия: $JAVA_VERSION"
    exit 1
fi

success "Java версия: $(java -version 2>&1 | head -n 1 | cut -d'"' -f2)"

# Очистка build если нужно
if [ "$CLEAN" = true ]; then
    info "🧹 Очистка build директории..."
    ./gradlew clean
    success "Очистка завершена"
fi

# Запуск сервера
if [ "$MODE" = "production" ]; then
    info "📦 Запуск production сервера..."
    TASK="composeApp:wasmJsBrowserProductionRun"
else
    info "📦 Запуск development сервера..."
    TASK="composeApp:wasmJsBrowserDevelopmentRun"
fi

echo ""
success "🌐 Приложение будет доступно по адресу: http://localhost:$PORT"
warning "Для остановки нажмите Ctrl+C"
echo ""

./gradlew "$TASK"
