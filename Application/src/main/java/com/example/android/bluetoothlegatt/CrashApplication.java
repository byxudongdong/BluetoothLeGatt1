package com.example.android.bluetoothlegatt;

import android.app.Application;

/**
 * Created by doyle on 2016/8/4 0004.
 */
public class CrashApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
    }

}
