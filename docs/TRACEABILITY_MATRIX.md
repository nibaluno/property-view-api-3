# Матрица трассируемости требований и выполненных работ (Traceability Matrix)
**Проект:** `property-view-api`  
**Дата:** 2026-10-26  
**Связанные документы:** [docs/SRS.md](SRS.md), [docs/TEST_PLAN.md](TEST_PLAN.md), [docs/CHECKLIST.md](CHECKLIST.md), [docs/TEST_CASES.md](TEST_CASES.md), [docs/LAB4_REPORT.md](LAB4_REPORT.md), [docs/LOAD_TEST_REPORT.md](LOAD_TEST_REPORT.md), [ISSUES.md](../ISSUES.md)

---

## 1. Полная матрица трассируемости (Требования $\leftrightarrow$ Артефакты Заданий 1–5)

| ID требования ([SRS.md](SRS.md)) | Приоритет | Краткое содержание требования | Чек-лист (ЛР3) | Тест-кейсы (ЛР3) | Юнит / Интеграционные / E2E автотесты (ЛР2) | Postman / UI / Безопасность / Нагрузка (ЛР4–5) | Найденные дефекты ([ISSUES.md](../ISSUES.md)) | Уровень риска |
|---|---|---|---|---|---|---|---|---|
| **FR-01** | High | Единый префикс `/property-view` на порту `8092` | `CL-01.1`..`01.3` | `TC-01` | `HotelApiEndToEndTest#everythingLivesUnderThePropertyViewPrefix` | `smoke.sh`, Postman `#1–#15` | Нет | Низкий |
| **FR-02** | High | Краткий список всех отелей `GET /hotels` по возрастанию `id` | `CL-02.1`..`02.3` | `TC-02`, `TC-10` | `HotelControllerTest#getAllReturnsBriefList`, `HotelQueryServiceTest#getAllMapsOverviewsToSummaries`, `HotelRepositoryAdapterTest#listsOverviewsOrderedById` | Postman `#6`, Playwright `Test #1`, Locust `list_hotels` | **DEF-05** (нет пагинации) | Средний |
| **FR-03** | High | Форматирование адреса в одну строку в `HotelSummary` | `CL-03.1`, `03.2` | `TC-03`, `TC-10` | `AddressTest#formatsSingleLineAsInTask`, `HotelSummaryTest`, `HotelTest#overviewUsesPhoneAndAddress` | Postman `#3`, Playwright `Test #1` | Нет | Низкий |
| **FR-04** | High | Полная карточка отеля `GET /hotels/{id}` | `CL-04.1`, `04.2` | `TC-11`, `TC-13` | `HotelControllerTest#getByIdReturnsFullStructure`, `HotelControllerTest#getByIdRejectsNonNumericId`, `HotelQueryServiceTest#getByIdReturnsDetailsWithAmenitiesInOrder` | Postman `#5`, Playwright `Test #3`, Locust `get_hotel_details` | Нет | Низкий |
| **FR-05** | High | Возврат `404 Not Found` (`ProblemDetail`) для неизвестного `id` | `CL-05.1`..`05.3` | `TC-12`, `TC-16` | `HotelControllerTest#getByIdReturns404ProblemForUnknownHotel`, `HotelAmenityControllerTest#unknownHotelGives404`, `HotelRepositoryAdapterTest#findByIdReturnsEmptyForUnknownId` | Postman `#15`, `smoke.sh` | Нет | Низкий |
| **FR-06** | High | Создание отеля `POST /hotels` (`201 Created` + `Location`) | `CL-06.1`, `06.2` | `TC-03`, `TC-26` | `HotelControllerTest#createReturns201WithLocationAndSummary`, `CreateHotelServiceTest#savesNewHotelAndReturnsSummary`, `HotelRepositoryAdapterTest#savesNewHotelAndReadsItBack` | Postman `#3`, Playwright `Test #1`, Locust `create_hotel` | Нет | Низкий |
| **FR-07** | High | Опциональность `description` и `arrivalTime.checkOut` | `CL-07.1`..`07.3` | `TC-04`, `TC-05` | `HotelControllerTest#createAcceptsMissingOptionalFields`, `HotelTest#newHotelHasNoIdAndNoAmenities` | `HotelApiEndToEndTest` (`MOSCOW_HILTON`) | Нет | Низкий |
| **FR-08** | High | Валидация обязательных полей при создании отеля (`400`) | `CL-08.1`..`08.3` | `TC-06`, `TC-07`, `TC-08` | `HotelControllerTest#createRejectsInvalidFieldsWithProblemDetail`, `HotelControllerTest#createRejectsMissingRequiredObjects`, `AddressTest` (`@ParameterizedTest`), `CreateHotelServiceTest#invalidCommandNeverReachesRepository` | Postman `#13` | **DEF-04** (слабая валидация формата `phone`) | Средний |
| **FR-09** | High | Валидация и сериализация времени `HH:mm` | `CL-09.1`..`09.3` | `TC-03`, `TC-09` | `HotelControllerTest#createRejectsMalformedTimeAndBrokenJson` | Postman `#3`, `smoke.sh` | **DEF-04** (допускается `checkIn == checkOut`) | Средний |
| **FR-10** | High | Добавление списка удобств `POST /hotels/{id}/amenities` | `CL-10.1`..`10.3` | `TC-14`, `TC-16`, `TC-26`, `TC-29` | `HotelAmenityControllerTest#addsAmenitiesAndReturnsUpdatedHotel`, `HotelAmenityControllerTest#rejectsBlankElement`, `AddAmenitiesServiceTest` | Postman `#4`, Playwright `Test #3`, Locust `add_amenities` | **DEF-01** (гонка при параллельной вставке) | **Высокий (High Risk)** |
| **FR-11** | High | Нормализация пробелов, регистронезависимая дедупликация и порядок удобств | `CL-11.1`..`11.3` | `TC-14`, `TC-15`, `TC-29`, `TC-30` | `AmenityTest` (`@ParameterizedTest`), `HotelTest#addAmenitiesKeepsOrderAndIgnoresDuplicates`, `HotelRepositoryAdapterTest#reusesAmenityRowsCaseInsensitively`, `keepsAmenityInsertionOrder` | Postman `#4`, Playwright `Test #3`, Стресс-тест | **DEF-01**, **DEF-02** | **Высокий (High Risk)** |
| **FR-12** | High | Регистронезависимый поиск по `name` (подстрока) и `brand/city/country/amenities` | `CL-12.1`..`12.3` | `TC-17`, `TC-18`, `TC-19`, `TC-30` | `SearchControllerTest#passesAllFiltersToUseCase`, `SearchHotelsServiceTest`, `HotelRepositoryAdapterTest#searchesByCityIgnoringCase`, `searchesByNameContaining` | Postman `#7`, Playwright `Test #2`, Locust `search_hotels` | **DEF-02**, **DEF-05** | **Высокий (High Risk)** |
| **FR-13** | Medium | Объединение фильтров по `AND`, форматы `amenities` (повтор и запятая), пустой фильтр | `CL-13.1`..`13.3` | `TC-18`, `TC-19` | `SearchControllerTest#acceptsCommaSeparatedAmenities`, `noParametersMeansEmptyCriteria`, `HotelSearchCriteriaTest` (`@ParameterizedTest`), `HotelRepositoryAdapterTest#searchByMultipleAmenitiesRequiresAll` | Postman `#7`, `HotelApiEndToEndTest` | Нет | Низкий |
| **FR-14** | Medium | Экранирование спецсимволов SQL `LIKE` (`%`, `_`, `\`) | `CL-14.1`, `14.2` | `TC-20` | `HotelRepositoryAdapterTest#searchTreatsLikeWildcardsLiterally` | `HotelApiEndToEndTest` | Нет | Низкий |
| **FR-15** | High | Гистограммы по `brand`, `city`, `country`, `amenities` (сортировка ключей) | `CL-15.1`..`15.3` | `TC-21` | `HistogramControllerTest#returnsCountsByCity`, `parameterIsCaseInsensitive`, `HistogramServiceTest`, `HotelRepositoryAdapterTest#countsGroupedByEachParameter` | Postman `#8`, Playwright `Test #3`, Locust `get_histogram` | **DEF-03** (collation в MySQL) | Средний |
| **FR-16** | Medium | Ошибка `400 Bad Request` со списком параметров на неизвестный параметр гистограммы | `CL-16.1`, `16.2` | `TC-22` | `HistogramParamTest#unknownValueListsSupported` (`@ParameterizedTest`), `HistogramControllerTest#unknownParameterGives400WithSupportedList` | Postman `#14`, `smoke.sh` | Нет | Низкий |
| **FR-17** | Medium | Аутентификация (`/auth/login`), запрет доступа без токена (`401`) и защита от IDOR (`403`) | `CL-17.1`..`17.4` | `TC-23`, `TC-24`, `TC-25`, `TC-26` | `ManageHotelServiceTest` (`@ParameterizedTest`), `HotelManagementControllerTest`, `HotelApiEndToEndTest#hotelLifecycleStateTransitionsAndSecurityScenario` | Postman `#1, #2, #9–#12`, Проверки безопасности ЛР4 п. 3 | Нет | Низкий |
| **FR-18** | Medium | Наличие OpenAPI (`/v3/api-docs`), Swagger UI и веб-клиента (`/index.html`) | `CL-18.1`..`18.3` | `TC-28` | `HotelApiEndToEndTest#swaggerOpenApiAndWebClientAreAvailable` | Playwright `Test #1–#4`, Таблица совместимости браузеров | Нет | Низкий |
| **NFR-01** | High | Производительность: `p95 <= 100 мс` (чтение) и `<= 200 мс` (запись) при 50 пользователях | — | `TC-29` | — | Locust / `run_load_tests.py` (Прогоны 1, 2, 3) | **DEF-01**, **DEF-05** (проявляются при `>=100` пользователях) | **Высокий (High Risk)** |
| **NFR-02** | High | Отсутствие проблемы `N+1` запросов (ровно 1 SQL-запрос на операцию) | — | — | `QueryCountTest` (`listingDoesNotQueryAmenitiesPerHotel`, `searchIsASingleQueryEvenWithAmenityFilters`, `detailsLoadHotelWithAmenitiesInOneQuery`, `histogramsAreSingleAggregateQueries`) | — | Нет | Низкий |
| **NFR-03** | High | Чистота слоёв Clean Architecture и отсутствие циклических зависимостей | — | — | `ArchitectureTest` (9 правил ArchUnit) | — | Нет | Низкий |
| **NFR-04** | High | Формат ошибок RFC 7807 `ProblemDetail` и отсутствие утечки стектрейсов/SQL | `CL-17.4` | `TC-06`, `TC-27` | `HotelApiEndToEndTest#errorsAreProblemDetails` | Проверки безопасности ЛР4 п. 3 | Нет | Низкий |
| **NFR-05** | Medium | Управление схемой через Liquibase и переносимость между H2, PostgreSQL, MySQL | — | — | `HotelRepositoryAdapterTest#liquibaseMigrationsAreApplied`, `amenityNormalizedNameIsUniqueInDatabase` | — | **DEF-03** (особенность collation в MySQL) | Средний |
| **NFR-06** | Medium | Структурное логирование основных бизнес-операций и ошибок через SLF4J | — | — | Вызовы логгера во всех сервисах и `GlobalExceptionHandler` | Анализ `docs/evidence/application.log` (ЛР4 п. 3) | Нет | Низкий |
| **NFR-07** | High | Покрытие кода тестами `>= 70%` и отсутствие нарушений статического анализатора | — | — | JaCoCo (`96.92%` инструкций, `96.80%` строк) | Checkstyle (`0` нарушений после исправления 9 ошибок) | Исправлено 9 нарушений Checkstyle | Низкий |

---

## 2. Вычисление покрытия требований тест-кейсами и анализ полноты

### 2.1. Расчёт покрытия требований
- **Функциональные требования (`FR-01`..`FR-18`):** покрыто **18 из 18** как ручными/системными тест-кейсами (`TC-01`..`TC-30`), так и автоматизированными JUnit/Playwright/Postman-тестами:
  $$\text{Покрытие FR} = \frac{18}{18} \times 100\% = \mathbf{100.0\%}$$
- **Нефункциональные требования (`NFR-01`..`NFR-07`):** покрыто **7 из 7** специализированными проверками (`QueryCountTest`, `ArchitectureTest`, `GlobalExceptionHandler` / `TC-27`, `LiquibaseTest`, `application.log`, `Locust`, `JaCoCo + Checkstyle`):
  $$\text{Покрытие NFR} = \frac{7}{7} \times 100\% = \mathbf{100.0\%}$$

### 2.2. Выделение требований без прямых системных тест-кейсов (или с инфраструктурными ограничениями)
Все 18 основных функциональных требований (`FR-01`..`FR-18`) покрыты тест-кейсами `TC-01`..`TC-30`. Вместе с тем при анализе нефункционального требования **`NFR-05` (Переносимость между СУБД H2, PostgreSQL и MySQL)** выделена подзадача, которая **не покрыта автоматическим прогоном в стандартном CI-пайплайне**:
- **Частично непокрытый аспект `NFR-05`:** автоматические интеграционные тесты `@DataJpaTest` (`HotelRepositoryAdapterTest`, `QueryCountTest`) запускаются только на in-memory СУБД **H2**, тогда как профили `postgres` и `mysql` требуют внешнего запущенного Docker-окружения (`docker compose --profile postgres up`). Именно поэтому расхождение **DEF-03** (регистронезависимая группировка в MySQL) не перехватывалось стандартным прогоном `mvn test` на H2.

---

## 3. Анализ тестов, не связанных напрямую с требованиями исходного ТЗ

При обратном сопоставлении 19 тестовых классов (`src/test/java`) со списком пользовательских требований были выявлены **4 теста**, которые не привязаны напрямую к какому-либо пользовательскому сценарию из исходного ТЗ:

| Тестовый класс и метод | Что проверяет тест | Классификация (Упущенное требование в SRS или избыточный/внутренний тест) |
|---|---|---|
| 1. `HttpAddressVerificationClientTest` (4 тестовых метода) | Проверяет взаимодействие `HttpAddressVerificationClient` с внешним HTTP-сервисом верификации адресов через HTTP-заглушку (`200`, `404/422`, `500/502/503`). | **Упущенное в исходном README интеграционное требование:** добавлено для проверки точки внешней HTTP-интеграции (см. раздел 5 [README.md](../README.md) и п. 1 Задания 2). Рекомендуется закрепить как `FR-19` при подключении внешней геолокации. |
| 2. `HotelTest#restoreRequiresId` | Проверяет, что фабричный метод `Hotel.restore(null, ...)` выбрасывает `DomainValidationException`, если из БД восстанавливается сущность без `id`. | **Внутренний защитный тест инварианта маппера (Техническое требование):** пользователь никогда не вызывает `Hotel.restore` напрямую, этот метод используется только `HotelPersistenceMapper` при чтении из БД. |
| 3. `HotelRepositoryAdapterTest#savingUnknownExistingHotelFails` | Проверяет, что вызов `repository.save(ghost)` с не-null `id = 999999L`, отсутствующим в БД, выбрасывает `HotelNotFoundException`. | **Внутренний защитный тест адаптера БД:** в прикладных сервисах перед изменением отеля всегда сначала вызывается `findById`, поэтому эта ветка в `save` является защитным барьером (defensive programming). |
| 4. `PropertyViewApiApplicationTests#contextLoads` | Проверяет старт пустого контекста Spring Boot без вызова бизнес-логики. | **Избыточный шаблонный тест:** полностью перекрывается сквозным тестом `HotelApiEndToEndTest`, который также поднимает полный контекст `@SpringBootTest` и проверяет реальные HTTP-вызовы. |

---

## 4. Зоны с высоким риском (Требования с наибольшим количеством дефектов)

Распределение обнаруженных дефектов ([ISSUES.md](../ISSUES.md)) по требованиям позволяет выделить наиболее уязвимые модули приложения:

| Ранг риска | Требования | Связанный модуль / класс | Количество и ID дефектов | Причина высокого риска и рекомендации |
|---|---|---|---|---|
| **1 (Критический фокус)** | **`FR-11`, `FR-10`, `NFR-01`** | `HotelRepositoryAdapter#appendMissingAmenities`, `Amenity`, `HotelSearchCriteria` | **2 дефекта** (`DEF-01` — Major, `DEF-02` — Minor) | Сложная логика нормализации и регистронезависимой дедупликации удобств на стыке доменного слоя и реляционной БД (`SELECT` + `INSERT` без блокировки/upsert приводит к гонке `DEF-01` под нагрузкой). |
| **2 (Высокий фокус)** | **`FR-12`, `FR-02`, `NFR-01`** | `SearchController`, `HotelController`, `HotelSpecifications` | **2 дефекта** (`DEF-02` — Minor, `DEF-05` — Moderate) | Отсутствие пагинации (`DEF-05`) и несогласованность нормализации пробелов в поисковом запросе (`DEF-02`) создают риск просадки производительности при росте каталога свыше нескольких тысяч отелей. |
| **3 (Средний фокус)** | **`FR-08`, `FR-09`** | `ContactsDto`, `ArrivalTimeDto` | **1 составной дефект** (`DEF-04` — Minor) | Декларативная валидация DTO проверяет `@NotBlank` и `@Size`, но не проверяет телефонный паттерн и бизнес-соотношение `checkIn` / `checkOut`. |
| **4 (Средний фокус)** | **`FR-15`, `NFR-05`** | `HotelJpaRepository` (`GROUP BY`) | **1 дефект** (`DEF-03` — Minor) | Зависимость регистрочувствительности `GROUP BY` от настроек collation конкретной СУБД (MySQL vs H2/PostgreSQL). |
