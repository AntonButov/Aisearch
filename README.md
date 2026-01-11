# AI Search - RAG Demo with AnythingLLM

MVP проект для демонстрации возможностей RAG (Retrieval-Augmented Generation) с AnythingLLM. Приложение работает исключительно через **Kotlin/Wasm** и использует Compose Multiplatform для веб-интерфейса.

## О проекте

Это минимально жизнеспособный продукт (MVP), демонстрирующий интеграцию с AnythingLLM для реализации RAG-функциональности. Проект использует современные технологии:

- **Kotlin Multiplatform** - для кроссплатформенной разработки
- **Compose Multiplatform** - для декларативного UI
- **Kotlin/Wasm** - для выполнения в браузере (только wasmJs таргет)
- **AnythingLLM** - для RAG-функциональности

## Требования

- JDK 17 или выше
- Современный браузер с поддержкой WebAssembly
- Gradle 8.0+

## Сборка и запуск

### Разработка

Приложение автоматически откроется в браузере по адресу `http://localhost:8080`

### Продакшн сборка

Для создания продакшн сборки:

Собранные файлы будут находиться в `composeApp/build/dist/wasmJs/productionExecutable/`

## Структура проекта

```
composeApp/
├── src/
│   ├── commonMain/          # Общий код для всех платформ
│   ├── wasmJsMain/          # Код специфичный для WebAssembly
│   └── webMain/              # Веб-ресурсы и точка входа
```

## Особенности

- ✅ Работает только через **wasmJs** таргет (JS таргет удален)
- ✅ Современный UI на Compose Multiplatform
- ✅ Интеграция с AnythingLLM для RAG
- ✅ Быстрая работа благодаря WebAssembly

## Технологии

- [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)
- [Kotlin/Wasm](https://kotl.in/wasm/)
- [AnythingLLM](https://github.com/Mintplex-Labs/anything-llm)

## Лицензия

[Укажите лицензию проекта]
