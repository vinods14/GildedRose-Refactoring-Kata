# Architecture — Gilded Rose Inventory API

## Pattern: Hexagonal Architecture (Ports & Adapters)

Hexagonal Architecture, introduced by Alistair Cockburn, organises an application into
concentric zones. The innermost zone contains pure business logic. Each outer zone knows
about the inner zones but never the reverse. Communication between zones is mediated by
**ports** (interfaces) and **adapters** (implementations).

```
┌──────────────────────────────────────────────────────────────┐
│  INFRASTRUCTURE / FRAMEWORK                                  │
│  (Spring MVC, Jackson, MapStruct, SpringDoc)                 │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  ADAPTER LAYER  (adapter/in/web)                       │  │
│  │  GildedRoseController   ItemMapper                     │  │
│  │  GlobalExceptionHandler  DTOs                          │  │
│  │                                                        │  │
│  │  ┌──────────────────────────────────────────────────┐  │  │
│  │  │  APPLICATION LAYER  (application/)               │  │  │
│  │  │  InventoryUpdateService ◄── INPUT PORT           │  │  │
│  │  │  GildedRoseInventoryService                      │  │  │
│  │  │    (orchestration + strategy selection)          │  │  │
│  │  │                                                  │  │  │
│  │  │  ┌────────────────────────────────────────────┐  │  │  │
│  │  │  │  DOMAIN LAYER  (domain/)                   │  │  │  │
│  │  │  │  Item  (data model — DO NOT MODIFY)        │  │  │  │
│  │  │  │  ItemUpdateStrategy ◄── DOMAIN PORT        │  │  │  │
│  │  │  │  BaseQualityUpdateStrategy (template +     │  │  │  │
│  │  │  │    static quality/sell-in helpers)         │  │  │  │
│  │  │  │  *UpdateStrategy (5 implementations)       │  │  │  │
│  │  │  └────────────────────────────────────────────┘  │  │  │
│  │  └──────────────────────────────────────────────────┘  │  │
│  └────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

---

## Layers in Detail

### Domain Layer — `domain/`

The innermost ring. Contains the business rules that define *what* the application does.
No Spring annotations. No HTTP concepts. No logging framework dependencies.

| Class | Role |
|---|---|
| `Item` | Plain data model. Public fields per kata constraint. Never modified. |
| `ItemUpdateStrategy` | Interface (domain port). Defines the `updateQuality` contract for one item type. |
| `BaseQualityUpdateStrategy` | Abstract base. Implements Template Method: calls `updateQualityBeforeSellIn` → `decrementSellIn` → (if expired) `updateQualityAfterSellIn`. Provides `protected static` helpers: `increaseQuality`, `decreaseQuality`, `setQuality`, `isExpired`. |
| `NormalItemUpdateStrategy` | Default rule: −1/day, −2/day after sell date. |
| `AgedBrieUpdateStrategy` | Appreciates: +1/day, +2/day after sell date. |
| `SulfurasUpdateStrategy` | Legendary: quality and sellIn never change. |
| `BackstagePassUpdateStrategy` | Concert pass: +1, then +2 (≤10d), then +3 (≤5d), then 0 after concert. |
| `ConjuredItemUpdateStrategy` | Magic: degrades 2× faster than normal (−2/day, −4/day after sell date). |

**Why this structure?**
Adding a new item type requires *only* creating a new `*UpdateStrategy` class and
registering it in `GildedRoseConfiguration`. Nothing else changes — Open/Closed Principle
in practice. Each strategy calls the `protected static` quality helpers inherited from
`BaseQualityUpdateStrategy` instead of delegating to separate service objects.

---

### Application Layer — `application/`

Orchestration without business logic. Answers *how* the domain is coordinated.

| Class | Role |
|---|---|
| `InventoryUpdateService` | **Input Port** interface. The controller depends on this, not on any concrete class. Inversion of Control at the boundary. |
| `GildedRoseInventoryService` | Implements the port. Holds the ordered `List<ItemUpdateStrategy>`. Iterates items, validates them (null/blank name), selects a strategy via a private `getStrategy()` method (stream + `canHandle()`), and fires it. Contains zero business rules. |

**Why no separate factory class?**
Strategy selection is a one-liner: `strategies.stream().filter(s -> s.canHandle(name)).findFirst()`.
Extracting it to a dedicated factory class adds indirection without adding value — the
service *is* the only consumer. This keeps the application layer to its minimum
necessary shape.

---

### Adapter Layer — `adapter/in/web/`

Translates HTTP into application calls and back. No business logic whatsoever.

| Class | Role |
|---|---|
| `GildedRoseController` | Receives `ItemRequest[]`, delegates to the port, returns `ItemResponse[]`. Knows about HTTP and DTOs; nothing else. |
| `ItemMapper` | MapStruct-generated. Converts `ItemRequest ↔ Item ↔ ItemResponse`. Field names match — no `@Mapping` annotations needed. |
| `ItemRequest` / `ItemResponse` | DTOs. `@Schema` annotations for Swagger. Completely independent of the domain `Item`. |
| `GlobalExceptionHandler` | Catches `InvalidItemException` (400), `HttpMessageNotReadableException` (400), and `Exception` (500). Returns a consistent `ErrorResponse` envelope. |

**Why DTOs instead of exposing `Item` directly?**
`Item` is a legacy kata class with public fields and no no-arg constructor. Exposing it
directly over HTTP would couple the API contract to the kata's constraints. DTOs decouple
the wire format from the domain model — the API can evolve independently.

---

### Configuration & Cross-cutting — `config/`, `common/`

| Class | Role |
|---|---|
| `GildedRoseConfiguration` | **The only place Spring annotations touch the domain internals.** Single `@Bean` method that wires `GildedRoseInventoryService` with all five concrete strategies (Sulfuras, Aged Brie, Backstage Pass, Conjured, Normal) in priority order. This is the composition root. |
| `OpenApiConfiguration` | `@OpenAPIDefinition` metadata for Swagger UI. |
| `InvalidItemException` | Unchecked exception in `common/` so both the adapter layer (handler) and application layer (service) can reference it without violating layering. |
| `ErrorResponse` | Wire-format error envelope: status, error, message, path, timestamp. |

---

## Design Patterns Used

### 1. Strategy Pattern
`ItemUpdateStrategy` defines the algorithm interface. Each concrete strategy encapsulates
one item type's rules. The client (`GildedRoseInventoryService`) is unaware of which
strategy it holds.

```
ItemUpdateStrategy (interface)
  ├── NormalItemUpdateStrategy
  ├── AgedBrieUpdateStrategy
  ├── SulfurasUpdateStrategy
  ├── BackstagePassUpdateStrategy
  └── ConjuredItemUpdateStrategy
```

### 2. Template Method Pattern
`BaseQualityUpdateStrategy.updateQuality()` is declared `final`. It defines the
*skeleton* of the algorithm (before-sell → decrement-sell-in → after-sell if expired)
and delegates the *variable steps* to abstract hook methods that subclasses implement.
Quality and sell-in helpers are `protected static` methods on the base class.

```java
// fixed skeleton — subclasses cannot reorder steps
final void updateQuality(Item item) {
    updateQualityBeforeSellIn(item);   // abstract hook
    decrementSellIn(item);             // can override (Sulfuras does)
    if (isExpired(item)) {
        updateQualityAfterSellIn(item); // abstract hook — only when expired
    }
}

// Shared static helpers — subclasses call these directly
protected static void increaseQuality(Item item, int amount) { ... }
protected static void decreaseQuality(Item item, int amount) { ... }
protected static void setQuality(Item item, int value)       { ... }
protected static boolean isExpired(Item item)                { ... }
```

### 3. Chain of Responsibility (in the Service)
`GildedRoseInventoryService.getStrategy()` iterates the strategy list in priority order
and returns the first that answers `canHandle(itemName) == true`. The list always ends
with `NormalItemUpdateStrategy`, which returns `true` for every item name, acting as the
default. Adding a new item type is one line in `GildedRoseConfiguration`.

```java
private ItemUpdateStrategy getStrategy(String itemName) {
    return strategies.stream()
            .filter(s -> s.canHandle(itemName))
            .findFirst()
            .orElseThrow(() -> new InvalidItemException("No strategy found for: " + itemName));
}
```

### 4. Adapter Pattern (REST)
`GildedRoseController` *adapts* HTTP requests into calls on the `InventoryUpdateService`
port. The port knows nothing about HTTP; the controller knows nothing about business rules.

---

## Why Hexagonal Architecture for This Kata?

| Concern | Benefit in this project |
|---|---|
| **Testability** | Domain strategies are plain Java classes — no Spring context needed. Unit tests run instantly. |
| **Framework independence** | `GildedRoseInventoryService` and all strategies have zero Spring annotations. They can run outside of a Spring container. |
| **Open/Closed Principle** | Adding a new item type requires only a new strategy class + one line in `GildedRoseConfiguration`. No existing class is modified. |
| **Separation of concerns** | HTTP handling, orchestration, and business rules are in three distinct, non-overlapping places. A bug in the REST layer cannot affect quality calculation. |
| **Clear dependency direction** | Adapters depend on ports. Ports depend on nothing. The domain never depends on Spring. Dependency Inversion Principle is enforced structurally. |
| **Independent deployability** | The domain layer could be extracted into a library without any changes. |

---

## Dependency Direction Summary

```
adapter/in/web  ──depends on──►  application/port  ◄── implemented by ── application/service
                                                                               │
                                                               depends on ▼
                                                          domain/strategy (interface)
                                                               │
                                                    implemented by ▼
                                                 *UpdateStrategy classes
                                                 (call static helpers on
                                                  BaseQualityUpdateStrategy)

Arrow direction = "knows about / imports"
No arrow points from domain → application → adapter (the dependency rule)
```

---

## What Was Deliberately Kept Out of the Domain

| Thing | Where it lives instead | Reason |
|---|---|---|
| Spring `@Service`, `@Component` | `config/GildedRoseConfiguration` only | Domain stays framework-agnostic |
| Logging (`Logger`) | Application service only (orchestration boundary) | Domain rules have no side effects |
| HTTP concepts (`ResponseEntity`, `@RequestBody`) | Adapter layer only | Domain has no concept of HTTP |
| Jackson / JSON serialisation | DTO classes and adapter layer | Domain model pre-dates the API |
| Validation annotations (`@Valid`) | Not used — domain-meaningful validation is in the service | Keeps validation close to the business rule that defines it |
| Strategy selection / wiring | `GildedRoseConfiguration` (composition root) | Domain strategies know only their own rule, not how they are chosen |
