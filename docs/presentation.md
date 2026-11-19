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
- Reviewable: acknowledge(), dispute(comment), getReviewStatus(). Implemented by Inspection and DepositDeduction.
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
- Demo (3 minutes, script below).
- Next: web version with Spring Boot, PostgreSQL, login, inspection items, settlement.

## Demo script (3 minutes)
1. Start the app. Add user "Ana Pop" with a wrong email first (ana@mail), then ana@mail.com.
2. Add a property for landlord 1, then a PRIVATE_ROOM space.
3. Add tenancy 2026-01-01 to 2026-06-30, rent 300, deposit 500.
4. Add a second tenancy 2026-05-01 to 2026-09-01. It is rejected as overlapping.
5. Propose deductions 300, 300 and 200. Accept the first, then the second (rejected: 600 > 500), then the third (500 = 500, allowed).
6. Exit with 0, open data/users.csv to show the file, start again and list everything with option 7.

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
