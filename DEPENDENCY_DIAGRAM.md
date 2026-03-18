# Module Dependency Diagram

## Circular Dependency Resolution (Phase 6)

### Before (CIRCULAR - BROKEN)
```
:app → :core ↔ :game  (circular dependency!)
```

### After (FIXED - Unidirectional)
```
:app
 ├── :common (shared types, entities, models)
 ├── :core (database, repository, work)
 └── :game (engine, simulation logic)
       ↓
     :core
       ↓
    :common
```

## Module Responsibilities

### :common (NO project dependencies)
**Purpose:** Shared data classes, Room entities, and utility types

**Contains:**
- `model/` - Decision, Event, WorldState, EventChain, UiEventNode
- `entity/` - CountryEntity, NpcEntity, EventEntity
- `util/` - Result sealed class

**Dependencies:**
- AndroidX Core
- Room (for entity annotations)
- Kotlinx Coroutines
- Gson

**Zero dependencies on :core or :game**

---

### :core (depends on :common)
**Purpose:** Database layer, repository pattern, background work

**Contains:**
- `db/` - AppDatabase, WorldDao, converters
- `repository/` - WorldRepository
- `work/` - SimulationWorker
- `di/` - Hilt dependency injection modules

**Dependencies:**
- `:common` (for entities and types)
- Room, Hilt, WorkManager, Gson

---

### :game (depends on :common, :core)
**Purpose:** Simulation engine, NPC reactor, game logic

**Contains:**
- `engine/` - CascadeEngine, NpcReactor, EventSink
- `model/` - Re-exports from :common (backward compat)

**Dependencies:**
- `:common` (for Decision, Event, WorldState)
- `:core` (for EventSink interface if needed)

---

### :app (depends on all)
**Purpose:** UI layer, navigation, ViewModels

**Contains:**
- `ui/` - Compose screens (Home, Decisions, Events)
- `navigation/` - NavGraph
- MainActivity, ViewModel

**Dependencies:**
- `:common` (for types used in UI)
- `:core` (for Repository injection)
- `:game` (if direct engine access needed)
- Compose, Hilt, Navigation

---

## Dependency Flow

```
┌─────────────────────────────────────────┐
│                  :app                   │
│  (UI, Navigation, ViewModels)           │
└────────────┬────────────────────────────┘
             │
    ┌────────┼──────────┐
    │        │          │
    ▼        ▼          ▼
┌──────┐  ┌──────┐  ┌────────┐
│:common│ │:core │  │ :game  │
│(types)│ │(repo)│  │(engine)│
└──────┘  └──┬───┘  └───┬────┘
             │          │
             │    ┌─────┘
             │    │
             ▼    ▼
          ┌──────────┐
          │ :common  │
          │ (shared) │
          └──────────┘
```

## Key Principles

1. **:common has ZERO project dependencies**
   - Only external libraries (AndroidX, Room, Gson)
   - Can be used by any module without circular deps

2. **Unidirectional dependency flow**
   - :app → :game → :core → :common
   - No upward dependencies allowed

3. **Shared types live in :common**
   - Data classes
   - Room entities
   - Sealed classes/enums
   - Interfaces for cross-module communication

4. **Module-specific logic stays in respective modules**
   - Database logic → :core
   - Game logic → :game
   - UI logic → :app

## Backward Compatibility

Type aliases are provided in :core and :game for smooth migration:
```kotlin
// In game/src/main/java/com/cascadesim/game/model/Decision.kt
@Deprecated("Use common instead")
typealias Decision = com.cascadesim.common.model.Decision
```

## Build Order

Gradle builds modules in dependency order:
1. `:common` (no deps)
2. `:core` (depends on :common)
3. `:game` (depends on :common, :core)
4. `:app` (depends on all)

## Testing Strategy

- Unit tests in each module test module-specific logic
- `:common` tests verify data class behavior
- `:core` tests verify database operations
- `:game` tests verify simulation logic
- `:app` tests verify UI and integration
