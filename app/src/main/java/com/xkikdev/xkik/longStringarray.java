package com.xkikdev.xkik;

import java.util.ArrayList;

/**
 * Created by Dylan on 9/17/2017.
 */

public class longStringarray {

    private long lng;
    ArrayList<String> strarr;

    public longStringarray(long l,ArrayList<String> str){
        lng = l;
        strarr = str;
    }

    public long getLong() {
        return lng;
    }

    public void setLong(long lng) {
        this.lng = lng;
    }

    public ArrayList<String> getStrarr() {
        return strarr;
    }

    public void addStrarr(String value){
        strarr.add(value);
    }

    public void delete(String value){
        strarr.remove(value);
    }

    public boolean contains (String value){
        return strarr.contains(value);
    }

}
