package com.fairshare.app;

public class Main {

    public static void main(String[] args) {
        System.out.println("FairShare starting...");
        FairShareRegistry registry = new FairShareRegistry();
        FairShareFileStore store = new FairShareFileStore("data");
        store.load(registry);               // fill the registry from the files
        ConsoleMenu menu = new ConsoleMenu(registry);
        menu.run();                         // returns when you choose 0
        if (store.save(registry)) {
            System.out.println("Data saved.");
        }
    }
}  