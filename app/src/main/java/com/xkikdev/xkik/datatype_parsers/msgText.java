package com.xkikdev.xkik.datatype_parsers;

import com.xkikdev.xkik.hooks;
import com.xkikdev.xkik.xposedObject;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class msgText {

    static Pattern unameparser = Pattern.compile("(.*)_[^_]*");

    String content;
    String fromGroup;
    String fromUser;
    String UUID;
    Long timestamp;
    Boolean isgroup;

    public msgText(Object o) {
        this(new xposedObject(o));
    }

    public msgText(xposedObject o) {
        Vector msgdata = (Vector) o.get("i");
        if (msgdata.get(0).getClass().getName().equals(hooks.DATATYPE_MSG_TEXT)) {
            xposedObject msgobj = new xposedObject(o.getSelf());
            content = (String) new xposedObject(msgdata.get(0)).get("a");
            fromGroup = (String) msgobj.get("a");
            fromUser = (String) msgobj.get("b");
            UUID = (String) msgobj.get("f");
            timestamp = (Long) msgobj.get("n");
            isgroup = fromGroup.contains("groups.kik.com");
        }
    }

    public Boolean getIsgroup() {
        return isgroup;
    }

    public String getContent() {
        return content;
    }

    public String getFromGroup() {
        return fromGroup;
    }

    public String getFromUser() {
        return fromUser;
    }

    public String getFromUserParsed() {
        Matcher m = unameparser.matcher(getFromUser());
        if (m.find()){
            return m.group(1);
        }
        return null;
    }

    public String getUUID() {
        return UUID;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}