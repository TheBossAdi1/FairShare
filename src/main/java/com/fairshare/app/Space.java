package com.fairshare.app;

public class Space implements Storable {

    private final long id;
    private final String name;
    private final SpaceType type;

    public Space(long id, String name, SpaceType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public SpaceType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Space{id=" + id + ", name=" + name + ", type=" + type + "}";
    }
    @Override
    public String toCsvLine() {
        return id + "," + name + "," + type;
    }

    public static Space fromCsvLine(String line) {
        String[] parts = line.split(",", -1);
        return new Space(Long.parseLong(parts[0]), parts[1], SpaceType.valueOf(parts[2]));
    }

}
