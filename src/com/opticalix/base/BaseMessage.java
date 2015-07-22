package com.opticalix.base;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * For EventBus 消息基类
 * Created by Felix on 2015/5/13.
 */
public class BaseMessage<T extends BaseMessage>{

    private T mMsgInstance;

    /**
     * 必须使用该方法获取Message类
     * @param msgClass
     * @return
     */
    public T getMsg(Class msgClass) {
        try {
            mMsgInstance = (T) msgClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return mMsgInstance;
    }

    private BaseMessage() {
    }

    //Map存储数据
    private Map<String, Object> ps;

    public BaseMessage put(String key, Object value) {
        if (ps == null) {
            ps = new HashMap();
        }

        if (key != null) {
            ps.put(key, value);
        }
        return this;
    }

    public Object get(String key){
        if(ps == null || TextUtils.isEmpty(key)){
            return null;
        }
        return ps.get(key);
    }

    public static BaseMessage g(){
        return new BaseMessage();
    }

    //--------业务回调--------
    public static class OnViewMeasured extends BaseMessage{}
    //--------网络回调--------
}
