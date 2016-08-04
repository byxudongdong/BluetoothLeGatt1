package com.example.android.bluetoothlegatt;

import com.google.gson.Gson;

import java.util.List;

/**
 * Created by doyle on 2016/8/4 0004.
 */
public class GsonFunc {
    /**
     * name : image_W16_15_20160719_c.hyc
     * url : http://hycosoft.cc/w16/image_W16_15_20160719_c.hyc
     */

    public List<KeyBean> key;

    public static GsonFunc objectFromData(String str) {

        return new Gson().fromJson(str, GsonFunc.class);
    }

    public static class KeyBean {
        public String name;
        public String url;

        public static KeyBean objectFromData(String str) {

            return new Gson().fromJson(str, KeyBean.class);
        }
    }
}
