package com.xkikdev.xkik;

import de.robv.android.xposed.XposedHelpers;

class xposedObject {

    private Object obj;

    xposedObject(Object obj) {
        this.obj = obj;
    }

    Object get(String var) {
        return Util.getObjField(obj, var);
    }

    xposedObject getXObj(String var) {
        return new xposedObject(Util.getObjField(obj, var));
    }

    void set(String var, Object value) {
        XposedHelpers.setObjectField(obj, var, value);
    }

    Object call(String func, Object... args) {
        return XposedHelpers.callMethod(obj, func, args);
    }

}
