package com.xkikdev.xkik;

import de.robv.android.xposed.XposedHelpers;

public class xposedObject {

    Object obj;

    public xposedObject(Object obj){
        this.obj=obj;
    }

    public Object get(String var){
        return Util.getObjField(obj,var);
    }

    public xposedObject getXObj(String var){
        return new xposedObject(Util.getObjField(obj,var));
    }

    public void set(String var,Object value){
        XposedHelpers.setObjectField(obj,var,value);
    }

    public Object call(String func, Object... args){
        return XposedHelpers.callMethod(obj,func,args);
    }

}
