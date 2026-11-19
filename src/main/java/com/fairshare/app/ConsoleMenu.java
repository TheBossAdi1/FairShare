package com.fairshare.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class ConsoleMenu {

    private final FairShareRegistry registry;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleMenu(FairShareRegistry registry) {
        this.registry = registry;
    }

    // ---------- main loop ----------

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ", 0, 7);
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
        System.out.println("0) Exit");
    }

    // ---------- actions ----------

    private void addUser() {
        String name = readText("Full name: ");
        String email = readText("Email: ");
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
        BigDecimal rent = readPositiveMoney("Monthly rent: ");
        BigDecimal deposit = readPositiveMoney("Agreed deposit: ");
        Tenancy tenancy = new Tenancy(registry.generateId(), tenantId, spaceId,
                start, end, rent, deposit);
        registry.addTenancy(tenancy);
        System.out.println("Added: " + tenancy);
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
        BigDecimal amount = readPositiveMoney("Deduction amount: ");
        String reason = readText("Reason: ");
        DepositDeduction deduction = new DepositDeduction(registry.generateId(),
                tenancyId, amount, reason);
        registry.addDeduction(deduction);
        System.out.println("Added: " + deduction);
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
        }
        System.out.println("--- Deductions ---");
        for (DepositDeduction d : registry.getDeductions()) {
            System.out.println(d);
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
            } else {
                return line;
            }
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
                return Long.parseLong(line);
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
                if (value.compareTo(BigDecimal.ZERO) > 0) {
                    return value;
                }
                System.out.println("The amount must be greater than zero.");
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

    private InspectionType readInspectionType() {
        System.out.println("1) MOVE_IN");
        System.out.println("2) ROUTINE");
        System.out.println("3) MOVE_OUT");
        int choice = readInt("Inspection type: ", 1, 3);
        return InspectionType.values()[choice - 1];
    }
}
