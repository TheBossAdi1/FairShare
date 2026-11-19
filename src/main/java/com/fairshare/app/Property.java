package com.fairshare.app;

import java.util.ArrayList;
import java.util.List;

public class Property {

    private final long id;
    private final long landlordId;
    private final String name;
    private final String city;
    private final List<Space> spaces;

    public Property(long id, long landlordId, String name, String city) {
        this.id = id;
        this.landlordId = landlordId;
        this.name = name;
        this.city = city;
        this.spaces = new ArrayList<>();   
    }
    public void addSpace(Space space) {
        spaces.add(space);
    }

    public long getId() {
        return id;
    }

    public long getLandlordId() {
        return landlordId;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public List<Space> getSpaces() {
        return spaces;
    }

    @Override
    public String toString() {
        return "Property{id=" + id + ", name=" + name + ", city=" + city
                + ", spaces=" + spaces.size() + "}";
    }
}
