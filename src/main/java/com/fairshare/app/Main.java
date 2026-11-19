package com.fairshare.app;

public class Main {

    public static void main(String[] args) {
        System.out.println("FairShare starting...");
        AppConfig config = AppConfig.load(args);   // config.properties + program arguments
        System.out.println("Data folder: " + config.getDataDirectory());
        FairShareRegistry registry = new FairShareRegistry();
        FairShareFileStore store = new FairShareFileStore(config.getDataDirectory());
        store.load(registry);               // fill the registry from the files
        ConsoleMenu menu = new ConsoleMenu(registry, config.getDefaultCurrency());
        menu.run();                         // returns when you choose 0
        if (store.save(registry)) {
            System.out.println("Data saved.");
        }
    }
}
