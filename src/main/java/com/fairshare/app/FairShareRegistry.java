package com.fairshare.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FairShareRegistry {

    private final Map<Long, User> users = new HashMap<>();
    private final List<Property> properties = new ArrayList<>();
    private final List<Tenancy> tenancies = new ArrayList<>();
    private final List<Inspection> inspections = new ArrayList<>();
    private final List<DepositDeduction> deductions = new ArrayList<>();

    private long nextId = 1;   // one counter shared by all object types

    public long generateId() {
        long id = nextId;
        nextId++;
        return id;
    }

    // keeps the counter ahead of any id we add (important when loading files)
    private void keepCounterAhead(long id) {
        if (id >= nextId) {
            nextId = id + 1;
        }
    }

    // ---------- Users ----------

    public void addUser(User user) {
        users.put(user.getId(), user);
        keepCounterAhead(user.getId());
    }

    public User findUserById(long id) {
        return users.get(id);   // null if not found
    }

    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    // ---------- Properties and spaces ----------

    public void addProperty(Property property) {
        properties.add(property);
        keepCounterAhead(property.getId());
        for (Space space : property.getSpaces()) {
            keepCounterAhead(space.getId());
        }
    }

    public Property findPropertyById(long id) {
        for (Property p : properties) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public Space findSpaceById(long id) {
        for (Property p : properties) {
            for (Space s : p.getSpaces()) {
                if (s.getId() == id) {
                    return s;
                }
            }
        }
        return null;
    }

    // ---------- Tenancies ----------

    public void addTenancy(Tenancy tenancy) {
        tenancies.add(tenancy);
        keepCounterAhead(tenancy.getId());
    }

    public Tenancy findTenancyById(long id) {
        for (Tenancy t : tenancies) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }

    public List<Tenancy> getTenancies() {
        return tenancies;
    }

    // ---------- Inspections ----------

    public void addInspection(Inspection inspection) {
        inspections.add(inspection);
        keepCounterAhead(inspection.getId());
    }

    public Inspection findInspectionById(long id) {
        for (Inspection i : inspections) {
            if (i.getId() == id) {
                return i;
            }
        }
        return null;
    }

    public List<Inspection> getInspections() {
        return inspections;
    }

    // ---------- Deductions ----------

    public void addDeduction(DepositDeduction deduction) {
        deductions.add(deduction);
        keepCounterAhead(deduction.getId());
    }

    public DepositDeduction findDeductionById(long id) {
        for (DepositDeduction d : deductions) {
            if (d.getId() == id) {
                return d;
            }
        }
        return null;
    }

    public List<DepositDeduction> getDeductions() {
        return deductions;
    }
}
