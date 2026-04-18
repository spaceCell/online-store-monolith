# Online Store Monolith

Модульный монолит интернет-магазина на **Java 21** и **Spring Boot 3.1**: один запускаемый модуль `store-app`, доменная логика разнесена по Gradle-модулям (`catalog`, `user`, `order`, `payment`).

## Стек

- Spring Web, Spring Data JPA  
- H2 in-memory (режим, близкий к PostgreSQL)  
- Lombok, MapStruct  
- OpenAPI / Swagger UI (springdoc)

## Модули

| Модуль        | Назначение |
|---------------|------------|
| `store-app`   | Точка входа, конфигурация, сборка исполняемого JAR |
| `catalog-module` | Товары, резерв и возврат остатков |
| `user-module` | Пользователи, проверка активного пользователя |
| `payment-module` | Платежи (имитация), валидация карты |
| `order-module` | REST API заказов, оркестрация каталога, пользователей и оплаты |

## Запуск

```bash
./gradlew :store-app:bootRun
```

Приложение по умолчанию: **http://localhost:8080**

Сборка JAR:

```bash
./gradlew :store-app:bootJar
java -jar store-app/build/libs/store-app-1.0.0.jar
```

## Документация API (Swagger)

После запуска:

- **Swagger UI:** http://localhost:8080/swagger-ui.html  
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs  

## Тестовые данные (UUID)

Из `data.sql` удобно брать идентификаторы для запросов.

**Пользователи**

| UUID | Описание |
|------|----------|
| `10000000-0000-0000-0000-000000000001` | Иван Иванов (активен) |
| `10000000-0000-0000-0000-000000000099` | Неактивный пользователь (заказ с ним вернёт ошибку) |

**Товары (примеры)**

| UUID | Название |
|------|----------|
| `30000000-0000-0000-0000-000000000001` | Ноутбук ASUS ROG |
| `30000000-0000-0000-0000-000000000002` | Смартфон Samsung S23 |

## Имитация оплаты

- Номер карты **оканчивается на `0000`** — оплата всегда успешна.  
- **Оканчивается на `9999`** — всегда отказ.  
- Иначе — случайно (~80% успех).  
- Срок действия в формате **MM/YY**, CVV **3–4 цифры**, номер карты не короче **16** символов.

## REST API заказов

Базовый путь: **`/api/v1/orders`**

### 1. Создать заказ с оплатой

`POST /api/v1/orders`  
Тело: JSON (`Content-Type: application/json`).  
Успех: **201 Created** и тело заказа (включая `paymentStatus`, `transactionId` при успешной оплате).  
Ошибка оплаты / бизнес-ошибка: **400** с телом ошибки.

**Пример `curl` (успешная оплата):**

```bash
curl -s -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "10000000-0000-0000-0000-000000000001",
    "items": [
      {
        "productId": "30000000-0000-0000-0000-000000000001",
        "quantity": 1
      },
      {
        "productId": "30000000-0000-0000-0000-000000000004",
        "quantity": 2
      }
    ],
    "paymentInfo": {
      "cardNumber": "4111111111110000",
      "cardHolderName": "IVAN IVANOV",
      "expiryDate": "12/28",
      "cvv": "123"
    }
  }' | jq .
```

Сохраните из ответа поле **`id`** заказа для следующих запросов.

**Пример с гарантированным отказом оплаты (ожидается 400):**

```bash
curl -s -w "\nHTTP_CODE:%{http_code}\n" -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "10000000-0000-0000-0000-000000000001",
    "items": [
      {
        "productId": "30000000-0000-0000-0000-000000000003",
        "quantity": 1
      }
    ],
    "paymentInfo": {
      "cardNumber": "4111111111119999",
      "cardHolderName": "TEST USER",
      "expiryDate": "12/28",
      "cvv": "123"
    }
  }'
```

### 2. Получить заказ по ID

`GET /api/v1/orders/{id}`  
Успех: **200**. Не найден: **404**.

```bash
ORDER_ID="замените-uuid-из-ответа-создания"

curl -s "http://localhost:8080/api/v1/orders/${ORDER_ID}" | jq .
```

Пример с конкретным UUID (подставьте реальный `id` после создания заказа):

```bash
curl -s http://localhost:8080/api/v1/orders/550e8400-e29b-41d4-a716-446655440000 | jq .
```

### 3. Отменить заказ

`POST /api/v1/orders/{id}/cancel?reason=...`  
Успех: **200** с пустым телом. Не найден: **404**.

```bash
ORDER_ID="замените-uuid-из-ответа-создания"

curl -s -w "\nHTTP_CODE:%{http_code}\n" -X POST \
  "http://localhost:8080/api/v1/orders/${ORDER_ID}/cancel?reason=Клиент%20передумал"
```

## Примечания

- База **в памяти**: после остановки приложения данные сбрасываются.  
- Отдельных публичных REST-эндпоинтов для каталога и пользователей нет — они используются через сценарий заказа и тестовые `data.sql`.  
- Для интерактивных тестов удобно использовать **Swagger UI** и те же UUID из раздела «Тестовые данные».
