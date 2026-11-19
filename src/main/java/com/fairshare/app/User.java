package com.fairshare.app;

public class User implements Storable {
    private final long id;
    private final String fullName;
    private final String email;

    public User(long id, String fullName, String email) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", name=" + fullName + ", email=" + email + "}";
    }
    @Override
    public String toCsvLine() {
        return id + "," + fullName + "," + email;
    }

    public static User fromCsvLine(String line) {
        String[] parts = line.split(",", -1);
        return new User(Long.parseLong(parts[0]), parts[1], parts[2]);
    }

}