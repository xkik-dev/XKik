package com.xkikdev.xkik;

import java.io.File;

public class KikAccount {

    private String name;
    private boolean active = false;

    public KikAccount(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getStorageDir() {
        throw new UnsupportedOperationException();
    }

}
