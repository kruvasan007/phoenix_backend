cx# Phoenix Backend API

Серверное приложение для Phoenix с JWT аутентификацией, REST API для отчетов и интеграцией с внешними сервисами.

## Запуск

### С Docker Compose (рекомендуется)

```bash
# Запустить все сервисы (база данных + приложение)
docker-compose up -d

# Проверить статус
docker-compose ps

# Посмотреть логи
docker-compose logs -f app

# Остановить
docker-compose down
```

### Локальная разработка

1. Установить PostgreSQL локально
2. Создать базу данных `phoenix`
3. Создать пользователя `admin` с паролем `password`
4. Запустить приложение:

```bash
./gradlew run
```

Переменные окружения для локальной разработки:
```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/phoenix
export DATABASE_USER=admin
export DATABASE_PASSWORD=password
export JWT_SECRET=phoenix-super-secret-jwt-key-2024
export PYTHON_API_URL=http://localhost:8000
```

## API Документация

### Аутентификация

#### Регистрация
```http
POST /auth/register
Content-Type: application/json

{
  "username": "user123",
  "email": "user@example.com", 
  "password": "password123",
  "fullName": "Иван Иванов"
}
```

Ответ:
```json
{
  "user": {
    "id": 1,
    "username": "user123",
    "email": "user@example.com",
    "fullName": "Иван Иванов",
    "isActive": true
  },
  "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 86400
}
```

#### Авторизация
```http
POST /auth/login
Content-Type: application/json

{
  "username": "user123",
  "password": "password123"
}
```

#### Получение информации о пользователе
```http
GET /auth/me
Authorization: Bearer <token>
```

### Отчеты (требует аутентификацию)

Все эндпоинты отчетов требуют JWT токен в заголовке `Authorization: Bearer <token>`

#### Создать отчет
```http
POST /reports
Authorization: Bearer <token>
Content-Type: application/json

{
  "deviceId": "Pixel 7 Pro",
  "body": "Без дефектов",
  "screen": "Без дефектов", 
  "frequency": "930000",
  "mark": "1",
  "width": 1080,
  "height": 2340,
  "density": 2.625,
  "ram": 11,
  "totalSpace": 109,
  "gyroscope": "true",
  "versionOS": "",
  "batteryState": 2,
  "level": 4,
  "dataStatus": 2,
  "gps": false,
  "bluetooth": false,
  "audioReport": true
}
```

#### Получить все отчеты пользователя
```http
GET /reports
Authorization: Bearer <token>
```

#### Получить отчет по deviceId
```http
GET /reports/{deviceId}
Authorization: Bearer <token>
```

#### Обновить отчет
```http
PUT /reports/{deviceId}
Authorization: Bearer <token>
Content-Type: application/json

{
  // обновленные данные отчета
}
```

#### Удалить отчет
```http
DELETE /reports/{id}
Authorization: Bearer <token>
```

### Интеграция с Graph API (требует аутентификацию)

#### Очистить граф
```http
DELETE /clear
Authorization: Bearer <token>
```

#### Добавить данные в граф
```http
POST /ingest
Authorization: Bearer <token>
Content-Type: application/json

{
  "question": "Ваш вопрос для обработки"
}
```

### Health Check

```http
GET /health
```

## Переменные окружения

| Переменная | Описание | По умолчанию |
|-----------|----------|-------------|
| `DATABASE_URL` | URL подключения к БД | `jdbc:postgresql://localhost:5432/phoenix` |
| `DATABASE_USER` | Пользователь БД | `admin` |
| `DATABASE_PASSWORD` | Пароль БД | `password` |
| `JWT_SECRET` | Секретный ключ для JWT | `phoenix-super-secret-jwt-key-2024` |
| `JWT_ISSUER` | Издатель JWT токенов | `phoenix-app` |
| `JWT_AUDIENCE` | Аудитория JWT токенов | `phoenix-users` |
| `PYTHON_API_URL` | URL Python API сервиса | `http://localhost:8000` |
| `PYTHON_API_TIMEOUT` | Таймаут запросов к Python API (мс) | `60000` |

## Порты

- **8080** - HTTP API сервер
- **5432** - PostgreSQL (только в docker-compose)

## Безопасность

- JWT токены действительны 24 часа
- Все эндпоинты (кроме аутентификации и health check) защищены JWT аутентификацией
- Пользователи могут работать только со своими отчетами
- Пароли хешируются с помощью SHA-256

## Логи

Логи приложения доступны через:
```bash
# В Docker
docker-compose logs -f app

# Локально
./gradlew run
```
