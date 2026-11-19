package com.fairshare.app;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

// Holds the program settings. Order of priority:
// 1) program argument --data-dir=PATH   2) config.properties   3) built-in defaults
public class AppConfig {

    private static final String CONFIG_FILE = "config.properties";
    private static final String DATA_DIR_ARG = "--data-dir=";

    private final String dataDirectory;
    private final String defaultCurrency;

    public AppConfig(String dataDirectory, String defaultCurrency) {
        this.dataDirectory = dataDirectory;
        this.defaultCurrency = defaultCurrency;
    }

    public String getDataDirectory() {
        return dataDirectory;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public static AppConfig load(String[] args) {
        String dataDirectory = "data";      // defaults, used when nothing else is given
        String currency = "EUR";

        Properties props = new Properties();
        // try-with-resources: the file is closed automatically at the end of the block
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            props.load(in);
            dataDirectory = readSetting(props, "dataDirectory", dataDirectory);
            currency = readSetting(props, "defaultCurrency", currency);
        } catch (FileNotFoundException e) {
            // normal case when the file is missing: keep the defaults
            System.out.println("No " + CONFIG_FILE + " found, using default settings.");
        } catch (IOException e) {
            // the file exists but could not be read
            System.out.println("Could not read " + CONFIG_FILE + ": " + e.getMessage()
                    + " (using default settings)");
        }

        // a program argument overrides the config file
        for (String arg : args) {
            if (arg.startsWith(DATA_DIR_ARG)) {
                String value = arg.substring(DATA_DIR_ARG.length()).trim();
                if (value.isEmpty()) {
                    System.out.println("Ignoring --data-dir= because the path is empty.");
                } else {
                    dataDirectory = value;
                }
            } else {
                System.out.println("Ignoring unknown argument: " + arg);
            }
        }
        return new AppConfig(dataDirectory, currency);
    }

    // returns the value of a key, or the fallback if the key is missing or blank
    private static String readSetting(Properties props, String key, String fallback) {
        String value = props.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }
}
