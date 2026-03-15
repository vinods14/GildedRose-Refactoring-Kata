# Application Flow — Gilded Rose Inventory API

## Overview

Every call follows two paths: the **Happy Path** (valid data in, updated data out)
and one of several **Error Paths** (invalid data → structured error response).

---

## Happy Path — POST /api/items/update-quality

```
Client
  │
  │  POST /api/items/update-quality
  │  Content-Type: application/json
  │  Body: [ {"name":"Aged Brie","sellIn":5,"quality":20}, ... ]
  │
  ▼
┌─────────────────────────────────────────────────────────────┐
│  HTTP Layer  (Spring DispatcherServlet)                      │
│  Deserialises JSON → ItemRequest[]                          │
└──────────────────────────┬──────────────────────────────────┘
                           │  ItemRequest[]
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  GildedRoseController  [adapter/in/web]                     │
│                                                             │
│  1. log.info("Received update-quality request for N items") │
│  2. itemMapper.toItems(requests)  ──────────────────────┐   │
│  3. inventoryUpdateService.updateInventory(items)       │   │
│  4. itemMapper.toItemResponses(items) ◄─────────────────┘   │
│  5. log.info("Successfully updated N items")                │
│  6. return ResponseEntity.ok(responses)                     │
└──────────┬──────────────────────────────────────────────────┘
           │
           │  Step 2: ItemRequest[] → Item[]
           ▼
┌─────────────────────────────────────────────────────────────┐
│  ItemMapper  [adapter/in/web/mapper]  (MapStruct generated) │
│                                                             │
│  toItems(ItemRequest[])                                     │
│    → for each request: new Item(name, sellIn, quality)      │
│  toItemResponses(Item[])                                    │
│    → for each item: new ItemResponse(name, sellIn, quality) │
└─────────────────────────────────────────────────────────────┘

           │  Step 3: Item[]
           ▼
┌─────────────────────────────────────────────────────────────┐
│  GildedRoseInventoryService  [application/service]          │
│                                                             │
│  updateInventory(Item[] items)                              │
│    if items == null → log.info + return                     │
│    log.info("Starting inventory update for N items")        │
│    for each item → updateSingleItem(item)                   │
│                                                             │
│  updateSingleItem(Item item)  [private]                     │
│    if item == null     → throw InvalidItemException         │
│    if name blank/null  → throw InvalidItemException         │
│    log.debug("Updating item: 'name'")                       │
│    strategy = getStrategy(item.name)                        │
│    strategy.updateQuality(item)                             │
│                                                             │
│  getStrategy(String itemName)  [private]                    │
│    Chain of Responsibility — first match wins:              │
│      SulfurasUpdateStrategy    canHandle?  ──► yes/no       │
│      AgedBrieUpdateStrategy    canHandle?  ──► yes/no       │
│      BackstagePassUpdateStrategy canHandle? ─► yes/no       │
│      ConjuredItemUpdateStrategy  canHandle? ─► yes/no       │
│      NormalItemUpdateStrategy    canHandle? ─► true (always)│
│    return strategy                                          │
└──────────┬──────────────────────────────────────────────────┘
           │  ItemUpdateStrategy
           ▼
┌─────────────────────────────────────────────────────────────┐
│  BaseQualityUpdateStrategy  [domain/strategy]               │
│  Template Method Pattern — fixed skeleton, variable steps   │
│                                                             │
│  final updateQuality(item)                                  │
│    1. updateQualityBeforeSellIn(item)   ◄── subclass hook   │
│    2. decrementSellIn(item)             ◄── can override    │
│    3. if isExpired(item)                                    │
│         → updateQualityAfterSellIn(item) ◄── subclass hook  │
│                                                             │
│  Static helpers (used by all concrete strategies):          │
│    increaseQuality(item, n)  → capped at MAX_QUALITY (50)   │
│    decreaseQuality(item, n)  → floored at MIN_QUALITY (0)   │
│    setQuality(item, n)       → clamped to [0, 50]           │
│    isExpired(item)           → item.sellIn < 0              │
└─────────────────────────────────────────────────────────────┘

  Item[] is mutated in-place (sellIn and quality updated)

           │  Item[] (updated)  flows back to controller (Step 4)
           ▼
  200 OK  →  [ {"name":"Aged Brie","sellIn":4,"quality":21}, ... ]
```

---

## Strategy Behaviour at a Glance

| Item | Before sell date | After sell date | Special |
|---|---|---|---|
| **Normal** | quality −1/day | quality −2/day | — |
| **Aged Brie** | quality +1/day | quality +2/day | — |
| **Sulfuras** | no change | no change | sellIn never changes |
| **Backstage Pass** | +1 (>10d), +2 (≤10d), +3 (≤5d) | quality → 0 | — |
| **Conjured** | quality −2/day | quality −4/day | starts with "Conjured" |

---

## Error Paths

### 1 — Null or blank item name

```
Client sends: [ {"name": "", "sellIn": 5, "quality": 20} ]

GildedRoseController
  → itemMapper.toItems(requests)       Item[] contains Item("", 5, 20)
  → inventoryUpdateService.updateInventory(items)
      → updateSingleItem(item)
          item.name.isBlank() == true
          log.warn("Encountered item with null or blank name")
          throw InvalidItemException("Item name must not be null or blank")

GlobalExceptionHandler.handleInvalidItemException()
  log.warn("Invalid item request at /api/items/update-quality: ...")
  return 400 Bad Request + ErrorResponse {
    status:    400,
    error:     "Bad Request",
    message:   "Item name must not be null or blank",
    path:      "/api/items/update-quality",
    timestamp: "2026-03-14T22:00:00"
  }
```

### 2 — Malformed JSON

```
Client sends:  not-valid-json

Spring DispatcherServlet
  → Jackson fails to deserialise
  → throws HttpMessageNotReadableException

GlobalExceptionHandler.handleHttpMessageNotReadable()  [overrides parent]
  log.warn("Malformed JSON request at ...")
  return 400 Bad Request + ErrorResponse {
    status:  400,
    error:   "Bad Request",
    message: "Malformed or unreadable JSON request body",
    path:    "/api/items/update-quality",
    ...
  }
```

### 3 — Unexpected error

```
Any uncaught Exception propagates to:

GlobalExceptionHandler.handleGenericException()
  log.error("Unexpected error at ...", ex)
  return 500 Internal Server Error + ErrorResponse {
    status:  500,
    error:   "Internal Server Error",
    message: "An unexpected error occurred",
    ...
  }
```

---

## Component Dependency Map

```
[HTTP Client]
    │
    ▼
GildedRoseController ──── depends on ──► InventoryUpdateService (interface/port)
    │                                           │
    │                                           ▼ (implemented by)
    │                               GildedRoseInventoryService
    │                                 holds List<ItemUpdateStrategy>
    │                                 owns private getStrategy()
    │
    │                              each strategy extends
    │                           BaseQualityUpdateStrategy
    │                             (static quality helpers)
    │
ItemMapper (MapStruct)
    │
    ▼
ItemRequest ──(mapper)──► Item ──(mapper)──► ItemResponse
```

---

## Sequence Diagram (text form)

```
Client  Controller    Mapper    Service      Strategy
  │         │            │         │            │
  │─POST───►│            │         │            │
  │         │─toItems()─►│         │            │
  │         │◄─Item[]────│         │            │
  │         │─updateInventory()───►│            │
  │         │            │         │─getStrategy(name)          │
  │         │            │         │  (stream.filter.findFirst) │
  │         │            │         │─updateQuality(item)───────►│
  │         │            │         │              │  static helpers mutate item
  │         │◄────────────────────Item[] mutated  │
  │         │─toItemResponses()──►│               │
  │         │◄─ItemResponse[]────│               │
  │◄200─────│            │         │             │
```
