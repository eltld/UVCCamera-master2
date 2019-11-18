package com.serenegiant.usbcameratest;

import android.app.Application;

public class MyApp extends Application {
    public static boolean isDemo = false;
    public MyApp() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        CrashHandler.newsIntance().init(this);
    }

}
