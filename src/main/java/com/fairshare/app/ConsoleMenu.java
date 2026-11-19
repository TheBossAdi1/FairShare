package com.fairshare.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class ConsoleMenu {

    private static final int MAX_TEXT_LENGTH = 100;
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000");

    private final FairShareRegistry registry;
    private final String currency;   // from the configuration, shown in money prompts
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleMenu(FairShareRegistry registry, String currency) {
        this.registry = registry;
        this.currency = currency;
    }

    // ---------- main loop ----------

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ", 0, 11);
            if (choice == 1) {
                addUser();
            } else if (choice == 2) {
                addProperty();
            } else if (choice == 3) {
                addSpace();
            } else if (choice == 4) {
                addTenancy();
            } else if (choice == 5) {
                addInspection();
            } else if (choice == 6) {
                addDeduction();
 
            } else if (choice == 7) {
                listAll();
            } else if (choice == 8) {
                respondToDeduction();
            } else if (choice == 9) {
                respondToInspection();
            } else if (choice == 10) {
                rejectDeduction();
            } else if (choice == 11) {
                changeTenancyStatus();
            } else {
                running = false;
            }
        }
        System.out.println("Goodbye!");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("===== FairShare =====");
        System.out.println("1) Add user");
        System.out.println("2) Add property");
        System.out.println("3) Add space to a property");
        System.out.println("4) Add tenancy");
        System.out.println("5) Add inspection");
        System.out.println("6) Propose deposit deduction");
        System.out.println("7) List everything");
        System.out.println("8) Answer a deduction (accept / dispute)");
        System.out.println("9) Answer an inspection (acknowledge / dispute)");
        System.out.println("10) Reject a proposed deduction (landlord)");
        System.out.println("11) Change a tenancy status");
        System.out.println("0) Exit");
    }

    // ---------- actions ----------

    private void addUser() {
        String name = readText("Full name: ");
        String email = readEmail("Email: ");
        User user = new User(registry.generateId(), name, email);
        registry.addUser(user);
        System.out.println("Added: " + user);
    }

    private void addProperty() {
        long landlordId = readLong("Landlord user id: ");
        if (registry.findUserById(landlordId) == null) {
            System.out.println("No user with that id.");
            return;
        }
        String name = readText("Property name: ");
        String city = readText("City: ");
        Property property = new Property(registry.generateId(), landlordId, name, city);
        registry.addProperty(property);
        System.out.println("Added: " + property);
    }

    private void addSpace() {
        long propertyId = readLong("Property id: ");
        Property property = registry.findPropertyById(propertyId);
        if (property == null) {
            System.out.println("No property with that id.");
            return;
        }
        String name = readText("Space name: ");
        SpaceType type = readSpaceType();
        Space space = new Space(registry.generateId(), name, type);
        property.addSpace(space);
        System.out.println("Added: " + space);
    }

    private void addTenancy() {
        long tenantId = readLong("Tenant user id: ");
        if (registry.findUserById(tenantId) == null) {
            System.out.println("No user with that id.");
            return;
        }
        long spaceId = readLong("Space id: ");
        if (registry.findSpaceById(spaceId) == null) {
            System.out.println("No space with that id.");
            return;
        }
        LocalDate start = readDate("Start date (yyyy-MM-dd): ");
        LocalDate end = readDate("End date (yyyy-MM-dd): ");
        if (!end.isAfter(start)) {
            System.out.println("End date must be after the start date.");
            return;
        }
        BigDecimal rent = readPositiveMoney("Monthly rent (" + currency + "): ");
        BigDecimal deposit = readPositiveMoney("Agreed deposit (" + currency + "): ");
        Tenancy tenancy = new Tenancy(registry.generateId(), tenantId, spaceId,
                start, end, rent, deposit);
        try {
            registry.registerTenancy(tenancy);
            System.out.println("Added: " + tenancy);
        } catch (OverlappingTenancyException e) {
            System.out.println("Rejected: " + e.getMessage());
        }

    }

    private void addInspection() {
        long spaceId = readLong("Space id: ");
        if (registry.findSpaceById(spaceId) == null) {
            System.out.println("No space with that id.");
            return;
        }
        InspectionType type = readInspectionType();
        LocalDate date = readDate("Inspection date (yyyy-MM-dd): ");
        Inspection inspection = new Inspection(registry.generateId(), spaceId, type, date);
        registry.addInspection(inspection);
        System.out.println("Added: " + inspection);
    }

    private void addDeduction() {
        long tenancyId = readLong("Tenancy id: ");
        if (registry.findTenancyById(tenancyId) == null) {
            System.out.println("No tenancy with that id.");
            return;
        }
        BigDecimal amount = readPositiveMoney("Deduction amount (" + currency + "): ");
        String reason = readText("Reason: ");
        DepositDeduction deduction = new DepositDeduction(registry.generateId(),
                tenancyId, amount, reason);
        registry.addDeduction(deduction);
        System.out.println("Added: " + deduction);
    }

    private void respondToDeduction() {
        long id = readLong("Deduction id: ");
        DepositDeduction deduction = registry.findDeductionById(id);
        if (deduction == null) {
            System.out.println("No deduction with that id.");
            return;
        }
        if (deduction.getStatus() != DeductionStatus.PROPOSED) {
            System.out.println("Only PROPOSED deductions can be answered. This one is "
                    + deduction.getStatus() + ".");
            return;
        }
        if (registry.findTenancyById(deduction.getTenancyId()) == null) {
            System.out.println("The tenancy of this deduction no longer exists.");
            return;
        }
        System.out.println("1) Accept");
        System.out.println("2) Dispute");
        int choice = readInt("Answer: ", 1, 2);
        if (choice == 1) {
            try {
                registry.acceptDeduction(deduction);
                System.out.println("Accepted: " + deduction);
            } catch (DepositLimitExceededException e) {
                System.out.println("Rejected: " + e.getMessage());
            }
        } else {
            String comment = readText("Comment: ");
            deduction.dispute(comment);
            System.out.println("Disputed: " + deduction);
        }
    }


    private void respondToInspection() {
        long id = readLong("Inspection id: ");
        Inspection inspection = registry.findInspectionById(id);
        if (inspection == null) {
            System.out.println("No inspection with that id.");
            return;
        }
        if (inspection.getReviewStatus() != ReviewStatus.PENDING) {
            System.out.println("Only PENDING inspections can be answered. This one is "
                    + inspection.getReviewStatus() + ".");
            return;
        }
        System.out.println("1) Acknowledge");
        System.out.println("2) Dispute");
        int choice = readInt("Answer: ", 1, 2);
        if (choice == 1) {
            inspection.acknowledge();
            System.out.println("Acknowledged: " + inspection);
        } else {
            String comment = readText("Comment: ");
            inspection.dispute(comment);
            System.out.println("Disputed: " + inspection);
        }
    }

    // the landlord withdraws a deduction that is still only PROPOSED
    private void rejectDeduction() {
        long id = readLong("Deduction id: ");
        DepositDeduction deduction = registry.findDeductionById(id);
        if (deduction == null) {
            System.out.println("No deduction with that id.");
            return;
        }
        if (deduction.getStatus() != DeductionStatus.PROPOSED) {
            System.out.println("Only PROPOSED deductions can be rejected. This one is "
                    + deduction.getStatus() + ".");
            return;
        }
        deduction.reject();
        System.out.println("Rejected: " + deduction);
    }

    // PENDING, ACTIVE or ENDED. An ENDED tenancy is final: it cannot be reopened,
    // otherwise it could come back and overlap a newer tenancy of the same room.
    private void changeTenancyStatus() {
        long id = readLong("Tenancy id: ");
        Tenancy tenancy = registry.findTenancyById(id);
        if (tenancy == null) {
            System.out.println("No tenancy with that id.");
            return;
        }
        if (tenancy.getStatus() == TenancyStatus.ENDED) {
            System.out.println("This tenancy is ENDED and cannot be changed.");
            return;
        }
        System.out.println("Current status: " + tenancy.getStatus());
        TenancyStatus newStatus = readTenancyStatus();
        if (newStatus == tenancy.getStatus()) {
            System.out.println("The tenancy already has that status.");
            return;
        }
        tenancy.setStatus(newStatus);
        System.out.println("Updated: " + tenancy);
    }

    // works for ANY Reviewable (inspection or deduction): only the interface is used
    private void printReview(Reviewable item) {
        System.out.println("    review status: " + item.getReviewStatus());
    }

    private void listAll() {
        System.out.println("--- Users ---");
        for (User u : registry.getUsers()) {
            System.out.println(u);
        }
        System.out.println("--- Properties and spaces ---");
        for (Property p : registry.getProperties()) {
            System.out.println(p);
            for (Space s : p.getSpaces()) {
                System.out.println("    " + s);
            }
        }
        System.out.println("--- Tenancies ---");
        for (Tenancy t : registry.getTenancies()) {
            System.out.println(t);
        }
        System.out.println("--- Inspections ---");
        for (Inspection i : registry.getInspections()) {
            System.out.println(i);
            printReview(i);
        }
        System.out.println("--- Deductions ---");
        for (DepositDeduction d : registry.getDeductions()) {
            System.out.println(d);
            printReview(d);
        }
    }

    // ---------- input helpers: each repeats until the answer is valid ----------
    
    private String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                System.out.println("This cannot be empty.");
            } else if (line.contains(",")) {
                System.out.println("Commas are not allowed (they separate fields in the data file).");
            } else if (line.length() > MAX_TEXT_LENGTH) {
                System.out.println("Too long: at most " + MAX_TEXT_LENGTH + " characters.");
            } else {
                return line;
            }
        }
    }

    // text + the shape name@domain.ext (checked with a regular expression)
    private String readEmail(String prompt) {
        while (true) {
            String email = readText(prompt);
            if (email.matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+([.][A-Za-z0-9-]+)*[.][A-Za-z]{2,}")) {
                return email;
            }
            System.out.println("That is not a valid email (example: ana@mail.com).");
        }
    }

    private int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Enter a number from " + min + " to " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("That is not a whole number.");
            }
        }
    }

    private long readLong(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                long value = Long.parseLong(line);
                if (value > 0) {
                    return value;
                }
                System.out.println("An id must be a positive number.");
            } catch (NumberFormatException e) {
                System.out.println("That is not a valid id.");
            }
        }
    }

    private BigDecimal readPositiveMoney(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                BigDecimal value = new BigDecimal(line);
                if (value.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("The amount must be greater than zero.");
                } else if (value.compareTo(MAX_AMOUNT) > 0) {
                    System.out.println("The amount cannot be more than " + MAX_AMOUNT + ".");
                } else if (value.stripTrailingZeros().scale() > 2) {
                    System.out.println("Use at most 2 decimals (example: 150.50).");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("That is not a valid amount (example: 150.50).");
            }
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return LocalDate.parse(line);   // expects yyyy-MM-dd
            } catch (DateTimeParseException e) {
                System.out.println("Use the format yyyy-MM-dd (example: 2026-03-15).");
            }
        }
    }

    private SpaceType readSpaceType() {
        System.out.println("1) PRIVATE_ROOM");
        System.out.println("2) SHARED_AREA");
        int choice = readInt("Space type: ", 1, 2);
        return SpaceType.values()[choice - 1];   // values() = array of all enum constants
    }

    private TenancyStatus readTenancyStatus() {
        System.out.println("1) PENDING");
        System.out.println("2) ACTIVE");
        System.out.println("3) ENDED");
        int choice = readInt("New status: ", 1, 3);
        return TenancyStatus.values()[choice - 1];
    }

    private InspectionType readInspectionType() {
        System.out.println("1) MOVE_IN");
        System.out.println("2) ROUTINE");
        System.out.println("3) MOVE_OUT");
        int choice = readInt("Inspection type: ", 1, 3);
        return InspectionType.values()[choice - 1];
    }
}
