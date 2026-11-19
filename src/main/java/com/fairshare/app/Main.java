package com.fairshare.app;

public class Main {

    public static void main(String[] args) {
        System.out.println("FairShare starting...");
        FairShareRegistry registry = new FairShareRegistry();
        ConsoleMenu menu = new ConsoleMenu(registry);
        menu.run();
    }
}
