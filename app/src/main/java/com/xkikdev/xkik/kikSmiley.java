package com.xkikdev.xkik;

public class kikSmiley {

    String title;
    String text;
    String id;
    long idate;

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getId() {
        return id;
    }

    public long getIdate() {
        return idate;
    }

    public kikSmiley(String title, String text, String id, long installdate){
        this.title = title;
        this.text = text;
        this.id = id;
        this.idate = installdate;
    }

}
