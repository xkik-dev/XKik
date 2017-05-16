package com.xkikdev.xkik;

/**
 * Setting for changing string resources
 */

public class StringSetting {

    public String label;
    public String id;
    public String defval;

    /**
     * @param label  String lable shown in settings app
     * @param id     resource id used in kik app
     * @param defval default value
     */
    public StringSetting(String label, String id, String defval) {
        this.label = label;
        this.id = id;
        this.defval = defval;
    }

}
