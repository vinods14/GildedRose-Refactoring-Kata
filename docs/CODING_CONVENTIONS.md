# Coding Conventions — Gilded Rose Inventory API

These conventions are derived from the patterns already established in this codebase.
They apply to all new code added to the project.

---

## 1. Package Structure

Follow the hexagonal architecture layering strictly. Every new class belongs to exactly
one of the following packages:

```
com.vinods.gildedrose
  │
  ├── adapter/in/web/          HTTP driving adapter — controllers, exception handler
  │     ├── dto/               Request and response DTOs (no domain types)
  │     └── mapper/            MapStruct mapper interfaces
  │
  ├── application/
  │     ├── port/              Input port interfaces (contracts for use cases)
  │     └── service/           Application service implementations (orchestration + strategy selection)
  │
  ├── common/
  │     └── exception/         Shared exceptions and error envelope DTOs
  │
  ├── config/                  Spring @Configuration and @OpenAPIDefinition classes
  │
  ├── constants/               Package-private constant holders (no instances)
  │
  └── domain/
        └── strategy/          Strategy interface, base class, and 5 concrete implementations
```

**Rule:** Dependency direction flows inward only.
`adapter → application → domain`. Nothing in `domain/` or `application/` imports from
`adapter/`.

---

## 2. Naming Conventions

### Classes

| Type | Convention | Example |
|---|---|---|
| Entity / data model | Noun | `Item` |
| DTO (request) | `<Entity>Request` | `ItemRequest` |
| DTO (response) | `<Entity>Response` | `ItemResponse` |
| Port interface | `<UseCase>Service` | `InventoryUpdateService` |
| Service implementation | `<Domain><UseCase>Service` | `GildedRoseInventoryService` |
| Strategy interface | `<Subject>Strategy` | `ItemUpdateStrategy` |
| Strategy base class | `Base<Subject>Strategy` | `BaseQualityUpdateStrategy` |
| Strategy implementation | `<ItemType>UpdateStrategy` | `AgedBrieUpdateStrategy` |
| Controller | `<Domain>Controller` | `GildedRoseController` |
| Exception handler | `GlobalExceptionHandler` | — |
| Custom exception | `<Cause>Exception` | `InvalidItemException` |
| Error DTO | `ErrorResponse` | — |
| Spring config | `<Domain>Configuration` | `GildedRoseConfiguration` |
| Constants holder | `<Category>Constants` | `QualityConstants`, `ItemNames` |

### Methods

| Purpose | Convention | Example |
|---|---|---|
| Boolean predicate | `is*` / `can*` / `has*` | `isExpired()`, `canHandle()` |
| Conversion | `to<TargetType>` | `toItem()`, `toItemResponse()` |
| Factory / selector | `get<Product>` | `getStrategy()` |
| Domain action | Verb phrase | `updateQuality()`, `decrementSellIn()` |
| Update (mutating) | `update*` / `adjust*` / `decrement*` / `increment*` | — |

### Constants

```java
// In a constants holder class — always static final, UPPER_SNAKE_CASE
public static final int MAX_QUALITY = 50;
public static final String AGED_BRIE = "Aged Brie";
```

All magic numbers and magic strings that represent business concepts **must** be
extracted to `constants/QualityConstants.java` or `constants/ItemNames.java`.

---

## 3. Class Structure Rules

### 3.1 Domain and Application Classes — No Spring Annotations

Domain and application-service classes must not carry Spring annotations (`@Service`,
`@Component`, `@Autowired`, etc.). They are wired as beans exclusively in
`config/GildedRoseConfiguration.java`.

```java
// CORRECT — domain strategy, no Spring
public class AgedBrieUpdateStrategy extends BaseQualityUpdateStrategy {
    @Override
    protected void updateQualityBeforeSellIn(Item item) {
        increaseQuality(item, 1);
    }
    // ...
}

// WRONG — Spring leaks into domain
@Service
public class AgedBrieUpdateStrategy extends BaseQualityUpdateStrategy { ... }
```

### 3.2 Constants Classes — Utility Pattern

```java
// final class, private no-arg constructor, all fields static final
public final class QualityConstants {
    private QualityConstants() {}
    public static final int MAX_QUALITY = 50;
}
```

### 3.3 DTOs — Adapter Layer Only

DTOs must stay in `adapter/in/web/dto/`. They must never be used as method parameters
or return types in the application or domain layers.

```java
// CORRECT — domain boundary uses Item, not DTOs
void updateInventory(Item[] items);

// WRONG — DTO leaks into application layer
void updateInventory(ItemRequest[] requests);
```

### 3.4 Exception Hierarchy

Custom exceptions extend `RuntimeException` (unchecked). They are defined in
`common/exception/` so both the adapter layer (handler) and the application layer
(thrower) can import them without violating the dependency rule.

```java
public class InvalidItemException extends RuntimeException {
    public InvalidItemException(String message) { super(message); }
    public InvalidItemException(String message, Throwable cause) { super(message, cause); }
}
```

---

## 4. Strategy Pattern Conventions

Every new item type requires exactly these three steps:

**Step 1** — Create a strategy class in `domain/strategy/`:

```java
public class ConjuredItemUpdateStrategy extends BaseQualityUpdateStrategy {

    private static final int CONJURED_DEGRADATION = 2;

    @Override
    protected void updateQualityBeforeSellIn(Item item) {
        decreaseQuality(item, CONJURED_DEGRADATION);   // static helper from base
    }

    @Override
    protected void updateQualityAfterSellIn(Item item) {
        decreaseQuality(item, CONJURED_DEGRADATION);   // only called when expired
    }

    @Override
    public boolean canHandle(String itemName) {
        return itemName != null && itemName.startsWith(ItemNames.CONJURED_PREFIX);
    }
}
```

**Step 2** — Add the item name constant (if new) to `constants/ItemNames.java`.

**Step 3** — Register the strategy in `GildedRoseConfiguration.inventoryUpdateService()`:

```java
return new GildedRoseInventoryService(List.of(
    new SulfurasUpdateStrategy(),
    new AgedBrieUpdateStrategy(),
    new BackstagePassUpdateStrategy(),
    new ConjuredItemUpdateStrategy(),   // ← add here, before NormalItemUpdateStrategy
    new NormalItemUpdateStrategy()
));
```

No other file needs to change.

---

## 5. Logging Conventions

Use SLF4J (`org.slf4j.Logger`) throughout. Never use `System.out.println`.

### Logger Declaration

```java
private static final Logger log = LoggerFactory.getLogger(ClassName.class);
```

### Log Level Usage

| Level | When to use | Examples |
|---|---|---|
| `ERROR` | Unexpected failures that need immediate attention | Uncaught exceptions in `handleGenericException` |
| `WARN` | Expected domain violations / bad input | Null item, blank name, malformed JSON |
| `INFO` | Significant business events, entry/exit of use-case | "Received request for N items", "Starting inventory update" |
| `DEBUG` | Per-item detail; useful during development | "Updating item: 'name'", "Selected strategy 'X'" |
| `TRACE` | Not currently used | — |

### Log Message Format

```java
// CORRECT — use SLF4J placeholders, never string concatenation
log.info("Received update-quality request for {} item(s)", requests.length);
log.debug("Updating item: '{}'", item.name);
log.warn("Invalid item request at {}: {}", request.getRequestURI(), ex.getMessage());
log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

// WRONG — string concatenation defeats lazy evaluation
log.info("Received request for " + requests.length + " items");
```

**Rule:** ERROR logs must include the exception as the last argument so the stack trace
is captured.

### What Not to Log

- Passwords, tokens, or sensitive personal data
- Full request/response bodies at INFO level (use DEBUG)
- `System.out.println` anywhere in production code

---

## 6. Exception Handling Conventions

### In the Application Layer (service)

Validate at the boundary of the application service. Do not validate in the domain.

```java
private void updateSingleItem(Item item) {
    if (item == null) {
        log.warn("Encountered null item in inventory array");
        throw new InvalidItemException("Item must not be null");
    }
    if (item.name == null || item.name.isBlank()) {
        log.warn("Encountered item with null or blank name");
        throw new InvalidItemException("Item name must not be null or blank");
    }
    // ... proceed
}
```

### In the Adapter Layer (exception handler)

All exceptions are caught by `GlobalExceptionHandler`. Do not add try/catch blocks in
the controller — let them propagate.

| Exception type | HTTP status | Handler method |
|---|---|---|
| `InvalidItemException` | 400 | `handleInvalidItemException` |
| `HttpMessageNotReadableException` | 400 | `handleHttpMessageNotReadable` (override) |
| Any other `Exception` | 500 | `handleGenericException` |

### Never Swallow Exceptions

```java
// WRONG
try {
    service.updateInventory(items);
} catch (Exception e) {
    // silent
}

// CORRECT — let GlobalExceptionHandler deal with it
service.updateInventory(items);
```

---

## 7. Null Safety Conventions

### Spring Null-Safety Annotations

When overriding Spring Framework methods whose parameters are under `@NonNullApi`,
annotate the override parameters with `@NonNull` and the return type with `@Nullable`
if the parent declares it:

```java
@Override
@Nullable
protected ResponseEntity<Object> handleHttpMessageNotReadable(
        HttpMessageNotReadableException ex,
        @NonNull HttpHeaders headers,
        @NonNull HttpStatusCode status,
        @NonNull WebRequest request) { ... }
```

Imports: `org.springframework.lang.NonNull`, `org.springframework.lang.Nullable`

### Null Guards

Apply null guards at the service layer input boundary only. Domain code may assume
non-null inputs once the guard has passed.

---

## 8. Testing Conventions

### Test Class Naming

| Test type | Convention | Example |
|---|---|---|
| Unit test for a class | `<ClassName>Test` | `GildedRoseInventoryServiceTest` |
| Slice test (web layer) | `<ClassName>Test` with `@WebMvcTest` | `GildedRoseControllerTest` |

### Test Method Naming

Use the pattern: `methodName_expectedBehaviour_whenCondition`

```java
void updateQuality_decreasesQualityByOne_forNormalItem()
void updateInventory_throwsInvalidItemException_forNullItem()
void returns400_whenInvalidItemExceptionThrown()
```

### Arrange-Act-Assert Structure

Every test must have explicit `// Arrange`, `// Act`, `// Assert` comments when the
setup is non-trivial:

```java
@Test
void updateInventory_updatesSingleItem() {
    // Arrange
    Item[] items = {new Item("Normal Item", 10, 20)};

    // Act
    service.updateInventory(items);

    // Assert
    assertEquals(19, items[0].quality);
    assertEquals(9, items[0].sellIn);
}
```

### Test Scope per Layer

| Layer | Test tool | Spring context | What is mocked |
|---|---|---|---|
| Domain strategies | JUnit 5, plain Java | None | Nothing |
| Application service | JUnit 5, plain Java | None | Nothing (use real `List.of(...)` strategies) |
| Controller | `@WebMvcTest` + MockMvc | Web slice only | `InventoryUpdateService`, `ItemMapper` |

**Rule:** Never use `@SpringBootTest` for unit tests. Web slice tests use `@WebMvcTest`.

### Mocking in Controller Tests

`@WebMvcTest` does not load `@Component` beans (including MapStruct-generated mappers).
Always declare them as `@MockBean`:

```java
@MockBean
private ItemMapper itemMapper;

@MockBean
private InventoryUpdateService inventoryUpdateService;
```

Always stub the mapper before performing the request:

```java
when(itemMapper.toItems(any(ItemRequest[].class))).thenReturn(items);
when(itemMapper.toItemResponses(any(Item[].class))).thenReturn(responses);
```

---

## 9. MapStruct Conventions

- Mapper interfaces live in `adapter/in/web/mapper/`.
- Always use `componentModel = "spring"`.
- Do **not** declare a `@Bean` for the mapper in `GildedRoseConfiguration` — the
  generated `@Component` is picked up by component scanning automatically.
- When field names match between source and target, no `@Mapping` annotations are needed.
- For array mapping, declare explicit array overloads:

```java
@Mapper(componentModel = "spring")
public interface ItemMapper {
    Item toItem(ItemRequest request);
    ItemResponse toItemResponse(Item item);
    Item[] toItems(ItemRequest[] requests);
    ItemResponse[] toItemResponses(Item[] items);
}
```

---

## 10. OpenAPI / Swagger Conventions

### DTOs

Every field in a request or response DTO must have `@Schema(description=..., example=...)`.
The class itself must have `@Schema(description=...)`.

```java
@Schema(description = "Request DTO representing an inventory item to be updated")
public class ItemRequest {

    @Schema(description = "The name of the item", example = "Aged Brie")
    private String name;
    ...
}
```

### Controller

- Use `@Tag` on the controller class to group endpoints in Swagger UI.
- Use `@Operation(summary=..., description=...)` on each endpoint method.
- Declare `@ApiResponse` for every possible status code (200, 400, 500 at minimum).

```java
@Tag(name = "Inventory", description = "Endpoints for managing Gilded Rose inventory updates")
public class GildedRoseController {

    @Operation(summary = "Update inventory quality", description = "...")
    @ApiResponse(responseCode = "200", ...)
    @ApiResponse(responseCode = "400", ...)
    @ApiResponse(responseCode = "500", ...)
    @PostMapping("/update-quality")
    public ResponseEntity<ItemResponse[]> updateQuality(...) { ... }
}
```

### Error DTO

`ErrorResponse` fields must also have `@Schema` annotations so the 400/500 response
schema is visible in Swagger UI.

---

## 11. pom.xml Dependency Rules

- Add **runtime** dependencies (e.g., MapStruct) and their **annotation processors**
  together in the same commit.
- Annotation processors go in `<annotationProcessorPaths>` inside the
  `maven-compiler-plugin` configuration, not as regular dependencies.
- Test-scoped dependencies (`spring-boot-starter-test`) must have `<scope>test</scope>`.

---

## Quick Reference Card

```
New item type?
  → domain/strategy/<ItemType>UpdateStrategy.java
  → constants/ItemNames.java  (add name constant)
  → config/GildedRoseConfiguration.java  (register in List.of(...), before NormalItemUpdateStrategy)
  → No other file changes needed

New endpoint?
  → adapter/in/web/dto/<X>Request.java  +  <X>Response.java  (with @Schema)
  → adapter/in/web/mapper/<X>Mapper.java
  → adapter/in/web/<X>Controller.java   (with @Tag, @Operation, @ApiResponse)
  → application/port/<X>Service.java    (port interface)
  → application/service/<X>ServiceImpl.java
  → config/GildedRoseConfiguration.java (wire the bean)

New validation rule?
  → application/service/<X>Service.java  (guard in updateSingleItem / equivalent)
  → common/exception/<Cause>Exception.java  (if new exception type needed)

Never:
  → Spring annotations in domain/ or application/ classes
  → Business logic in adapter/ or config/
  → try/catch in the controller (let GlobalExceptionHandler handle it)
  → String concatenation in log calls
  → @Bean for MapStruct mappers (componentModel="spring" handles it)
```
