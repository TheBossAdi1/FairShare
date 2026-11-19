package com.fairshare.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class FairShareFileStore {

    private final Path dataDir;

    public FairShareFileStore(String dataDirectory) {
        this.dataDir = Paths.get(dataDirectory);
    }

    // ---------- saving ----------

    // returns true if everything was written
    public boolean save(FairShareRegistry registry) {
        try {
            Files.createDirectories(dataDir);

            List<String> lines = new ArrayList<>();
            for (User user : registry.getUsers()) {
                addLine(lines, user);
            }
            Files.write(dataDir.resolve("users.csv"), lines);

            lines = new ArrayList<>();
            for (Property property : registry.getProperties()) {
                addLine(lines, property);
            }
            Files.write(dataDir.resolve("properties.csv"), lines);

            // a space line starts with the id of the property it belongs to
            lines = new ArrayList<>();
            for (Property property : registry.getProperties()) {
                for (Space space : property.getSpaces()) {
                    lines.add(property.getId() + "," + space.toCsvLine());
                }
            }
            Files.write(dataDir.resolve("spaces.csv"), lines);

            lines = new ArrayList<>();
            for (Tenancy tenancy : registry.getTenancies()) {
                addLine(lines, tenancy);
            }
            Files.write(dataDir.resolve("tenancies.csv"), lines);

            lines = new ArrayList<>();
            for (Inspection inspection : registry.getInspections()) {
                addLine(lines, inspection);
            }
            Files.write(dataDir.resolve("inspections.csv"), lines);

            lines = new ArrayList<>();
            for (DepositDeduction deduction : registry.getDeductions()) {
                addLine(lines, deduction);
            }
            Files.write(dataDir.resolve("deductions.csv"), lines);

            return true;
        } catch (IOException e) {
            System.out.println("Could not save the data: " + e.getMessage());
            return false;
        }
    }

    // works for ANY Storable: only the interface is needed
    private void addLine(List<String> lines, Storable item) {
        lines.add(item.toCsvLine());
    }

    // ---------- loading ----------

    public void load(FairShareRegistry registry) {
        loadUsers(registry);
        loadProperties(registry);   // properties must exist before their spaces
        loadSpaces(registry);
        loadTenancies(registry);
        loadInspections(registry);
        loadDeductions(registry);
    }

    private List<String> readLines(String fileName) {
        Path file = dataDir.resolve(fileName);
        try {
            return Files.readAllLines(file);
        } catch (NoSuchFileException e) {
            return new ArrayList<>();   // first start: no file yet, start empty
        } catch (IOException e) {
            System.out.println("Could not read " + fileName + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void reportBadLine(String fileName, String line) {
        System.out.println("Skipped a damaged line in " + fileName + ": " + line);
    }

    private void loadUsers(FairShareRegistry registry) {
        for (String line : readLines("users.csv")) {
            if (line.trim().isEmpty()) {
                continue;
            }
            try {
                registry.addUser(User.fromCsvLine(line));
            } catch (NumberFormatException | DateTimeParseException | ArrayIndexOutOfBoundsException e) {
                reportBadLine("users.csv", line);
            } catch (IllegalArgumentException e) {
                reportBadLine("users.csv", line);   // e.g. unknown enum text
            }
        }
    }

    private void loadProperties(FairShareRegistry registry) {
        for (String line : readLines("properties.csv")) {
            if (line.trim().isEmpty()) {
                continue;
            }
            try {
                registry.addProperty(Property.fromCsvLine(line));
            } catch (NumberFormatException | DateTimeParseException | ArrayIndexOutOfBoundsException e) {
                reportBadLine("properties.csv", line);
            } catch (IllegalArgumentException e) {
                reportBadLine("properties.csv", line);
            }
        }
    }

    private void loadSpaces(FairShareRegistry registry) {
        for (String line : readLines("spaces.csv")) {
            if (line.trim().isEmpty()) {
                continue;
            }
            try {
                int firstComma = line.indexOf(',');
                long propertyId = Long.parseLong(line.substring(0, firstComma));
                Space space = Space.fromCsvLine(line.substring(firstComma + 1));
                if (!registry.addSpace(propertyId, space)) {
                    reportBadLine("spaces.csv", line);   // property not found
                }
            } catch (NumberFormatException | DateTimeParseException | ArrayIndexOutOfBoundsException e) {
                reportBadLine("spaces.csv", line);
            } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                reportBadLine("spaces.csv", line);
            }
        }
    }

    private void loadTenancies(FairShareRegistry registry) {
        for (String line : readLines("tenancies.csv")) {
            if (line.trim().isEmpty()) {
                continue;
            }
            try {
                registry.addTenancy(Tenancy.fromCsvLine(line));
            } catch (NumberFormatException | DateTimeParseException | ArrayIndexOutOfBoundsException e) {
                reportBadLine("tenancies.csv", line);
            } catch (IllegalArgumentException e) {
                reportBadLine("tenancies.csv", line);
            }
        }
    }

    private void loadInspections(FairShareRegistry registry) {
        for (String line : readLines("inspections.csv")) {
            if (line.trim().isEmpty()) {
                continue;
            }
            try {
                registry.addInspection(Inspection.fromCsvLine(line));
            } catch (NumberFormatException | DateTimeParseException | ArrayIndexOutOfBoundsException e) {
                reportBadLine("inspections.csv", line);
            } catch (IllegalArgumentException e) {
                reportBadLine("inspections.csv", line);
            }
        }
    }

    private void loadDeductions(FairShareRegistry registry) {
        for (String line : readLines("deductions.csv")) {
            if (line.trim().isEmpty()) {
                continue;
            }
            try {
                registry.addDeduction(DepositDeduction.fromCsvLine(line));
            } catch (NumberFormatException | DateTimeParseException | ArrayIndexOutOfBoundsException e) {
                reportBadLine("deductions.csv", line);
            } catch (IllegalArgumentException e) {
                reportBadLine("deductions.csv", line);
            }
        }
    }
}
