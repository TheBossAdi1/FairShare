package com.fairshare.app;

public class Space {

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
}
