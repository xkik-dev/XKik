package com.xkikdev.xkik;

import java.io.File;

public class KikAccount {

    private String name;

    public KikAccount(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getStorageDir(){
        throw new UnsupportedOperationException();
    }

}
