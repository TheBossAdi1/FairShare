# FairShare class diagram

Mermaid source. It renders in Kiro/VS Code (Markdown preview with a Mermaid extension), on GitHub, or at https://mermaid.live (paste only the code between the fences).

```mermaid
classDiagram
    direction TB

    %% ---------- interfaces ----------
    class Reviewable {
        <<interface>>
        +acknowledge()
        +dispute(String comment)
        +getReviewStatus() ReviewStatus
    }
    class Storable {
        <<interface>>
        +toCsvLine() String
    }

    %% ---------- domain classes ----------
    class User {
        -long id
        -String fullName
        -String email
        +fromCsvLine(String) User$
    }
    class Property {
        -long id
        -long landlordId
        -String name
        -String city
        -List~Space~ spaces
        +addSpace(Space)
        +fromCsvLine(String) Property$
    }
    class Space {
        -long id
        -String name
        -SpaceType type
        +fromCsvLine(String) Space$
    }
    class Tenancy {
        -long id
        -long tenantId
        -long spaceId
        -LocalDate startDate
        -LocalDate endDate
        -BigDecimal monthlyRent
        -BigDecimal agreedDeposit
        -TenancyStatus status
        +setStatus(TenancyStatus)
        +fromCsvLine(String) Tenancy$
    }
    class Inspection {
        -long id
        -long spaceId
        -InspectionType type
        -LocalDate date
        -ReviewStatus reviewStatus
        -String reviewComment
        +fromCsvLine(String) Inspection$
    }
    class DepositDeduction {
        -long id
        -long tenancyId
        -BigDecimal amount
        -String reason
        -DeductionStatus status
        -String tenantComment
        +reject()
        +fromCsvLine(String) DepositDeduction$
    }

    %% ---------- enums ----------
    class SpaceType {
        <<enumeration>>
        PRIVATE_ROOM
        SHARED_AREA
    }
    class TenancyStatus {
        <<enumeration>>
        PENDING
        ACTIVE
        ENDED
    }
    class InspectionType {
        <<enumeration>>
        MOVE_IN
        ROUTINE
        MOVE_OUT
    }
    class ReviewStatus {
        <<enumeration>>
        PENDING
        ACKNOWLEDGED
        DISPUTED
    }
    class DeductionStatus {
        <<enumeration>>
        PROPOSED
        ACCEPTED
        DISPUTED
        REJECTED
    }

    %% ---------- registry, files, menu, config ----------
    class FairShareRegistry {
        -Map~Long, User~ users
        -List~Property~ properties
        -List~Tenancy~ tenancies
        -List~Inspection~ inspections
        -List~DepositDeduction~ deductions
        +registerTenancy(Tenancy)
        +acceptDeduction(DepositDeduction)
        +getAcceptedTotal(long) BigDecimal
    }
    class FairShareFileStore {
        -Path dataDir
        +save(FairShareRegistry) boolean
        +load(FairShareRegistry)
    }
    class ConsoleMenu {
        +run()
    }
    class AppConfig {
        -String dataDirectory
        -String defaultCurrency
        +load(String[]) AppConfig$
    }

    %% ---------- custom exceptions ----------
    class OverlappingTenancyException
    class DepositLimitExceededException
    Exception <|-- OverlappingTenancyException
    Exception <|-- DepositLimitExceededException

    %% ---------- interface implementation ----------
    Reviewable <|.. Inspection
    Reviewable <|.. DepositDeduction
    Storable <|.. User
    Storable <|.. Property
    Storable <|.. Space
    Storable <|.. Tenancy
    Storable <|.. Inspection
    Storable <|.. DepositDeduction

    %% ---------- collections: the registry owns the data ----------
    FairShareRegistry "1" *-- "0..*" User : Map of users
    FairShareRegistry "1" *-- "0..*" Property : List
    FairShareRegistry "1" *-- "0..*" Tenancy : List
    FairShareRegistry "1" *-- "0..*" Inspection : List
    FairShareRegistry "1" *-- "0..*" DepositDeduction : List
    Property "1" *-- "0..*" Space : List of spaces

    %% ---------- links by id ----------
    Property ..> User : landlordId
    Tenancy ..> User : tenantId
    Tenancy ..> Space : spaceId
    Inspection ..> Space : spaceId
    DepositDeduction ..> Tenancy : tenancyId

    %% ---------- enum use ----------
    Space --> SpaceType
    Tenancy --> TenancyStatus
    Inspection --> InspectionType
    Inspection --> ReviewStatus
    DepositDeduction --> DeductionStatus

    %% ---------- who uses whom ----------
    FairShareRegistry ..> OverlappingTenancyException : throws
    FairShareRegistry ..> DepositLimitExceededException : throws
    FairShareFileStore ..> FairShareRegistry : saves / loads
    FairShareFileStore ..> Storable : writes lines
    ConsoleMenu --> FairShareRegistry : calls
```

Not drawn, to keep the picture readable: `Main` (starts the program and wires `AppConfig`, `FairShareFileStore`, `FairShareRegistry`, `ConsoleMenu` together).

## Audit against the course checklist

| Requirement | Result | Evidence |
|---|---|---|
| At least 4 classes of my domain | OK, 6 domain classes | User, Property, Space, Tenancy, Inspection, DepositDeduction (plus FairShareRegistry, FairShareFileStore, ConsoleMenu, AppConfig) |
| At least 2 interfaces of my domain | OK, 2 | `Reviewable` (Inspection, DepositDeduction), `Storable` (all six domain classes) |
| At least 2 collections in use | OK, 2 kinds in 6 places | `Map<Long, User>` and `List` in FairShareRegistry, `List<Space>` in Property |
| Nothing generic with no domain content | OK | The registry holds domain rules (no overlapping tenancy, deposit limit). The interfaces are `Reviewable` and `Storable`, not a generic Repository or Manager. |
| Relationships between classes shown | OK | implements, composition, id links, enum use, throws |
| Custom exceptions | OK, 2 | OverlappingTenancyException, DepositLimitExceededException |

### Honest weak spots (be ready to explain them)
- `Storable` is a technical interface (saving to files). It is real and used by every domain class and by `FairShareFileStore`, but `Reviewable` is the more domain-specific one. Present `Reviewable` first.
- There is no inheritance between domain classes. The course checklist does not require it.
- Links between objects use ids (`tenantId`, `spaceId`) and not direct references. This matches the CSV files and the database design later. Say it on purpose if asked.
