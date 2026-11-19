# FairShare: presentation notes (Programming 3 mid-term)

## Slide outline (6 slides, about 8 minutes)

**1. The problem and the idea**
- Shared flats: rooms, tenants, inspections and deposits are scattered in chats and spreadsheets.
- FairShare: console app that records properties, tenancies, inspections and deposit deductions.
- Two rules it protects: a private room is never double-rented; accepted deductions never exceed the deposit.

**2. Class diagram** (show the exported image from docs/class-diagram.md)
- 6 domain classes: User, Property, Space, Tenancy, Inspection, DepositDeduction.
- Support classes: FairShareRegistry (data and rules), FairShareFileStore (files), ConsoleMenu (input/output), AppConfig (settings).

**3. Interfaces and collections**
- Reviewable: acknowledge(), dispute(comment), getReviewStatus(). Implemented by Inspection and DepositDeduction. One menu method, printReview(Reviewable), prints the status of either kind: this is the interface paying off.
- Storable: toCsvLine(). Implemented by all six domain classes.
- Collections: Map<Long, User> (find a user by id), List<Property> with List<Space> inside, Lists of Tenancy, Inspection, DepositDeduction.

**4. Files and configuration**
- CSV files in the data folder: users, properties, spaces, tenancies, inspections, deductions.
- Saved on exit, loaded at start. Missing file on first start means start empty. A damaged line is skipped with a message.
- config.properties (dataDirectory, defaultCurrency) and the argument --data-dir=PATH. Priority: argument, then file, then defaults.

**5. Exceptions and validation**
- Custom: OverlappingTenancyException, DepositLimitExceededException.
- Built-in handled: NoSuchFileException, FileNotFoundException, IOException, NumberFormatException, DateTimeParseException, IllegalArgumentException, ArrayIndexOutOfBoundsException, NoSuchElementException.
- Every menu input is validated and asked again until valid.

**6. Live demo and next steps**
- Demo (4 minutes, script below).
- Next: web version with Spring Boot, PostgreSQL, login, inspection items, settlement.

## Demo script (4 minutes)
Start with an empty data folder (delete data/ before the presentation). Ids come from ONE counter shared by all object types, so they follow the order below. A rejected tenancy still uses up an id, so the inspection gets 6, not 5.

| Step | Menu option and input | What you should see |
|---|---|---|
| 1 | `1`, name `Ana Pop`, email `ana@mail` | rejected: not a valid email, asks again |
| 2 | same question, email `ana@mail.com` | User id **1** |
| 3 | `2`, landlord id `1`, `Flat Central`, `Cluj` | Property id **2** |
| 4 | `3`, property id `2`, `Room A`, type `1` (PRIVATE_ROOM) | Space id **3** |
| 5 | `4`, tenant `1`, space `3`, `2026-01-01` to `2026-06-30`, rent `300`, deposit `500` | Tenancy id **4**, status PENDING |
| 6 | `4` again, tenant `1`, space `3`, `2026-05-01` to `2026-09-01` | **Rejected**: room already rented (uses id 5) |
| 7 | `11`, tenancy `4`, new status `2` (ACTIVE) | Tenancy status ACTIVE |
| 8 | `5`, space `3`, type `1` (MOVE_IN), date `2026-01-02` | Inspection id **6** |
| 9 | `9`, inspection `6`, answer `1` (acknowledge) | review ACKNOWLEDGED |
| 10 | `6` three times: tenancy `4` with `300` (id **7**), `300` (id **8**), `200` (id **9**) | three PROPOSED deductions |
| 11 | `8`, deduction `7`, accept | ACCEPTED |
| 12 | `8`, deduction `8`, accept | **Rejected**: 600 would exceed the deposit 500. Stays PROPOSED |
| 13 | `8`, deduction `9`, accept | ACCEPTED (300 + 200 = 500, allowed) |
| 14 | `10`, deduction `8` | landlord rejects it: status REJECTED |
| 15 | `7` | list everything, with the review status line under each inspection and deduction |
| 16 | `0`, then look at data/users.csv, start again, `7` | the data is back |

Try also (quick, shows validation): option `11` on an ENDED tenancy, or a bad date like `2026-13-40`.

## Menu options
1 add user, 2 add property, 3 add space, 4 add tenancy, 5 add inspection, 6 propose deduction, 7 list everything, 8 answer a deduction (accept or dispute), 9 answer an inspection (acknowledge or dispute), 10 landlord rejects a PROPOSED deduction, 11 change a tenancy status, 0 exit.

## 10 likely questions and short answers

**1. What is an interface and why do you have Reviewable?**
An interface is a contract: it lists methods a class must provide, without bodies. Inspection and DepositDeduction are different things, but both can be acknowledged or disputed by a tenant, so both implement Reviewable.

**2. Why is fromCsvLine static?**
It builds a new object from text, so no object exists yet to call it on. A static method belongs to the class and can be called as User.fromCsvLine(line), like main is static for the same reason.

**3. Which collections do you use and why?**
A Map<Long, User> in FairShareRegistry to find a user by id in one step. Lists for properties, tenancies, inspections and deductions because order matters and we loop over them. Each Property also has a List<Space>.

**4. What happens if the data file is missing or a line is damaged?**
Missing file (first start): readAllLines throws NoSuchFileException, I catch it and start with empty lists. Damaged line: parsing throws NumberFormatException, DateTimeParseException or similar, I catch it, print the line, skip it, and keep loading the rest.

**5. Why do your custom exceptions extend Exception?**
That makes them checked exceptions. The compiler forces the caller to catch them or declare throws, so a rule violation cannot be ignored by accident. throw raises it, throws warns callers, catch handles it.

**6. How do you detect overlapping tenancies?**
Two periods overlap when each starts before the other ends: new start is not after old end, and old start is not after new end. It is checked only for PRIVATE_ROOM spaces and only for tenancies that are not ENDED. Shared areas can have several tenants.

**7. Why BigDecimal for money, not double?**
double stores some decimals inexactly (0.1 + 0.2 gives 0.30000000000000004). BigDecimal is exact. I add with add() and compare with compareTo(), never with + or ==.

**8. How does the deposit rule work?**
acceptDeduction adds up the already ACCEPTED deductions of that tenancy plus the new amount. If the total is greater than agreedDeposit it throws DepositLimitExceededException and the deduction stays PROPOSED. A PROPOSED or DISPUTED deduction never counts. Exactly equal to the deposit is allowed.

**9. Why are fields private final and why do you use getters?**
private: only the class can touch the field, so rules cannot be bypassed. final: the value is set once in the constructor and cannot change later. Getters let other code read values. Only fields that must change (statuses) are not final.

**10. Why is the logic in FairShareRegistry and not in ConsoleMenu?**
The menu only reads input, validates it and prints. The rules live in the registry, so any other front end, like a web controller later, can reuse the same rules.

## Extra questions to be ready for
- Where is configuration read, and what has priority? (AppConfig.load: argument, then config.properties, then defaults.)
- Why catch FileNotFoundException before IOException? (It is a more specific kind of IOException.)
- What does try-with-resources do? (Closes the file automatically.)
- What does the 1 to 0..* diamond line in the diagram mean? (The registry owns the collection.)
- Why do objects link by id (tenantId, spaceId)? (Simple to store in CSV files, and matches a database later.)
- What does printReview(Reviewable item) show? (One method works for an Inspection and for a DepositDeduction, because it only uses the interface. It does not need to know which class it got.)
- Why can an ENDED tenancy not be changed? (Otherwise it could be reopened and overlap a newer tenancy of the same room. Ended is final.)
- What is the difference between option 8 and option 10? (8: the tenant accepts or disputes a deduction. 10: the landlord withdraws a deduction that is still PROPOSED.)
- Why does a REJECTED deduction show review status DISPUTED? (Reviewable has only three states. Rejected is mapped to "not agreed", the closest one.)
