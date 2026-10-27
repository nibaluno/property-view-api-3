# Отчёт по Заданию 4: Тестирование вёрстки и кроссбраузерности, Postman API, безопасности и UI-автотестов
**Проект:** `property-view-api`  
**Дата:** 2026-10-26  
**Связанные артефакты:**
- Клиентский веб-интерфейс: [src/main/resources/static/index.html](../src/main/resources/static/index.html) и [favicon.svg](../src/main/resources/static/favicon.svg)
- Коллекция Postman: [docs/postman/property-view-api.postman_collection.json](postman/property-view-api.postman_collection.json)
- Отчёт прогона коллекции Postman: [docs/evidence/postman-report.html](evidence/postman-report.html) и [docs/evidence/postman-report.json](evidence/postman-report.json)
- Автотесты UI (Playwright + Page Object): [ui-tests/pages/CatalogPage.js](../ui-tests/pages/CatalogPage.js) и [ui-tests/tests/catalog.spec.js](../ui-tests/tests/catalog.spec.js)

---

## 1. Тестирование вёрстки, адаптивности и кроссбраузерности клиентской части

Клиентская часть приложения расположена по адресу `http://localhost:8092/property-view/index.html` и представляет собой одностраничное веб-приложение (SPA на базе семантического HTML5, CSS Grid / Flexbox и Fetch API) для работы с каталогом отелей, многокритериальным поиском и аналитическими гистограммами.

### 1.1. Выполненные проверки визуальной части, адаптивности, интерактива и реальных данных
1. **Визуальная часть:**
   - **Целостность блоков и выравнивание по сетке:** Основной макет построен на двухколоночной CSS Grid сетке (`grid-template-columns: 340px 1fr`), а каталог карточек отелей — на адаптивной сетке `repeat(auto-fill, minmax(280px, 1fr))` с единым отступом `1rem`.
   - **Корректное отображение текста вокруг изображений:** В каждой карточке `.hotel-card` блок миниатюры `.hotel-thumb` (`56×56 px`, `float: left; margin: 0 0.75rem 0.4rem 0`) корректно обтекается заголовком `<h3>` и метаданными адреса/описания без наложения текста на иконку.
2. **Адаптивность (ширина экрана и масштаб):**
   - Проверены разрешения: Desktop FullHD (`1920×1080`), Laptop (`1280×800`), Tablet (`768×1024`) и Mobile (`375×812`, iPhone X / Pixel 5), а также масштабирование страницы `75%`, `100%`, `125%`, `150%`.
   - При ширине экрана `<= 900px` медиа-запрос `@media (max-width: 900px)` перестраивает двухколоночный макет в одноколоночный стек без горизонтальной прокрутки (`overflow-x: hidden`).
3. **Интерактивные элементы:**
   - Все кнопки (`.btn-primary`, `.btn-secondary`, `.hist-btn`) и навигационные ссылки кликабельны и изменяют цвет фона при наведении (`:hover`).
   - При установке фокуса (`Tab` / клик) в поля ввода форм `#search-form`, `#create-hotel-form` и `#add-amenity-form` применяется визуальное кольцо фокуса `box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.25)`.
   - Клик по логотипу `#logo-link` (`Property View Catalog`) в шапке выполняет переход на главную страницу `/property-view/index.html`.
   - В `<head>` подключена векторная иконка сайта `<link rel="icon" type="image/svg+xml" href="/property-view/favicon.svg">`, возвращающая статус `200 OK`.
4. **Работа с реальными и граничными данными:**
   - **Очень длинные строки:** названия отелей длиной `255` символов и описания длиной до `4000` символов без пробелов корректно переносятся внутри карточки благодаря свойствам `overflow-wrap: anywhere; word-break: break-word` без разрыва сетки.
   - **Очень короткие строки:** односимвольные значения (`name: "H"`, `brand: "B"`, `houseNumber: 1`) выравниваются без схлопывания карточек.
   - **Пустые списки:** при отсутствии отелей в БД или при поиске без совпадений (`?city=nowhere`) скрывается сетка и отображается информативный блок `#empty-state` (`"No hotels found matching the current criteria."`).
   - **Большое количество элементов:** при отображении `50+` карточек отелей и `20+` бейджей удобств сетка автоматически переносит строки без деградации FPS и вёрстки.
5. **Отсутствие ошибок 404 и ошибок в консоли (DevTools Network & Console):**
   - Вкладка **Network:** все ресурсы (`index.html`, `favicon.svg`, REST-вызовы `/property-view/hotels`, `/search`, `/histogram/*`) возвращают HTTP `200`/`201`. Ошибок `404` при загрузке страницы и работе с интерфейсом — **`0`**.
   - Вкладка **Console:** ошибок (`console.error`) и необработанных исключений Promise (`Uncaught (in promise)`) — **`0`**.

### 1.2. Таблица совместимости «Проверка × Браузер»

| № | Категория и проверка | Google Chrome (Desktop 1440×900) | Mozilla Firefox (Desktop 1440×900) | Apple Safari / WebKit (Desktop 1440×900) | Mobile Emulation (DevTools 375×812, Pixel 5 / iPhone) |
|---|---|---|---|---|---|
| 1 | **Целостность блоков и выравнивание по CSS Grid сетке** | PASSED (ровная 2-колоночная сетка) | PASSED (ровная 2-колоночная сетка) | PASSED (ровная 2-колоночная сетка) | PASSED (перестроение в 1 колонку при `<=900px`) |
| 2 | **Обтекание миниатюры `.hotel-thumb` текстом (`float: left`)** | PASSED (отступ `0.75rem`, без наложений) | PASSED (отступ `0.75rem`, без наложений) | PASSED (отступ `0.75rem`, без наложений) | PASSED (корректное обтекание на узком экране) |
| 3 | **Адаптивность при масштабе `75% / 100% / 125% / 150%`** | PASSED (без горизонтального скролла) | PASSED (без горизонтального скролла) | PASSED (без горизонтального скролла) | PASSED (корректный `viewport width=device-width`) |
| 4 | **Кликабельность кнопок и реакция на `:hover`** | PASSED (`#1e3a8a` $\rightarrow$ `#1d4ed8`) | PASSED (`#1e3a8a` $\rightarrow$ `#1d4ed8`) | PASSED (`#1e3a8a` $\rightarrow$ `#1d4ed8`) | PASSED (активация по tap без залипания) |
| 5 | **Установка `:focus` в полях форм и навигация с клавиатуры (`Tab`)** | PASSED (синее кольцо фокуса `3px`) | PASSED (синее кольцо фокуса `3px`) | PASSED (синее кольцо фокуса `3px`) | PASSED (автопрокрутка к активному полю ввода) |
| 6 | **Переход на главную по логотипу (`#logo-link`) и наличие `favicon.svg`** | PASSED (`200 OK`, иконка во вкладке) | PASSED (`200 OK`, иконка во вкладке) | PASSED (`200 OK`, иконка во вкладке) | PASSED (`200 OK`, переход на `/property-view/index.html`) |
| 7 | **Длинные строки (`255`–`4000` симв.) и короткие строки (`1` симв.)** | PASSED (`overflow-wrap: anywhere`) | PASSED (`overflow-wrap: anywhere`) | PASSED (`overflow-wrap: anywhere`) | PASSED (перенос длинных строк внутри карточки) |
| 8 | **Отображение пустого списка (`#empty-state`) и списка из `50+` элементов** | PASSED (блок-заглушка / плавный скролл) | PASSED (блок-заглушка / плавный скролл) | PASSED (блок-заглушка / плавный скролл) | PASSED (блок-заглушка / вертикальный скролл) |
| 9 | **Отсутствие ошибок `404` во вкладке Network и ошибок в Console** | PASSED (`0` ошибок Console, `0` `404`) | PASSED (`0` ошибок Console, `0` `404`) | PASSED (`0` ошибок Console, `0` `404`) | PASSED (`0` ошибок Console, `0` `404`) |

Скриншоты проверок сохранены в директории `docs/evidence/`:
- Десктопный вид каталога: [docs/evidence/ui-catalog-desktop.png](evidence/ui-catalog-desktop.png)
- Создание отеля и карточка деталей: [docs/evidence/ui-hotel-created.png](evidence/ui-hotel-created.png), [docs/evidence/ui-hotel-details.png](evidence/ui-hotel-details.png)
- Фильтрация поиска и гистограмма: [docs/evidence/ui-search-filtered.png](evidence/ui-search-filtered.png), [docs/evidence/ui-histogram-amenities.png](evidence/ui-histogram-amenities.png)
- Мобильная эмуляция (`375×812`): [docs/evidence/ui-mobile-viewport.png](evidence/ui-mobile-viewport.png)

---

## 2. Тестирование API в Postman

Разработана коллекция Postman v2.1: [docs/postman/property-view-api.postman_collection.json](postman/property-view-api.postman_collection.json), включающая **15 запросов** и **24 автопроверки (`pm.test`)**, охватывающая все эндпоинты вариантов использования:
1. **Связывание запросов (Request Chaining):**
   - Запрос `1. Auth — Login as Alice` (`POST /property-view/auth/login`) извлекает выданный токен из JSON-ответа (`body.token`) и сохраняет его в переменную коллекции `{{authToken}}`.
   - Запрос `2. Auth — Login as Bob` сохраняет токен второго пользователя в `{{otherToken}}` для последующей проверки IDOR.
   - Запрос `3. Hotels — Create Hotel` (`POST /property-view/hotels`) сохраняет `body.id` созданного отеля в переменную `{{hotelId}}`, которая автоматически подставляется в запросы `POST /hotels/{{hotelId}}/amenities`, `GET /hotels/{{hotelId}}`, `POST /hotels/{{hotelId}}/claim` и `GET /hotels/{{hotelId}}/management`.
2. **Позитивные проверки:** проверка кодов `200 OK`, `201 Created`, `204 No Content`, наличия заголовка `Location`, структуры и значений полей краткой и полной карточек отеля, дедупликации удобств, поиска и гистограмм.
3. **Негативные проверки:** вызов защищённой ручки без авторизации (`401 Unauthorized`), доступ с чужим токеном (`403 Forbidden`), создание отеля с пустыми/некорректными данными (`400 Bad Request` с картой `errors`), неизвестный параметр гистограммы `/histogram/stars` (`400 Bad Request`), обращение к несуществующему отелю `/hotels/999999` (`404 Not Found`).
4. **Отчёт выполнения коллекции:**
   - Сформированы HTML и JSON отчёты прогона коллекции: [docs/evidence/postman-report.html](evidence/postman-report.html) и [docs/evidence/postman-report.json](evidence/postman-report.json) (**15 из 15 запросов и 24 из 24 assertions выполнены успешно, 0 ошибок**).

---

## 3. Базовые проверки безопасности и логирования

| Проверка безопасности | Способ проверки (Запрос) | Ожидаемый результат | Фактический результат | Статус |
|---|---|---|---|---|
| **1. Доступ в авторизованную зону без авторизации по прямому URL** | `GET /property-view/hotels/1/management` и `DELETE /property-view/hotels/1` без заголовка `Authorization` | Отказ в доступе со статусом `401 Unauthorized` (`application/problem+json`) | Возвращён `401 Unauthorized`: `{"title":"Unauthorized","status":401,"detail":"Missing Authorization header"}` | **PASSED** |
| **2. Подмена идентификатора в URL (IDOR: `id=1` $\rightarrow$ `id=2`)** | Пользователь `bob` (`Bearer pv-token-bob`) вызывает `GET /property-view/hotels/1/management` или `DELETE /property-view/hotels/1` для отеля пользователя `alice` | Доступ к чужим данным запрещён со статусом `403 Forbidden` | Возвращён `403 Forbidden`: `{"title":"Forbidden","status":403,"detail":"Access denied: user 'bob' is not authorized to manage hotel with id 1"}` | **PASSED** |
| **3. Защита от раскрытия внутренней информации в сообщениях об ошибках** | Провокация ошибок `400` (битый JSON/валидация), `401`, `403`, `404` (`/hotels/999999`), `500` (`GlobalExceptionHandler#handleUnexpected`) | В ответах отсутствуют стектрейсы Java (`at com.propertyview...`), пути файловой системы и текст SQL-запросов | Все ошибки возвращаются строго как RFC 7807 `ProblemDetail`; при `500` клиенту отдаётся только `{"title":"Internal server error","status":500,"detail":"Unexpected error"}` | **PASSED** |
| **4. Логирование основных операций и ошибок** | Выполнение операций создания отеля, добавления удобств, поиска, гистограмм, входа и ошибок валидации/IDOR; проверка [docs/evidence/application.log](evidence/application.log) | В серверном логе фиксируются события `INFO` для бизнес-операций и `WARN`/`ERROR` для ошибок и попыток несанкционированного доступа | В логе присутствуют записи `CreateHotelService`, `AddAmenitiesService`, `SearchHotelsService`, `HistogramService`, `ManageHotelService` и `GlobalExceptionHandler` | **PASSED** |

### Выдержка из журнала событий сервера ([docs/evidence/application.log](evidence/application.log)):
```text
INFO  c.p.a.service.ManageHotelService         : User 'alice' authenticated successfully
INFO  c.p.a.service.CreateHotelService         : Created hotel id=1 name='DoubleTree by Hilton Minsk' brand='Hilton'
INFO  c.p.a.service.AddAmenitiesService        : Updated amenities for hotel id=1 (changed=true, totalAmenities=3)
INFO  c.p.a.service.SearchHotelsService        : Executed hotel search (emptyCriteria=false, matchedCount=1)
INFO  c.p.a.service.HistogramService           : Calculated histogram for param=AMENITIES (buckets=3)
INFO  c.p.a.service.ManageHotelService         : Assigned ownership of hotel id=1 to user 'alice'
WARN  c.p.p.web.error.GlobalExceptionHandler   : Unauthorized access attempt: Missing Authorization header
WARN  c.p.a.service.ManageHotelService         : IDOR attempt detected: user 'bob' tried to access hotel id=1 owned by 'alice'
WARN  c.p.p.web.error.GlobalExceptionHandler   : Forbidden access attempt: Access denied: user 'bob' is not authorized to manage hotel with id 1
WARN  c.p.p.web.error.GlobalExceptionHandler   : Request validation failed: [name, brand, address, contacts, arrivalTime]
```

---

## 4. Автоматизация UI-тестов (Playwright + паттерн Page Object)

Для автоматизации тестирования пользовательского интерфейса разработан тестовый набор на базе **Playwright**:
- **Класс Page Object:** [ui-tests/pages/CatalogPage.js](../ui-tests/pages/CatalogPage.js) — инкапсулирует все локаторы элементов страницы (`/property-view/index.html`) и бизнес-действия (`open()`, `createHotel()`, `searchBy()`, `openHotelDetailsByName()`, `addAmenitiesToSelectedHotel()`, `loadHistogram()`).
- **Тестовый набор:** [ui-tests/tests/catalog.spec.js](../ui-tests/tests/catalog.spec.js) и конфигурация [ui-tests/playwright.config.js](../ui-tests/playwright.config.js):
  1. **Паттерн Page Object:** тесты не обращаются к низкоуровневым селекторам напрямую, а вызывают методы объекта `CatalogPage`.
  2. **Полная независимость тестов и генерация собственных данных:** каждый тест генерирует уникальный идентификатор `uid = Date.now() + '_' + Math.random()` и создаёт собственные тестовые отели/города/удобства, поэтому тесты могут запускаться параллельно (`fullyParallel: true`) и в любом порядке.
  3. **Явные ожидания вместо фиксированных пауз:** в коде нет ни одного `waitForTimeout` / `Thread.sleep`; используются только явные ожидания сетевых ответов (`page.waitForResponse(...)`) и веб-утверждения Playwright (`await expect(locator).toBeVisible()`, `toContainText()`, `toHaveCount()`).
  4. **Автоматическое сохранение скриншота при падении теста:** настроено как на уровне конфигурации (`screenshot: 'only-on-failure'`), так и в хуке `test.afterEach`, который при несовпадении `testInfo.status !== testInfo.expectedStatus` сохраняет полностраничный скриншот в `docs/evidence/failed_<test_name>.png`.
