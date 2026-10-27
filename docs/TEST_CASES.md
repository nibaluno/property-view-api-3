# Каталог тест-кейсов и результаты выполнения (Test Cases)
**Проект:** `property-view-api`  
**Связанный чек-лист:** [docs/CHECKLIST.md](CHECKLIST.md)  
**Доказательства выполнения (скриншоты и логи):** папка [docs/evidence/](evidence/)

В каталоге представлено **30 тест-кейсов (`TC-01`..`TC-30`)**, покрывающих 100% функциональных требований с высоким и средним приоритетом (`FR-01`..`FR-18`).

---

### TC-01: Проверка доступности API под префиксом `/property-view` и отказа без префикса
- **ID:** `TC-01`
- **Приоритет:** High
- **Связанное требование:** `FR-01`
- **Модуль/подмодуль:** `presentation.web` / Контекстная маршрутизация Tomcat
- **Заглавие (суть):** Запрос к `/property-view/hotels` возвращает `200 OK`, а к `/hotels` без префикса — `404 Not Found`
- **Исходные данные:** Приложение запущено на `http://localhost:8092`.
- **Шаги тест-кейса:**
  1. Отправить HTTP-запрос `GET http://localhost:8092/property-view/hotels`.
  2. Отправить HTTP-запрос `GET http://localhost:8092/hotels`.
- **Ожидаемый результат:** Шаг 1 возвращает `200 OK` и JSON-массив; шаг 2 возвращает `404 Not Found`.
- **Фактический результат и статус:** **PASSED** — Шаг 1 вернул `200 OK`, шаг 2 вернул `404 Not Found` ([evidence/tc-execution-report.json](evidence/tc-execution-report.json), [evidence/ui-catalog-desktop.png](evidence/ui-catalog-desktop.png)).

### TC-02: Получение пустого списка отелей на чистой БД
- **ID:** `TC-02`
- **Приоритет:** High
- **Связанное требование:** `FR-02`
- **Модуль/подмодуль:** `presentation.web.controller.HotelController` / `getAll`
- **Заглавие (суть):** Вызов `GET /property-view/hotels` при отсутствии записей в БД возвращает `[]`
- **Исходные данные:** Чистая база данных H2 (после старта приложения без профиля `demo`).
- **Шаги тест-кейса:**
  1. Выполнить `GET /property-view/hotels`.
  2. Проверить HTTP-код, заголовок `Content-Type` и тело ответа.
- **Ожидаемый результат:** Статус `200 OK`, `Content-Type: application/json`, тело `[]`.
- **Фактический результат и статус:** **PASSED** — Получен `200 OK`, тело `[]`.

### TC-03: Успешное создание отеля со всеми заполненными полями
- **ID:** `TC-03`
- **Приоритет:** High
- **Связанное требование:** `FR-03`, `FR-06`, `FR-09`
- **Модуль/подмодуль:** `HotelController` / `CreateHotelService`
- **Заглавие (суть):** `POST /property-view/hotels` создаёт отель, возвращает `201 Created`, заголовок `Location` и краткую карточку с однострочным адресом
- **Исходные данные:** Валидный JSON отеля `"DoubleTree by Hilton Minsk"` (дом `9`, ул. `"Pobediteley Avenue"`, г. `"Minsk"`, индекс `"220004"`, страна `"Belarus"`, `checkIn: "14:00"`, `checkOut: "12:00"`).
- **Шаги тест-кейса:**
  1. Отправить `POST /property-view/hotels` с указанным JSON-телом.
  2. Проверить статус ответа и заголовок `Location`.
  3. Проверить поля `id`, `name`, `description`, `address`, `phone` в теле ответа.
- **Ожидаемый результат:** Статус `201 Created`, `Location` оканчивается на `/property-view/hotels/1`, поле `address` равно `"9 Pobediteley Avenue, Minsk, 220004, Belarus"`.
- **Фактический результат и статус:** **PASSED** — Статус `201 Created`, адрес отформатирован по шаблону ([evidence/ui-hotel-created.png](evidence/ui-hotel-created.png)).

### TC-04: Создание отеля без необязательных полей `description` и `arrivalTime.checkOut`
- **ID:** `TC-04`
- **Приоритет:** High
- **Связанное требование:** `FR-06`, `FR-07`
- **Модуль/подмодуль:** `HotelController` / `CreateHotelRequest`
- **Заглавие (суть):** `POST /property-view/hotels` успешно создаёт отель при отсутствии `description` и `arrivalTime.checkOut`
- **Исходные данные:** JSON отеля `"Hilton Moscow Leningradskaya"` без ключей `description` и `arrivalTime.checkOut` (`checkIn: "15:00"`).
- **Шаги тест-кейса:**
  1. Отправить `POST /property-view/hotels` без `description` и `checkOut`.
  2. Выполнить `GET /property-view/hotels/{id}` для созданного отеля.
- **Ожидаемый результат:** Шаг 1 возвращает `201 Created`; шаг 2 возвращает `200 OK`, где `description == null` и `arrivalTime.checkOut == null`.
- **Фактический результат и статус:** **PASSED** — Отель создан со статусом `201 Created`, опциональные поля равны `null`.

### TC-05: Нормализация состоящего из пробелов `description` в `null`
- **ID:** `TC-05`
- **Приоритет:** Medium
- **Связанное требование:** `FR-07`
- **Модуль/подмодуль:** `domain.model.Hotel` / `Preconditions.optionalText`
- **Заглавие (суть):** Передача `description: "    "` при создании отеля сохраняет `null`
- **Исходные данные:** Валидный JSON отеля с `"description": "    "`.
- **Шаги тест-кейса:**
  1. Отправить `POST /property-view/hotels`.
  2. Проверить значение `description` в ответе.
- **Ожидаемый результат:** Статус `201 Created`, поле `description` равно `null`.
- **Фактический результат и статус:** **PASSED** — Пробельная строка нормализована в `null`.

### TC-06: Отказ в создании отеля при пустом названии и неположительном номере дома
- **ID:** `TC-06`
- **Приоритет:** High
- **Связанное требование:** `FR-08`, `NFR-04`
- **Модуль/подмодуль:** `HotelController` / `GlobalExceptionHandler`
- **Заглавие (суть):** Передача `name: " "` и `address.houseNumber: 0` возвращает `400 Bad Request` (`application/problem+json`)
- **Исходные данные:** JSON с `"name": " "` и `"address": {"houseNumber": 0, ...}`.
- **Шаги тест-кейса:**
  1. Отправить `POST /property-view/hotels` с невалидными полями.
  2. Проверить код ответа, `Content-Type` и объект `errors`.
- **Ожидаемый результат:** Статус `400 Bad Request`, `Content-Type: application/problem+json`, присутствуют ключи `errors.name` и `errors['address.houseNumber']`.
- **Фактический результат и статус:** **PASSED** — Возвращён `400 Bad Request` с детализацией ошибок валидации.

### TC-07: Отказ в создании отеля при невалидном формате email
- **ID:** `TC-07`
- **Приоритет:** High
- **Связанное требование:** `FR-08`
- **Модуль/подмодуль:** `ContactsDto` / `HotelController`
- **Заглавие (суть):** Передача `contacts.email: "not-an-email"` отклоняется со статусом `400 Bad Request`
- **Исходные данные:** JSON отеля с `"contacts": {"phone": "+375 17 309-80-00", "email": "not-an-email"}`.
- **Шаги тест-кейса:**
  1. Отправить `POST /property-view/hotels`.
  2. Проверить наличие ошибки `errors['contacts.email']`.
- **Ожидаемый результат:** Статус `400 Bad Request`, поле `errors['contacts.email']` содержит сообщение об ошибке формата email.
- **Фактический результат и статус:** **PASSED** — Статус `400 Bad Request`, ошибка зафиксирована в `errors['contacts.email']`.

### TC-08: Отказ в создании отеля при отсутствии обязательных вложенных объектов (`address`, `contacts`, `arrivalTime`)
- **ID:** `TC-08`
- **Приоритет:** High
- **Связанное требование:** `FR-08`
- **Модуль/подмодуль:** `CreateHotelRequest` / `HotelController`
- **Заглавие (суть):** Отправка `{"name": "H", "brand": "B"}` без вложенных объектов возвращает `400 Bad Request`
- **Исходные данные:** Тело запроса `{"name": "H", "brand": "B"}`.
- **Шаги тест-кейса:**
  1. Отправить `POST /property-view/hotels` с минимальным неполным JSON.
  2. Проверить объект `errors` в ответе.
- **Ожидаемый результат:** Статус `400 Bad Request`, в `errors` присутствуют поля `address`, `contacts`, `arrivalTime`.
- **Фактический результат и статус:** **PASSED** — Все три пропущенных объекта перечислены в `errors`.

### TC-09: Отказ при передаче некорректного времени заезда (`25:00`) и синтаксически битого JSON
- **ID:** `TC-09`
- **Приоритет:** High
- **Связанное требование:** `FR-09`
- **Модуль/подмодуль:** `ArrivalTimeDto` / `GlobalExceptionHandler`
- **Заглавие (суть):** Передача `checkIn: "25:00"` или `{not json` возвращает `400 Bad Request` без раскрытия стектрейса
- **Исходные данные:** JSON с `"checkIn": "25:00"`, а затем строка `"{not json"`.
- **Шаги тест-кейса:**
  1. Отправить `POST /property-view/hotels` с `"checkIn": "25:00"`.
  2. Отправить `POST /property-view/hotels` с телом `"{not json"`.
- **Ожидаемый результат:** Оба запроса возвращают `400 Bad Request` (`application/problem+json`).
- **Фактический результат и статус:** **PASSED** — Оба запроса отклонены со статусом `400 Bad Request`.

### TC-10: Получение краткого списка отелей с проверкой сортировки по возрастанию `id`
- **ID:** `TC-10`
- **Приоритет:** High
- **Связанное требование:** `FR-02`, `FR-03`
- **Модуль/подмодуль:** `HotelController` / `HotelRepositoryAdapter`
- **Заглавие (суть):** `GET /property-view/hotels` возвращает список всех созданных отелей в порядке возрастания `id`
- **Исходные данные:** В БД созданы 2 отеля (`id=1` в Минске, `id=2` в Москве).
- **Шаги тест-кейса:**
  1. Выполнить `GET /property-view/hotels`.
  2. Сравнить порядок `id` и структуру элементов ответа.
- **Ожидаемый результат:** Статус `200 OK`, массив из >=2 элементов, `id` расположены по возрастанию (`1, 2, ...`), каждый элемент содержит `id`, `name`, `description`, `address`, `phone`.
- **Фактический результат и статус:** **PASSED** — Список отсортирован по `id`, структура полностью соответствует схеме.

### TC-11: Получение детальной карточки существующего отеля (`GET /property-view/hotels/{id}`)
- **ID:** `TC-11`
- **Приоритет:** High
- **Связанное требование:** `FR-04`
- **Модуль/подмодуль:** `HotelController` / `HotelQueryService`
- **Заглавие (суть):** `GET /property-view/hotels/1` возвращает полную иерархическую структуру отеля
- **Исходные данные:** В БД существует отель `id=1` (`DoubleTree by Hilton Minsk`).
- **Шаги тест-кейса:**
  1. Выполнить `GET /property-view/hotels/1`.
  2. Проверить вложенные поля `address.houseNumber`, `address.postCode`, `contacts.email`, `arrivalTime.checkIn`, `arrivalTime.checkOut`, `amenities`.
- **Ожидаемый результат:** Статус `200 OK`, все вложенные поля совпадают с сохранёнными данными.
- **Фактический результат и статус:** **PASSED** — Детальная карточка возвращена корректно ([evidence/ui-hotel-details.png](evidence/ui-hotel-details.png)).

### TC-12: Обращение к несуществующему отелю (`GET /property-view/hotels/999999`)
- **ID:** `TC-12`
- **Приоритет:** High
- **Связанное требование:** `FR-05`
- **Модуль/подмодуль:** `HotelController` / `GlobalExceptionHandler`
- **Заглавие (суть):** Запрос `GET /property-view/hotels/999999` возвращает `404 Not Found` в формате `ProblemDetail`
- **Исходные данные:** Отель с `id=999999` отсутствует в БД.
- **Шаги тест-кейса:**
  1. Выполнить `GET /property-view/hotels/999999`.
  2. Проверить статус, `Content-Type` и поля `title`, `status`, `detail`.
- **Ожидаемый результат:** Статус `404 Not Found`, `Content-Type: application/problem+json`, `title: "Hotel not found"`, `status: 404`.
- **Фактический результат и статус:** **PASSED** — Получен корректный ответ `404 ProblemDetail`.

### TC-13: Обращение к `GET /property-view/hotels/{id}` с нечисловым параметром пути
- **ID:** `TC-13`
- **Приоритет:** Medium
- **Связанное требование:** `FR-04`
- **Модуль/подмодуль:** `HotelController`
- **Заглавие (суть):** Запрос `GET /property-view/hotels/abc` возвращает `400 Bad Request`
- **Исходные данные:** Строковый параметр `abc` вместо `long id`.
- **Шаги тест-кейса:**
  1. Выполнить `GET /property-view/hotels/abc`.
- **Ожидаемый результат:** Статус `400 Bad Request`.
- **Фактический результат и статус:** **PASSED** — Получен `400 Bad Request`.

### TC-14: Успешное добавление списка из 10 удобств к отелю с сохранением порядка
- **ID:** `TC-14`
- **Приоритет:** High
- **Связанное требование:** `FR-10`, `FR-11`
- **Модуль/подмодуль:** `HotelAmenityController` / `AddAmenitiesService`
- **Заглавие (суть):** `POST /property-view/hotels/1/amenities` сохраняет 10 удобств в точном порядке передачи
- **Исходные данные:** Отель `id=1`, массив из 10 удобств (`["Free parking", "Free WiFi", ..., "Meeting rooms"]`).
- **Шаги тест-кейса:**
  1. Отправить `POST /property-view/hotels/1/amenities` с массивом из 10 удобств.
  2. Выполнить `GET /property-view/hotels/1` и проверить порядок элементов в `amenities`.
- **Ожидаемый результат:** Статус `200 OK`, массив `amenities` содержит ровно 10 элементов в исходном порядке.
- **Фактический результат и статус:** **PASSED** — Порядок и состав из 10 удобств полностью сохранены.

### TC-15: Регистронезависимая дедупликация и нормализация пробелов при добавлении удобств
- **ID:** `TC-15`
- **Приоритет:** High
- **Связанное требование:** `FR-11`
- **Модуль/подмодуль:** `domain.model.Amenity` / `HotelRepositoryAdapter`
- **Заглавие (суть):** Дубликаты удобств с разным регистром и лишними пробелами игнорируются, сохраняется первое написание
- **Исходные данные:** Отель `id=2`, массив `["  Free   WiFi ", "Pool", "free wifi", "POOL"]`.
- **Шаги тест-кейса:**
  1. Отправить `POST /property-view/hotels/2/amenities` с указанным массивом.
  2. Проверить список `amenities` в ответе.
- **Ожидаемый результат:** Статус `200 OK`, в списке `amenities` содержатся ровно 2 элемента: `["Free WiFi", "Pool"]`.
- **Фактический результат и статус:** **PASSED** — Дубликаты отброшены, пробелы нормализованы.

### TC-16: Отказ при добавлении пустого имени удобства и при обращении к несуществующему отелю
- **ID:** `TC-16`
- **Приоритет:** High
- **Связанное требование:** `FR-05`, `FR-10`
- **Модуль/подмодуль:** `HotelAmenityController`
- **Заглавие (суть):** Передача `["Pool", " "]` возвращает `400 Bad Request`, а `POST /hotels/999999/amenities` возвращает `404 Not Found`
- **Исходные данные:** Существующий отель `id=1` и несуществующий `id=999999`.
- **Шаги тест-кейса:**
  1. Отправить `POST /property-view/hotels/1/amenities` с телом `["Pool", " "]`.
  2. Отправить `POST /property-view/hotels/999999/amenities` с телом `["Pool"]`.
- **Ожидаемый результат:** Шаг 1 возвращает `400 Bad Request`; шаг 2 возвращает `404 Not Found`.
- **Фактический результат и статус:** **PASSED** — Получены статусы `400` и `404`.

### TC-17: Поиск отелей по подстроке названия (`name`) без учёта регистра
- **ID:** `TC-17`
- **Приоритет:** High
- **Связанное требование:** `FR-12`
- **Модуль/подмодуль:** `SearchController` / `HotelSpecifications`
- **Заглавие (суть):** `GET /property-view/search?name=double` находит `"DoubleTree by Hilton Minsk"`
- **Исходные данные:** В БД присутствуют `"DoubleTree by Hilton Minsk"` и `"Hilton Moscow Leningradskaya"`.
- **Шаги тест-кейса:**
  1. Выполнить `GET /property-view/search?name=double`.
  2. Выполнить `GET /property-view/search?name=HILTON`.
- **Ожидаемый результат:** Шаг 1 возвращает 1 отель (`DoubleTree by Hilton Minsk`); шаг 2 возвращает оба отеля.
- **Фактический результат и статус:** **PASSED** — Поиск по подстроке работает регистронезависимо ([evidence/ui-search-filtered.png](evidence/ui-search-filtered.png)).

### TC-18: Поиск отелей по точному совпадению `city`, `brand`, `country` и их комбинации (`AND`)
- **ID:** `TC-18`
- **Приоритет:** High
- **Связанное требование:** `FR-12`, `FR-13`
- **Модуль/подмодуль:** `SearchController` / `SearchHotelsService`
- **Заглавие (суть):** Комбинированный фильтр `?brand=hilton&city=minsk&country=belarus` возвращает только отели, удовлетворяющие всем условиям
- **Исходные данные:** Два отеля бренда `Hilton` (в Минске и в Москве).
- **Шаги тест-кейса:**
  1. Выполнить `GET /property-view/search?brand=hilton&city=minsk&country=belarus`.
  2. Выполнить `GET /property-view/search?brand=hilton&city=nowhere`.
- **Ожидаемый результат:** Шаг 1 возвращает 1 отель в Минске; шаг 2 возвращает `[]`.
- **Фактический результат и статус:** **PASSED** — Логическое `AND` работает корректно.

### TC-19: Поиск отелей по нескольким удобствам (повторные параметры и через запятую)
- **ID:** `TC-19`
- **Приоритет:** High
- **Связанное требование:** `FR-12`, `FR-13`
- **Модуль/подмодуль:** `SearchController` / `HotelSpecifications`
- **Заглавие (суть):** Отель возвращается в поиске только при наличии всех запрошенных удобств (`?amenities=A&amenities=B` и `?amenities=A,B`)
- **Исходные данные:** Отель 1 имеет `["Free WiFi", "Concierge"]`, Отель 2 имеет `["Free WiFi", "Pool"]`.
- **Шаги тест-кейса:**
  1. Выполнить `GET /property-view/search?amenities=free%20wifi`.
  2. Выполнить `GET /property-view/search?amenities=Free%20WiFi&amenities=Concierge`.
  3. Выполнить `GET /property-view/search?amenities=Free%20WiFi,Pool`.
- **Ожидаемый результат:** Шаг 1 возвращает оба отеля; шаг 2 возвращает только Отель 1; шаг 3 возвращает только Отель 2.
- **Фактический результат и статус:** **PASSED** — Оба формата передачи `amenities` возвращают точное пересечение.

### TC-20: Экранирование спецсимволов SQL `LIKE` (`%` и `_`) в параметре поиска `name`
- **ID:** `TC-20`
- **Приоритет:** Medium
- **Связанное требование:** `FR-14`
- **Модуль/подмодуль:** `HotelSpecifications`
- **Заглавие (суть):** Передача `GET /property-view/search?name=%25` не приводит к возврату всех записей таблицы
- **Исходные данные:** В БД есть 2 отеля без символа `%` в названии.
- **Шаги тест-кейса:**
  1. Выполнить `GET /property-view/search?name=%25`.
- **Ожидаемый результат:** Статус `200 OK`, пустой массив `[]`.
- **Фактический результат и статус:** **PASSED** — Подстановочный символ `%` экранирован и обработан как литерал.

### TC-21: Построение гистограмм по параметрам `brand`, `city`, `country`, `amenities`
- **ID:** `TC-21`
- **Приоритет:** High
- **Связанное требование:** `FR-15`
- **Модуль/подмодуль:** `HistogramController` / `HistogramService`
- **Заглавие (суть):** `GET /property-view/histogram/{param}` возвращает корректные счётчики с алфавитной сортировкой ключей и без учёта регистра параметра
- **Исходные данные:** Отель 1 (`Hilton`, `Minsk`, `Belarus`, `Free WiFi`, `Concierge`), Отель 2 (`Hilton`, `Moscow`, `Russia`, `Free WiFi`, `Pool`).
- **Шаги тест-кейса:**
  1. Вызвать `GET /property-view/histogram/city`.
  2. Вызвать `GET /property-view/histogram/BRAND`.
  3. Вызвать `GET /property-view/histogram/country`.
  4. Вызвать `GET /property-view/histogram/Amenities`.
- **Ожидаемый результат:** Все 4 запроса возвращают `200 OK`; `city` -> `{"Minsk":1,"Moscow":1}`, `brand` -> `{"Hilton":2}`, `country` -> `{"Belarus":1,"Russia":1}`, `amenities` -> `{"Free WiFi":2,"Pool":1,"Concierge":1}`.
- **Фактический результат и статус:** **PASSED** — Все гистограммы рассчитаны верно ([evidence/ui-histogram-amenities.png](evidence/ui-histogram-amenities.png)).

### TC-22: Обработка неподдерживаемого параметра гистограммы (`GET /property-view/histogram/stars`)
- **ID:** `TC-22`
- **Приоритет:** Medium
- **Связанное требование:** `FR-16`
- **Модуль/подмодуль:** `HistogramController` / `HistogramParam`
- **Заглавие (суть):** Вызов `/property-view/histogram/stars` возвращает `400 Bad Request` со списком допустимых параметров
- **Исходные данные:** Неподдерживаемый параметр `stars`.
- **Шаги тест-кейса:**
  1. Выполнить `GET /property-view/histogram/stars`.
  2. Проверить статус ответа и поле `detail`.
- **Ожидаемый результат:** Статус `400 Bad Request`, `detail` содержит `"brand, city, country, amenities"`.
- **Фактический результат и статус:** **PASSED** — Возвращён `400 Bad Request` с перечнем допустимых параметров.

### TC-23: Аутентификация пользователя и получение Bearer-токена (`POST /property-view/auth/login`)
- **ID:** `TC-23`
- **Приоритет:** High
- **Связанное требование:** `FR-17`
- **Модуль/подмодуль:** `HotelManagementController` / `ManageHotelService`
- **Заглавие (суть):** Успешный вход возвращает токен, а короткий пароль или пустой логин отклоняется с `401`/`400`
- **Исходные данные:** Валидная пара `{"username":"alice","password":"password123"}` и невалидная `{"username":"alice","password":"12"}`.
- **Шаги тест-кейса:**
  1. Отправить `POST /property-view/auth/login` с валидными данными.
  2. Отправить `POST /property-view/auth/login` с коротким паролем `"12"`.
- **Ожидаемый результат:** Шаг 1 возвращает `200 OK` и `"token": "pv-token-alice"`; шаг 2 возвращает `401 Unauthorized`.
- **Фактический результат и статус:** **PASSED** — Выдача токена и отказ при неверных данных работают корректно.

### TC-24: Защита закрытой зоны от неавторизованного доступа по прямому URL (`401 Unauthorized`)
- **ID:** `TC-24`
- **Приоритет:** High
- **Связанное требование:** `FR-17`, `NFR-04`
- **Модуль/подмодуль:** `HotelManagementController`
- **Заглавие (суть):** Обращение к `GET /property-view/hotels/1/management` и `DELETE /property-view/hotels/1` без заголовка `Authorization` возвращает `401 Unauthorized`
- **Исходные данные:** В БД существует отель `id=1`, заголовок `Authorization` не передаётся.
- **Шаги тест-кейса:**
  1. Выполнить `GET /property-view/hotels/1/management` без заголовков авторизации.
  2. Выполнить `DELETE /property-view/hotels/1` без заголовков авторизации.
- **Ожидаемый результат:** Оба запроса отклоняются со статусом `401 Unauthorized` (`application/problem+json`).
- **Фактический результат и статус:** **PASSED** — Доступ без авторизации заблокирован с кодом `401`.

### TC-25: Защита от подмены идентификатора (IDOR) при попытке доступа к чужому отелю (`403 Forbidden`)
- **ID:** `TC-25`
- **Приоритет:** High
- **Связанное требование:** `FR-17`
- **Модуль/подмодуль:** `HotelManagementController` / `ManageHotelService`
- **Заглавие (суть):** Пользователь `bob` не может просмотреть управление или удалить отель `id=1`, принадлежащий пользователю `alice`
- **Исходные данные:** Отель `id=1` закреплён за `alice` (`POST /hotels/1/claim` с токеном `pv-token-alice`); получен токен `pv-token-bob`.
- **Шаги тест-кейса:**
  1. Выполнить `GET /property-view/hotels/1/management` с заголовком `Authorization: Bearer pv-token-bob`.
  2. Выполнить `DELETE /property-view/hotels/1` с заголовком `Authorization: Bearer pv-token-bob`.
- **Ожидаемый результат:** Оба запроса возвращают `403 Forbidden` (`"Access denied: user 'bob' is not authorized to manage hotel with id 1"`), отель `id=1` остаётся в БД.
- **Фактический результат и статус:** **PASSED** — Уязвимость IDOR предотвращена, возвращён `403 Forbidden`.

### TC-26: Сквозной сценарий жизненного цикла сущности `Hotel` до состояния `Deleted` (`204 No Content` -> `404`)
- **ID:** `TC-26`
- **Приоритет:** High
- **Связанное требование:** `FR-06`, `FR-10`, `FR-17` (State Diagram ЛР1 п. 4)
- **Модуль/подмодуль:** `HotelController`, `HotelAmenityController`, `HotelManagementController`
- **Заглавие (суть):** Перевод отеля по цепочке состояний `TransientDraft` -> `CreatedWithoutAmenities` -> `EnrichedWithAmenities` -> `Deleted`
- **Исходные данные:** Авторизованный владелец `alice`.
- **Шаги тест-кейса:**
  1. Создать отель через `POST /property-view/hotels` (`CreatedWithoutAmenities`).
  2. Добавить удобства через `POST /property-view/hotels/{id}/amenities` (`EnrichedWithAmenities`).
  3. Закрепить владение через `POST /property-view/hotels/{id}/claim` и удалить отель через `DELETE /property-view/hotels/{id}` с токеном `alice` (`Deleted`).
  4. Проверить `GET /property-view/hotels/{id}`.
- **Ожидаемый результат:** Шаг 1 -> `201`, шаг 2 -> `200`, шаг 3 -> `204 No Content`, шаг 4 -> `404 Not Found`.
- **Фактический результат и статус:** **PASSED** — Все переходы состояний диаграммы выполнены успешно.

### TC-27: Проверка отсутствия утечки стектрейсов, путей сервера и SQL в ответах об ошибках
- **ID:** `TC-27`
- **Приоритет:** High
- **Связанное требование:** `NFR-04`
- **Модуль/подмодуль:** `GlobalExceptionHandler`
- **Заглавие (суть):** Ответы `400`, `401`, `403`, `404` и `500` содержат только безопасные поля RFC 7807 `ProblemDetail`
- **Исходные данные:** Набор ошибочных запросов к API.
- **Шаги тест-кейса:**
  1. Вызвать `GET /property-view/hotels/999999` (`404`), `POST /property-view/hotels` с `{}` (`400`), `GET /property-view/hotels/1/management` (`401`).
  2. Проверить тела ответов на отсутствие подстрок `Exception`, `at com.propertyview`, `/Users/`, `SELECT `, `INSERT `.
- **Ожидаемый результат:** Ни одно тело ответа не содержит стектрейсов, серверных путей или SQL.
- **Фактический результат и статус:** **PASSED** — Утечки технической информации отсутствуют.

### TC-28: Проверка работы клиентского веб-интерфейса (создание, поиск, просмотр деталей, гистограмма)
- **ID:** `TC-28`
- **Приоритет:** High
- **Связанное требование:** `FR-18`, `UR-07`
- **Модуль/подмодуль:** Веб-клиент `/property-view/index.html`
- **Заглавие (суть):** Пользователь через браузер создаёт отель, фильтрует каталог по городу, открывает карточку, добавляет удобство и строит гистограмму
- **Исходные данные:** Открыта страница `http://localhost:8092/property-view/index.html` в браузере.
- **Шаги тест-кейса:**
  1. Заполнить форму `#create-hotel-form` и нажать `Create Hotel`.
  2. В форме `#search-form` ввести город `Minsk` и нажать `Search`.
  3. Нажать `View Details` на карточке отеля, ввести удобство `Spa` и нажать `Add Amenities`.
  4. Нажать кнопку `By Amenities` в блоке `Analytics Histogram`.
- **Ожидаемый результат:** Карточка появляется в сетке, фильтрация оставляет нужный отель, бейдж `Spa` появляется в деталях и в гистограмме, ошибок в консоли браузера (`Console` / `Network 404`) нет.
- **Фактический результат и статус:** **PASSED** — Проверено автотестами Playwright и ручным прогоном со скриншотами в [docs/evidence/](evidence/).

### TC-29: Конкурентное добавление одинакового нового удобства двумя параллельными запросами
- **ID:** `TC-29`
- **Приоритет:** Medium
- **Связанное требование:** `FR-10`, `FR-11`, `NFR-01`
- **Модуль/подмодуль:** `HotelRepositoryAdapter#appendMissingAmenities`
- **Заглавие (суть):** Одновременное добавление ранее неизвестного удобства к двум отелям параллельными потоками
- **Исходные данные:** Два существующих отеля `id=1` и `id=2`, новое удобство `"UniqueConcurrentAmenity"`, отсутствующее в таблице `amenity`.
- **Шаги тест-кейса:**
  1. Одновременно в 2 параллельных потока отправить `POST /property-view/hotels/1/amenities` и `POST /property-view/hotels/2/amenities` с телом `["UniqueConcurrentAmenity"]`.
  2. Проверить коды ответов обоих потоков.
- **Ожидаемый результат:** Оба запроса возвращают `200 OK`.
- **Фактический результат и статус:** **FAILED (Known Defect [DEF-01](../ISSUES.md))** — Один из потоков при одновременной вставке получает `500 Internal Server Error` из-за нарушения уникального индекса `uq_amenity_normalized_name` (повторный запрос проходит со статусом `200 OK`).

### TC-30: Поиск по удобству с несколькими пробелами подряд между словами в query-параметре
- **ID:** `TC-30`
- **Приоритет:** Low
- **Связанное требование:** `FR-11`, `FR-12`
- **Модуль/подмодуль:** `HotelSearchCriteria#cleanAmenities`
- **Заглавие (суть):** Проверка поиска `GET /property-view/search?amenities=Free%20WiFi` (одиночный пробел) и граничного случая с двойным пробелом
- **Исходные данные:** Отель `id=1` с удобством `"Free WiFi"`.
- **Шаги тест-кейса:**
  1. Выполнить `GET /property-view/search?amenities=Free%20WiFi`.
  2. Выполнить `GET /property-view/search?amenities=Free%20%20WiFi` (два пробела).
- **Ожидаемый результат:** Шаг 1 возвращает отель `id=1`; шаг 2 фиксирует поведение нормализации пробелов ([DEF-02](../ISSUES.md)).
- **Фактический результат и статус:** **PASSED (с замечанием [DEF-02](../ISSUES.md))** — Шаг 1 находит отель (`200 OK`), шаг 2 возвращает `[]` и зарегистрирован как косметический дефект `DEF-02`.
